package org.fortytwo.c64;

import org.fortytwo.common.cpu.MOS6502Emulator;
import org.fortytwo.c64.io.Joystick;
import org.fortytwo.c64.io.Keyboard;
import org.fortytwo.c64.video.Screen;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class UI {
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

        JMenu testMenu = setupTestMenu();

        cartridgeMenu = new JMenu("Cartridges");

        menuBar.add(testMenu);
        menuBar.add(cartridgeMenu);

        setupVideoFrame(videoFrame, menuBar);
        //            emulatorFrame.getContentPane().add(videoFrame);
        return this;
    }

    void show(){
        this.videoFrame.setVisible(true);
    }

    void hide(){
        this.videoFrame.setVisible(false);
    }

    JMenu setupTestMenu() {
        var testMenu = new JMenu("Debug");
        var consoleItem = new JMenuItem("Debugger");
        consoleItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                cpu.setBreak(-1);
            }
        });
        testMenu.add(consoleItem);

        var resetItem = new JMenuItem("Reset");
        resetItem.addActionListener(new ActionListener () {
            public void actionPerformed(ActionEvent e){
                System.out.println("Signalling restart");
                cpu.restart();
            }
        });
        testMenu.add(resetItem);
        return testMenu;
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

    static void initUnloadCartridgeMenuItem(JMenu cartridgeMenu, Memory6502 memory6502) {
        var unloadItem = new JMenuItem("Unload Cartridge");
        unloadItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                (memory6502).setGameStatus(1);
            }
        });
        cartridgeMenu.add(unloadItem);
    }
}
