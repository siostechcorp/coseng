/*
 * Concurrent Selenium TestNG (COSENG)
 * Copyright (c) 2013-2017 SIOS Technology Corp.  All rights reserved.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.openqa.selenium.Platform;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;

import com.sios.stc.coseng.RunTests;
import com.sios.stc.coseng.run.Browsers.Browser;
import com.sios.stc.coseng.run.Locations.Location;
import com.sios.stc.coseng.util.Resource;

/**
 * The Class Validate. Validates the logical combination test parameters.
 *
 * @since 2.0
 * @version.coseng
 */
class Validate {

    private static final Logger log = LogManager.getLogger(RunTests.class.getName());
    private static Node         node;
    private static Tests        tests;
    private static int          testSuiteCount;

    /**
     * Tests. Validates the node and test parameters for logical combinations
     * based on location, platform, browser and other details.
     *
     * @param node
     *            the node
     * @param tests
     *            the tests
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.Node
     * @see com.sios.stc.coseng.run.Tests
     * @see com.sios.stc.coseng.run.Test
     * @see com.sios.stc.coseng.run.Validate#notNull()
     * @see com.sios.stc.coseng.run.Validate#uniqueName()
     * @see com.sios.stc.coseng.run.Validate#createExplicitTests()
     * @see com.sios.stc.coseng.run.Validate#node()
     * @see com.sios.stc.coseng.run.Validate#tests()
     * @since 2.0
     * @version.coseng
     */
    protected static void tests(Node node, Tests tests) throws CosengException {
        if (node != null && tests != null && tests.size() > 0) {
            Validate.node = node;
            Validate.tests = tests;
            notNull();
            /*
             * Some tests will be identified as ANY/ALL. This implies multiple
             * COSENG tests against all supported Platform and Browsers. Create
             * explicit combinations for supported Platform and Browsers.
             */
            createExplicitTests();
            uniqueName();
            /*
             * Validate node *first*; tests depends on their valid tests.
             */
            node();
            tests();
            /* Set the max execution time */
            tests.setMaxTestExecutionMinutes(node.getMaxTestExecutionMinutes());
        } else {
            throw new CosengException("Node null or 0 tests; nothing to do");
        }
    }

    /**
     * Not null; that there are no null test.
     *
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.Validate#tests(Node, Tests)
     * @since 2.0
     * @version.coseng
     */
    private static void notNull() throws CosengException {
        for (Test test : tests.getAll()) {
            if (test == null) {
                throw new CosengException("Test was null");
            }
        }
    }

    /**
     * Unique name; that all test have unique names.
     *
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.Validate#tests(Node, Tests)
     * @since 2.0
     * @version.coseng
     */
    private static void uniqueName() throws CosengException {
        Set<String> names = new HashSet<String>();
        for (Test test : tests.getAll()) {
            String name = test.getName();
            if (name == null || name.isEmpty()) {
                throw new CosengException("Test name is undefined or empty");
            }
            if (names.contains(name)) {
                throw new CosengException("Test name [" + name
                        + "] is not unique; another test of the same name exists");
            } else {
                names.add(name);
            }
        }
    }

    /**
     * Creates the explicit tests. Some tests will be identified as ANY/ALL.
     * This implies multiple COSENG tests against all supported platform and
     * browsers. Create explicit combinations for supported platform and
     * browsers.
     *
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.Validate#tests(Node, Tests)
     * @see com.sios.stc.coseng.run.Validate#newTests(Test, Platform)
     * @since 2.0
     * @version.coseng
     */
    private static void createExplicitTests() throws CosengException {
        try {
            List<Test> explicitTests = new ArrayList<Test>();
            List<Test> implicitTests = new ArrayList<Test>();
            List<Test> newTests;
            for (Test test : tests.getAll()) {
                Location location = test.getLocation();
                Platform platform = test.getPlatform();
                if (Platform.ANY.equals(platform) && Location.NODE.equals(location)) {
                    /* For location NODE can only support osPlatform */
                    platform = OperatingSystem.getPlatform();
                }
                newTests = newTests(test, platform);
                if (!newTests.isEmpty()) {
                    implicitTests.add(test);
                    explicitTests.addAll(newTests);
                }
            }
            tests.removeAll(implicitTests);
            tests.addAll(explicitTests);
        } catch (Exception e) {
            throw new CosengException("Error creating explicit tests", e);
        }

    }

