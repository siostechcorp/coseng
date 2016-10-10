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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.xml.XmlSuite.ParallelMode;

import com.google.common.annotations.VisibleForTesting;
import com.paulhammant.ngwebdriver.NgWebDriver;

/**
 * The Class CosengRunner should be used to extend TestNG test classes.
 * Extending TestNG test classes provides the appropriate web driver for the
 * given TestNG parallel mode. CosengRunner manages the collection of Runner
 * created for the COSENG tests. It must be populated with Tests prior to
 * creating any Runner.
 *
 * @see com.sios.stc.coseng.run.Runner
 * @see com.sios.stc.coseng.run.Tests
 * @since 2.0
 * @version.coseng
 */
public class CosengRunner {
    /**
     * The Enum WebDriverAction.
     *
     * @since 2.0
     * @version.coseng
     */
    private static enum WebDriverAction {
        START, STOP
    };

    private static final Logger log     = LogManager.getLogger(Coseng.class.getName());
    private static List<Runner> runners = new ArrayList<Runner>();
    private static Tests        tests   = new Tests();

    /**
     * Instantiates a new coseng runner. Protected so new instances can't be
     * created in extending class. Does nothing; avoids having to declare a
     * default constructor for the TestNG test classes which would be required
     * if instantiating the this class as it throws CosengExceptions. Reason why
     * each test class extends this class.
     *
     * @since 2.0
     * @version.coseng
     */
    protected CosengRunner() {
        // Do nothing
    }

    /**
     * Populate. Pre-populates the instance with validated tests that are to be
     * executed. The tests provide a means to cross-reference the TestNG Suite
     * XML tests and associated thread Runner.
     *
     * @param tests
     *            the tests; may not be null
     * @throws CosengException
     *             the coseng exception if tests null
     * @see com.sios.stc.coseng.run.Node
     * @see com.sios.stc.coseng.run.Test
     * @see com.sios.stc.coseng.run.Tests
     * @since 2.0
     * @version.coseng
     */
    protected static void populate(Tests tests) throws CosengException {
        /* Load up the tests that will be run */
        if (tests != null) {
            CosengRunner.tests = tests;
        } else {
            throw new CosengException("Tests null; nothing to do");
        }
    }

    /**
     * Creates the runner for the current thread. Must be populated with
     * validated tests prior to creation.
     *
     * @param test
     *            the test; may not be null
     * @return the runner
     * @throws CosengException
     *             the coseng exception on test or tests null
     * @see com.sios.stc.coseng.run.CosengRunner#populate(Tests)
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized Runner createRunner(Test test) throws CosengException {
        if (tests != null && test != null && tests.hasTest(test)) {
            Runner runner = new Runner(Thread.currentThread(), test);
            log.debug("Runner [{}] created for Test [{}]", runner.hashCode(), test.getName());
            runners.add(runner);
            return runner;
        } else {
            throw new CosengException(
                    "Tests, test null or test doesn't exist; assure populated prior with CosengRunner.populate(tests)");
        }
    }

    /**
     * Creates the runner for the current thread by test name.
     *
     * @param name
     *            the name
     * @return the runner
     * @throws CosengException
     *             the coseng exception on test not found by name
     * @see com.sios.stc.coseng.run.CosengRunner#createRunner(Test)
     * @since 2.0
     * @version.coseng
     */
    private static synchronized Runner createRunner(String name) throws CosengException {
        Test test = tests.getTest(name);
        if (test != null) {
            return createRunner(test);
        } else {
            throw new CosengException(
                    "Create new thread runner failed; test name [" + name + "] not found");
        }
    }

    /**
     * Gets the runner for the current thread.
     *
     * @return the runner
     * @since 2.0
     * @version.coseng
     */
    private synchronized Runner get() {
        Thread currentThread = Thread.currentThread();
        for (Runner runner : runners) {
            if (runner != null) {
                Thread runnerThread = runner.getThread();
                if (currentThread.equals(runnerThread)) {
                    return runner;
                }
            }
        }
        return null;
    }

    /**
     * Checks if is runner started.
     *
     * @return true, if is runner started
     * @since 2.0
     * @version.coseng
     */
    private boolean isRunnerStarted() {
        Runner runner = get();
        if (runner != null && runner.hasWebDriverToolbox()) {
            return true;
        }
        return false;
    }

    /**
     * Gets the test from the current thread runner.
     *
     * @return the test
     * @since 2.0
     * @version.coseng
     */
    protected Test getTest() {
        Runner runner = get();
        if (runner != null) {
            return runner.getTest();
        }
        return null;
    }

    /**
     * Checks for web driver.
     *
     * @return true, if successful
     * @since 2.0
     * @version.coseng
     */
    protected boolean hasWebDriver() {
        Runner runner = get();
        if (isRunnerStarted() && runner.getWebDriverToolbox().getWebDriver() != null) {
            return true;
        }
        return false;
    }

