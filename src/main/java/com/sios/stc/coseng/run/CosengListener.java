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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IClassListener;
import org.testng.IExecutionListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite.ParallelMode;

import com.sios.stc.coseng.RunTests;

/**
 * The listener class for receiving all TestNG lifecycle events. This listener
 * is primarily tasked with starting and stopping a web driver at the
 * appropriate level of parallelization. The listener is registered for all
 * classes under test programmatically at construction of the TestNG test. This
 * class listens for TestNG lifecycle events and reacts to it's before and after
 * states. This listener implements all of the TestNG lifecycle events;
 * execution, suite, test, class and method. The activation of test, class and
 * method lifecycle events is influenced by the requested parallel mode. It
 * coordinates with CosengRunner to provide the appropriate web driver for the
 * given current thread. <b>Caution:</b> The listener methods must be visible
 * for access to TestNG listener contexts. Do not call these methods from within
 * the TestNG test classes or test methods. Unless you manage the test context,
 * calling these methods from within the TestNG test classes or test methods
 * will have unintended consequences.
 *
 * 
 * <dl>
 * <dt>TestNG &lt;suite parallel="false|none"&gt; and Coseng Test JSON oneWebDriver "true"</dt>
 * <dd>One web driver will be started at the start of TestNG execution</dd>
 * <dd>It will persist across all suites and tests and will be the only web
 * driver presented</dd>
 * <dd>The singular web driver will be stopped at the end of TestNG
 * execution</dd>
 * <dt>TestNG &lt;suite parallel="false|none"&gt; and Coseng Test JSON oneWebDriver
 * "false"</dt>
 * <dd>A web driver will be created at the start of the suite</dd>
 * <dd>It will persist across all tests and will be the only web driver for the
 * &lt;suite&gt; under test</dd>
 * <dd>The suite's web driver will be stopped at the end of the suite</dd>
 * <dt>TestNG &lt;suite parallel="tests"&gt;</dt>
 * <dd>A separate web driver will be created for each &lt;test&gt; in the
 * suite</dd>
 * <dd>Each web driver provisioned for a suite's &lt;test&gt; will be the only
 * web driver for all &lt;classes&gt; and included &lt;method&gt;</dd>
 * <dd>Each web driver will be stopped at the conclusion of the respective
 * &lt;test&gt;</dd>
 * <dt>TestNG &lt;suite parallel="classes"&gt;</dt>
 * <dd>A separate web driver will be created for each &lt;class&gt; in the
 * suite</dd>
 * <dd>Each web driver provisioned for a suite's &lt;class&gt; will be the only
 * web driver for all included &lt;method&gt;</dd>
 * <dd>Each web driver will be stopped at the conclusion of the respective
 * &lt;class&gt;</dd>
 * <dt>TestNG &lt;suite parallel="methods"&gt;</dt>
 * <dd>A separate web driver will be created for each &lt;method&gt; in the
 * suite</dd>
 * <dd>Each web driver provisioned for a suite's &lt;method&gt; will be the only
 * web driver for the included &lt;method&gt;</dd>
 * <dd>Each web driver will be stopped at the conclusion of the respective
 * &lt;method&gt;</dd>
 * </dl>
 *
 * @since 2.0
 * @version.coseng
 */
