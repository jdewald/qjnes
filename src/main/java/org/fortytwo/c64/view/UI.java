package org.fortytwo.c64.view;

import org.fortytwo.c64.model.memory.Memory6502;
import org.fortytwo.common.cpu.MOS6502Emulator;
import org.fortytwo.c64.model.io.Joystick;
import org.fortytwo.c64.model.io.Keyboard;
import org.fortytwo.c64.model.video.Screen;

import javax.swing.*;

public class UI {
    private MOS6502Emulator cpu;
    private Keyboard keyboard;
    private Joystick joystick;
    private JMenu cartridgeMenu;
    private Screen videoScreen;
    private JFrame videoFrame;

    public UI(MOS6502Emulator cpu, Keyboard keyboard, Joystick joystick) {
        this.cpu = cpu;
        this.keyboard = keyboard;
        this.joystick = joystick;
    }

    public JMenu getCartridgeMenu() {
        return cartridgeMenu;
    }

    public Screen getVideoScreen() {
        return videoScreen;
    }

    public UI invoke() {
        videoFrame = new JFrame("Commodore 64");

        /*** Setup Menu ***/
        var menuBar = new JMenuBar();

        JMenu testMenu = setupMainMenu();

        cartridgeMenu = new JMenu("Cartridges");

        menuBar.add(testMenu);
        menuBar.add(cartridgeMenu);

        setupVideoFrame(videoFrame, menuBar);
        //            emulatorFrame.getContentPane().add(videoFrame);
        return this;
    }

    public void show(){
        this.videoFrame.setVisible(true);
    }

    public void hide(){
        this.videoFrame.setVisible(false);
    }

    JMenu setupMainMenu() {
        var mainMenu = new JMenu("Debug");
        var consoleItem = new JMenuItem("Debugger");
        consoleItem.addActionListener(e -> cpu.setBreak(-1));
        mainMenu.add(consoleItem);

        var resetItem = new JMenuItem("Reset");
        resetItem.addActionListener(e -> {
            System.out.println("Signalling restart");
            cpu.restart();
        });
        mainMenu.add(resetItem);
        return mainMenu;
    }

    private void setupVideoFrame(JFrame videoFrame, JMenuBar menuBar) {
        videoFrame.setJMenuBar(menuBar);
        videoFrame.setSize(375,375);
        videoScreen = new Screen();
        videoScreen.setSize(350,350);
        //imageScreen = new BufferedImage(320,200, BufferedImage.TYPE_RGB);
        //            JPanel panel = new JPanel();
        //panel.add(videoScreen);
        videoFrame.getContentPane().add(videoScreen);

        videoFrame.addKeyListener(keyboard);
        videoFrame.addKeyListener(joystick);
        videoFrame.setFocusable(true);
        videoFrame.setVisible(true);
    }

    public static void initUnloadCartridgeMenuItem(JMenu cartridgeMenu, Memory6502 memory6502) {
        var unloadItem = new JMenuItem("Unload Cartridge");
        unloadItem.addActionListener(e -> (memory6502).setGameStatus(1));
        cartridgeMenu.add(unloadItem);
    }
}
