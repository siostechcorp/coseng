/*
 * Concurrent Selenium TestNG (COSENG)
 * Copyright (c) 2013-2016 SIOS Technology Corp.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sios.stc.coseng.run;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;

import com.sios.stc.coseng.RunTests;
import com.sios.stc.coseng.run.Browsers.Browser;
import com.sios.stc.coseng.run.Locations.Location;
import com.sios.stc.coseng.run.Matcher.MatchBy;
import com.sios.stc.coseng.util.Resource;

/**
 * The Class WebDriverUtil is a collection of convenience methods to perform
 * common and repetitive web driver actions. Actions include such methods as
 * accepting invalid ssl certificates, taking screenshots, uploading files,
 * searching for web elements and pausing a thread.
 *
 * @since 2.0
 * @version.coseng
 */
public class WebDriverUtil {

    private static final Logger log = LogManager.getLogger(RunTests.class.getName());
    private Test                test;
    private WebDriver           webDriver;

    /**
     * Instantiates a new web driver util.
     *
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.WebDriverUtil#WebDriverUtil(Test, WebDriver)
     * @since 2.0
     * @version.coseng
     */
    public WebDriverUtil() throws CosengException {
        this(null, null);
    }

    /**
     * Instantiates a new web driver util.
     *
     * @param test
     *            the test
     * @param webDriver
     *            the web driver
     * @throws CosengException
     *             the coseng exception
     * @since 2.0
     * @version.coseng
     */
    public WebDriverUtil(Test test, WebDriver webDriver) throws CosengException {
        if (test != null && webDriver != null) {
            this.test = test;
            this.webDriver = webDriver;
        } else {
            throw new CosengException("test or webDriver null; nothing to do");
        }
    }

    /**
     * Current url contains.
     *
     * @param route
     *            the route
     * @return true, if successful
     * @since 2.0
     * @version.coseng
     */
    public boolean currentUrlContains(String route) {
        return currentUrlMatchBy(route, MatchBy.CONTAIN);
    }

    /**
     * Current url equals.
     *
     * @param route
     *            the route
     * @return true, if successful
     * @since 2.0
     * @version.coseng
     */
    public boolean currentUrlEquals(String route) {
        return currentUrlMatchBy(route, MatchBy.EQUAL);
    }

    /**
     * Current url match by.
     *
     * @param route
     *            the route
     * @param matchBy
     *            the match by
     * @return true, if successful
     * @since 2.0
     * @version.coseng
     */
    private boolean currentUrlMatchBy(String route, MatchBy matchBy) {
        boolean matched = false;
        if (webDriver.getCurrentUrl() != null) {
            String currentUrl = webDriver.getCurrentUrl();
            if (currentUrl != null) {
                if (MatchBy.CONTAIN.equals(matchBy)) {
                    if (currentUrl.contains(route)) {
                        matched = true;
                    }
                } else {
                    // default matcher
                    if (currentUrl.equals(route)) {
                        matched = true;
                    }
                }
            }
            Assert.assertTrue(matched, "Expected current URL [" + currentUrl + "] to "
                    + matchBy.toString().toLowerCase() + " [" + route + "]");
        }
        return matched;
    }

    /**
     * Accept invalid SSL certificate.
     *
     * @since 2.0
     * @version.coseng
     */
    public void acceptInvalidSSLCertificate() {
        /*
         * To accept self-signed or other SSL Certificates Should try with
         * browser profile; this as last resort.
         */
        /*
         * 2016-09-01 Doesn't work with FF 48. Can't do gimick as with IE since
         * geckodriver will bomb before getting chance to 'drive' thru manually
         * accepting the invalid certs. (Awaiting upstream geckodriver fix).
         * Till then import cert into profile or add to browser.
         */
        Browser browser = test.getBrowser();
        if (Browser.IE.equals(browser)) {
            boolean title = webDriver.getTitle().equals("Certificate Error: Navigation Blocked");
            int count = webDriver.findElements(By.id("overridelink")).size();
            if (title && (count == 1)) {
                WebElement overrideLink = webDriver.findElement(By.id("overridelink"));
                if (overrideLink.isDisplayed()) {
                    new Actions(webDriver).moveToElement(overrideLink).click().build().perform();
                }
            }
        } else if (Browser.EDGE.equals(browser)) {
            boolean title = webDriver.getTitle().equals("Certificate error: Navigation blocked");
            int count = webDriver.findElements(By.id("continueLink")).size();
            if (title && (count == 1)) {
                WebElement overrideLink = webDriver.findElement(By.id("continueLink"));
                if (overrideLink.isDisplayed()) {
                    new Actions(webDriver).moveToElement(overrideLink).click().build().perform();
                }
            }
        }
    }

