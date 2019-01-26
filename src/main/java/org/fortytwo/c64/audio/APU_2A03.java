package org.fortytwo.c64.audio;

import org.fortytwo.common.cpu.CycleObserver;
import org.fortytwo.common.cpu.CPU;
import org.fortytwo.common.memory.Memory;
import org.fortytwo.common.memory.MemoryHandler;

import java.util.logging.Logger;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat;

/* Sound!
 */
public class APU_2A03 implements MemoryHandler, CycleObserver {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    boolean shouldLog = true;

    public static final int NUM_REGISTERS = 24;
    public static final int CPU_SPEED = 1789773; // 1.79Mhz ftw

    private int[] registers_r = new int[NUM_REGISTERS];
    private int[] registers_w = new int[NUM_REGISTERS];

    // registers
    public static final int CONTROL_DMC_1 = 0x10;
    public static final int CONTROL_DMC_2 = 0x11;
    public static final int CONTROL_DMC_3 = 0x12;
    public static final int CONTROL_DMC_4 = 0x13;
    public static final int CONTROL_APU_FLAGS = 0x15;
    public static final int CONTROL_FRAME_COUNTER = 0x17;

    public static final int APUFLAG_SQUARE1 = 0x01;
    public static final int APUFLAG_SQUARE2 = 0x02;
    public static final int APUFLAG_TRIANGLE = 0x04;
    public static final int APUFLAG_NOISE = 0x08;
    public static final int APUFLAG_DMC = 0x10;

    public static final int DMC_MASK_RATE = 0xF;

    public static final int FRAME_MODE = 0x80;
    public static final int FRAME_IRQ = 0x60;

    public static int[] DMC_RATES = {428, 380, 340, 320,
            286, 254, 226, 214,
            190, 160, 142, 128,
            106, 84, 72, 54};

    public static int DMC_INIT_LEVEL = 0;
    public static int DMC_SAMPLE_ADDRESS = 0xC000;

    private int dmcRate = DMC_RATES[0xF];
    private int dmcLevel = DMC_INIT_LEVEL;
    private int dmcAddress = DMC_SAMPLE_ADDRESS;
    private int dmcSampleLength = 0;
    private int dmcRemaining = 0;

    // -- Java Audio Stuff
    int SAMPLE_RATE = 8192; // the APU is actually smaller
    int CHANNELS = 1;
    int BYTES_PER_SAMPLE = 1;
    int FRAME_SIZE = BYTES_PER_SAMPLE * CHANNELS;
    int FRAME_RATE = SAMPLE_RATE;

    private SourceDataLine outputLine;
    private AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
            SAMPLE_RATE, // 1/s
            8, // 1 byte
            1, // mono
            FRAME_SIZE, // 1 byte per frame
            FRAME_RATE, // 1 frame per second,
            false);

    private Memory memory = null;

    public APU_2A03() {

        try {
            outputLine = AudioSystem.getSourceDataLine(format);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    public int read(int address) {
        logger.info("APU Read from: " + Integer.toHexString(address));
        return 0;
    }

    public void write(int address, int value) {
        logger.info("APU Write " + Integer.toHexString(value) + " to " + Integer.toHexString(address));

        switch (address) {
            case CONTROL_DMC_1:
                dmcRate = DMC_RATES[CONTROL_DMC_1 & DMC_MASK_RATE];
                logger.info("APU - DMC Rate = " + dmcRate);
                break;
            case CONTROL_APU_FLAGS:
				/*
	public static final int APUFLAG_SQUARE1 = 	0x01;	
	public static final int APUFLAG_SQUARE2 = 	0x02;
	public static final int APUFLAG_TRIANGLE = 	0x04;
	public static final int APUFLAG_NOISE    =	0x08;
	public static final int APUFLAG_DMC    =	0x10;
	*/
                if ((value & APUFLAG_SQUARE1) != 0) {
                    logger.info("\tSQUARE1 Enabled");
                }
                if ((value & APUFLAG_SQUARE2) != 0) {
                    logger.info("\tSQUARE2 Enabled");
                }
                if ((value & APUFLAG_TRIANGLE) != 0) {
                    logger.info("\tTRIANGLE Enabled");
                }
                if ((value & APUFLAG_NOISE) != 0) {
                    logger.info("\tNOISE Enabled");
                }
                if ((value & APUFLAG_DMC) != 0) {
                    logger.info("\tDMC Enabled");
                }

                break;
        }
    }

    public int tick(int cycles, CPU cpu) {
        //	logger.info("APU DMC Read sample: " + Integer.toHexString(dmcAddress));
        if (dmcRemaining > 0) {
            int sample = memory.read(dmcAddress++);
            if (dmcAddress > 0xFFFF) {
                dmcAddress = 0x8000;
            }
            playSample(sample);
        }
        return dmcRate * 8; // we'll load up 7 samples
    }

    public void disableLogging() {
        shouldLog = false;
//        memory.disableLogging();
    }

    public void enableLogging() {
        shouldLog = true;
    }


    private void playSample(int sample) {
        try {
            if (!outputLine.isOpen()) {
                outputLine.open(format, SAMPLE_RATE);
                outputLine.start();
                logger.info("Opened Audio Line");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        double currentAPUSampleRate = CPU_SPEED / dmcRate;

        // How many duplicates should we put of each sample value
        // to get us up to the actual sample rate
        int multiplier = (int) Math.ceil(SAMPLE_RATE / currentAPUSampleRate);

        byte[] frames = new byte[8 * multiplier];

        for (int i = 0; i < frames.length; i++) {
            int value = (sample >> i) & 0x1;
            if (value == 1) {
                dmcLevel = Math.min(dmcLevel + 2, 127);
            } else {
                dmcLevel = Math.max(dmcLevel - 2, 0);
            }

            for (int m = 0; m < multiplier; m++) {
                frames[i++] = (byte) dmcLevel;
            }
        }

        outputLine.write(frames, 0, frames.length);
    }
}
