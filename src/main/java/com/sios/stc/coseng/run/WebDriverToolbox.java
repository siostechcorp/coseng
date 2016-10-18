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

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.paulhammant.ngwebdriver.NgWebDriver;

/**
 * The Class WebDriverToolbox holds the previously selected webDriver type and
 * creates common web driver derivatives such as WebDriverWait, Actions,
 * JavascriptExecutor and NgWebDriver. It also creates the convenience
 * WebDriverUtil and empty WebElements.
 *
 * @since 2.0
 * @version.coseng
 */
class WebDriverToolbox {

    private Test               test;
    private WebDriver          webDriver;
    private Object             webDriverService;
    private WebDriverWait      webDriverWait;
    private Actions            actions;
    private JavascriptExecutor jsExecutor;
    private NgWebDriver        ngWebDriver;
    private WebDriverUtil      webDriverUtil;
    private WebElements        webElements;

    /**
     * Instantiates a new web driver toolbox.
     *
     * @param test
     *            the test; may not be null
     * @param webDriver
     *            the web driver; may not be null
     * @param webDriverService
     *            the web driver service; may be null if location {@code GRID}
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.WebDriverUtil
     * @see com.sios.stc.coseng.run.WebElement
     * @see com.sios.stc.coseng.run.WebElements
     * @since 2.0
     * @version.coseng
     */
    protected WebDriverToolbox(Test test, WebDriver webDriver, Object webDriverService)
            throws CosengException {
        if (test != null && webDriver != null) {
            ((RemoteWebDriver) webDriver).setLogLevel(Level.FINE);
            webDriver.manage().timeouts().implicitlyWait(test.getWebDriverTimeoutSeconds(),
                    TimeUnit.SECONDS);
            webDriverWait = new WebDriverWait(webDriver, test.getWebDriverWaitTimeoutSeconds());
            actions = new Actions(webDriver);
            jsExecutor = (JavascriptExecutor) webDriver;
            ngWebDriver = new NgWebDriver(jsExecutor);
        } else {
            throw new CosengException("Error creating WebDriverToolbox");
        }
        this.webDriver = webDriver;
        this.webDriverService = webDriverService;
        this.webDriverUtil = new WebDriverUtil(test, webDriver);
        this.webElements = new WebElements();
        this.test = test;
    }

    protected Test getTest() {
        return test;
    }

    /**
     * Gets the web driver.
     *
     * @return the web driver
     * @since 2.0
     * @version.coseng
     */
    protected WebDriver getWebDriver() {
        return webDriver;
    }

    /**
     * Gets the web driver service.
     *
     * @return the web driver service
     * @since 2.0
     * @version.coseng
     */
    protected Object getWebDriverService() {
        return webDriverService;
    }

    /**
     * Gets the web driver wait.
     *
     * @return the web driver wait
     * @since 2.0
     * @version.coseng
     */
    protected WebDriverWait getWebDriverWait() {
        return webDriverWait;
    }

    /**
     * Gets the actions.
     *
     * @return the actions
     * @since 2.0
     * @version.coseng
     */
    protected Actions getActions() {
        return actions;
    }

    /**
     * Gets the javascript executor.
     *
     * @return the javascript executor
     * @since 2.0
     * @version.coseng
     */
    protected JavascriptExecutor getJavascriptExecutor() {
        return jsExecutor;
    }

    /**
     * Gets the ng web driver.
     *
     * @return the ng web driver
     * @since 2.0
     * @version.coseng
     */
    protected NgWebDriver getNgWebDriver() {
        return ngWebDriver;
    }

    /**
     * Gets the web driver util.
     *
     * @return the web driver util
     * @see {@link WebDriverUtil}
     * @since 2.0
     * @version.coseng
     */
    protected WebDriverUtil getWebDriverUtil() {
        return webDriverUtil;
    }

    /**
     * New web element.
     *
     * @param by
     *            the by
     * @return the web element
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.WebElement
     * @since 2.0
     * @version.coseng
     */
    protected WebElement newWebElement(final By by) throws CosengException {
        WebElement webElement = new WebElement(this, by);
        webElements.add(webElement);
        return webElement;
    }

    /**
     * Gets the web elements.
     *
     * @return the web elements
     * @see com.sios.stc.coseng.run.WebElement
     * @see com.sios.stc.coseng.run.WebElement.WebElements
     * @since 2.0
     * @version.coseng
     */
    protected WebElements getWebElements() {
        return webElements;
    }

    /**
     * Gets the web element list.
     *
     * @return the web element list
     * @see com.sios.stc.coseng.run.WebElement
     * @see com.sios.stc.coseng.run.WebElement.WebElements
     * @since 2.0
     * @version.coseng
     */
    protected List<WebElement> getWebElementList() {
        return webElements.get();
    }

}
