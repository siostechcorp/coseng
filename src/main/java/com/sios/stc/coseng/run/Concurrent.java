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
import org.testng.ITestNGListener;
import org.testng.TestNG;

import com.sios.stc.coseng.RunTests;

/**
 * The Class Concurrent creates a runnable instance for a given COSENG test.
 *
 * @since 2.0
 * @version.coseng
 */
class Concurrent implements Runnable {

    private static final Logger log = LogManager.getLogger(RunTests.class.getName());
    private Test                test;

    /**
     * Instantiates a new concurrent runnable for a given COSENG test.
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
        Thread thread = Thread.currentThread();
        String name = test.getName();
        /* Seed runner */
        CosengRunner.setThreadTest(thread, test);
        log.debug("Test [{}] thread [{}] {}]", name, thread.getId(), thread.getName());
        TestNG testNg = new TestNG();
        try {
            testNg.setXmlSuites(test.getXmlSuites());
            testNg.setVerbose(test.getVerbosity());
            testNg.setOutputDirectory(test.getReportDirectory());
            /* Add the all important CosengListener */
            List<Class<? extends ITestNGListener>> listeners =
                    new ArrayList<Class<? extends ITestNGListener>>();
            listeners.add(com.sios.stc.coseng.run.CosengListener.class);
            testNg.setListenerClasses(listeners);
            /* Run the TestNG test */
            testNg.run();
            /* Test completed; mark test if failure */
            if (testNg.hasFailure()) {
                test.setIsFailed(true);
            }
        } catch (Exception e) {
            test.setIsFailed(true);
            throw new RuntimeException("Unable to execute TestNG run for test [" + name + "]", e);
        }
    }

}
