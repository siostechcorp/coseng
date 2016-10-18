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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.paulhammant.ngwebdriver.NgWebDriver;

/**
 * The Class CosengRunner. This is the class that each TestNG class under test
 * must extend to access the appropriate web driver at the requested depth of
 * parallelism. This class provides convenience methods to access the available
 * web driver and its derived objects such as Actions and JavascriptExecutor as
 * well as manage Selenium WebElements as objects for the level of parallelism.
 * <b>Note:</b> Call these methods from within test class methods.
 * <b>Caution:</b> Unless you manage the test context, calling these methods
 * outside of the TestNG test class methods will have unintended consequences.
 *
 * @since 2.0
 * @version.coseng
 */
public class CosengRunner {

    private static Map<Thread, Test>             threadTest             =
            new HashMap<Thread, Test>();
    private static Map<Thread, WebDriverToolbox> threadWebDriverToolbox =
            new HashMap<Thread, WebDriverToolbox>();
    private static int                           startedWebDriver       = 0;
    private static int                           stoppedWebDriver       = 0;

    /**
     * Instantiates a new coseng runner.
     * 
     * @since 2.0
     * @version.coseng
     */
    protected CosengRunner() {
        /*
         * Do nothing; avoids having to declare a default constructor for the
         * TestNG test classes which would be required if instantiating the this
         * class as it throws CosengExceptions. Reason why each test class
         * extends this class.
         */
    }

    /**
     * Gets the test.
     *
     * @return the test
     * @see com.sios.stc.coseng.run.WebDriverToolbox#getTest()
     * @since 2.0
     * @version.coseng
     */
    protected static Test getTest() {
        if (hasWebDriverToolbox()) {
            return getWebDriverToolbox().getTest();
        }
        return null;
    }

    /**
     * Gets the test associated for a given thread.
     *
     * @param thread
     *            the thread
     * @return the thread test
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized Test getThreadTest(Thread thread) {
        if (threadTest.containsKey(thread)) {
            return threadTest.get(thread);
        }
        return null;
    }

    /**
     * Sets the thread test association.
     *
     * @param thread
     *            the thread
     * @param test
     *            the test
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized void setThreadTest(Thread thread, Test test) {
        if (thread != null && test != null) {
            threadTest.put(thread, test);
        }
    }

    /**
     * Gets the web driver toolbox for the current thread.
     *
     * @return the web driver toolbox
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized WebDriverToolbox getWebDriverToolbox() {
        return getWebDriverToolbox(Thread.currentThread());
    }

    /**
     * Gets the web driver toolbox for a given thread.
     *
     * @param thread
     *            the thread
     * @return the web driver toolbox
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized WebDriverToolbox getWebDriverToolbox(Thread thread) {
        if (threadWebDriverToolbox.containsKey(thread)) {
            return threadWebDriverToolbox.get(thread);
        }
        return null;
    }

    /**
     * Sets the web driver toolbox for a given thread.
     *
     * @param thread
     *            the thread
     * @param webDriverToolbox
     *            the web driver toolbox
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized void setWebDriverToolbox(Thread thread,
            WebDriverToolbox webDriverToolbox) {
        if (thread != null && webDriverToolbox != null) {
            threadWebDriverToolbox.put(thread, webDriverToolbox);
            startedWebDriver++;
        }
    }

    /**
     * Checks for web driver toolbox for the current thread.
     *
     * @return true, if successful
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized boolean hasWebDriverToolbox() {
        Thread thread = Thread.currentThread();
        return hasWebDriverToolbox(thread);
    }

    /**
     * Checks for web driver toolbox for a given thread.
     *
     * @param thread
     *            the thread
     * @return true, if successful
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized boolean hasWebDriverToolbox(Thread thread) {
        if (threadWebDriverToolbox.containsKey(thread)) {
            return true;
        }
        return false;
    }

    /**
     * Checks for web driver started.
     *
     * @return true, if successful
     * @see com.sios.stc.coseng.run.WebDriverToolbox
     * @since 2.0
     * @version.coseng
     */
    protected boolean hasWebDriver() {
        if (hasWebDriverToolbox() && getWebDriverToolbox().getWebDriver() != null) {
            return true;
        }
        return false;
    }

    /**
     * Gets the started web driver.
     *
     * @return the web driver
     * @see com.sios.stc.coseng.run.WebDriverToolbox#getWebDriver()
     * @since 2.0
     * @version.coseng
     */
    protected WebDriver getWebDriver() {
        if (hasWebDriverToolbox()) {
            return getWebDriverToolbox().getWebDriver();
        }
        return null;
    }

