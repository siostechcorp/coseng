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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.sios.stc.coseng.util.Common;
import com.sios.stc.coseng.util.Common.Browser;
import com.sios.stc.coseng.util.Common.Spot;
import com.sios.stc.coseng.util.Run;

public class Base {

    public String testName;
    public String baseUrl;
    public Platform platform;
    public Browser browser;
    public WebDriver driver;
    private Spot spot;
    private String testReportDirectoryPath;

    @BeforeSuite(alwaysRun = true, description = "Get Selenium browser driver, start services")
    public void onStart() throws MalformedURLException, IOException {
        final Run rp = Common.sharedRunParam;
        testName = rp.getThreadTestName(Thread.currentThread()
                .getId());
        baseUrl = rp.getBaseUrl(testName);
        spot = rp.getSpot(testName);
        platform = rp.getPlatform(testName);
        browser = rp.getBrowser(testName);
        testReportDirectoryPath = rp.getTestReportDirectoryPath(testName);
        driver = rp.getWebDriver(testName);
        // *may* start a service depending on spot and driver
        // only started *once* per driver type
        // stopped at conclusion of all test runners.
        rp.startService(spot, driver);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }

    @AfterSuite(alwaysRun = true, description = "Quit Selenium browser driver")
    public void onFinish() {
        driver.quit();
    }

    public String logTestName() {
        return "testName (" + testName + ")";
    }

    // To accept self-signed or other SSL Certificates
    // Should try with browser profile; this as last resort.
    // Firefox and Chrome WebDrivers handle invalid SSL Certificates.
    // IE9,10,11 all use 'overridelink' for invalid SSL Certificate warning.
    public void acceptSslCertificate(final WebDriver webDriver)
            throws InterruptedException {
        // For Internet Explorer
        if (!browser.equals(Browser.FIREFOX) || !browser.equals(Browser.CHROME)) {
            final Boolean title = webDriver.getTitle().equals(
                    "Certificate Error: Navigation Blocked");
            final int count = webDriver.findElements(By.id("overridelink"))
                    .size();
            if (title && (count == 1)) {
                final WebElement overrideLink = webDriver.findElement(By
                        .id("overridelink"));
                if (overrideLink.isDisplayed()) {
                    new Actions(driver).moveToElement(overrideLink).click()
                    .build().perform();
                }
            }
        }
    }

    // Save a screenshot; best attempt - don't fail if can't
    public void saveScreenshot(final WebDriver webDriver, final String name)
            throws IOException {
        final File scrFile = ((TakesScreenshot) webDriver)
                .getScreenshotAs(OutputType.FILE);
        // Now you can do whatever you need to do with it, for example copy
        // somewhere
        FileUtils.copyFile(scrFile,
                new File(testReportDirectoryPath
                        + File.separator + "screenshot-" + name + ".png"));
    }

}