    /**
     * New tests based on the original and the supported platform.
     *
     * @param original
     *            the original
     * @param platform
     *            the platform
     * @return the list
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.Validate#createExplicitTests()
     * @see com.sios.stc.coseng.run.Validate#newTest(Test, Platform, Browser)
     * @since 2.0
     * @version.coseng
     */
    private static List<Test> newTests(Test original, Platform platform) throws CosengException {
        try {
            List<Test> tests = new ArrayList<Test>();
            Location location = original.getLocation();
            Browser browser = original.getBrowser();
            if (Browser.ALL.equals(browser)) {
                for (Browser supportedBrowser : Platforms.getSupportedBrowsers(location,
                        platform)) {
                    if (Platforms.isSupportedBrowser(location, platform, supportedBrowser)) {
                        Test test = newTest(original, platform, supportedBrowser);
                        tests.add(test);
                    }
                }
            } else {
                if (Platforms.isSupportedBrowser(location, platform, browser)) {
                    Test test = newTest(original, platform, browser);
                    tests.add(test);
                }
            }
            return tests;
        } catch (Exception e) {
            throw new CosengException("Error creating new tests", e);
        }
    }

    /**
     * New test based on the original, platform and supported browser.
     *
     * @param original
     *            the original
     * @param platform
     *            the platform
     * @param browser
     *            the browser
     * @return the test
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.Validate#newTests(Test, Platform)
     * @since 2.0
     * @version.coseng
     */
    private static Test newTest(Test original, Platform platform, Browser browser)
            throws CosengException {
        String separator = "-";
        String name = original.getName();
        ArrayList<String> newNames = new ArrayList<String>();
        newNames.add(name);
        newNames.add(original.getLocation().toString());
        newNames.add(platform.toString());
        newNames.add(browser.toString());
        String newName = StringUtils.join(newNames, separator);
        Test test = original.deepCopy();
        test.setName(newName.toLowerCase());
        test.setPlatform(platform);
        test.setBrowser(browser);
        test.setIsSynthetic(true);
        return test;
    }

    /**
     * Node. Validates the reports and resources directories.
     *
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.Validate#tests(Node, Tests)
     * @see com.sios.stc.coseng.run.Validate#directory(File)
     * @since 2.0
     * @version.coseng
     */
    private static void node() throws CosengException {
        if (node.getReportsDirectory() == null || node.getResourcesTempDirectory() == null) {
            throw new CosengException(
                    "Node reportsDirectory and/or resourcesTempDirectory is null");
        } else {
            File reportsDirectory = node.getReportsDirectory();
            File resourcesTempDirectory = node.getResourcesTempDirectory();
            directory(reportsDirectory);
            directory(resourcesTempDirectory);
            int maxTestExecutionMinutes = node.getMaxTestExecutionMinutes();
            if (maxTestExecutionMinutes <= 0) {
                throw new CosengException("Node maxTestExecutionMinutes invalid; must be > 0");
            }
        }
    }

    /**
     * Directory. Create directory if absent; validate that resource is a
     * directory.
     *
     * @param directory
     *            the directory
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.Validate#node()
     * @see com.sios.stc.coseng.run.Validate#resourceDirectory(Test)
     * @since 2.0
     * @version.coseng
     */
    private static void directory(File directory) throws CosengException {
        try {
            if (!directory.getName().isEmpty() && !directory.exists()) {
                // Only make dirs if not current working directory "" and not
                // already exists
                FileUtils.forceMkdir(directory);
            } else if (!(directory.getCanonicalFile()).isDirectory()) {
                throw new CosengException("Existing [" + directory + "] not a directory");
            }
        } catch (NullPointerException e1) {
            throw new CosengException("Argument directory is null", e1);
        } catch (IOException e2) {
            throw new CosengException(
                    "Directory [" + directory + "] could not be accessed or created", e2);
        }
    }