    /**
     * Save screenshot.
     *
     * @see com.sios.stc.coseng.run.WebDriverUtil#saveScreenshot(String)
     * @since 2.0
     * @version.coseng
     */
    public void saveScreenshot() {
        saveScreenshot(null);
    }

    /**
     * Save screenshot. Best effort. Will warn if unable to save screenshot.
     *
     * @param name
     *            the name; may not be null or empty
     * @since 2.0
     * @version.coseng
     */
    public void saveScreenshot(String name) {
        File testReportDirectory = test.getReportDirectoryFile();
        if (name == null || name.isEmpty()) {
            DateFormat dateFormat = new SimpleDateFormat("YYYYMMddHHmmss");
            Calendar cal = Calendar.getInstance();
            name = dateFormat.format(cal.getTime());
        }
        try {
            File scrFile = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile,
                    new File(testReportDirectory + File.separator + "screenshot-" + name + ".png"));
        } catch (Exception e) {
            log.warn("Save screenshot [" + name + "] unsuccessful", e);
        }
    }

    /**
     * Upload file.
     *
     * @param uploadElement
     *            the upload element; must exist, be displayed and not have
     *            "readonly" attribute
     * @param fileName
     *            the file name must not be null or empty
     * @return true, if successful
     * @see com.sios.stc.coseng.util.Resource#get(String)
     * @see com.sios.stc.coseng.util.Resource#create(InputStream, File)
     * @since 2.0
     * @version.coseng
     */
    public boolean uploadFile(WebElement uploadElement, String fileName) {
        if (fileName != null && !fileName.isEmpty() && uploadElement != null
                && uploadElement.isDisplayed() && uploadElement.getAttribute("readonly") == null) {
            try {
                InputStream fileInput = Resource.get(fileName);
                Location location = test.getLocation();
                File resourceDir = test.getResourceDirectory();
                File resource = new File(resourceDir + File.separator + fileName);
                Resource.create(fileInput, resource);
                if (Location.GRID.equals(location)) {
                    RemoteWebDriver wd = (RemoteWebDriver) webDriver;
                    wd.setFileDetector(new LocalFileDetector());
                    uploadElement.sendKeys(resource.getCanonicalPath());
                } else {
                    uploadElement.sendKeys(resource.getCanonicalPath());
                }
                return true;
            } catch (CosengException | IOException e) {
                log.error("Unable to upload file [{}]; assure file exists", fileName, e);
            }
        } else {
            log.warn(
                    "Unable to upload file [{}]; fileName may not be empty, uploadElement must exist, be displayed and must not have 'readonly' attribute",
                    fileName);
        }
        return false;
    }

    /**
     * Pause.
     *
     * @see com.sios.stc.coseng.run.WebDriverUtil#pause(Long)
     * @since 2.0
     * @version.coseng
     */
    public void pause() {
        pause(null);
    }

    /**
     * Pause.
     *
     * @param milliseconds
     *            the milliseconds
     * @since 2.0
     * @version.coseng
     */
    public void pause(Long milliseconds) {
        Long defaultMilliseconds = new Long(1000);
        Long millis;
        if (milliseconds == null) {
            millis = defaultMilliseconds;
        } else {
            millis = milliseconds;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.warn("Sleep thread [" + millis.toString() + "] milliseconds interrupted", e);
        }
    }

}
