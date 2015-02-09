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
package com.sios.stc.coseng.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

public class GetParam {

    private static final Logger log = Logger.getLogger(GetParam.class
            .getName());

    public Run runParam;
    public static final int NUM_REQUIRED_PARAMS = 1;

    public GetParam(final String[] args) throws Exception {

        // Only parameter provided should be the JSON configuration file

        if ((args == null) || (args.length > GetParam.NUM_REQUIRED_PARAMS)) {
            throw new Exception("(" + GetParam.NUM_REQUIRED_PARAMS
                    + ") parameters required. Got (" + args.length + ")");
        }

        final File jsonConfig = new File(args[0]);
        if (!jsonConfig.exists() || !jsonConfig.canRead()) {
            GetParam.log.log(Level.SEVERE,
                    "JSON Config file (" + jsonConfig.getCanonicalPath()
                    + ") doesn't exist or can't be read");
        }

        // Read the JSON configuration file
        final Gson gson = new Gson();

        final Reader jsonReader = new FileReader(jsonConfig);

        final ParamCollection param = gson.fromJson(jsonReader,
                ParamCollection.class);

        // Initialize the localParam
        param.localParam.initialize();

        // Validate the params
        Boolean isAllValid = true;
        for (final TestParam p : param.testParam) {
            if (!p.isValid(param.localParam)) {
                isAllValid = false;
            }
        }

        if (!isAllValid) {
            GetParam.log
            .log(Level.SEVERE,
                    "JSON Config file ("
                            + jsonConfig.getCanonicalPath()
                            + ") has a configuration problem, check logging details; ALL testParam and localParam must be valid before the tests will run");
            // Clean-up the testReportDirectories that aren't being used now
            GetParam.removeTestReportDirectory();
            // Logs have parameter problem details
            System.exit(Common.EXIT_FAILURE);
        }

        // Collect test run params with other run parameters
        // for sharing with the concurrent test runners.
        runParam = new Run(param);
        Common.sharedRunParam = runParam;
    }

    // testReportDirectory clean-up
    public static void removeTestReportDirectory() throws IOException {
        for (final File f : Common.testReportDirectoryCollector) {
            if (!f.delete()) {
                GetParam.log.log(
                        Level.WARNING,
                        "Unable to remove testReportDirectory ("
                                + f.getCanonicalPath()
                                + "); assure the directory is empty");
            }
        }
    }

}