    /**
     * Tests. Validate each test's parameters.
     *
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.Validate#platform(Test)
     * @see com.sios.stc.coseng.run.Validate#browser(Test)
     * @see com.sios.stc.coseng.run.Validate#webDriver(Test)
     * @see com.sios.stc.coseng.run.Validate#reportDirectory(Test)
     * @see com.sios.stc.coseng.run.Validate#resourceDirectory(Test)
     * @see com.sios.stc.coseng.run.Validate#suites(Test)
     * @see com.sios.stc.coseng.run.Validate#gridUrl(Test)
     * @see com.sios.stc.coseng.run.Validate#verbosity(Test)
     * @see com.sios.stc.coseng.run.Validate#webDriverTimeout(Test)
     * @see com.sios.stc.coseng.run.Validate#webDriverWaitTimeout(Test)
     * @see com.sios.stc.coseng.run.Validate#warnBaseUrlUndefined(Test)
     * @see com.sios.stc.coseng.run.Validate#warnBrowserRequestVersionForNode(Test)
     * @since 2.0
     * @version.coseng
     */
    private static void tests() throws CosengException {
        for (Test test : tests.getAll()) {
            platform(test);
            browser(test);
            webDriver(test);
            reportDirectory(test);
            resourceDirectory(test);
            suites(test);
            gridUrl(test);
            browserDimension(test);
            verbosity(test);
            webDriverTimeout(test);
            webDriverWaitTimeout(test);
            warnBaseUrlUndefined(test);
            warnBrowserRequestVersionForNode(test);
        }
    }

    /**
     * Platform.
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.Validate#tests()
     * @since 2.0
     * @version.coseng
     */
    private static void platform(Test test) throws CosengException {
        String name = test.getName();
        Platform platform = test.getPlatform();
        Location location = test.getLocation();
        if (platform == null) {
            throw new CosengException(
                    Message.details(name, "no platform was provided; REQUIRED platform "
                            + Platforms.getSupported(location)));
        }
        if (Location.NODE.equals(location)) {
            Platform osPlatform = OperatingSystem.getPlatform();
            if (!platform.equals(osPlatform)) {
                throw new CosengException(Message.details(name,
                        "the platform [" + platform.toString()
                                + "] does not match operating system [" + osPlatform
                                + "]; must match where location [" + location + "]"));
            }
            if (Platform.ANY.equals(platform)) {
                throw new CosengException(Message.details(name, "platform may not be [" + platform
                        + "] for location [" + location + "]; failed creating synthetic tests"));
            }
        }
    }

    /**
     * Browser.
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.Validate#tests()
     * @since 2.0
     * @version.coseng
     */
    private static void browser(Test test) throws CosengException {
        String name = test.getName();
        Location location = test.getLocation();
        Platform platform = test.getPlatform();
        Browser browser = test.getBrowser();
        if (browser == null) {
            throw new CosengException(Message.details(name, "no browser provided; REQUIRED browser "
                    + Platforms.getSupportedBrowsers(location, platform)));
        }
        if (!Platforms.isSupportedBrowser(location, platform, browser)) {
            throw new CosengException(Message.details(name,
                    "browser [" + browser + "] not supported; REQUIRED browser "
                            + Platforms.getSupportedBrowsers(location, platform)));
        }
    }

