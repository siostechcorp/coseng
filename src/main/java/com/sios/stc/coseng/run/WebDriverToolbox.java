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

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
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
@Deprecated
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
    private CosengRunner       cosengRunner = new CosengRunner();

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
    @Deprecated
    protected WebDriverToolbox(Test test, WebDriver webDriver, Object webDriverService)
            throws CosengException {
        // do nothing
    }

    @Deprecated
    protected Test getTest() {
        return CosengRunner.getTest();
    }

    /**
     * Gets the web driver.
     *
     * @return the web driver
     * @since 2.0
     * @version.coseng
     */
    @Deprecated
    protected WebDriver getWebDriver() {
        return CosengRunner.getWebDriver();
    }

    /**
     * Gets the web driver service.
     *
     * @return the web driver service
     * @since 2.0
     * @version.coseng
     */
    @Deprecated
    protected Object getWebDriverService() {
        return CosengRunner.getWebDriverService();
    }

    /**
     * Gets the web driver wait.
     *
     * @return the web driver wait
     * @since 2.0
     * @version.coseng
     */
    @Deprecated
    protected WebDriverWait getWebDriverWait() {
        return CosengRunner.getWebDriverWait();
    }

    /**
     * Gets the actions.
     *
     * @return the actions
     * @since 2.0
     * @version.coseng
     */
    @Deprecated
    protected Actions getActions() {
        return CosengRunner.getActions();
    }

    /**
     * Gets the javascript executor.
     *
     * @return the javascript executor
     * @since 2.0
     * @version.coseng
     */
    @Deprecated
    protected JavascriptExecutor getJavascriptExecutor() {
        return CosengRunner.getJavascriptExecutor();
    }

    /**
     * Gets the ng web driver.
     *
     * @return the ng web driver
     * @since 2.0
     * @version.coseng
     */
    @Deprecated
    protected NgWebDriver getNgWebDriver() {
        return CosengRunner.getNgWebDriver();
    }

    /**
     * Gets the web driver util.
     *
     * @return the web driver util
     * @see {@link WebDriverUtil}
     * @since 2.0
     * @version.coseng
     */
    @Deprecated
    protected WebDriverUtil getWebDriverUtil() {
        return cosengRunner.getWebDriverUtil();
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
    @Deprecated
    protected WebElement newWebElement(final By by) throws CosengException {
        return cosengRunner.newWebElement(by);
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
    @Deprecated
    protected WebElements getWebElements() {
        return cosengRunner.getWebElements();
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
    @Deprecated
    protected List<WebElement> getWebElementList() {
        return cosengRunner.getWebElementList();
    }

}
