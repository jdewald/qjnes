package org.fortytwo.c64;

import org.fortytwo.c64.util.NESFile;
import org.fortytwo.c64.mappers.Mapper;
import org.fortytwo.c64.mappers.MMC1Mapper;
import org.fortytwo.c64.mappers.MMC3Mapper;
import org.fortytwo.c64.mappers.GameGenie;

import org.fortytwo.c64.cpu.CPU;
import org.fortytwo.c64.cpu.CycleObserver;

import org.fortytwo.c64.cpu.MOS6502Emulator;
import org.fortytwo.c64.cpu.NESInstructionSet;
import org.fortytwo.c64.cpu.MOS6502InstructionSet;

import org.fortytwo.c64.memory.Memory;
import org.fortytwo.c64.memory.ROM;
import org.fortytwo.c64.memory.RAM;

import org.fortytwo.c64.memory.MemoryHandler;
import org.fortytwo.c64.video.PPU_2C02;
import org.fortytwo.c64.video.Screen;
import org.fortytwo.c64.video.LineObserver;

import org.fortytwo.c64.io.NESController;
import org.fortytwo.c64.io.GamepadController;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

import java.io.File;
import java.io.FileInputStream;

import java.util.Properties;

/**
 * This is the entry point into the NES. Here the screen, inputs and memory mappings are set up in addition to taking
 * in the ROM to load up.
 */
public class NES {
   
    public static void main(String args[]){
        try {

            Properties properties = new Properties();
            properties.load(new FileInputStream(new File("nes.properties")));

            NESFile nesFile = new NESFile(new File(args[0]));
            final CPU cpu = new MOS6502Emulator(new NESInstructionSet());            
            
            ROM programROM = nesFile.getProgramROM(); 
            ROM characterROM = nesFile.getCharROM();
            RAM ram = new RAM(2048);
            
            //RAM ppuRAM = new RAM(2048); // really should be 2048, need to properly map Pattern
            
            /** SET UP VIDEO **/
            PPU_2C02 ppu = new PPU_2C02();
            int mirroringType = nesFile.getMirroringType();
            if (args[0].indexOf("metalgear.nes") != -1){
                mirroringType = 0;
            }
            //int mirroringType = 0;
            Mapper cartridgeMemory = Mapper.getMapper(nesFile.getMapper(),programROM, characterROM);


            MemoryPPU ppuMemory = null;


            boolean useGenie = "1".equals(properties.getProperty("org.fortytwo.c64.NES.useGenie"));


            GameGenie genie = null;

            if (useGenie){
                genie = new GameGenie(cpu, (Mapper) cartridgeMemory);
                ppuMemory = new MemoryPPU(genie.getCharacterROM(), mirroringType);
            }
            else {
                ppuMemory = new MemoryPPU(cartridgeMemory.getCharacterROM(), mirroringType);
            }

            ppu.setMemory(ppuMemory);

            cartridgeMemory.setMemoryPPU(ppuMemory);
            cartridgeMemory.setCPU(cpu);
            cartridgeMemory.setPPU(ppu);          
            
            if (cartridgeMemory instanceof LineObserver){
                ppu.setLineObserver((LineObserver) cartridgeMemory);
            }
            

            // I suppose we should have a display... since we're a game console and all

            Screen screen = new Screen(); // 256x240
            screen.setSize(241,262); // h,w 240,256, yay for hardcoding, I'm an idiot
            ppu.setScreen(screen);

            JFrame nesFrame = createFrame(screen, cpu);

            
            // Being able to control is useful too
            String controllerClass = "org.fortytwo.c64.io.NESController";
            
            String tmpClass = properties.getProperty("org.fortytwo.c64.NES.controllerClass");
            if (tmpClass != null && ! tmpClass.trim().equals("")){
                controllerClass = tmpClass;
            }

            NESController controller = (NESController) Class.forName(controllerClass).newInstance();

            if (controller instanceof KeyListener){
                nesFrame.addKeyListener((KeyListener)controller);
            }

            // Almost there, provide the view into the memory/io that the CPU will get
            Memory cpuMemory = null;
            if (useGenie){
                cpuMemory = new MemoryNES(genie, cartridgeMemory.getSaved(), ram, ppu, controller);
            }
            else {
                cpuMemory = new MemoryNES(cartridgeMemory,cartridgeMemory.getSaved(), ram, ppu, controller);
            }

            cpu.setMemory(cpuMemory);

            // The PPU counts cycles so it can generate interrupts
            cpu.registerCycleObserver((CycleObserver)ppu);

            //cpu.setBreak(0xB6AC);

            cpu.run();
        }
        catch (Throwable t){
            t.printStackTrace();
        }
    }

    /**
     * Sets up our display window including menus
     */
    private static JFrame createFrame(Screen screen, final CPU cpu){
        JFrame nesFrame = new JFrame("NES");
        nesFrame.setSize(256+30,262+60); // w, h -> add 50 to desired height -- this actually changes based on the size of the window text, so it really should be autocalculated
	    //	    nesFrame.setSize(512+30,524+60);
        
        nesFrame.getContentPane().add(screen);
        nesFrame.setFocusable(true);
        
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
        menuBar.add(testMenu);
        nesFrame.setJMenuBar(menuBar);
        nesFrame.setFocusable(true);
        nesFrame.setVisible(true);
        
        return nesFrame;
    }
}