    /**
     * Web driver.
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.Validate#tests()
     * @see com.sios.stc.coseng.run.Validate#isExecutable(File)
     * @since 2.0
     * @version.coseng
     */
    private static void webDriver(Test test) throws CosengException {
        String name = test.getName();
        Platform platform = test.getPlatform();
        Location location = test.getLocation();
        Browser browser = test.getBrowser();
        File webDriver = null;
        if (Platforms.isSupportedBrowser(location, platform, browser)) {
            if (Location.NODE.equals(location)) {
                if (Browser.CHROME.equals(browser)) {
                    webDriver = node.getChromeDriver();
                } else if (Browser.FIREFOX.equals(browser)) {
                    webDriver = node.getGeckoDriver();
                } else if (Browser.EDGE.equals(browser)) {
                    webDriver = node.getEdgeDriver();
                } else if (Browser.IE.equals(browser)) {
                    webDriver = node.getIeDriver();
                }
                if (webDriver == null) {
                    throw new CosengException(
                            Message.details(name, "no webDriver available for browser [" + browser
                                    + "] and platform [" + platform + "]"));
                }
                if (!isExecutable(webDriver)) {
                    throw new CosengException(Message.details(name, "webDriver [" + webDriver
                            + "] absent or not executable for browser [" + browser + "]"));
                }
                test.setWebDriver(webDriver);
            }
        } else {
            throw new CosengException(Message.details(name,
                    "Browser [" + browser + "] not supported for the location [" + location
                            + "] and platform [" + platform + "]"));
        }
    }

    /**
     * Checks if is executable.
     *
     * @param file
     *            the file
     * @return the boolean
     * @see com.sios.stc.coseng.run.Validate#webDriver(Test)
     * @since 2.0
     * @version.coseng
     */
    private static Boolean isExecutable(File file) {
        if (file != null && file.exists() && file.canExecute()) {
            return true;
        }
        return false;
    }

    /**
     * Report directory.
     *
     * @param test
     *            the test
     * @see com.sios.stc.coseng.run.Validate#tests()
     * @since 2.0
     * @version.coseng
     */
    private static void reportDirectory(Test test) {
        File reportsDirectory = node.getReportsDirectory();
        String name = test.getName();
        test.setReportDirectory(reportsDirectory + File.separator + name);
    }

    /**
     * Resource directory.
     *
     * @param test
     *            the test
     * @throws CosengException
     * @see com.sios.stc.coseng.run.Validate#tests()
     * @since 2.0
     * @version.coseng
     */
    private static void resourceDirectory(Test test) throws CosengException {
        File nodeResourceDirectory = node.getResourcesTempDirectory();
        String name = test.getName();
        File testResourceDirectory = new File(nodeResourceDirectory + File.separator + name);
        directory(testResourceDirectory);
        test.setResourceDirectory(testResourceDirectory);
    }

    /**
     * Suites. Modify for uniqueness and validity. Create TestNG XmlSuite list
     * and attach to the COSENG test.
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.Validate#tests()
     * @see com.sios.stc.coseng.run.Validate#modifySuiteXml(String, File, List,
     *      boolean)
     * @since 2.0
     * @version.coseng
     */
    private static void suites(Test test) throws CosengException {
        String name = test.getName();
        List<String> suites = test.getSuites();
        if (suites == null || suites.isEmpty()) {
            throw new CosengException(
                    Message.details(name, "no suites provided; at least one suite XML REQUIRED"));
        } else {
            /* Reset counter for next test */
            testSuiteCount = 0;
            List<XmlSuite> xmlSuites = new ArrayList<XmlSuite>();
            XmlSuite xmlSuite = new XmlSuite();
            xmlSuite.setSuiteFiles(modifySuiteXml(name, test.getResourceDirectory(), suites,
                    test.isOneWebDriver()));
            xmlSuite.setName(name);
            xmlSuites.add(xmlSuite);
            test.setXmlSuites(xmlSuites);
            test.setTestSuiteCount(testSuiteCount);
        }
    }

    /**
     * Modify suite xml. Convenience method for a single suite. Calls
     * modifySuiteXml with a list with one element.
     *
     * @param name
     *            the name
     * @param resourceDirectory
     *            the resource directory
     * @param suite
     *            the suite
     * @param isOneWebDriver
     *            the is one web driver
     * @return the list
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.Validate#suites(Test)
     * @see com.sios.stc.coseng.run.Validate#modifySuiteXml(String, File,
     *      String, boolean)
     * @since 2.0
     * @version.coseng
     */
    private static List<String> modifySuiteXml(String name, File resourceDirectory, String suite,
            boolean isOneWebDriver) throws CosengException {
        List<String> suites = new ArrayList<String>();
        suites.add(suite);
        return modifySuiteXml(name, resourceDirectory, suites, isOneWebDriver);
    }

