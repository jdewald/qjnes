package org.fortytwo.c64;

import org.fortytwo.c64.cpu.MOS6502Emulator;
import org.fortytwo.c64.cpu.MOS6502InstructionSet;
import org.fortytwo.c64.io.Joystick;
import org.fortytwo.c64.io.Keyboard;
import org.fortytwo.c64.memory.Memory;
import org.fortytwo.c64.memory.ROM;
import org.fortytwo.c64.memory.StandardFactory;
import org.fortytwo.c64.util.CRTFile;
import org.fortytwo.c64.video.Screen;
import org.fortytwo.c64.video.VICII;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

//import org.fortytwo.c64.io._1541;

public class Emulator {

    protected static UI ui;

    public static void main(String[] args) {
        try {
            final MOS6502Emulator emulator = createMos6502Emulator(args);
            emulator.run();

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            Runtime.getRuntime().exit(0);
        }
    }

    public static MOS6502Emulator createMos6502Emulator(String[] params) throws IOException {
        final MOS6502Emulator cpu = new MOS6502Emulator(new MOS6502InstructionSet());

        Keyboard keyboard = new Keyboard(cpu);
        Joystick joystick = new Joystick();

        Joystick joystick2 = new Joystick();
        //_1541 diskDrive = new _1541();

        CIA cia1 = setupCia1(keyboard, joystick);
        //cia2.setIODevice1(joystick2);
        //cia2.setIODevice(diskDrive);

        int breakpoint = (params.length > 0) ? Integer.parseInt(params[0], 16) : 0;

        File cartridgeFile = (params.length > 1) ? new File(params[1]) : null;

        ROM cartridgeROM = null;
        int romStart = 0;
        CRTFile crtFile = null;
        if (cartridgeFile != null) {
            System.out.println("reading cartridge from: " + cartridgeFile.getName());
            crtFile = new CRTFile(cartridgeFile);
            byte[] romData = crtFile.getCHIPData().getROMData();
            cartridgeROM = new ROM(crtFile.getName(), romData);
            romStart = crtFile.getCHIPData().getStartAddress();
            //cartridgeROM = new ROM("cartridge", cartridgeFile);

        }

        ui = new UI(cpu, keyboard, joystick);
        ui.invoke();

        VICII vic = createVic(ui);

        final Memory6502 memory6502 = createMemory(cia1, cartridgeROM, crtFile, vic);

        JMenu cartridgeMenu = ui.getCartridgeMenu();
        ui.initUnloadCartridgeMenuItem(cartridgeMenu, memory6502);


        //	memory6502.enableLogging();
        setupCpu(cpu, cia1, breakpoint, vic, memory6502);

        setupVic(vic, memory6502);
        

            /*Thread cpuThread = new Thread(new Runnable() { public void run() { emulator.run(); } } );
            cpuThread.start();
            cpuThread.join();
            */

        return cpu;
    }

    public static void enableSilentRun() {
        ui.hide();
    }

    static void setupCpu(MOS6502Emulator cpu, CIA cia1, int breakpoint, VICII vic, Memory6502 memory6502) {
        cpu.registerCycleObserver(cia1);
        cpu.registerCycleObserver(memory6502.getCia2());
        cpu.registerCycleObserver(vic);

        cpu.setMemory(memory6502);
        cpu.setBreak(breakpoint);
    }

    static void setupVic(VICII vic, Memory6502 memory6502) {
        /**
         * Now create the view that the VIC sees
         */
        Memory memoryVIC = new VICMemory(memory6502.getRam(), memory6502.getCia2(), memory6502.getCharROM(), memory6502.getColorRAM());
        vic.setMemory(memoryVIC);
    }

    static Memory6502 createMemory(CIA cia1, ROM cartridgeROM, CRTFile crtFile, VICII vic) throws IOException {
        final Memory6502 memory6502 = new StandardFactory().createStandardMemory6502(vic, cia1, cartridgeROM);
        if (crtFile != null) {
            memory6502.setGameStatus(crtFile.getGameStatus());
            memory6502.setExromStatus(crtFile.getExromStatus());
        }
        return memory6502;
    }

    static VICII createVic(UI ui) {
        VICII vic = new VICII();
        Screen videoScreen = ui.getVideoScreen();
        vic.setScreen(videoScreen);
        return vic;
    }

    static CIA setupCia1(Keyboard keyboard, Joystick joystick) {
        CIA cia1 = new CIA("CIA1");

        cia1.setKeyboard(keyboard);
        cia1.setJoystick1(joystick);

        //cia1.setIODevice1(keyboard);
        //cia1.setIODevice1(joystick2);
        //            cia1.setIODevice2(joystick);
        return cia1;
    }


}