    /**
     * Gets the web driver.
     *
     * @return the web driver
     * @since 2.0
     * @version.coseng
     */
    protected WebDriver getWebDriver() {
        Runner runner = get();
        if (isRunnerStarted()) {
            return runner.getWebDriverToolbox().getWebDriver();
        }
        return null;
    }

    /**
     * Gets the web driver wait.
     *
     * @return the web driver wait
     * @since 2.0
     * @version.coseng
     */
    protected WebDriverWait getWebDriverWait() {
        Runner runner = get();
        if (isRunnerStarted()) {
            return runner.getWebDriverToolbox().getWebDriverWait();
        }
        return null;
    }

    /**
     * Gets the actions.
     *
     * @return the actions
     * @since 2.0
     * @version.coseng
     */
    protected Actions getActions() {
        Runner runner = get();
        if (isRunnerStarted()) {
            return runner.getWebDriverToolbox().getActions();
        }
        return null;
    }

    /**
     * Gets the javascript executor.
     *
     * @return the javascript executor
     * @since 2.0
     * @version.coseng
     */
    protected JavascriptExecutor getJavascriptExecutor() {
        Runner runner = get();
        if (isRunnerStarted()) {
            return runner.getWebDriverToolbox().getJavascriptExecutor();
        }
        return null;
    }

    /**
     * Gets the ng web driver.
     *
     * @return the ng web driver
     * @since 2.0
     * @version.coseng
     */
    protected NgWebDriver getNgWebDriver() {
        Runner runner = get();
        if (isRunnerStarted()) {
            return runner.getWebDriverToolbox().getNgWebDriver();
        }
        return null;
    }

    /**
     * Gets the web driver util.
     *
     * @return the web driver util
     * @see com.sios.stc.coseng.run.WebDriverUtil
     * @since 2.0
     * @version.coseng
     */
    protected WebDriverUtil getWebDriverUtil() {
        Runner runner = get();
        if (isRunnerStarted()) {
            return runner.getWebDriverToolbox().getWebDriverUtil();
        }
        return null;
    }

    /**
     * New web element.
     *
     * @return the web element
     * @throws CosengException
     *             the coseng exception
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
     * @see com.sios.stc.coseng.run.WebElement
     * @since 2.0
     * @version.coseng
     */
    protected WebElement newWebElement(final By by) throws CosengException {
        Runner runner = get();
        if (isRunnerStarted()) {
            return runner.getWebDriverToolbox().newWebElement(by);
        }
        return null;
    }

    /**
     * Gets the web elements.
     *
     * @return the web elements
     * @see com.sios.stc.coseng.run.WebElements
     * @since 2.0
     * @version.coseng
     */
    protected WebElements getWebElements() {
        Runner runner = get();
        if (isRunnerStarted()) {
            return runner.getWebDriverToolbox().getWebElements();
        }
        return null;
    }

    /**
     * Gets the web element list.
     *
     * @return the web element list
     * @see com.sios.stc.coseng.run.WebElements
     * @since 2.0
     * @version.coseng
     */
    protected List<WebElement> getWebElementList() {
        Runner runner = get();
        if (isRunnerStarted()) {
            return runner.getWebDriverToolbox().getWebElementList();
        }
        return null;
    }

    /**
     * Web driver context.
     *
     * @param testContext
     *            the test context
     * @param parallelMode
     *            the parallel mode
     * @param action
     *            the action
     * @throws CosengException
     *             the coseng exception if current thread runner null or has no
     *             associated test
     * @since 2.0
     * @version.coseng
     */
    private void webDriverContext(ITestContext testContext, ParallelMode parallelMode,
            WebDriverAction action) throws CosengException {
        Runner runner = initializeTestContext(testContext);

        if (runner == null || runner.getTest() == null) {
            throw new CosengException("No runner for thread");
        }

        boolean isOneWebDriver = runner.getTest().isOneWebDriver();

        if (isOneWebDriver && testContext == null && parallelMode == null) {
            webDriverAction(runner, action);
        } else if (!isOneWebDriver && testContext != null && testContext.getSuite() != null
                && parallelMode != null) {
            String testContextParallelMode = testContext.getSuite().getParallel();
            if (parallelMode.toString().equalsIgnoreCase(testContextParallelMode)) {
                webDriverAction(runner, action);
            }
        }
    }

