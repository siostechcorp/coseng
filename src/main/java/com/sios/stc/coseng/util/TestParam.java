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
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.Platform;
import org.testng.xml.XmlSuite;

import com.sios.stc.coseng.util.Common.Browser;
import com.sios.stc.coseng.util.Common.Spot;

public class TestParam {
    private final String testName = null;
    private Integer verbosity = null;
    private Spot spot = null;
    private final Platform platform = null;
    private final Browser browser = null;
    private final List<String> suite = new ArrayList<String>();
    private final String baseUrl = null;
    private final String gridUrl = null;

    private Boolean isValid = null;
    private File testReportDirectory = null;

    private long threadId = 0L;

    private static final Logger log = Logger.getLogger(TestParam.class.getName());

    TestParam() {
        // do nothing
    }

    public synchronized Boolean isValid(final LocalParam localParam)
            throws IOException {
        Boolean isValid = true;
        // Warning items that don't necessarily invalidate the config
        if ((baseUrl == null) || baseUrl.isEmpty()) {
            TestParam.log.log(Level.WARNING, logDetails("A baseUrl was not provided; some tests may fail"));
        }
        if (verbosity == null) {
            TestParam.log
            .log(Level.INFO,
                    logDetails("A verbosity was not provided; assuming level (0)"));
            verbosity = 0;
        }
        if (spot == null) {
            TestParam.log.log(Level.INFO,
                    logDetails("A spot was not provided; assuming LOCAL"));
            spot = Spot.LOCAL;
        }
        // True|False validation parameters
        if (platform == null) {
            TestParam.log.log(Level.SEVERE, logDetails("No platform was provided; a platform of ("
                    + getPlatformOptions() + ") is REQUIRED"));
            isValid = false;
        }
        if (browser == null) {
            TestParam.log.log(Level.SEVERE, logDetails("No browser was provided; a browser of ("
                    + getBrowserOptions() + ") is REQUIRED"));
            isValid = false;
        }
        // Special; chromeDriver|ieDriver when Spot.LOCAL and !Browser.FIREFOX
        if ((browser != null) && (spot == Spot.LOCAL)) {
            if (browser == Browser.CHROME) {
                // If can't find chromeDriver; bail
                final File chromeDriver = localParam.getChromeDriver();
                if ((chromeDriver == null) || !chromeDriver.exists()
                        || !chromeDriver.canExecute()) {
                    TestParam.log
                    .log(Level.SEVERE, logDetails("Testing with browser ("
                            + browser.toString()
                            + ") at spot ("
                            + spot.toString()
                            + ") REQUIRES chromeDriver. Could not find executable chromeDriver ("
                            + chromeDriver + ")"));
                    isValid = false;
                }
            } else if (browser != Browser.FIREFOX) {
                // Must be IE*
                final File ieDriver = localParam.getIeDriver();
                // If can't find ieDriver; bail
                if ((ieDriver == null) || !ieDriver.exists()
                        || !ieDriver.canExecute()) {
                    TestParam.log
                    .log(Level.SEVERE, logDetails("Testing with browser ("
                            + browser.toString()
                            + ") at spot ("
                            + spot.toString()
                            + ") REQUIRES ieDriver. Could not find executable ieDriver ("
                            + ieDriver + ")"));
                    isValid = false;
                }
            }
        }
        // If !windows but browser is IE*; bail
        if ((browser != null) && (platform != null)) {
            if (platform != Platform.WINDOWS) {
                if ((browser != Browser.FIREFOX) && (browser != Browser.CHROME)) {
                    TestParam.log
                    .log(Level.SEVERE, logDetails("Internet Explorer IE*, any version; REQUIRES platform WINDOWS"));
                    isValid = false;
                }
            }
        }
        if ((suite == null) || suite.isEmpty()) {
            TestParam.log.log(Level.SEVERE, logDetails("No suite provided; at least one suite XML REQUIRED"));
            isValid = false;
        } else {
            // jarSuiteTempDirectory is checked in findTestSuite
            if (!findTestSuite(localParam)) {
                TestParam.log.log(Level.SEVERE, logDetails("Could not find ALL suite ("
                        + suite.toString() + ")"));
                isValid = false;
            }
        }
        if ((spot == Spot.GRID) && ((gridUrl == null) || gridUrl.isEmpty())) {
            TestParam.log.log(Level.SEVERE, logDetails("A gridUrl was not provided; spot ("
                    + spot.toString() + ") REQUIRES gridUrl"));
            isValid = false;
        }
        if (!setTestReportDirectory(localParam)) {
            TestParam.log.log(Level.SEVERE,
                    logDetails("Could not create testReportDirectory ("
                            + testReportDirectory.getCanonicalPath() + ")"));
            isValid = false;
        }
        if ((verbosity < 0) || (verbosity > 10)) {
            TestParam.log.log(Level.SEVERE, logDetails("Invalid verbosity; must be 0-10"));
            isValid = false;
        }
        setIsValid(isValid);
        return isValid;
    }

