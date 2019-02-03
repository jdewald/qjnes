package org.fortytwo.nes;

import org.fortytwo.c64.model.memory.MemoryPPU;
import org.fortytwo.nes.model.MemoryNES;
import org.fortytwo.nes.model.util.NESFile;
import org.fortytwo.nes.model.mappers.Mapper;
import org.fortytwo.nes.model.mappers.GameGenie;

import org.fortytwo.common.cpu.CPU;
import org.fortytwo.common.cpu.CycleObserver;

import org.fortytwo.common.cpu.MOS6502Emulator;
import org.fortytwo.common.cpu.NESInstructionSet;

import org.fortytwo.common.memory.RAM;

import org.fortytwo.c64.model.video.PPU_2C02;
import org.fortytwo.c64.model.audio.APU_2A03;
import org.fortytwo.c64.model.video.Screen;
import org.fortytwo.c64.model.video.LineObserver;

import org.fortytwo.nes.model.io.NESController;
import org.fortytwo.nes.view.UI;

import java.awt.event.KeyListener;

import java.io.File;
import java.io.FileInputStream;

import java.util.Properties;

/**
 * This is the entry point into the NES. Here the screen, inputs and memory mappings are set up in addition to taking
 * in the ROM to load up.
 */
public class NES {

    public static void main(String args[]) {
        try {

            if (args.length < 1) {
                System.err.println("I/O Error - please run with *.nes file as agrument");
                System.exit(-1);
            }
            var properties = new Properties();
            properties.load(new FileInputStream(new File("nes.properties")));

            var nesFile = new NESFile(new File(args[0]));
            final CPU cpu = new MOS6502Emulator(new NESInstructionSet());

            var programROM = nesFile.getProgramROM();
            var characterROM = nesFile.getCharROM();
            var ram = new RAM(2048);

            //RAM ppuRAM = new RAM(2048); // really should be 2048, need to properly map Pattern

            /** SET UP VIDEO **/
            var ppu = new PPU_2C02();
            int mirroringType = nesFile.getMirroringType();
            if (args[0].contains("metalgear.nes")) {
                mirroringType = 0;
            }
            //int mirroringType = 0;
            var cartridgeMemory = Mapper.getMapper(nesFile.getMapper(), programROM, characterROM);

            MemoryPPU ppuMemory = null;

            /** SET UP AUDIO **/
            var apu = new APU_2A03();

            boolean useGenie = "1".equals(properties.getProperty("org.fortytwo.nes.NES.useGenie"));
            GameGenie genie = null;

            if (useGenie) {
                genie = new GameGenie(cpu, (Mapper) cartridgeMemory);
                ppuMemory = new MemoryPPU(genie.getCharacterROM(), mirroringType);
            } else {
                ppuMemory = new MemoryPPU(cartridgeMemory.getCharacterROM(), mirroringType);
            }

            ppu.setMemory(ppuMemory);

            cartridgeMemory.setMemoryPPU(ppuMemory);
            cartridgeMemory.setCPU(cpu);
            cartridgeMemory.setPPU(ppu);

            if (cartridgeMemory instanceof LineObserver) {
                ppu.setLineObserver((LineObserver) cartridgeMemory);
            }

            // I suppose we should have a display... since we're a game console and all

            var screen = new Screen(); // 256x240
            screen.setSize(241, 262); // h,w 240,256, yay for hardcoding, I'm an idiot
            ppu.setScreen(screen);

            var nesFrame = UI.createFrame(screen, cpu);

            // Being able to control is useful too
            var controllerClass = "org.fortytwo.nes.model.io.NESController";

            String tmpClass = properties.getProperty("org.fortytwo.nes.NES.controllerClass");
            if (tmpClass != null && !tmpClass.trim().equals("")) {
                controllerClass = tmpClass;
            }

            var controller = (NESController) Class.forName(controllerClass).newInstance();

            if (controller instanceof KeyListener) {
                nesFrame.addKeyListener((KeyListener) controller);
            }

            // Almost there, provide the view into the memory/io that the CPU will get
            var cpuMemory = useGenie ?
                    new MemoryNES(genie, cartridgeMemory.getSaved(), ram, ppu, apu, controller)
                    : new MemoryNES(cartridgeMemory, cartridgeMemory.getSaved(), ram, ppu, apu, controller);

            apu.setMemory(cpuMemory);

            cpuMemory.write(0xD2, 0x00);

            cpu.setMemory(cpuMemory);

            // The PPU counts cycles so it can generate interrupts
            cpu.registerCycleObserver((CycleObserver) ppu);
            cpu.registerCycleObserver((CycleObserver) apu);

            //cpu.setBreak(0xB6AC);
//            cpu.setBreak(0xC000);

            cpu.run();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