    /**
     * Initialize test context. Extracts the suite xml parameter for the
     * embedded test name to create a runner if one doesn't exist for the
     * current thread.
     *
     * @param testContext
     *            the test context
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.Test#COSENG_XMLSUITE_PARAMETER_TEST_NAME
     * @since 2.0
     * @version.coseng
     */
    private Runner initializeTestContext(ITestContext testContext) throws CosengException {
        Runner runner = get();
        if (testContext != null) {
            String cosengTestName = null;
            if (testContext.getSuite() != null) {
                cosengTestName = testContext.getSuite()
                        .getParameter(Test.COSENG_XMLSUITE_PARAMETER_TEST_NAME);
            }
            if (cosengTestName == null || cosengTestName.isEmpty()) {
                throw new CosengException("Suite parameter ["
                        + Test.COSENG_XMLSUITE_PARAMETER_TEST_NAME + "] absent or not defined");
            }
            if (runner == null) {
                /*
                 * No runner for this thread; create one based on the passed xml
                 * suite parameter
                 */
                return createRunner(cosengTestName);
            }
        }
        return runner;
    }

    /**
     * Web driver action.
     *
     * @param runner
     *            the runner
     * @param action
     *            the action
     * @throws CosengException
     *             the coseng exception
     * @since 2.0
     * @version.coseng
     */
    private void webDriverAction(Runner runner, WebDriverAction action) throws CosengException {
        if (WebDriverAction.START.equals(action)) {
            runner.startWebDriver();
            log.debug("Runner [{}] started webdriver [{}]", runner.hashCode());
        } else if (WebDriverAction.STOP.equals(action)) {
            runner.stopWebDriver();
            log.debug("Runner [{}] stopped webdriver [{}]", runner.hashCode());
        }
    }

    /**
     * Before one web driver. Starts the location, platform and browser specific
     * web driver once for the duration of all test's TestNG suite(s).
     * <b>Caution:</b> Visible for access to TestNG ITestContext. Do not call
     * this method from within the TestNG test classes or test methods. Unless
     * you manage the test context, calling this method from within the TestNG
     * test classes or methods will have unintended consequences.
     *
     * @throws CosengException
     *             the coseng exception on failure to start web driver
     * @since 2.0
     * @version.coseng
     */
    @VisibleForTesting
    protected void _beforeOneWebDriver() throws CosengException {
        webDriverContext(null, null, WebDriverAction.START);
    }

    /**
     * After one web driver. Stops the location, platform and browser specific
     * web driver once for the duration of all test's TestNG suite(s).
     * <b>Caution:</b> Visible for access to TestNG ITestContext. Do not call
     * this method from within the TestNG test classes or test methods. Unless
     * you manage the test context, calling this method from within the TestNG
     * test classes or methods will have unintended consequences.
     *
     * @throws CosengException
     *             the coseng exception on failure to stop web driver
     * @since 2.0
     * @version.coseng
     */
    @VisibleForTesting
    protected void _afterOneWebDriver() throws CosengException {
        webDriverContext(null, null, WebDriverAction.STOP);
    }

    /**
     * Before suite coseng runner. TestNG {@code parallel="false"}. Starts the
     * location, platform and browser specific web driver at the beginning of
     * the TestNG suite {@code <suite></suite>} level. <b>Caution:</b> Visible
     * for access to TestNG ITestContext. Do not call this method from within
     * the TestNG test classes or test methods. Unless you manage the test
     * context, calling this method from within the TestNG test classes or
     * methods will have unintended consequences.
     *
     * @param testContext
     *            the test context
     * @throws CosengException
     *             the coseng exception on failure to start web driver
     * @since 2.0
     * @version.coseng
     */
    @BeforeSuite
    @VisibleForTesting
    protected void _beforeSuiteCosengRunner(ITestContext testContext) throws CosengException {
        webDriverContext(testContext, ParallelMode.FALSE, WebDriverAction.START);
    }

    /**
     * After suite coseng runner. TestNG {@code parallel="false"}. Stops the
     * location, platform and browser specific web driver at the end of the
     * TestNG suite {@code <suite></suite>} level. <b>Caution:</b> Visible for
     * access to TestNG ITestContext. Do not call this method from within the
     * TestNG test classes or test methods. Unless you manage the test context,
     * calling this method from within the TestNG test classes or methods will
     * have unintended consequences.
     *
     * @param testContext
     *            the test context
     * @throws CosengException
     *             the coseng exception on failure to stop web driver
     * @since 2.0
     * @version.coseng
     */
    @AfterSuite
    @VisibleForTesting
    protected void _afterSuiteCosengRunner(ITestContext testContext) throws CosengException {
        webDriverContext(testContext, ParallelMode.FALSE, WebDriverAction.STOP);
    }

