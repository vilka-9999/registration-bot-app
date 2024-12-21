package com.registrationbotapp;

import javax.swing.SwingUtilities;

import com.registrationbotapp.frames.MainFrame;
import com.registrationbotapp.registrationbot.BotUtils;

public class App {

    public static void main (String[] args) {
        BotUtils.createDataBase();
        MainFrame frame = new MainFrame();
        frame.setVisible(true);
    }

}