    public synchronized void setThreadId(final long threadId) {
        this.threadId = threadId;
    }

    public synchronized long getThreadId() {
        return threadId;
    }

    public synchronized String getTestName() {
        return testName;
    }

    public synchronized Spot getSpot() {
        return spot;
    }

    public synchronized Platform getPlatform() {
        return platform;
    }

    public synchronized Browser getBrowser() {
        return browser;
    }

    public synchronized int getVerbosity() {
        return verbosity;
    }

    public synchronized String getBaseUrl() {
        return baseUrl;
    }

    public synchronized List<String> getSuite() {
        return suite;
    }

    public synchronized String getSeleniumGridUrl() {
        return gridUrl;
    }

    public synchronized File getTestReportDirectory() {
        return testReportDirectory;
    }

    public synchronized String getTestReportDirectoryPath() {
        try {
            return testReportDirectory.getCanonicalPath();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    public synchronized List<XmlSuite> getXmlSuite() {
        final List<XmlSuite> xmlSuiteList = new ArrayList<XmlSuite>();
        final XmlSuite xmlSuite = new XmlSuite();
        xmlSuite.setSuiteFiles(suite);
        xmlSuite.setName(testName);
        xmlSuiteList.add(xmlSuite);
        return xmlSuiteList;
    }

    public synchronized Boolean isSetValid() {
        return isValid;
    }

    private synchronized void setIsValid(final Boolean isValid) {
        this.isValid = isValid;
    }

    private synchronized Boolean findTestSuite(final LocalParam localParam) throws IOException {
        // Copy src/main/resources/suites/* to /tmp/ (*only if running from jar)
        // Needed since TestNG uses List<String> to load test suites.
        // Allows running test from Eclipse without having a copy in /tmp.
        Boolean isValid = true;
        int index = 0;
        for (String s : suite) {
            // See if suite file listed in config exists
            File file = new File(s);
            if (file.exists() && file.canRead()) {
                s = file.getCanonicalPath();
            } else {
                // If found src/main/resources/suites (likely running in IDE
                // not from jar)
                file = new File(
                        Common.CLASS_PATH_TESTNG_RESOURCES_SUITE
                        + File.separator
                        + s);
                if (file.exists() && file.canRead()) {
                    s = file.getCanonicalPath();
                } else {
                    // Look in class path (likely executed from *.jar) and
                    // have to copy them to an external location to be
                    // referenced. DON'T USE File.Separator; use "/" since
                    // on windows it is "\" and won't find the resource in the
                    // *.jar
                    final InputStream suiteStream = getClass()
                            .getClassLoader().getResourceAsStream(
                                    Common.JAR_PATH_TESTNG_RESOURCES_SUITE
                                    + "/" + s);
                    if (suiteStream != null) {
                        final File jarSuitePath = localParam.getJarSuiteDirectory();
                        if (!jarSuitePath.exists()) {
                            if (!jarSuitePath.mkdirs()) {
                                TestParam.log.log(Level.SEVERE,
                                        "Could not create jarSuiteDirectory ("
                                                + jarSuitePath.getCanonicalPath() + ")");
                                isValid = false;
                            }
                        }
                        if (!jarSuitePath.canWrite()) {
                            TestParam.log.log(
                                    Level.SEVERE,
                                    "jarSuiteDirectory ("
                                            + jarSuitePath.getCanonicalPath()
                                            + ") is not writable");
                            isValid = false;
                        } else {
                            final File targetFile = new File(jarSuitePath.getCanonicalPath()
                                    + File.separator + s);
                            // Always overwrite the file (unless overwriteSuite = true)
                            // as the jar may have newer files than on disk.
                            if (!targetFile.exists()
                                    || localParam.isJarSuiteOverwrite()) {
                                FileUtils
                                .copyInputStreamToFile(suiteStream, targetFile);
                            }
                            if (targetFile.exists() && targetFile.canRead()) {
                                // Update the suite path
                                s = targetFile.getCanonicalPath();
                            } else {
                                TestParam.log.log(
                                        Level.SEVERE,
                                        "Could not create ("
                                                + targetFile.getCanonicalPath()
                                                + ")");
                                isValid = false;
                            }
                        }
                    } else {
                        TestParam.log
                        .log(Level.SEVERE, logDetails("Could not find resource (" + s
                                + ") on system or in "
                                + Common.CLASS_PATH_TESTNG_RESOURCES_SUITE
                                + " or "
                                + localParam.getJarSuiteDirectory().getCanonicalPath()));
                        isValid = false;
                    }
                }
            }
            // Update the suite with the found path
            suite.set(index, s);
            index++;
        }
        return isValid;
    }

    private synchronized Boolean setTestReportDirectory(
            final LocalParam localParam) throws IOException {
        Boolean isValid = true;
        String unique;
        Common.testNameCount++;

        // Set date string for unique test outputle
        String dateFormat;
        SimpleDateFormat localDateFormat;
        if (SystemUtils.IS_OS_WINDOWS) {
            dateFormat = "yyyy-MM-dd-HH.mm.ss";
        } else {
            dateFormat = "yyyy-MM-dd-HH:mm:ss";
        }
        localDateFormat = new SimpleDateFormat(dateFormat);
        final String localDateTimeStamp = localDateFormat.format(new Date());

        if ((testName != null) || !testName.isEmpty()) {
            unique = testName;
        } else {
            unique = "TestNumber-" + Common.testNameCount;
        }
        if (spot != null) {
            unique += "-" + spot.toString();
        }
        if (platform != null) {
            unique += "-" + platform.toString();
        }
        if (browser != null) {
            unique += "-" + browser.toString();
        }
        unique += "-" + localDateTimeStamp;
        String reportDir = null;
        if (localParam.getTestReportDirectory() != null) {
            reportDir = localParam.getTestReportDirectory().getCanonicalPath()
                    + File.separator + unique;
        } else {
            reportDir = unique;
        }
        final File outDir = new File(reportDir);
        // Create directory if not exists
        if (!outDir.exists()) {
            if (!outDir.mkdirs()) {
                isValid = false;
            } else {
                testReportDirectory = outDir;
                // Collect the created report directories for clean-up if ANY or
                // ALL
                // config properties are invalid; since a bad config will not
                // execute any tests.
                Common.testReportDirectoryCollector.add(testReportDirectory);
            }
        }
        return isValid;
    }

    private String logDetails(final String logDetail) {
        String log = "testName (" + testName + ")";
        if (logDetail != null) {
            log += " " + logDetail;
        }
        return log;
    }

    private List<String> getPlatformOptions() {
        final List<String> options = new ArrayList<String>();
        for (final Platform p : Platform.values()) {
            options.add(p.name());
        }
        return options;
    }

    private List<String> getBrowserOptions() {
        final List<String> options = new ArrayList<String>();
        for (final Browser b : Browser.values()) {
            options.add(b.name());
        }
        return options;
    }

    @Override
    public synchronized String toString() {
        return "testName (" + testName
                + "), spot ("
                + spot.toString()
                + "), gridUrl ("
                + (spot == Spot.GRID ? gridUrl : "n/a")
                + "), platform ("
                + platform.toString()
                + "), browser ("
                + browser.toString()
                + "), suite ("
                + suite.toString()
                + "), baseUrl ("
                + baseUrl
                + "), verbosity ("
                + verbosity
                + "), testReportDirectory ("
                + testReportDirectory
                + ")";
    }
}
