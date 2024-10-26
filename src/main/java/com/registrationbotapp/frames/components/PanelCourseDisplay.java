package com.registrationbotapp.frames.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import com.registrationbotapp.registrationbot.BotUtils;

import java.net.URL;

public class PanelCourseDisplay extends JPanel {

    // address in jar
    private static final String BTN_DELETE_ICON_PATH = "/icons/btnDeleteIcon.png";
    private static final String BTN_DELETE_ICON_HOVERED_PATH = "/icons/btnDeleteHoveredIcon.png";
    

    public PanelCourseDisplay(String toDisplay, String onDeletValue) {
        add(new JLabel(toDisplay));
        setName(onDeletValue);
        createButton();
        customize();
    }
    

    private void customize() {
        setOpaque(false);

        // show button when hoovered
        JButton button = (JButton) this.getComponent(1);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Hide the button when the mouse exits the text field and is not over the button
                if (!button.getBounds().contains(e.getPoint())) {
                    button.setVisible(false);
                }
            }
        });
    }

    private void createButton() {
        
        URL imageUrl;
        ImageIcon imgOriginal;
        Image scaledImage;
        // scale delete img
        imageUrl = getClass().getResource(BTN_DELETE_ICON_PATH);
        imgOriginal = new ImageIcon(imageUrl);
        scaledImage = imgOriginal.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon imgDelete = new ImageIcon(scaledImage);
        // scale delete hovered img
        imageUrl = getClass().getResource(BTN_DELETE_ICON_HOVERED_PATH);
        imgOriginal = new ImageIcon(imageUrl); 
        scaledImage = imgOriginal.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon imgDeleteHovered = new ImageIcon(scaledImage);

        JButton button = new JButton(imgDelete);
        // set properties
        button.setVisible(false); // it is invisible until parentPanel is hovered
        button.setBorderPainted(false); // button's border 
        button.setFocusPainted(false);  // border around text
        button.setContentAreaFilled(false); // remove default background filling


        // hoover logic
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setIcon(imgDeleteHovered); 
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setIcon(imgDelete); 
            }
        });
        
        
        // delete course from db and remove the panel
        button.addActionListener(e -> {
            BotUtils.deleteData("courses", "crn", this.getName());
            JPanel displayCourses = (JPanel) this.getParent();
            displayCourses.remove(this);
            displayCourses.revalidate();
            displayCourses.repaint();
        });

        this.add(button);
    }
}
