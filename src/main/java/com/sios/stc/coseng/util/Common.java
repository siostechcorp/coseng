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