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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * The Class WebDriverUtil is a collection of convenience methods to perform
 * common and repetitive web driver actions. Actions include such methods as
 * accepting invalid ssl certificates, taking screenshots, uploading files,
 * searching for web elements and pausing a thread.
 *
 * @since 2.0
 * @version.coseng
 */
@Deprecated
public class WebDriverUtil {

    private CosengRunner cosengRunner = new CosengRunner();

    /**
     * Instantiates a new web driver util.
     *
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.WebDriverUtil#WebDriverUtil(Test, WebDriver)
     * @since 2.0
     * @version.coseng
     */
    @Deprecated
    public WebDriverUtil() throws CosengException {
        // do nothing
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
    @Deprecated
    public WebDriverUtil(Test test, WebDriver webDriver) throws CosengException {
        // do nothing
    }

    /**
     * Current url contains.
     *
     * @param route
     *            the route
     * @return true, if successful
     * @see com.sios.stc.coseng.run.CosengRunner#currentUrlContains(String)
     * @since 2.0
     * @version.coseng
     */
    @Deprecated
    public boolean currentUrlContains(String route) {
        return cosengRunner.currentUrlContains(route);
    }

    /**
     * Current url equals.
     *
     * @param route
     *            the route
     * @return true, if successful
     * @see com.sios.stc.coseng.run.CosengRunner#currentUrlEquals(String)
     * @since 2.0
     * @version.coseng
     */
    @Deprecated
    public boolean currentUrlEquals(String route) {
        return cosengRunner.currentUrlEquals(route);
    }

    /**
     * Accept invalid SSL certificate.
     *
     * @see com.sios.stc.coseng.run.CosengRunner#acceptInvalidSSLCertificate()
     * @since 2.0
     * @version.coseng
     */
    @Deprecated
    public void acceptInvalidSSLCertificate() {
        cosengRunner.acceptInvalidSSLCertificate();
    }

    /**
     * Save screenshot.
     *
     * @see com.sios.stc.coseng.run.CosengRunner#saveScreenshot()
     * @since 2.0
     * @version.coseng
     */
    @Deprecated
    public void saveScreenshot() {
        cosengRunner.saveScreenshot(null);
    }

    /**
     * Save screenshot. Best effort. Will warn if unable to save screenshot.
     *
     * @param name
     *            the name; may not be null or empty
     * @see com.sios.stc.coseng.run.CosengRunner#saveScreenshot(String)
     * @since 2.0
     * @version.coseng
     */
    @Deprecated
    public void saveScreenshot(String name) {
        cosengRunner.saveScreenshot(name);
    }

    /**
     * Pause.
     *
     * @see com.sios.stc.coseng.run.WebDriverUtil#pause(Long)
     * @see com.sios.stc.coseng.run.CosengRunner#pause()
     * @since 2.0
     * @version.coseng
     */
    @Deprecated
    public void pause() {
        cosengRunner.pause();
    }

    /**
     * Pause.
     *
     * @param milliseconds
     *            the milliseconds
     * @see com.sios.stc.coseng.run.CosengRunner#pause(Long)
     * @since 2.0
     * @version.coseng
     */
    @Deprecated
    public void pause(Long milliseconds) {
        cosengRunner.pause(milliseconds);
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
     * @see com.sios.stc.coseng.run.CosengRunner#uploadFile(WebElement, String)
     * @since 2.0
     * @version.coseng
     */
    @Deprecated
    public boolean uploadFile(WebElement uploadElement, String fileName) {
        try {
            cosengRunner.uploadFile(uploadElement, fileName);
            return true;
        } catch (CosengException e) {
            e.printStackTrace();
            return false;
        }
    }

}
