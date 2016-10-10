/*
 * Concurrent Selenium TestNG (COSENG)
 * Copyright (c) 2013-2016 SIOS Technology Corp.  All rights reserved.
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
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
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

import com.rits.cloning.Cloner;
import com.sios.stc.coseng.run.Browsers.Browser;
import com.sios.stc.coseng.run.Locations.Location;
import com.sios.stc.coseng.util.Resource;

/**
 * The Class Validate.
 *
 * @since 2.0
 * @version.coseng
 */
class Validate {

    private static final Logger log = LogManager.getLogger(Coseng.class.getName());
    private static Node         node;
    private static Tests        tests;

    /**
     * Tests.
     *
     * @param node
     *            the node
     * @param tests
     *            the tests
     * @throws CosengException
     *             the coseng exception
     * @since 2.0
     * @version.coseng
     */
    protected static void tests(Node node, Tests tests) throws CosengException {
        if (node != null && tests != null && tests.size() > 0) {
            Validate.node = node;
            Validate.tests = tests;
            notNull();
            uniqueName();
            /*
             * Some tests will be identified as ANY/ALL. This implies multiple
             * COSENG tests against all supported Platform and Browsers. Create
             * explicit combinations for supported Platform and Browsers.
             */
            createExplicitTests();
            /*
             * Validate node *first*; tests depends on their valid tests.
             */
            node();
            tests();
        } else {
            throw new CosengException("Node null or 0 tests; nothing to do");
        }
    }

    /**
     * Not null.
     *
     * @throws CosengException
     *             the coseng exception
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
     * Unique name.
     *
     * @throws CosengException
     *             the coseng exception
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
     * Creates the explicit tests.
     *
     * @throws CosengException
     *             the coseng exception
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
     * New tests.
     *
     * @param original
     *            the original
     * @param platform
     *            the platform
     * @return the list
     * @throws CosengException
     *             the coseng exception
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
     * New test.
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
     * @since 2.0
     * @version.coseng
     */
    private static Test newTest(Test original, Platform platform, Browser browser)
            throws CosengException {
        String nameSeparator = "_";
        String name = original.getName();
        Test test = cloneTest(original);
        test.setPlatform(platform);
        test.setBrowser(browser);
        test.setName(name + nameSeparator + browser.toString().toLowerCase());
        test.setIsSynthetic(true);
        return test;
    }

    /**
     * Clone test.
     *
     * @param original
     *            the original
     * @return the test
     * @throws CosengException
     *             the coseng exception
     * @since 2.0
     * @version.coseng
     */
    private static Test cloneTest(Test original) throws CosengException {
        String name = original.getName();
        try {
            Cloner cloner = new Cloner();
            Test test = cloner.deepClone(original);
            return test;
        } catch (Exception e) {
            throw new CosengException(Message.details(name, "error creating test clone"));
        }
    }

    /**
     * Node.
     *
     * @throws CosengException
     *             the coseng exception
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
        }
    }

    /**
     * Directory.
     *
     * @param directory
     *            the directory
     * @throws CosengException
     *             the coseng exception
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
     * Tests.
     *
     * @throws CosengException
     *             the coseng exception
     * @since 2.0
     * @version.coseng
     */
    private static void tests() throws CosengException {
        for (Test test : tests.getAll()) {
            platform(test);
            browser(test);
            webDriver(test);
            reportDirectory(test);
            suites(test);
            gridUrl(test);
            verbosity(test);
            webDriverTimeout(test);
            webDriverWaitTimeout(test);
            warnBaseUrlUndefined(test);
            warnBrowserVersionForNode(test);
        }
    }

    /**
     * Platform.
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
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
     * @since 2.0
     * @version.coseng
     */
    private static void reportDirectory(Test test) {
        File reportsDirectory = node.getReportsDirectory();
        String name = test.getName();
        test.setReportDirectory(reportsDirectory + File.separator + name);
    }

