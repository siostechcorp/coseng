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