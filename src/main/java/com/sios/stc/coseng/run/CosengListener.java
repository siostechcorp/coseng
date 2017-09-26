/*
 * Concurrent Selenium TestNG (COSENG)
 * Copyright (c) 2013-2017 SIOS Technology Corp.  All rights reserved.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.IClassListener;
import org.testng.IExecutionListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener2;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;

import com.sios.stc.coseng.integration.Integrator;
import com.sios.stc.coseng.integration.Integrator.TriggerOn;

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
 * <dt>TestNG &lt;suite parallel="false|none"&gt; and Coseng Test JSON
 * oneWebDriver "true"</dt>
 * <dd>One web driver will be started at the start of TestNG execution</dd>
 * <dd>It will persist across all suites and tests and will be the only web
 * driver presented</dd>
 * <dd>The singular web driver will be stopped at the end of TestNG
 * execution</dd>
 * <dt>TestNG &lt;suite parallel="false|none"&gt; and Coseng Test JSON
 * oneWebDriver "false"</dt>
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
        ISuiteListener, ITestListener, IClassListener, IInvokedMethodListener2, IReporter {

    /**
     * The Enum WebDriverAction.
     *
     * @since 2.0
     * @version.coseng
     */
    private static enum WebDriverAction {
        START, STOP
    };

    private static final Logger log           =
            LogManager.getLogger(CosengListener.class.getName());
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
    private synchronized boolean setCosengContext() throws CosengException {
        Thread thread = Thread.currentThread();
        Test threadTest = CosengRunner.getThreadTest(thread);
        if (threadTest == null) {
            /*
             * No test-thread association. Copy current test for new thread and
             * record for further thread use. NOTE! Cloned test is solely to
             * identify the proper TestNG suite, test, class and methods under
             * testing. Setting cloned fields will likely have no influence
             * under TestNG's watch. Here when TestNG parallel mode is tests,
             * classes or methods.
             */
            log.debug("No existing test matches thread [{}]; making copy", thread.getId());
            threadTest = test.deepCopy();
            CosengRunner.addThreadTest(thread, threadTest);
        }
        if (CosengRunner.getThreadTest(thread) != null) {
            test = CosengRunner.getThreadTest(thread);
            isOneWebDriver = test.isOneWebDriver();
            return true;
        } else {
            test.setIsFailed(true);
            throw new CosengException("Unable to deep copy test [" + test.getName() + "]");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.IExecutionListener#onExecutionFinish()
     */
    /* <IExecutionListener> */
    @Override
    public synchronized void onExecutionFinish() {
        Thread thread = Thread.currentThread();
        log.debug(
                "TestNG Executor AFTER; thread [{}], test [{}], testHashCode [{}], parallelMode [{}], isOneWebDriver [{}]",
                thread.getId(), test.getName(), test.hashCode(), parallelMode, isOneWebDriver);
        try {
            notifyIntegrators(TriggerOn.EXECUTIONFINISH);
        } catch (CosengException e) {
            throw new RuntimeException(e);
        }
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
    public synchronized void onExecutionStart() {
        /*
         * Don't use getXmlSuites().size() - it doesn't count the recursively
         * occurring XmlSuite with <test>
         */
        xmlSuiteCount = test.getTestSuiteCount();

        try {
            setCosengContext();
            notifyIntegrators(TriggerOn.EXECUTIONSTART);
        } catch (CosengException e) {
            throw new RuntimeException(e);
        }
        log.debug(
                "TestNG Executor BEFORE; thread [{}], test [{}], testHashCode [{}], parallelMode [{}], isOneWebDriver [{}], xmlSuiteCount [{}]",
                Thread.currentThread().getId(), test.getName(), test.hashCode(), parallelMode,
                isOneWebDriver, xmlSuiteCount);
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
    public synchronized void onFinish(ISuite arg0) {
        try {
            setCosengContext();
            notifyIntegrators(TriggerOn.SUITEFINISH);
        } catch (CosengException e) {
            throw new RuntimeException(e);
        }
        log.debug(
                "Suite AFTER; suite [{}], thread [{}], test [{}], testHashCode [{}], parallelMode [{}], xmlSuiteCount [{}], isOneWebDriver [{}]",
                arg0.getName(), Thread.currentThread().getId(), test.getName(), test.hashCode(),
                parallelMode, xmlSuiteCount, isOneWebDriver);
        if (!isOneWebDriver && ParallelMode.NONE.equals(parallelMode) && xmlSuiteCount >= 0) {
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
    public synchronized void onStart(ISuite arg0) {
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

        try {
            setCosengContext();
            test.setTestNgSuite(arg0);
            notifyIntegrators(TriggerOn.SUITESTART);
        } catch (CosengException e) {
            throw new RuntimeException(e);
        }
        log.debug(
                "Suite BEFORE; suite [{}], thread [{}], test [{}], testHashCode [{}], parallelMode [{}], xmlSuiteCount [{}], isOneWebDriver [{}]",
                arg0.getName(), Thread.currentThread().getId(), test.getName(), test.hashCode(),
                parallelMode, xmlSuiteCount, isOneWebDriver);
        if (!isOneWebDriver && ParallelMode.NONE.equals(parallelMode) && xmlSuiteCount >= 0) {
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
    public synchronized void onFinish(ITestContext arg0) {
        try {
            setCosengContext();
            notifyIntegrators(TriggerOn.TESTFINISH);
        } catch (CosengException e) {
            throw new RuntimeException(e);
        }
        log.debug(
                "Test AFTER; thread [{}], test [{}], testHashCode [{}], parallelMode [{}], isOneWebDriver [{}]",
                Thread.currentThread().getId(), test.getName(), test.hashCode(), parallelMode,
                isOneWebDriver);
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
    /* <ITestListener> */
    @Override
    public synchronized void onStart(ITestContext arg0) {
        try {
            setCosengContext();
            test.setTestNgTest(arg0);
            notifyIntegrators(TriggerOn.TESTSTART);
        } catch (CosengException e) {
            throw new RuntimeException(e);
        }
        log.debug(
                "Test BEFORE; thread [{}], test [{}], testHashCode [{}], parallelMode [{}], isOneWebDriver [{}]",
                Thread.currentThread().getId(), test.getName(), test.hashCode(), parallelMode,
                isOneWebDriver);

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
    public synchronized void onTestFailedButWithinSuccessPercentage(ITestResult arg0) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.ITestListener#onTestFailure(org.testng.ITestResult)
     */
    @Override
    public synchronized void onTestFailure(ITestResult arg0) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.ITestListener#onTestSkipped(org.testng.ITestResult)
     */
    @Override
    public synchronized void onTestSkipped(ITestResult arg0) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.ITestListener#onTestStart(org.testng.ITestResult)
     */
    @Override
    public synchronized void onTestStart(ITestResult arg0) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.ITestListener#onTestSuccess(org.testng.ITestResult)
     */
    @Override
    public synchronized void onTestSuccess(ITestResult arg0) {
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
    public synchronized void onAfterClass(ITestClass arg0) {
        try {
            setCosengContext();
            notifyIntegrators(TriggerOn.CLASSFINISH);
        } catch (CosengException e) {
            throw new RuntimeException(e);
        }
        log.debug(
                "Class AFTER; class [{}], thread [{}], test [{}], testHashCode [{}], parallelMode [{}], isOneWebDriver [{}]",
                arg0.getName(), Thread.currentThread().getId(), test.getName(), test.hashCode(),
                parallelMode, isOneWebDriver);
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
    public synchronized void onBeforeClass(ITestClass arg0) {
        try {
            setCosengContext();
            test.setTestNgClass(arg0);
            notifyIntegrators(TriggerOn.CLASSSTART);
        } catch (CosengException e) {
            throw new RuntimeException(e);
        }
        log.debug(
                "Class BEFORE; class [{}], thread [{}], test [{}], testHashCode [{}], parallelMode [{}], isOneWebDriver [{}]",
                arg0.getName(), Thread.currentThread().getId(), test.getName(), test.hashCode(),
                parallelMode, isOneWebDriver);
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
    public synchronized void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        afterInvocation(method, testResult, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.IInvokedMethodListener#beforeInvocation(org.testng.
     * IInvokedMethod, org.testng.ITestResult)
     */
    @Override
    public synchronized void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        beforeInvocation(method, testResult, null);
    }
    /* </IMethodListener> */

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.IInvokedMethodListener2#afterInvocation(org.testng.
     * IInvokedMethod, org.testng.ITestResult, org.testng.ITestContext)
     */
    /* <IMethodListener2> */
    @Override
    public synchronized void afterInvocation(IInvokedMethod method, ITestResult testResult,
            ITestContext context) {
        try {
            setCosengContext();
            notifyIntegrators(TriggerOn.METHODFINISH);
        } catch (CosengException e) {
            throw new RuntimeException(e);
        }
        log.debug(
                "Method AFTER; class [{}], method [{}], thread [{}], test [{}], testHashCode [{}], parallelMode [{}], isOneWebDriver [{}]",
                method.getTestMethod().getRealClass().getName(),
                method.getTestMethod().getMethodName(), Thread.currentThread().getId(),
                test.getName(), test.hashCode(), parallelMode, isOneWebDriver);
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
     * @see org.testng.IInvokedMethodListener2#beforeInvocation(org.testng.
     * IInvokedMethod, org.testng.ITestResult, org.testng.ITestContext)
     */
    @Override
    public synchronized void beforeInvocation(IInvokedMethod method, ITestResult testResult,
            ITestContext context) {
        try {
            setCosengContext();
            test.setTestNgMethod(method);
            notifyIntegrators(TriggerOn.METHODSTART);
        } catch (CosengException e) {
            throw new RuntimeException(e);
        }
        log.debug(
                "Method BEFORE; class [{}], method [{}], thread [{}], test [{}], testHashCode [{}], parallelMode [{}], isOneWebDriver [{}]",
                method.getTestMethod().getRealClass().getName(),
                method.getTestMethod().getMethodName(), Thread.currentThread().getId(),
                test.getName(), test.hashCode(), parallelMode, isOneWebDriver);
        if (!isOneWebDriver && ParallelMode.METHODS.equals(parallelMode)) {
            try {
                webDriverAction(WebDriverAction.START);
            } catch (CosengException e) {
                test.setIsFailed(true);
                throw new RuntimeException(e);
            }
        }
    }
    /* </IMethodListener2> */

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
     * @see com.sios.stc.coseng.run.CosengRunner#getWebDriver()
     * @see com.sios.stc.coseng.run.CosengRunner#getWebDriverService()
     * @since 2.0
     * @version.coseng
     */
    private synchronized void webDriverAction(WebDriverAction action) throws CosengException {
        Thread thread = Thread.currentThread();
        log.debug("Web driver action [{}], thread [{}]", action, thread.getId());
        if (WebDriverAction.START.equals(action)) {
            startWebDriver(test);
            CosengRunner.incrementStartedWebDriverCount();
            log.debug("Started Web driver [{}], thread [{}]",
                    CosengRunner.getWebDriver(thread).hashCode(), thread.getId());
        } else if (WebDriverAction.STOP.equals(action)) {
            if (CosengRunner.hasWebDriver(thread)) {
                WebDriver webDriver = CosengRunner.getWebDriver(thread);
                Object webDriverService = CosengRunner.getWebDriverService(thread);
                stopWebDriver(webDriver, webDriverService);
                CosengRunner.incrementStoppedWebDriverCount();
                log.debug("Stopped web driver [{}], thread [{}]",
                        CosengRunner.getWebDriver(thread).hashCode(), thread.getId());
            }
        }
    }

    /**
     * Notify integrators
     *
     * @param trigger
     *            the trigger
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    private synchronized void notifyIntegrators(TriggerOn trigger) throws CosengException {
        for (Integrator i : GetIntegrators.wired()) {
            switch (trigger) {
                case EXECUTIONSTART:
                    i.onExecutionStart(test);
                    break;
                case EXECUTIONFINISH:
                    i.onExecutionFinish(test);
                    break;
                case SUITESTART:
                    i.onSuiteStart(test);
                    break;
                case SUITEFINISH:
                    i.onSuiteFinish(test);
                    break;
                case TESTSTART:
                    i.onTestStart(test);
                    break;
                case TESTFINISH:
                    i.onTestFinish(test);
                    break;
                case CLASSSTART:
                    i.onClassStart(test);
                    break;
                case CLASSFINISH:
                    i.onClassFinish(test);
                    break;
                case METHODSTART:
                    if (test.isOneWebDriver()) {
                        /*
                         * For tests with one/single web driver it is necessary
                         * to clear the the test steps, expected and actual test
                         * step results as there is only one instantiation of
                         * the field list. If not cleared the fields accumulate
                         * prior test method execution steps.
                         */
                        i.clearAllTestSteps(test);
                    }
                    i.onMethodStart(test);
                    break;
                case METHODFINISH:
                    i.onMethodFinish(test);
                    break;
                default:
                    // do nothing
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.IReporter#generateReport(java.util.List, java.util.List,
     * java.lang.String)
     */
    @Override
    public synchronized void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
            String outputDirectory) {
        // nothing for now
    }

}
