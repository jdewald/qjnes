package org.fortytwo.nes.view;

import org.fortytwo.c64.model.video.Screen;
import org.fortytwo.common.cpu.CPU;

import javax.swing.*;

public class UI {
    public static JFrame createFrame(Screen screen, final CPU cpu) {
        var nesFrame = new JFrame("NES");
        nesFrame.setSize(256 + 30, 262 + 60); // w, h -> add 50 to desired height -- this actually changes based on the size of the window text, so it really should be autocalculated
        //	    nesFrame.setSize(512+30,524+60);

        nesFrame.getContentPane().add(screen);
        nesFrame.setFocusable(true);

        /*** Setup Menu ***/
        var menuBar = new JMenuBar();

        var testMenu = new JMenu("Debug");
        var consoleItem = new JMenuItem("Debugger");
        consoleItem.addActionListener(e -> cpu.setBreak(-1));
        testMenu.add(consoleItem);

        var resetItem = new JMenuItem("Reset");
        resetItem.addActionListener(e -> {
            System.out.println("Signalling restart");
            cpu.restart();
        });
        testMenu.add(resetItem);
        menuBar.add(testMenu);
        nesFrame.setJMenuBar(menuBar);
        nesFrame.setFocusable(true);
        nesFrame.setVisible(true);

        return nesFrame;
    }
}