    /**
     * Gets the web driver wait.
     *
     * @return the web driver wait
     * @see com.sios.stc.coseng.run.WebDriverToolbox#getWebDriverWait()
     * @since 2.0
     * @version.coseng
     */
    protected WebDriverWait getWebDriverWait() {
        if (hasWebDriverToolbox()) {
            return getWebDriverToolbox().getWebDriverWait();
        }
        return null;
    }

    /**
     * Gets the actions.
     *
     * @return the actions
     * @see com.sios.stc.coseng.run.WebDriverToolbox#getActions()
     * @since 2.0
     * @version.coseng
     */
    protected Actions getActions() {
        if (hasWebDriverToolbox()) {
            return getWebDriverToolbox().getActions();
        }
        return null;
    }

    /**
     * Gets the javascript executor.
     *
     * @return the javascript executor
     * @see com.sios.stc.coseng.run.WebDriverToolbox#getJavascriptExecutor()
     * @since 2.0
     * @version.coseng
     */
    protected JavascriptExecutor getJavascriptExecutor() {
        if (hasWebDriverToolbox()) {
            return getWebDriverToolbox().getJavascriptExecutor();
        }
        return null;
    }

    /**
     * Gets the ng web driver.
     *
     * @return the ng web driver
     * @see com.sios.stc.coseng.run.WebDriverToolbox#getNgWebDriver()
     * @since 2.0
     * @version.coseng
     */
    protected NgWebDriver getNgWebDriver() {
        if (hasWebDriverToolbox()) {
            return getWebDriverToolbox().getNgWebDriver();
        }
        return null;
    }

    /**
     * Gets the web driver util.
     *
     * @return the web driver util
     * @see com.sios.stc.coseng.run.WebDriverToolbox#getWebDriverUtil()
     * @see com.sios.stc.coseng.run.WebDriverUtil
     * @since 2.0
     * @version.coseng
     */
    protected WebDriverUtil getWebDriverUtil() {
        if (hasWebDriverToolbox()) {
            return getWebDriverToolbox().getWebDriverUtil();
        }
        return null;
    }

    /**
     * New web element.
     *
     * @return the web element
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.CosengRunner#newWebElement(By)
     * @see com.sios.stc.coseng.run.WebElements
     * @see com.sios.stc.coseng.run.WebElement
     * @since 2.0
     * @version.coseng
     */
    protected WebElement newWebElement() throws CosengException {
        return newWebElement(null);
    }

    /**
     * New web element.
     *
     * @param by
     *            the by
     * @return the web element
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.WebDriverToolbox#newWebElement(By)
     * @see com.sios.stc.coseng.run.WebElement#WebElement(WebDriverToolbox, By)
     * @see com.sios.stc.coseng.run.WebElements
     * @see com.sios.stc.coseng.run.WebElement
     * @since 2.0
     * @version.coseng
     */
    protected WebElement newWebElement(final By by) throws CosengException {
        if (hasWebDriverToolbox()) {
            return getWebDriverToolbox().newWebElement(by);
        }
        return null;
    }

    /**
     * Gets the web elements.
     *
     * @return the web elements
     * @see com.sios.stc.coseng.run.WebDriverToolbox#getWebElements()
     * @see com.sios.stc.coseng.run.WebElements
     * @see com.sios.stc.coseng.run.WebElement
     * @since 2.0
     * @version.coseng
     */
    protected WebElements getWebElements() {
        if (hasWebDriverToolbox()) {
            return getWebDriverToolbox().getWebElements();
        }
        return null;
    }

    /**
     * Gets the web element list.
     *
     * @return the web element list
     * @see com.sios.stc.coseng.run.WebDriverToolbox#getWebElementList()
     * @see com.sios.stc.coseng.run.WebElements
     * @see com.sios.stc.coseng.run.WebElement
     * @since 2.0
     * @version.coseng
     */
    protected List<WebElement> getWebElementList() {
        if (hasWebDriverToolbox()) {
            return getWebDriverToolbox().getWebElementList();
        }
        return null;
    }

    /**
     * Gets the started web driver count.
     *
     * @return the started web driver count
     * @see com.sios.stc.coseng.run.CosengRunner#setWebDriverToolbox(Thread,
     *      WebDriverToolbox)
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized int getStartedWebDriverCount() {
        return startedWebDriver;
    }

    /**
     * Increment stopped web driver count.
     *
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized void incrementStoppedWebDriverCount() {
        stoppedWebDriver++;
    }

    /**
     * Gets the stopped web driver count.
     *
     * @return the stopped web driver count
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized int getStoppedWebDriverCount() {
        return stoppedWebDriver;
    }

}
