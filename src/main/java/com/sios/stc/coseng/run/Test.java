/*
 * This file is part of COSENG (Concurrent Selenium TestNG Runner).
 * 
 * Copyright (c) 2015 SIOS Technology Corp. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sios.stc.coseng.run;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sios.stc.coseng.util.Common;
import com.sios.stc.coseng.util.GetParam;

public class Test {

    private static final int TIMEOUT_MINUTES = 5;
    private static final Logger log = Logger.getLogger(Test.class.getName());

    public static void main(final String[] args) throws Exception {

        // Parse the command line arguments
        final GetParam gp = new GetParam(args);

        // For each testName; aka minion fire off a test thread
        // NOTE! The suite/test may *also* be executing concurrent threads
        final ExecutorService executor = Executors.newCachedThreadPool();
        for (final String testName : gp.runParam.getTestName()) {
            final Runnable worker = new Concurrent(testName);
            executor.execute(worker);
        }
        executor.shutdown();
        executor.awaitTermination(Test.TIMEOUT_MINUTES, TimeUnit.MINUTES);
        // wrap up thread.

        // Finally, stop any started services ...
        gp.runParam.stopService();
        // ... or lingering drivers (of which there shouldn't be any if the
        // @AfterSuite quit the assigned driver correctly.
        gp.runParam.quitDriver();

        if (Common.isTestFailure) {
            Test.log.log(Level.SEVERE, "SELENIUM TESTS Completed with errors");
        } else {
            Test.log.log(Level.INFO, "SELENIUM TESTS Completed successfully");
        }

        Test.log.log(Level.INFO,
                "SELENIUM TestNG REPORTS @ "
                        + gp.runParam.getTestReportDirectory());

        // !IMPORTANT! exit non-zero if there was an unsuccessful test case.
        if (Common.isTestFailure) {
            System.exit(Common.EXIT_FAILURE);
        } else {
            System.exit(Common.EXIT_SUCCESS);
        }

    }

}