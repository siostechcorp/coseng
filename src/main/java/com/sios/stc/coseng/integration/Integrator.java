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
package com.sios.stc.coseng.integration;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sios.stc.coseng.RunTests;
import com.sios.stc.coseng.integration.versionone.VersionOne;
import com.sios.stc.coseng.run.CosengException;
import com.sios.stc.coseng.run.Test;

/**
 * The Class Integrator.
 *
 * @since 3.0
 * @version.coseng
 */
public abstract class Integrator {

    private static final Logger log = LogManager.getLogger(RunTests.class.getName());

    /**
     * The Enum TriggerOn.
     *
     * @since 3.0
     * @version.coseng
     */
    public static enum TriggerOn {
        UNKNOWN, EXECUTIONSTART, EXECUTIONFINISH, SUITESTART, SUITEFINISH, TESTSTART, TESTFINISH,
        CLASSSTART, CLASSFINISH, METHODSTART, METHODFINISH, METHODPASS, METHODFAIL;
    }

    /**
     * Attach integrator data.
     *
     * @param test
     *            the test
     * @param data
     *            the data
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    protected synchronized void attachIntegratorData(Test test, Data data) throws CosengException {
        if (test != null && data != null) {
            if (!test.addIntegratorData(data)) {
                if (test.hasIntegratorData(VersionOne.class)) {
                    log.warn("Test already has an attached data object");
                } else {
                    throw new CosengException("Unable to attach integrator data to test");
                }
            }
        }
    }

    /**
     * Attach reports.
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    abstract public void attachReports(Test test, File reportDirectory, File resourceDirectory)
            throws CosengException;

    /**
     * On execution start. NOTE! Sychronize overridden instances!
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    abstract public void onExecutionStart(Test test) throws CosengException;

    /**
     * On execution finish. NOTE! Sychronize overridden instances!
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    abstract public void onExecutionFinish(Test test) throws CosengException;

    /**
     * On suite start. NOTE! Sychronize overridden instances!
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    abstract public void onSuiteStart(Test test) throws CosengException;

    /**
     * On suite finish. NOTE! Sychronize overridden instances!
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    abstract public void onSuiteFinish(Test test) throws CosengException;

    /**
     * On test start. NOTE! Sychronize overridden instances!
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    abstract public void onTestStart(Test test) throws CosengException;

    /**
     * On test finish. NOTE! Sychronize overridden instances!
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    abstract public void onTestFinish(Test test) throws CosengException;

    /**
     * On class start. NOTE! Sychronize overridden instances!
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    abstract public void onClassStart(Test test) throws CosengException;

    /**
     * On class finish. NOTE! Sychronize overridden instances!
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    abstract public void onClassFinish(Test test) throws CosengException;

    /**
     * On method start. NOTE! Sychronize overridden instances!
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    abstract public void onMethodStart(Test test) throws CosengException;

    /**
     * On method finish. NOTE! Sychronize overridden instances!
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    abstract public void onMethodFinish(Test test) throws CosengException;

    /**
     * Adds the test step.
     *
     * @param test
     *            the test
     * @param stepMessage
     *            the step message
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    abstract public void addTestStep(Test test, String stepMessage) throws CosengException;

    /**
     * Adds the test step expected result.
     *
     * @param test
     *            the test
     * @param stepMessage
     *            the step message
     * @param expetedResult
     *            the expeted result
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    abstract public void addTestStepExpectedResult(Test test, String stepMessage,
            String expetedResult) throws CosengException;

    /**
     * Adds the test step actual result.
     *
     * @param test
     *            the test
     * @param stepMessage
     *            the step message
     * @param actualResult
     *            the actual result
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    abstract public void addTestStepActualResult(Test test, String stepMessage, String actualResult)
            throws CosengException;

    /**
     * Clear all test steps.
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    abstract public void clearAllTestSteps(Test test) throws CosengException;

}
