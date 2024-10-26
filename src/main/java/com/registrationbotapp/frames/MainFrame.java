package com.registrationbotapp.frames;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import com.registrationbotapp.frames.components.*;
import com.registrationbotapp.registrationbot.BotUtils;

import net.bytebuddy.jar.asm.Label;

import java.util.*;
import java.util.List;
import java.net.URL;

public class MainFrame extends JFrame {

    private static final String APP_ICON_PATH = "/icons/appIcon.png"; // address in jar
    private static final int SIZE_WIDTH = 1000;
    private static final int SIZE_HEIGHT = 600;
    private static final int MIN_SIZE_WIDTH = 500;
    private static final int MIN_SIZE_HEIGHT = 300;


    public MainFrame() {

        createFrame();
        JPanel panel = createMainPanel();
        add(panel);
        // set initial focuse to null (panel itself)
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                // Request focus on a non-focusable component
                panel.requestFocusInWindow();
            }
        });

    }

    // set mainFrame properties
    private void createFrame() {

        setTitle("Registration Bot");
        setSize(SIZE_WIDTH, SIZE_HEIGHT);
        setMinimumSize(new Dimension(MIN_SIZE_WIDTH, MIN_SIZE_HEIGHT));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        URL imageUrl = getClass().getResource(APP_ICON_PATH);
        Image appIcon = Toolkit.getDefaultToolkit().getImage(imageUrl);
        setIconImage(appIcon);
        requestFocus(false);
        setLayout(new BorderLayout());

    }


    // create and split mainPanel into grids
    private JPanel createMainPanel() {

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        

        JPanel userDataPanel = new JPanel();
        userDataPanel.setBackground(new Color(27,104,40));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 6;
        gbc.gridwidth = 1;
        fillUserDataPanel(userDataPanel);
        mainPanel.add(userDataPanel, gbc);

        JPanel courseDataPanel = new JPanel();
        courseDataPanel.setBackground(new Color(242,243,219));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 4;
        gbc.weighty = 6;
        gbc.gridwidth = 1;
        JPanel displayCourses =  fillCourseDataPanel(courseDataPanel);
        mainPanel.add(courseDataPanel, gbc);

        JPanel saveCoursePanel = new JPanel();
        saveCoursePanel.setBackground(new Color(47,158,66));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 2;
        fillSaveCoursePanel(saveCoursePanel, displayCourses);
        mainPanel.add(saveCoursePanel, gbc);

        return mainPanel;

    }


    private void fillUserDataPanel(JPanel panel) { 

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Add a vertical gap between components
        gbc.insets = new Insets(10, 0, 10, 0);  // 10 pixels of padding (top, left, bottom, right)

        // Add first button
        gbc.gridx = 0;
        gbc.gridy = 0;
        List<Map<String, String>> userDataList = BotUtils.getData("user");
        // only 1st row in the table if dattabase is not empty
        Map<String, String> userData = userDataList.isEmpty() ? new HashMap<>() : userDataList.get(0);
        
        FormTextField profile = new FormTextField(userData.get("google_profile_path"), "google_profile_path");
        formPanel.add(profile, gbc);

        JEditorPane helpProfile = new JEditorPane();
        helpProfile.setContentType("text/html");
        String htmlContent = "<html>"
                           + "Copy this link <font color='yellow' face='Arial' size='4'>chrome://version/</font> and paste it to the Chrome account<br>"
                           + "where you are logged in to the LaSalle portal.<br>"
                           + "Copy Profile Path and paste it here."
                           + "</html>";
        helpProfile.setText(htmlContent);
        helpProfile.setEditable(false);
        helpProfile.setOpaque(false);
        formPanel.add(helpProfile);

        // Add second button below the first one
        gbc.gridy = 1;
        FormTextField semester = new FormTextField(userData.get("semester"), "semester");
        formPanel.add(semester, gbc);

        // Add third button below the second one
        gbc.gridy = 2;
        FormTextField pin = new FormTextField(userData.get("pin"), "pin");
        formPanel.add(pin, gbc);

        // add 4rth
        gbc.gridy = 3;
        ButtonSave saveUser = new ButtonSave("user", profile, pin, semester);
        formPanel.add(saveUser, gbc);


        panel.add(formPanel);
    }


    private JPanel fillCourseDataPanel(JPanel panel) {

        // display courses in th edattabase
        JPanel displayCourses = new JPanel();
        displayCourses.setLayout(new BoxLayout(displayCourses, BoxLayout.Y_AXIS));
        displayCourses.setOpaque(false);
        displayCoursesData(displayCourses);
        panel.add(displayCourses);

        ButtonBotRun checkRun = new ButtonBotRun("Check courses", "check", displayCourses);
        ButtonBotRun regRun = new ButtonBotRun("Register","register", displayCourses);
        panel.add(checkRun);
        panel.add(regRun);

        return displayCourses;

    }


    public static void displayCoursesData(JPanel displayCourses) {
        displayCourses.removeAll();
        List<Map<String, String>> courseList = BotUtils.getData("courses");
        for (Map<String, String> course : courseList) {
            String result = course.get("result");
            result = result != null ? " " + result : "";
            String color = result.contains("succes") ? "green" : "red";
            String regResult = "<font color='" + color + "'> " + result + "</font>";
            String toDisplay = "<html>Title: " + course.get("title") + " CRN: " + course.get("crn") + regResult + "</html>";
            PanelCourseDisplay courseDisplay = new PanelCourseDisplay(toDisplay, course.get("crn"));
            displayCourses.add(courseDisplay);
        }
    }



    private void fillSaveCoursePanel(JPanel panel, JPanel displayCourses) {

        FormTextField courseTitle = new FormTextField("title");
        panel.add(courseTitle);

        FormTextField crn = new FormTextField("crn");
        panel.add(crn);

        ButtonSave saveCourse = new ButtonSave("courses", displayCourses, courseTitle, crn);
        panel.add(saveCourse);

    }
}