    /**
     * Suites. Iterate over the collection of TestNG Suit XML. During validation
     * insert a custom xml parameter
     * {@value Test#COSENG_XMLSUITE_PARAMETER_TEST_NAME} to cross-reference the
     * test by name.
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
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
            List<XmlSuite> xmlSuites = new ArrayList<XmlSuite>();
            List<String> tempSuites = new ArrayList<String>();
            Map<String, String> suiteXmlParameter = new HashMap<String, String>();
            XmlSuite xmlSuite = new XmlSuite();
            String attrName = "name";
            String attrParallel = "parallel";
            String attrTest = "test";
            String nameSuiteSeparator = "_";

            suiteXmlParameter.put(Test.COSENG_XMLSUITE_PARAMETER_TEST_NAME, name);

            for (String suite : suites) {
                File suiteResource = Resource.get(suite);
                /* Read original suite */
                SAXBuilder jdomBuilder = new SAXBuilder();
                Document jdomSuite = null;
                try {
                    jdomSuite = jdomBuilder.build(suiteResource);
                } catch (JDOMException | IOException e) {
                    throw new CosengException(
                            Message.details(name, "deserialize suite [" + suite + "] failed"), e);
                }
                /*
                 * Modify some elements to identify the COSENG name. Get 'suite'
                 * root and change name to be "cosengTestName_suiteName"
                 */
                Element suiteRoot = jdomSuite.getRootElement();
                Attribute suiteName = suiteRoot.getAttribute(attrName);
                suiteName.setValue(name + nameSuiteSeparator + suiteName.getValue());

                Attribute parallelMode = suiteRoot.getAttribute(attrParallel);
                if (parallelMode == null) {
                    throw new CosengException(Message.details(name,
                            "suite [" + suite + "] has no suite parallel mode"));
                }
                if (ParallelMode.INSTANCES.toString().equals(parallelMode.getValue())) {
                    throw new CosengException(
                            "Parallel mode [" + ParallelMode.INSTANCES + "] unsupported");
                }
                /*
                 * ALL XML suites must have parallel="false" if using one
                 * WebDriver
                 */
                if (test.isOneWebDriver()) {
                    if (!ParallelMode.FALSE.toString().equals(parallelMode.getValue())) {
                        throw new CosengException(Message.details(name,
                                "suite [" + suite + "] parallel mode must be [" + ParallelMode.FALSE
                                        + "] when onWebDriver is [" + test.isOneWebDriver() + "]"));
                    }
                }
                /*
                 * Get the 'test' children and change name to be
                 * "cosengTestName_TestName"
                 */
                for (Element element : suiteRoot.getChildren(attrTest)) {
                    Attribute suiteTestName = element.getAttribute(attrName);
                    suiteTestName.setValue(name + nameSuiteSeparator + suiteTestName.getValue());
                }
                /*
                 * Save to temp with unique name; single COSENG JSON config with
                 * multiple tests could be using the same original suite XML
                 * file.
                 */
                XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
                String suitePath = node.getResourcesTempDirectory() + File.separator + name
                        + nameSuiteSeparator + suite;
                try {
                    // xout.output(jdomSuite, System.out);
                    OutputStream out = new FileOutputStream(suitePath);
                    xout.output(jdomSuite, out);
                    // use temp suite file for populating xmlSuite
                    tempSuites.add(suitePath);
                } catch (IOException e) {
                    throw new CosengException(Message.details(name,
                            "saving suite resource [" + suite + "] to [" + suitePath + "] failed"));
                }
            }
            xmlSuite.setSuiteFiles(tempSuites);
            /*
             * TestNG requires setting some available file name; doesn't even
             * have to be a related suite xml file, any existing file will do.
             * So, just pick the first suite file.
             */
            xmlSuite.setFileName(tempSuites.get(0));
            xmlSuite.setName(name);
            /*
             * Add the test name for managing the webdriver instances across the
             * test suite files
             */
            xmlSuite.setParameters(suiteXmlParameter);
            /*
             * Can't 'read' the suites for suite parameters. Must be caught
             * during execution.
             */
            xmlSuites.add(xmlSuite);

            /*
             * Record the collection temporary and modified suite XML files for
             * reference by CosengRunner
             */
            test.setXmlSuites(xmlSuites);
        }

    }

    /**
     * Grid url.
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
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
     * Verbosity.
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
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
     * @since 2.0
     * @version.coseng
     */
    private static void warnBrowserVersionForNode(Test test) {
        String name = test.getName();
        Location location = test.getLocation();
        String browserVersion = test.getBrowserVersion();
        if (Location.NODE.equals(location)
                && !Browsers.BROWSER_VERSION_DEFAULT.equals(browserVersion)) {
            log.warn(Message.details(name,
                    "browserVersion ignored for test at location [" + location + "]"));
        }
    }
}