public class CosengListener extends WebDriverLifecycle implements IExecutionListener,
        ISuiteListener, ITestListener, IClassListener, IInvokedMethodListener {

    /**
     * The Enum WebDriverAction.
     *
     * @since 2.0
     * @version.coseng
     */
    private static enum WebDriverAction {
        START, STOP
    };

    private static final Logger log           = LogManager.getLogger(RunTests.class.getName());
    private Test                test;
    private boolean             isOneWebDriver;
    private ParallelMode        parallelMode;
    private int                 xmlSuiteCount = 0;

    /**
     * Instantiates a new coseng listener. The constructor must be public for
     * TestNG.
     *
     * @throws CosengException
     *             the coseng exception
     * @since 2.0
     * @version.coseng
     */
    public CosengListener() throws CosengException {
        setCosengContext();
        log.debug("CosengListener constructor, thread [{}]", Thread.currentThread().getId());
    }

    /**
     * Sets the coseng context with the current threads test.
     *
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.Concurrent#run()
     * @see com.sios.stc.coseng.run.CosengRunner
     * @since 2.0
     * @version.coseng
     */
    private synchronized void setCosengContext() throws CosengException {
        Thread thread = Thread.currentThread();
        Test test = CosengRunner.getThreadTest(thread);
        if (test != null) {
            this.test = test;
            isOneWebDriver = test.isOneWebDriver();
        } else {
            throw new CosengException("Unable to find a coseng test for this thread");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.IExecutionListener#onExecutionFinish()
     */
    /* <IExecutionListener> */
    @Override
    public void onExecutionFinish() {
        Thread thread = Thread.currentThread();
        log.debug(
                "TestNG Executor AFTER; thread [{}], test [{}], parallelMode [{}], isOneWebDriver [{}]",
                thread.getId(), test.getName(), parallelMode, isOneWebDriver);
        if (isOneWebDriver) {
            try {
                webDriverAction(WebDriverAction.STOP);
            } catch (CosengException e) {
                test.setIsFailed(true);
                throw new RuntimeException(e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.IExecutionListener#onExecutionStart()
     */
    @Override
    public void onExecutionStart() {
        Thread thread = Thread.currentThread();
        test = CosengRunner.getThreadTest(thread);
        xmlSuiteCount = test.getXmlSuites().size();
        log.debug(
                "TestNG Executor BEORE; thread [{}], test [{}], parallelMode [{}], isOneWebDriver [{}], xmlSuiteCount [{}]",
                thread.getId(), test.getName(), parallelMode, isOneWebDriver, xmlSuiteCount);
        if (isOneWebDriver) {
            try {
                webDriverAction(WebDriverAction.START);
            } catch (CosengException e) {
                test.setIsFailed(true);
                throw new RuntimeException(e);
            }
        }
    }
    /* <IExecutionListener> */

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.ISuiteListener#onFinish(org.testng.ISuite)
     */
    /* <ISuiteListener> */
    @Override
    public void onFinish(ISuite arg0) {
        Thread thread = Thread.currentThread();
        log.debug(
                "Suite AFTER; suite [{}], thread [{}], test [{}], parallelMode [{}], xmlSuiteCount [{}], isOneWebDriver [{}]",
                arg0.getName(), thread.getId(), test.getName(), parallelMode, xmlSuiteCount,
                isOneWebDriver);
        if (!isOneWebDriver && ParallelMode.NONE.equals(parallelMode) && xmlSuiteCount > 0) {
            try {
                webDriverAction(WebDriverAction.STOP);
            } catch (CosengException e) {
                test.setIsFailed(true);
                throw new RuntimeException(e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.ISuiteListener#onStart(org.testng.ISuite)
     */
    @Override
    public void onStart(ISuite arg0) {
        Thread thread = Thread.currentThread();
        /*
         * Get the current Suite XML parallel mode; likely different from any
         * previous suite execution.
         */
        parallelMode = arg0.getXmlSuite().getParallel();
        /*
         * Potential TestNG bug; listener gets Suite BEFORE & AFTER *again* at
         * end of suite processing; a spurious start/stop at the conclusion of
         * the suite. So, track the suite driver start; don't start again if
         * already been through the suite before/after cycle.
         */
        xmlSuiteCount--;
        log.debug(
                "Suite BEFORE; suite [{}], thread [{}], test [{}], parallelMode [{}], xmlSuiteCount [{}], isOneWebDriver [{}]",
                arg0.getName(), thread.getId(), test.getName(), parallelMode, xmlSuiteCount,
                isOneWebDriver);
        if (!isOneWebDriver && ParallelMode.NONE.equals(parallelMode) && xmlSuiteCount > 0) {
            try {
                webDriverAction(WebDriverAction.START);
            } catch (CosengException e) {
                test.setIsFailed(true);
                throw new RuntimeException(e);
            }
        }
    }
    /* </ISuiteListener> */

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.ITestListener#onFinish(org.testng.ITestContext)
     */
    /* <ITestListener> */
    @Override
    public void onFinish(ITestContext arg0) {
        Thread thread = Thread.currentThread();
        log.debug("Test AFTER; thread [{}], test [{}], parallelMode [{}], isOneWebDriver [{}]",
                thread.getId(), test.getName(), parallelMode, isOneWebDriver);
        if (!isOneWebDriver && ParallelMode.TESTS.equals(parallelMode)) {
            try {
                webDriverAction(WebDriverAction.STOP);
            } catch (CosengException e) {
                test.setIsFailed(true);
                throw new RuntimeException(e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.ITestListener#onStart(org.testng.ITestContext)
     */
    @Override
    public void onStart(ITestContext arg0) {
        Thread thread = Thread.currentThread();
        log.debug("Test BEFORE; thread [{}], test [{}], parallelMode [{}], isOneWebDriver [{}]",
                thread.getId(), test.getName(), parallelMode, isOneWebDriver);
        if (!isOneWebDriver && ParallelMode.TESTS.equals(parallelMode)) {
            try {
                webDriverAction(WebDriverAction.START);
            } catch (CosengException e) {
                test.setIsFailed(true);
                throw new RuntimeException(e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.ITestListener#onTestFailedButWithinSuccessPercentage(org.
     * testng.ITestResult)
     */
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult arg0) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.ITestListener#onTestFailure(org.testng.ITestResult)
     */
    @Override
    public void onTestFailure(ITestResult arg0) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.ITestListener#onTestSkipped(org.testng.ITestResult)
     */
    @Override
    public void onTestSkipped(ITestResult arg0) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.ITestListener#onTestStart(org.testng.ITestResult)
     */
    @Override
    public void onTestStart(ITestResult arg0) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.ITestListener#onTestSuccess(org.testng.ITestResult)
     */
    @Override
    public void onTestSuccess(ITestResult arg0) {
        // TODO Auto-generated method stub

    }
    /* </ITestListener> */

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.IClassListener#onAfterClass(org.testng.ITestClass)
     */
    /* <IClassListener> */
    @Override
    public void onAfterClass(ITestClass arg0) {
        Thread thread = Thread.currentThread();
        log.debug(
                "Class AFTER; class [{}], thread [{}], test [{}], parallelMode [{}], isOneWebDriver [{}]",
                arg0.getName(), thread.getId(), test.getName(), parallelMode, isOneWebDriver);
        if (!isOneWebDriver && ParallelMode.CLASSES.equals(parallelMode)) {
            try {
                webDriverAction(WebDriverAction.STOP);
            } catch (CosengException e) {
                test.setIsFailed(true);
                throw new RuntimeException(e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.IClassListener#onBeforeClass(org.testng.ITestClass)
     */
    @Override
    public void onBeforeClass(ITestClass arg0) {
        log.debug(
                "Class BEFORE; class [{}], thread [{}], test [{}], parallelMode [{}], isOneWebDriver [{}]",
                arg0.getName(), Thread.currentThread().getId(), test.getName(), parallelMode,
                isOneWebDriver);
        if (!isOneWebDriver && ParallelMode.CLASSES.equals(parallelMode)) {
            try {
                webDriverAction(WebDriverAction.START);
            } catch (CosengException e) {
                test.setIsFailed(true);
                throw new RuntimeException(e);
            }
        }
    }
    /* </IClassListener> */

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.IInvokedMethodListener#afterInvocation(org.testng.
     * IInvokedMethod, org.testng.ITestResult)
     */
    /* <IMethodListener> */
    @Override
    public void afterInvocation(IInvokedMethod arg0, ITestResult arg1) {
        log.debug(
                "Method AFTER; class [{}], method [{}], thread [{}], test [{}], parallelMode [{}], isOneWebDriver [{}]",
                arg0.getTestMethod().getRealClass().getName(), arg0.getTestMethod().getMethodName(),
                Thread.currentThread().getId(), test.getName(), parallelMode, isOneWebDriver);
        if (!isOneWebDriver && ParallelMode.METHODS.equals(parallelMode)) {
            try {
                webDriverAction(WebDriverAction.STOP);
            } catch (CosengException e) {
                test.setIsFailed(true);
                throw new RuntimeException(e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.IInvokedMethodListener#beforeInvocation(org.testng.
     * IInvokedMethod, org.testng.ITestResult)
     */
    @Override
    public void beforeInvocation(IInvokedMethod arg0, ITestResult arg1) {
        log.debug(
                "Method BEFORE; class [{}], method [{}], thread [{}], test [{}], parallelMode [{}], isOneWebDriver [{}]",
                arg0.getTestMethod().getRealClass().getName(), arg0.getTestMethod().getMethodName(),
                Thread.currentThread().getId(), test.getName(), parallelMode, isOneWebDriver);
        if (!isOneWebDriver && ParallelMode.METHODS.equals(parallelMode)) {
            try {
                webDriverAction(WebDriverAction.START);
            } catch (CosengException e) {
                test.setIsFailed(true);
                throw new RuntimeException(e);
            }
        }
    }
    /* </IMethodListener> */

    /**
     * Web driver action to start or stop the web driver based on the before and
     * after state of the TestNG listeners.
     *
     * @param action
     *            the action
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.WebDriverLifecycle#startWebDriver(Test)
     * @see com.sios.stc.coseng.run.WebDriverLifecycle#stopWebDriver(Test)
     * @see com.sios.stc.coseng.run.WebDriverToolbox
     * @see com.sios.stc.coseng.run.CosengRunner
     * @since 2.0
     * @version.coseng
     */
    private void webDriverAction(WebDriverAction action) throws CosengException {
        WebDriverToolbox webDriverToolbox;
        Thread thread = Thread.currentThread();
        log.debug("Web driver action [{}], thread [{}]", action, thread.getId());
        if (WebDriverAction.START.equals(action)) {
            webDriverToolbox = startWebDriver(test);
            CosengRunner.setWebDriverToolbox(thread, webDriverToolbox);
            log.debug("Started Web driver [{}], thread [{}]",
                    webDriverToolbox.getWebDriver().hashCode(), thread.getId());
        } else if (WebDriverAction.STOP.equals(action)) {
            if (CosengRunner.hasWebDriverToolbox(thread)) {
                webDriverToolbox = CosengRunner.getWebDriverToolbox(thread);
                stopWebDriver(webDriverToolbox);
                CosengRunner.incrementStoppedWebDriverCount();
                log.debug("Stopped web driver [{}], thread [{}]",
                        webDriverToolbox.getWebDriver().hashCode(), thread.getId());
            }
        }
    }

}
