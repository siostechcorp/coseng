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
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;


public class Common {
    // Browser enum most likely to change for future browser versions...
    public enum Browser {
        FIREFOX("any"), CHROME("any"), IE("any"), IE9("9.0"), IE10("10.0"), IE11(
                "11.0");
        private final String version;

        private Browser(final String version) {
            this.version = version;
        }

        public String getVersion() {
            return version;
        }
    }

    public final static String JAR_PATH_TESTNG_RESOURCES_SUITE = "suite";
    // 'suite' xml files located in src/main/resources/suite
    public final static String CLASS_PATH_TESTNG_RESOURCES_SUITE = "src"
            + File.separator + "main" + File.separator + "resources"
            + File.separator
            + Common.JAR_PATH_TESTNG_RESOURCES_SUITE;

    public final static int EXIT_SUCCESS = 0;
    public final static int EXIT_FAILURE = 1;

    public final static String BROWSER_NAME_INTERNET_EXPLORER = "internet explorer";
    public final static String BROWSER_CAPABILITY_FIREFOX_PROFILE = "firefox_profile";

    // Whether to execute TestNG locally or via Selenium Grid server
    public enum Spot {
        LOCAL, GRID
    };

    // !!IMPORTANT!! this is necessary for the 'test class' to get the runner
    // parameters. The JUnit test class only allows zero parameter constructors.
    // Each test will popTest the sharedRunParams; run and halt when no more
    // testClass instances of itself.
    public static Run sharedRunParam;

    public static Boolean isTestFailure = false;

    // To count TestParam testName counts in outputDir
    public static int testNameCount = 0;

    // Collector for tracking used driver|service for cleanup after test runs.
    // instanceClassHashInt -> instanceWebDriver
    public static List<WebDriver> driverCollector = new ArrayList<WebDriver>();
    public static List<Object> serviceCollector = new ArrayList<Object>();
    public static List<File> testReportDirectoryCollector = new ArrayList<File>();

}