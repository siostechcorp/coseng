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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.testng.TestNG;

import com.sios.stc.coseng.util.Common;
import com.sios.stc.coseng.util.TestParam;

public class Concurrent implements Runnable {

    private TestParam param;
    private static final Logger log = Logger.getLogger(Concurrent.class
            .getName());

    Concurrent(final String testName) {
        for (final TestParam p : Common.sharedRunParam.getParam()) {
            if (p.getTestName().equals(testName)) {
                param = p;
                break;
            }
        }
    }

    @Override
    public void run() {
        // Get *this* threadId and associate it with the testName to assure
        // that subsequent TestNG suites will be referencing the *same*
        // testName.
        param.setThreadId(Thread.currentThread().getId());
        final TestNG tng = new TestNG();
        tng.setXmlSuites(param.getXmlSuite());
        tng.setVerbose(param.getVerbosity());
        tng.setOutputDirectory(param.getTestReportDirectoryPath());
        Concurrent.log.log(Level.INFO,
                "Starting SELENIUM TestNG Run; " + param.toString());
        tng.run();
        if (tng.hasFailure()) {
            Concurrent.log.log(Level.SEVERE,
                    "testName (" + param.getTestName()
                    + ") had failures!");
            Common.isTestFailure = true;
        }
    }

}