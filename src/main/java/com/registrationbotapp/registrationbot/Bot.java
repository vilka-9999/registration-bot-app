package com.registrationbotapp.registrationbot;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;


public class Bot extends ChromeDriver {

    private WebDriverWait wait = new WebDriverWait(this, Duration.ofSeconds(10));
    private WebDriverWait checkWait = new WebDriverWait(this, Duration.ofSeconds(2));
    private WebDriverWait loginWait = new WebDriverWait(this, Duration.ofSeconds(1));
    private WebDriverWait finalWait = new WebDriverWait(this, Duration.ofSeconds(120));
    

    public Bot(String googleProfilePath) {
        super(defaultOptions(googleProfilePath));
    }


    // create options for a WebDriver
    private static ChromeOptions defaultOptions(String googleProfilePath) {
        ChromeOptions options = new ChromeOptions();
        int splitIndex = googleProfilePath.lastIndexOf("/");
        String profileDir = googleProfilePath.substring(0, splitIndex);
        String profileName = googleProfilePath.substring(splitIndex + 1);
        options.addArguments("--user-data-dir=" + profileDir);  // use user's google profiles
        options.addArguments("--profile-directory=" + profileName); // google profile
        options.addArguments("--start-maximized"); // maximized window
        //options.addArguments("--headless=old"); // doesnot open a window
        return options;
    }


    // open page
    public void openPage() {
        this.get("https://banreg.lasalle.edu/StudentRegistrationSsb/ssb/registration");
    }


    // choose the type of registration
    public void chooseRegType(String type) {
        WebElement link = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id(type)));
        link.click();
    }


    // check if the user is logged in
    public boolean isLoggedIn() {
        try {
            loginWait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("lightbox")));
        } catch (TimeoutException e) {
            return true;
        }
        return false;
    }


    // log in
    public void logIn(String email, String password) {
        // email form can be 2 types because google remembers email
        try {
            WebElement pickEmailButton = checkWait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[@id='tilesHolder']/div[1]/div/div[1]")));
            pickEmailButton.click();
        } catch (TimeoutException e) {
            WebElement emailForm = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[@id='i0116' and @aria-required='true']")));
            emailForm.sendKeys(email);
            WebElement buttonEmail = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[@id='idSIButton9' and @value='Next']")));
            buttonEmail.click();
        }

        // password form (need to check aria-required because form is hidden, but selenium finds it)
        WebElement passwordForm = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[@id='i0118' and @aria-required='true']")));
        passwordForm.sendKeys(password);

        // need @value because but is found before it is reloaded (event listener doesnt work) since it has the same id
        WebElement buttonPassword = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[@id='idSIButton9' and @value='Sign in']")));
        buttonPassword.click();

        //  extra authentication field
        WebElement buttonAuthenticate = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[@id='idDiv_SAOTCS_Proofs']/div[3]/div")));
        buttonAuthenticate.click();

       // since we need to complete part of the authentication by number we need to wait for the final button for 120 seconds
        WebElement buttonLogInFinal = finalWait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[@id='idSIButton9' and @value='Yes']")));
        buttonLogInFinal.click();

    }


    // select registration term
    public boolean selectTerm(String term, String pin) {
        // open form by clicking on it
        WebElement searchFrame = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("s2id_txt_term")));
        searchFrame.click();
        
        // term form
        WebElement termForm = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("s2id_autogen1_search")));
        termForm.sendKeys(term);

        // select the term and if nothing selected return false
        try {
        WebElement termSelect = checkWait.until(ExpectedConditions.presenceOfElementLocated(
            By.className("select2-result-label")));
        termSelect.click();
        } catch (TimeoutException e) {
            return false;
        }

        // pin form if needed
        if (pin != null) {
            WebElement pinForm = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("input_alt_pin")));
            pinForm.sendKeys(pin);
        }

        // button for selecting a term
        WebElement buttonContinue = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[@id='term-go']")));
        buttonContinue.click();

        // check if everything worked
        try {
            // return false if the select term form is still on the page
            this.findElement(By.id("element-id"));
            return false;
        } catch (NoSuchElementException e) {
            return true;
        }

    }


    // browse classes and check if exists
    // sort classes by seats left
    public void checkCourses(List<Map<String, String>> courses) {
        for (Map<String, String> course : courses) {

            // form for course title
            WebElement titleForm = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("txt_courseTitle")));
            titleForm.sendKeys(course.get("title"));

            // search button
            WebElement buttonSearch = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[@id='search-go']")));
            buttonSearch.click();
            
            /*try {
            Thread.sleep(500);
            }catch(Exception e){
                System.out.println(e.getMessage());
            }*/

            boolean exceptionOccured = false;
            WebElement courseElement = null;
            try {
                //select course
                courseElement = checkWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[@class='section-details-link' and contains(@data-attributes, " + course.get("crn") + ")]")));
                courseElement.click();
            // need 2 exceptions since it can catch an element that does not exists since DOM was not properly reload
            } catch (TimeoutException e) {
                BotUtils.updateData("courses", "result", "Course does not exist", "crn", course.get("crn"));
                System.out.println("Course does not exist");
                exceptionOccured = true;
            } finally {
                // click close course button if course was found
                if (!exceptionOccured) {
                    // select seats left text by using data id
                    String dataId = courseElement.findElement(By.xpath("..")).getAttribute("data-id");
                    String seatsLeft = wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//*[@data-property='status' and @data-id='" + dataId + "']"))).getText();
                    seatsLeft = seatsLeft.substring(0, seatsLeft.indexOf(" "));
                    
                    // convert string to int
                    int seatsLeftNum;
                    try {
                        seatsLeftNum = Integer.parseInt(seatsLeft);
                        BotUtils.updateData("courses", "result", "success seats available: " + seatsLeft, "crn", course.get("crn"));
                    } catch(NumberFormatException e) {
                        seatsLeftNum = 0;
                        BotUtils.updateData("courses", "result", "This course has no seats available", "crn", course.get("crn"));
                    }
                    System.out.println("Seats available: " + seatsLeftNum);

                    // close course button
                    WebElement buttonCloseCourse = wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//*[@class='primary-button small-button' and text()='Close']")));
                    buttonCloseCourse.click();
                }

                // search again button
                WebElement buttonSearchAgain = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[@id='search-again-button']")));
                buttonSearchAgain.click();

                // form clean button
                WebElement buttonClear = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[@id='search-clear']")));
                buttonClear.click();
            }

        }
    }


    // register for classes
    public void register(List<Map<String, String>> courses){

        for (Map<String, String> course : courses) {

            // form for course title
            WebElement titleForm = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("txt_courseTitle")));
            titleForm.sendKeys(course.get("title"));

            // search button
            WebElement buttonSearch = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[@id='search-go']")));
            buttonSearch.click();

           

            // select course dataId
            WebElement courseElement = checkWait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[@class='section-details-link' and contains(@data-attributes, " + course.get("crn") + ")]")));
            String dataId = courseElement.findElement(By.xpath("..")).getAttribute("data-id");
          
            

            // search again button
            WebElement buttonSearchAgain = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[@id='search-again-button']")));
            buttonSearchAgain.click();

            // form clean button
            WebElement buttonClear = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[@id='search-clear']")));
            buttonClear.click();

        }

    }

    
}