    /**
     * Modify suite xml. Deserialize XML and modify some elements to provide
     * unique report names and separation of tests based on multiple browsers
     * from the same test suites. Validate that all have parallel="false" if
     * isOneWebDriver 'true'. Does not support parallel="instances". Supports
     * suite xml that are composed with suite-files.
     *
     * @param name
     *            the name
     * @param resourceDirectory
     *            the resource directory
     * @param suites
     *            the suites
     * @param isOneWebDriver
     *            the is one web driver
     * @return the list
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.Validate#modifySuiteXml(String, File, List,
     *      boolean)
     * @since 2.0
     * @version.coseng
     */
    private static List<String> modifySuiteXml(String name, File resourceDirectory,
            List<String> suites, boolean isOneWebDriver) throws CosengException {
        List<String> modifiedSuites = new ArrayList<String>();
        String attrParallel = "parallel";
        String attrSuiteFiles = "suite-files";
        String attrSuiteFile = "suite-file";
        String attrTest = "test";
        String attrPath = "path";
        String parallelNone = "none";
        String parallelFalse = "false";
        for (String suite : suites) {
            InputStream suiteInput = Resource.getStream(suite);
            /* Read original suite */
            SAXBuilder jdomBuilder = new SAXBuilder();
            Document jdomSuite = null;
            try {
                jdomSuite = jdomBuilder.build(suiteInput);
                suiteInput.close();
            } catch (JDOMException | IOException e) {
                throw new CosengException(
                        Message.details(name, "deserialize suite [" + suite + "] failed"), e);
            }
            /* Check parallel mode */
            Element suiteRoot = jdomSuite.getRootElement();
            Attribute parallelMode = suiteRoot.getAttribute(attrParallel);
            if (parallelMode == null) {
                throw new CosengException(
                        Message.details(name, "suite [" + suite + "] has no suite parallel mode"));
            }
            if (ParallelMode.INSTANCES.toString().equals(parallelMode.getValue())) {
                throw new CosengException(
                        "Parallel mode [" + ParallelMode.INSTANCES + "] unsupported");
            }
            /*
             * ALL XML suites must have parallel="false" if using one WebDriver.
             * 20170331 Since testng-1.0.dtd still in conflict with the TestNG
             * code base (6.10) convert any parallel="none" to "false" (none is
             * the default for undefined property in DTD)
             */
            String parallelModeValue = parallelMode.getValue();
            if (parallelNone.equals(parallelModeValue)) {
                parallelMode.setValue(parallelFalse);
            }
            if (isOneWebDriver) {
                if (!parallelFalse.equals(parallelModeValue)) {
                    throw new CosengException(Message.details(name,
                            "suite [" + suite + "] parallel mode must be [" + parallelFalse
                                    + "] when onWebDriver is [" + isOneWebDriver + "]"));
                }
            }
            /*
             * Count <test> occurances; this is what determines a suite under
             * test from a suite of <suite-files>. Only care that a <test>
             * exists; not counting those. Used in suites(Test) for
             * setSuiteCount which is in turn used in CosengListener.
             */
            List<Element> tests = suiteRoot.getChildren(attrTest);
            if (tests != null && !tests.isEmpty()) {
                testSuiteCount++;
            }
            /* If suite-files modify each suite-file as well */
            for (Element element : suiteRoot.getChildren(attrSuiteFiles)) {
                for (Element e : element.getChildren(attrSuiteFile)) {
                    Attribute suiteFile = e.getAttribute(attrPath);
                    List<String> modifiedSuiteFiles = modifySuiteXml(name, resourceDirectory,
                            suiteFile.getValue(), isOneWebDriver);
                    suiteFile.setValue(modifiedSuiteFiles.get(0));
                }
            }
            /* Save full relative path to temp resource directory */
            String suitePath = Resource.getRelativePath(suite);
            suitePath = resourceDirectory + File.separator + suitePath;
            new File(suitePath).mkdirs();
            suitePath = suitePath + Resource.getName(suite);
            XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
            try {
                // xout.output(jdomSuite, System.out);
                OutputStream out = new FileOutputStream(suitePath);
                xout.output(jdomSuite, out);
                out.close();
                /* Use temp suite file for populating xmlSuite */
                modifiedSuites.add(suitePath);
            } catch (IOException e) {
                throw new CosengException(Message.details(name,
                        "saving suite resource [" + suite + "] to [" + suitePath + "] failed"));
            }
        }
        return modifiedSuites;
    }

