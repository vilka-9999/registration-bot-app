package com.registrationbotapp.frames.components;


import javax.swing.*;

import com.registrationbotapp.registrationbot.BotUtils;
import com.registrationbotapp.frames.MainFrame;
import com.registrationbotapp.registrationbot.BotException;

public class ButtonBotRun extends JButton {

    public ButtonBotRun(String text, String action, JPanel displayCourses) {
        super(text);
        customize();
        addActionListener(e -> {
            addActions(action, displayCourses);
        });

        
    }


    private void customize() {

    }


    private void addActions(String action, JPanel displayCourses) {
        if (action.equals("register") || action.equals("check")) {
            try {
                BotUtils.botRun(action);
            } catch (BotException e) {
                // need to use dialog for pop up since it needs to be on top of the google page
                JDialog dialog = new JDialog();
                dialog.setAlwaysOnTop(true);
                JOptionPane.showMessageDialog(dialog, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            //} catch(Exception randomE) {
                //OptionPane.showMessageDialog(null, "Something went wrong", "ERROR", JOptionPane.ERROR_MESSAGE);
            }
            // show changes in courses checking result
            MainFrame.displayCoursesData(displayCourses);
            displayCourses.revalidate();
            displayCourses.repaint();
        } else {
            throw new IllegalArgumentException("No such action.");
        }
            
    }
    
}
