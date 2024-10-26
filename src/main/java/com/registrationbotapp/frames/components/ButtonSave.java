package com.registrationbotapp.frames.components;

import javax.swing.*;
import java.awt.*;

import com.registrationbotapp.registrationbot.BotUtils;

import java.awt.event.*;
import java.util.*;



public class ButtonSave extends JButton {

    private static final Color BUTTON_COLOR = Color.lightGray;
    private static final Color BUTTON_COLOR_HOVERED = Color.yellow;
    private boolean displayResultOnPanel;

    // constructor
    public ButtonSave(String tableName, JPanel displayDataPanel, FormTextField... formTextFields) {
        super("Enter");
        customize();
        displayResultOnPanel = displayDataPanel != null;
        addActionListener(e -> {
            addActions(tableName, displayDataPanel, formTextFields);
        });
    }

    public ButtonSave(String tableName, FormTextField... formTextFields) {
        this(tableName, null, formTextFields);
    }

    // customize button
    private void customize() {

        // set button properties
        setBackground(BUTTON_COLOR);
        setBorderPainted(false); // button's border 
        setFocusPainted(false);  // border around text
        setContentAreaFilled(false); // remove default background filling
        setOpaque(true); // keep the background
        
        // hoover logic
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(BUTTON_COLOR_HOVERED); 
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(BUTTON_COLOR); 
            }
        });

    }


    // button action (save)
    private void addActions(String tableName, JPanel displayDataPanel, FormTextField... formTextFields) {

        // save courses
        Map<String, String> dataMap = new HashMap<>();
        boolean saveMap = true;
        for (FormTextField formTextField : formTextFields) {
            String dataName = formTextField.getName();
            String data = formTextField.getText();

            // set to placeholder if data displayed outside of the formField
            if (displayResultOnPanel)
                formTextField.setToPlaceholder();
            // check if form is empty (collected data == placeholder or blank)
            if (data.equals(dataName) || data.isBlank()) {
                saveMap = false;
                formTextField.setToPlaceholder();
                continue; // not break because we need to go every text field
            }
            dataMap.put(dataName, data);
        }

        // if form is invalid dont save the data
        if (!saveMap) 
            return;

        // display error mesage if couldn't save data
        boolean success = BotUtils.saveData(tableName, dataMap);
        if (!success) {
            String message = tableName.equals("courses") ? "You can't register for more than 6 courses" 
                                                                  : "Could not save data";
            JOptionPane.showMessageDialog(null, 
                                        message, 
                                        "Error", 
                                        JOptionPane.ERROR_MESSAGE);
            return;
        }

        // display data in the DataDisplayPanel
        if (displayResultOnPanel) {
            String toDisplay = "Title: " + dataMap.get("title") + " CRN: " + dataMap.get("crn");
            PanelCourseDisplay courseDisplay = new PanelCourseDisplay(toDisplay, dataMap.get("crn"));
            displayDataPanel.add(courseDisplay);
            displayDataPanel.revalidate();
            displayDataPanel.repaint();
        }
    }
    


    

    
}
