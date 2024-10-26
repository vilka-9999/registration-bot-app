package com.registrationbotapp.registrationbot;

import java.util.*;


import java.sql.*;
import org.openqa.selenium.SessionNotCreatedException;




public class BotUtils {

    // path for jar. Creates appdata.db in the folder of the jar file
    private static String DATABASE_URL = "jdbc:sqlite:appdata.db";
    // private static String DATABASE_URL = "jdbc:sqlite:src\main\resources\dataappdata.db"; // project url
    

    public static void botRun(String action) throws BotException {
        // check an action
        boolean register = action.equals("register");

        // select user data from the db
        List<Map<String, String>> userDataList = BotUtils.getData("user");
        // only one row in the table if dattabase is not empty
        Map<String, String> userData = userDataList.isEmpty() ? new HashMap<>() : userDataList.get(0);
        if (userData.isEmpty())
            throw new  BotException("Provide the data for google_profile_path, semester, and pin");
    
        // need to replace user input if meet this char \
        String path = userData.get("google_profile_path").replace("\\", "/");
        Bot bot;
        try {
            bot = new Bot(path);
        } catch (SessionNotCreatedException e) {
            throw new BotException("Close all Chrome Browsers and check your profile path ");
        }
        bot.openPage();

        // choose link to open depending n action type
        // TODO:
        String regType = register ? "" : "classSearchLink";
        bot.chooseRegType(regType);

        // check if user is logged in and throw an excaption if does not
        if (!bot.isLoggedIn()){
            //bot.logIn(email, password);
            bot.quit();
            throw new  BotException("Please log into your LaSalle portal.");
        }

        // select the term and throw an exception if incorrect use pin if register action
        String pin = register ? userData.get("pin") : null;
        if (!bot.selectTerm(userData.get("semester"), pin)) {
            bot.quit();
            throw new  BotException("Could not open search page.\nSemester or pin is not valid.");
        }
        
        // geth the data from db and use in for a Bot method
        List<Map<String, String>> courseList = BotUtils.getData("courses");
        // College Algebra 11489 College Writing I: Persuasion 11296
        if (register)
            bot.register(courseList);
        else
            bot.checkCourses(courseList);
            

        bot.quit();
    }


    // create 2 databases users, courses
    public static void createDataBase() {

        // SQL statement for creating a user table
        String sqlUserDB = "CREATE TABLE IF NOT EXISTS user (\n"
                        + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                        + " google_profile_path TEXT UNIQUE NOT NULL,\n"
                        + " pin TEXT UNIQUE NOT NULL,\n"
                        + " semester TEXT NOT NULL\n" 
                        + ");";

        //  SQL statement for creating a courses table
        String sqlCoursesDB = "CREATE TABLE IF NOT EXISTS courses (\n"
                            + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                            + " title TEXT NOT NULL,\n"
                            + " crn TEXT NOT NULL,\n"
                            + " result TEXT\n"
                            + ");";

        // use try with in order to close connections
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(sqlUserDB);
            stmt.execute(sqlCoursesDB);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    // save provided data in to provided db
    public static boolean saveData(String tableName, Map<String, String> dataMap) {

        // save data to the user table
        if (tableName.equals("user")) {
            String sql = "INSERT OR REPLACE INTO user (id, google_profile_path, pin, semester) VALUES (?, ?, ?, ?);";

            try (Connection conn = DriverManager.getConnection(DATABASE_URL); 
                 PreparedStatement pstmt = conn.prepareStatement(sql)) { 
            
                pstmt.setInt(1, 1); // id should be 1 since it is always 1 row
                pstmt.setString(2, dataMap.get("google_profile_path"));    
                pstmt.setString(3, dataMap.get("pin"));   
                pstmt.setString(4, dataMap.get("semester"));    
                pstmt.executeUpdate();
                return true;

            } catch(SQLException e) {
                System.out.println(e.getMessage());
            }

        }

        // save data to the courses table
        if (tableName.equals("courses")) {

            String sqlCount = "SELECT COUNT(*) FROM courses;";
            String sqlSave = "INSERT INTO courses (title, crn) VALUES (?, ?);";
            
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                 PreparedStatement pstmtCount = conn.prepareStatement(sqlCount);
                 PreparedStatement pstmtSave = conn.prepareStatement(sqlSave)) {

                // Count the number of rows
                ResultSet rs = pstmtCount.executeQuery();
                rs.next();
                int courseNum = rs.getInt(1);

                // Check if the number of rows is less then 6 and save if so
                if (courseNum < 6) {
                    pstmtSave.setString(1, dataMap.get("title"));    
                    pstmtSave.setString(2, dataMap.get("crn"));      
                    pstmtSave.executeUpdate();
                    return true;
                }

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }

        }

        return false;

    }


    // return data as a list of maps
    public static List<Map<String, String>> getData(String tableName) {

        // select all rows from db
        String query = "SELECT * FROM " + tableName;
        List<Map<String, String>> dataList = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement()){ 

            ResultSet rs = stmt.executeQuery(query);
            // Convert the ResultSet to a list of maps
            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.put(rs.getMetaData().getColumnName(i), rs.getString(i));
                }
                dataList.add(row);
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return dataList;

    }


    // delete data from the db based on column and value delete only 1 occurrence
    public static void deleteData(String tableName, String column, String delteValue) {

        String sqlOrder = "SELECT * FROM " + tableName + " WHERE " + column + " = ? ORDER BY id LIMIT 1";
        String sqlDelete = "DELETE FROM " + tableName + " WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmtOrder = conn.prepareStatement(sqlOrder);
             PreparedStatement pstmtDelete = conn.prepareStatement(sqlDelete)) {

            pstmtOrder.setString(1, delteValue);
            ResultSet resultSet = pstmtOrder.executeQuery();

            resultSet.next();
            int idDelete = resultSet.getInt("id");
            pstmtDelete.setInt(1,idDelete);
            pstmtDelete.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    // update existing data
    public static void updateData(String tableName, String columnUpdate, String updateValue, String id, String idValue) {

        String sql = "UPDATE " + tableName + " SET " + columnUpdate + " = ? WHERE " + id + " = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, updateValue);
            pstmt.setString(2, idValue);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}


