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
import org.testng.TestNG;

/**
 * The Class Concurrent creates a runnable instance for a given test.
 *
 * @see Test
 * @since 2.0
 * @version.coseng
 */
class Concurrent extends CosengRunner implements Runnable {
    private static final Logger log = LogManager.getLogger(Coseng.class.getName());
    private Test                test;

    /**
     * Instantiates a new concurrent runnable for a given test.
     *
     * @param test
     *            the test to run; may not be null
     * @throws CosengException
     *             the coseng exception
     * @see Test
     * @since 2.0
     * @version.coseng
     */
    public Concurrent(Test test) throws CosengException {
        if (test == null) {
            throw new CosengException("No test provided");
        }
        this.test = test;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        String name = test.getName();
        log.debug("Test [{}]", name);
        /* Create the test runner */
        try {
            createRunner(test);
        } catch (CosengException e) {
            test.setIsFailed(true);
            log.fatal(Message.details(name, "Unable to create test runner"), e);
            return;
        }
        /* Construct the TestNG instance */
        TestNG testNg = new TestNG();
        testNg.setXmlSuites(test.getXmlSuites());
        testNg.setVerbose(test.getVerbosity());
        testNg.setOutputDirectory(test.getReportDirectory());
        /* Start web driver if isOneWebDriver */
        try {
            _beforeOneWebDriver();
        } catch (CosengException e) {
            test.setIsFailed(true);
            log.fatal(Message.details(name, "Unable to start webdriver"), e);
            return;
        }
        /* Run the TestNG test */
        testNg.run();
        /* Test completed; mark test if failure */
        if (testNg.hasFailure()) {
            test.setIsFailed(true);
        }
        /* Stop web driver if isOnceWebDriver */
        try {
            _afterOneWebDriver();
        } catch (CosengException e) {
            log.warn(
                    Message.details(name,
                            "Unable to stop webdriver; may need to kill orphan webdriver processes"),
                    e);
        }
    }
}