    /**
     * Grid url.
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.Validate#tests()
     * @since 2.0
     * @version.coseng
     */
    private static void gridUrl(Test test) throws CosengException {
        String name = test.getName();
        Location location = test.getLocation();
        URL gridUrl = test.getGridUrl();
        URL nodeGridUrl = node.getGridUrl();
        if (gridUrl == null) {
            // assume node gridUrl value as default
            gridUrl = nodeGridUrl;
            test.setGridUrl(nodeGridUrl);
        }
        if (Location.GRID.equals(location) && gridUrl == null) {
            throw new CosengException(Message.details(name,
                    "gridUrl not provided; location [" + location + "] REQUIRES valid gridUrl"));
        }
    }

    /**
     * Browser dimension.
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @since 2.1
     * @version.coseng
     */
    private static void browserDimension(Test test) throws CosengException {
        /* browserMaximize overrides width and height */
        if (!test.getBrowserMaximize() && test.getBrowserDimension() == null) {
            if (test.getBrowserWidth() != null || test.getBrowserHeight() != null) {
                throw new CosengException(Message.details(test.getName(),
                        "invalid browser width or height; both must be defined and > 0"));
            }
        }
    }

    /**
     * Verbosity.
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.Validate#tests()
     * @since 2.0
     * @version.coseng
     */
    private static void verbosity(Test test) throws CosengException {
        String name = test.getName();
        Integer verbosity = test.getVerbosity();
        if (verbosity < 0 || verbosity > 10) {
            throw new CosengException(Message.details(name, "invalid verbosity; valid 0..10"));
        }
    }

    /**
     * Web driver timeout.
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.Validate#tests()
     * @since 2.0
     * @version.coseng
     */
    private static void webDriverTimeout(Test test) throws CosengException {
        String name = test.getName();
        Integer timeout = test.getWebDriverTimeoutSeconds();
        if (timeout < 0) {
            throw new CosengException(
                    Message.details(name, "invalid webDriverTimeoutSeconds; valid >= 0"));
        }
    }

    /**
     * Web driver wait timeout.
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.Validate#tests()
     * @since 2.0
     * @version.coseng
     */
    private static void webDriverWaitTimeout(Test test) throws CosengException {
        String name = test.getName();
        Integer timeout = test.getWebDriverWaitTimeoutSeconds();
        if (timeout < 0) {
            throw new CosengException(
                    Message.details(name, "invalid webDriverWaitTimeoutSeconds; valid >= 0"));
        }
    }

    /**
     * Warn base url undefined.
     *
     * @param test
     *            the test
     * @see com.sios.stc.coseng.run.Validate#tests()
     * @since 2.0
     * @version.coseng
     */
    private static void warnBaseUrlUndefined(Test test) {
        String name = test.getName();
        String baseUrl = test.getBaseUrl();
        if (baseUrl == null || baseUrl.isEmpty()) {
            log.warn(Message.details(name, "baseUrl not provided; some tests may fail"));
        }
    }

    /**
     * Warn browser version for node.
     *
     * @param test
     *            the test
     * @see com.sios.stc.coseng.run.Validate#tests()
     * @since 2.0
     * @version.coseng
     */
    private static void warnBrowserRequestVersionForNode(Test test) {
        String name = test.getName();
        Location location = test.getLocation();
        String browserRequestVersion = test.getBrowserRequestVersion();
        if (Location.NODE.equals(location)
                && !Browsers.BROWSER_VERSION_DEFAULT.equals(browserRequestVersion)) {
            log.warn(Message.details(name,
                    "browserRequestVersion ignored for test at location [" + location + "]"));
        }
    }

}
