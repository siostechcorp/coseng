/*
 * Copyright (c) 2015 SIOS Technology Corp. All rights reserved.
 * This file is part of COSENG (Concurrent Selenium TestNG Runner).
 * 
 * COSENG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * COSENG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with COSENG. If not, see <http://www.gnu.org/licenses/>.
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