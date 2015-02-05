/*
 * Copyright (c) 2015 SIOS Technology Corp. All rights reserved.
 * This file is part of COSENG (Concurrent Selenium TestNG Runner).
 * 
 * COSENG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * COSENG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with COSENG. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sios.stc.coseng.test;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

//import org.openqa.selenium.support.ui.Select;

public class User extends Base {

    private static final Logger log = Logger.getLogger(User.class
            .getName());

    @DataProvider(name = "credentials")
    public Object[][] credentials(final Method m) {
        final String newPassword = "abc123";
        // System.out.println("Method " + m.getName());
        if (m.getName().equals("invalidLogin")) {
            // Username, Password
            return new Object[][] { { "x", "x" }, { "y", "y" } };
        }
        if (m.getName().equals("firstLogin")) {
            // Username, CurrentPassword, NewPassword
            return new Object[][] { { "admin", "admin", newPassword } };
        }
        if (m.getName().equals("login")) {
            // Username, NewPassword
            return new Object[][] { { "admin", newPassword } };
        }
        // Return some default if can't match method
        return new Object[][] { { "admin", newPassword } };
    }

    @Test(description = "Verify bad credentials deny access past the login page", dataProvider = "credentials")
    // @Parameters({ "x,x", "y,y" })
    // public void aInvalidLogin(final String username, final String password)
    public void invalidLogin(final String username, final String password)
            throws Exception {

        User.log.log(Level.INFO, logTestName() + " Username: "
                + username + " Password: "
                + password);

        driver.get(baseUrl + "/ui/#/login");
        acceptSslCertificate(driver);

        final WebDriverWait wait = new WebDriverWait(driver, 10);
        final Actions actions = new Actions(driver);

        final WebElement weUsername = driver.findElement(By.name("userName"));
        final WebElement wePassword = driver.findElement(By.name("password"));
        final WebElement weSubmit = driver.findElement(By
                .className("login_submit_button"));

        wait.until(ExpectedConditions.visibilityOf(weUsername));
        wait.until(ExpectedConditions.visibilityOf(wePassword));
        wait.until(ExpectedConditions.visibilityOf(weSubmit));

        saveScreenshot(driver, "aftervisible-" + username);

        actions.moveToElement(wePassword).click()
        .sendKeys(wePassword, password).build().perform();
        actions.moveToElement(weUsername).click()
        .sendKeys(weUsername, username).build().perform();
        actions.click(weSubmit).build().perform();

        final WebElement weDialogBox = driver.findElement(By
                .className("dialog_box"));
        final WebElement weDialogBtnOk = driver.findElement(By
                .className("dialog_button"));

        wait.until(ExpectedConditions.visibilityOf(weDialogBox));

        actions.click(weDialogBtnOk).build().perform();
    }

    @Test(description = "Verity initial login, change password.", dataProvider = "credentials")
    public void firstLogin(final String username, final String password,
            final String newPassword)
                    throws Exception {

        User.log.log(Level.INFO, "Username: " + username + " Password: "
                + password + " NewPassword: " + newPassword);

        driver.get(baseUrl + "/ui/#/login");

        final WebDriverWait wait = new WebDriverWait(driver, 10);
        final Actions actions = new Actions(driver);

        WebElement weUsername = driver.findElement(By.name("userName"));
        WebElement wePassword = driver.findElement(By.name("password"));
        WebElement weSubmit = driver.findElement(By
                .className("login_submit_button"));

        actions.moveToElement(weUsername).sendKeys(weUsername, username)
        .moveToElement(wePassword).sendKeys(wePassword, password)
        .click(weSubmit).build().perform();

        WebElement weNewPassword = driver.findElement(By.name("newpassword"));
        WebElement weRePassword = driver.findElement(By.name("repassword"));
        final WebElement weCancel = driver.findElement(By.linkText("Cancel"));

        wait.until(ExpectedConditions.visibilityOf(weNewPassword));

        actions.moveToElement(weNewPassword)
        .sendKeys(weNewPassword, newPassword).build().perform();

        actions.moveToElement(weRePassword).sendKeys(weRePassword, newPassword)
        .build().perform();

        actions.click(weCancel).build().perform();

        weUsername = driver.findElement(By.name("userName"));
        wePassword = driver.findElement(By.name("password"));
        weSubmit = driver.findElement(By.className("login_submit_button"));

        wait.until(ExpectedConditions.visibilityOf(weUsername));

        actions.moveToElement(weUsername).sendKeys(weUsername, username)
        .moveToElement(wePassword).sendKeys(wePassword, password)
        .click(weSubmit).build().perform();

        weNewPassword = driver.findElement(By.name("newpassword"));
        weRePassword = driver.findElement(By.name("repassword"));
        final WebElement weOk = driver.findElement(By.linkText("OK"));

        wait.until(ExpectedConditions.visibilityOf(weNewPassword));

        actions.moveToElement(weNewPassword)
        .sendKeys(weNewPassword, newPassword)
        .moveToElement(weRePassword)
        .sendKeys(weRePassword, newPassword).click(weOk).build()
        .perform();

        final WebElement weManageArea = driver.findElement(By
                .className("manage_area_2"));

        wait.until(ExpectedConditions.visibilityOf(weManageArea));

        Assert.assertTrue(weManageArea.isDisplayed());
    }

    @Test(description = "Verify login with newly changed password.", dataProvider = "credentials")
    public void login(final String username, final String password)
            throws Exception {

        User.log.log(Level.INFO, "Username: " + username + " Password: "
                + password);

        driver.get(baseUrl + "/ui/#/login");
        new WebDriverWait(driver, 10);

        final WebElement weUsername = driver.findElement(By.name("userName"));
        final WebElement wePassword = driver.findElement(By.name("password"));
        final WebElement weSubmit = driver.findElement(By
                .className("login_submit_button"));

        final Actions action = new Actions(driver);

        action.moveToElement(weUsername).sendKeys(weUsername, username)
        .moveToElement(wePassword).sendKeys(wePassword, password)
        .click(weSubmit).build().perform();

        final WebElement weManageArea = driver.findElement(By
                .className("manage_area_2"));

        Assert.assertTrue(weManageArea.isDisplayed());
    }

}
