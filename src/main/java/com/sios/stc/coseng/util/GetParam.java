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