    /**
     * Before test coseng runner. TestNG {@code parallel="tests"}. Starts the
     * location, platform and browser specific web driver at the beginning of
     * the TestNG suite {@code <suite></suite>} level. <b>Caution:</b> Visible
     * for access to TestNG ITestContext. Do not call this method from within
     * the TestNG test classes or test methods. Unless you manage the test
     * context, calling this method from within the TestNG test classes or
     * methods will have unintended consequences.
     *
     * @param testContext
     *            the test context
     * @throws CosengException
     *             the coseng exception on failure to start web driver
     * @since 2.0
     * @version.coseng
     */
    @BeforeTest
    @VisibleForTesting
    protected void _beforeTestCosengRunner(ITestContext testContext) throws CosengException {
        webDriverContext(testContext, ParallelMode.TESTS, WebDriverAction.START);
    }

    /**
     * After test coseng runner. TestNG {@code parallel="tests"}. Stops the
     * location, platform and browser specific web driver at the end of the
     * TestNG suite {@code <test></test>} level. <b>Caution:</b> Visible for
     * access to TestNG ITestContext. Do not call this method from within the
     * TestNG test classes or test methods. Unless you manage the test context,
     * calling this method from within the TestNG test classes or methods will
     * have unintended consequences.
     *
     * @param testContext
     *            the test context
     * @throws CosengException
     *             the coseng exception on failure to stop web driver
     * @since 2.0
     * @version.coseng
     */
    @AfterTest
    @VisibleForTesting
    protected void _afterTestCosengRunner(ITestContext testContext) throws CosengException {
        webDriverContext(testContext, ParallelMode.TESTS, WebDriverAction.STOP);
    }

    /**
     * Before class coseng runner. TestNG {@code parallel="classes"}. Starts the
     * location, platform and browser specific web driver at the beginning of
     * the TestNG suite {@code <class></class>} level. <b>Caution:</b> Visible
     * for access to TestNG ITestContext. Do not call this method from within
     * the TestNG test classes or test methods. Unless you manage the test
     * context, calling this method from within the TestNG test classes or
     * methods will have unintended consequences.
     *
     * @param testContext
     *            the test context
     * @throws CosengException
     *             the coseng exception on failure to start web driver
     * @since 2.0
     * @version.coseng
     */
    @BeforeClass
    @VisibleForTesting
    protected void _beforeClassCosengRunner(ITestContext testContext) throws CosengException {
        webDriverContext(testContext, ParallelMode.CLASSES, WebDriverAction.START);
    }

    /**
     * After class coseng runner. TestNG {@code parallel="classes"}. Stops the
     * location, platform and browser specific web driver at the end of the
     * TestNG suite {@code <class></class>} level. <b>Caution:</b> Visible for
     * access to TestNG ITestContext. Do not call this method from within the
     * TestNG test classes or test methods. Unless you manage the test context,
     * calling this method from within the TestNG test classes or methods will
     * have unintended consequences.
     *
     * @param testContext
     *            the test context
     * @throws CosengException
     *             the coseng exception on failure to stop web driver
     * @since 2.0
     * @version.coseng
     */
    @AfterClass
    @VisibleForTesting
    protected void _afterClassCosengRunner(ITestContext testContext) throws CosengException {
        webDriverContext(testContext, ParallelMode.CLASSES, WebDriverAction.STOP);
    }

    /**
     * Before method coseng runner. TestNG {@code parallel="methods"}. Starts
     * the location, platform and browser specific web driver at the beginning
     * of the TestNG suite {@code <method></method>} level. <b>Caution:</b>
     * Visible for access to TestNG ITestContext. Do not call this method from
     * within the TestNG test classes or test methods. Unless you manage the
     * test context, calling this method from within the TestNG test classes or
     * methods will have unintended consequences.
     *
     * @param testContext
     *            the test context
     * @throws CosengException
     *             the coseng exception on failure to start web driver
     * @since 2.0
     * @version.coseng
     */
    @BeforeMethod
    @VisibleForTesting
    protected void _beforeMethodCosengRunner(ITestContext testContext) throws CosengException {
        webDriverContext(testContext, ParallelMode.METHODS, WebDriverAction.START);
    }

    /**
     * After method coseng runner. TestNG {@code parallel="methods"}. Starts the
     * location, platform and browser specific web driver at the beginning of
     * the TestNG suite {@code <method></method>} level. <b>Caution:</b> Visible
     * for access to TestNG ITestContext. Do not call this method from within
     * the TestNG test classes or test methods. Unless you manage the test
     * context, calling this method from within the TestNG test classes or
     * methods will have unintended consequences.
     *
     * @param testContext
     *            the test context
     * @throws CosengException
     *             the coseng exception on failure to stop web driver
     * @since 2.0
     * @version.coseng
     */
    @AfterMethod
    @VisibleForTesting
    protected void _afterMethodCosengRunner(ITestContext testContext) throws CosengException {
        webDriverContext(testContext, ParallelMode.METHODS, WebDriverAction.STOP);
    }
}
