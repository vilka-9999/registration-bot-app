package com.registrationbotapp.frames.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class FormTextField extends JTextField {
    private static final Color PLACEHOLDER_COLOR = Color.GRAY;
    private static final Color INPUT_COLOR = Color.BLACK;
    private static final Font PLACEHOLDER_FONT = new Font("Arial", Font.ITALIC, 14);
    private static final Font INPUT_FONT = new Font("Arial", Font.PLAIN, 14);



    public FormTextField(String dataName) {
        this(null, dataName);
    }

    public FormTextField(String displayText, String dataName) {
        super(displayText != null ? displayText : dataName);
        setName(dataName);
        placeholderFocus(dataName);
        setPreferredSize(new Dimension(80, 20));
    }


    // set placeholder to the text field
    public void setToPlaceholder() {
        String dataName = getName();
        // set textField properties
        setText(dataName);
        setForeground(PLACEHOLDER_COLOR);
        setFont(PLACEHOLDER_FONT);
    }

    // customize textField
    private void placeholderFocus(String dataName) {
        // set placeholder if empty
        if (getText().equals(dataName))
            setToPlaceholder();

        // placeholder change logic 
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getText().equals(dataName)) {
                    setText("");
                    setForeground(INPUT_COLOR);
                    setFont(INPUT_FONT);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    setToPlaceholder();
                }
            }
        });

    }


}
