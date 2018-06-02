package org.fortytwo.c64;

import org.fortytwo.c64.video.VICII;
import org.fortytwo.c64.video.Screen;
import org.fortytwo.c64.cpu.MOS6502Emulator;
import org.fortytwo.c64.cpu.MOS6502InstructionSet;
import org.fortytwo.c64.memory.Memory;
import org.fortytwo.c64.memory.RAM;
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

            CIA cia1 = new CIA("CIA1");
            CIA cia2 = new CIA("CIA2");
            VICII vic = new VICII();

            Keyboard keyboard = new Keyboard(cpu);
            Joystick joystick = new Joystick();
            
            Joystick joystick2 = new Joystick();
            //_1541 diskDrive = new _1541();
            JFrame videoFrame = new JFrame("Commodore 64");

            /*** Setup Menu ***/
            JMenuBar menuBar = new JMenuBar();

            JMenu testMenu = new JMenu("Debug");
            JMenuItem consoleItem = new JMenuItem("Debugger");
            consoleItem.addActionListener(new ActionListener (){
                    public void actionPerformed(ActionEvent e){
                        cpu.setBreak(-1);
                    }
                });
            testMenu.add(consoleItem);

            JMenuItem resetItem = new JMenuItem("Reset");
            resetItem.addActionListener(new ActionListener () {
                    public void actionPerformed(ActionEvent e){
                        System.out.println("Signalling restart");
                        cpu.restart();
                    }
                });
            testMenu.add(resetItem);

            JMenu cartridgeMenu = new JMenu("Cartridges");
            
            menuBar.add(testMenu);
            menuBar.add(cartridgeMenu);

            videoFrame.setJMenuBar(menuBar);


            videoFrame.setSize(375,375);
            Screen videoScreen = new Screen();
            videoScreen.setSize(350,350);
            //imageScreen = new BufferedImage(320,200, BufferedImage.TYPE_RGB);
            //            JPanel panel = new JPanel();
            //panel.add(videoScreen);
            videoFrame.getContentPane().add(videoScreen);

            videoFrame.addKeyListener(keyboard);
            videoFrame.addKeyListener(joystick);
            videoFrame.setFocusable(true);
            videoFrame.setVisible(true);
            //            emulatorFrame.getContentPane().add(videoFrame);

            vic.setScreen(videoScreen);
            
            cia1.setKeyboard(keyboard);
            cia1.setJoystick1(joystick);
            //cia1.setIODevice1(keyboard);
            //cia1.setIODevice1(joystick2);
            //            cia1.setIODevice2(joystick);
            //cia2.setIODevice1(joystick2);
            //cia2.setIODevice(diskDrive);

            ROM kernalROM = new ROM("kernal",new File("roms/kernal.901227-03.bin"));
            ROM basicROM = new ROM("basic", new File("roms/basic.901226-01.bin"));
            ROM charROM = new ROM("char", new File("roms/characters.901225-01.bin"));

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

            RAM ram = new RAM(65536);
            RAM colorRAM = new RAM(0x400);
            /**
             * Create the view that the 6502 sees
             */
            // cartridge should actually use a different view of memory (separate "emulator" mode)
            final Memory6502 memory6502 = new Memory6502(kernalROM,basicROM,charROM,cartridgeROM, romStart, ram,colorRAM, vic,cia1,cia2);
            if (crtFile != null){
                memory6502.setGameStatus(crtFile.getGameStatus());
                memory6502.setExromStatus(crtFile.getExromStatus());
            }

            JMenuItem unloadItem = new JMenuItem("Unload Cartridge");
            unloadItem.addActionListener(new ActionListener () {
                    public void actionPerformed(ActionEvent e){
                        (memory6502).setGameStatus(1);
                    }
                });
            cartridgeMenu.add(unloadItem);

            //	memory6502.enableLogging();
            cpu.registerCycleObserver(cia1);
            cpu.registerCycleObserver(cia2);
            cpu.registerCycleObserver(vic);

            cpu.setMemory(memory6502);
            cpu.setBreak(breakpoint);
            /**
             * Now create the view that the VIC sees
             */
            Memory memoryVIC = new VICMemory(ram,cia2, memory6502.getCharROM(), colorRAM);
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
}
