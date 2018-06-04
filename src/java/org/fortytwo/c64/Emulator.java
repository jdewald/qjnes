package org.fortytwo.c64;

import org.fortytwo.c64.memory.StandardFactory;
import org.fortytwo.c64.video.VICII;
import org.fortytwo.c64.video.Screen;
import org.fortytwo.c64.cpu.MOS6502Emulator;
import org.fortytwo.c64.cpu.MOS6502InstructionSet;
import org.fortytwo.c64.memory.Memory;
import org.fortytwo.c64.memory.ROM;
import org.fortytwo.c64.io.Keyboard;
import org.fortytwo.c64.io.Joystick;
//import org.fortytwo.c64.io._1541;

import org.fortytwo.c64.util.CRTFile;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class Emulator
{

    public static void main(String[] args){
        try {
            final MOS6502Emulator cpu = new MOS6502Emulator(new MOS6502InstructionSet());

            Keyboard keyboard = new Keyboard(cpu);
            Joystick joystick = new Joystick();
            
            Joystick joystick2 = new Joystick();
            //_1541 diskDrive = new _1541();

            CIA cia1 = setupCia1(keyboard, joystick);
            //cia2.setIODevice1(joystick2);
            //cia2.setIODevice(diskDrive);

            UI ui = new UI(cpu, keyboard, joystick).invoke();

            Screen videoScreen = ui.getVideoScreen();
            JMenu cartridgeMenu = ui.getCartridgeMenu();

            int breakpoint = 0;
            File cartridgeFile = null;
            if (args.length > 0){
                breakpoint = Integer.parseInt(args[0],16);
            }
            if (args.length > 1){
                cartridgeFile = new File(args[1]);
            }

            ROM cartridgeROM = null;
            int romStart = 0;
            CRTFile crtFile = null;
            if (cartridgeFile != null){
                System.out.println("reading cartridge from: " + cartridgeFile.getName());
                crtFile = new CRTFile(cartridgeFile);
                byte[] romData = crtFile.getCHIPData().getROMData();
                cartridgeROM = new ROM(crtFile.getName(),romData);
                romStart = crtFile.getCHIPData().getStartAddress();
                //cartridgeROM = new ROM("cartridge", cartridgeFile);
        
            }

            VICII vic = new VICII();
            vic.setScreen(videoScreen);

            final Memory6502 memory6502 = new StandardFactory().createStandardMemory6502(vic, cia1, cartridgeROM);
            if (crtFile != null){
                memory6502.setGameStatus(crtFile.getGameStatus());
                memory6502.setExromStatus(crtFile.getExromStatus());
            }

            ui.initUnloadCartridgeMenuItem(cartridgeMenu, memory6502);


            //	memory6502.enableLogging();
            cpu.registerCycleObserver(cia1);
            cpu.registerCycleObserver(memory6502.getCia2());
            cpu.registerCycleObserver(vic);

            cpu.setMemory(memory6502);
            cpu.setBreak(breakpoint);
            /**
             * Now create the view that the VIC sees
             */
            Memory memoryVIC = new VICMemory(memory6502.getRam(), memory6502.getCia2(), memory6502.getCharROM(), memory6502.getColorRAM());
            vic.setMemory(memoryVIC);

            /*Thread cpuThread = new Thread(new Runnable() { public void run() { cpu.run(); } } );
            cpuThread.start();
            cpuThread.join();
            */
            cpu.run();

        }
        catch (Throwable e){
            e.printStackTrace();
        }
        finally {
            Runtime.getRuntime().exit(0);
        }
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
