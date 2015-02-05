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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.sios.stc.coseng.util.Common.Browser;
import com.sios.stc.coseng.util.Common.Spot;

public class Run {

    private final ParamCollection param;

    public Run(final ParamCollection param) throws Exception {
        for (final TestParam p : param.testParam) {
            if ((p.isSetValid() == null) || !p.isSetValid()) {
                throw new Exception("Parameters for (" + p.getTestName()
                        + ") have not been validated");
            }
        }
        this.param = param;
    }

    public synchronized List<String> getTestName() {
        final List<String> testName = new ArrayList<String>();
        for (final TestParam p : param.testParam) {
            testName.add(p.getTestName());
        }
        return testName;
    }

    public synchronized Spot getSpot(final String testName) {
        Spot spot = null;
        if (testName != null) {
            for (final TestParam p : param.testParam) {
                if (p.getTestName().equals(testName)) {
                    spot = p.getSpot();
                }
            }
        }
        return spot;
    }

    public synchronized Platform getPlatform(final String testName) {
        Platform platform = null;
        if (testName != null) {
            for (final TestParam p : param.testParam) {
                if (p.getTestName().equals(testName)) {
                    platform = p.getPlatform();
                }
            }
        }
        return platform;
    }

    public synchronized Browser getBrowser(final String testName) {
        Browser browser = null;
        if (testName != null) {
            for (final TestParam p : param.testParam) {
                if (p.getTestName().equals(testName)) {
                    browser = p.getBrowser();
                }
            }
        }
        return browser;
    }

    public synchronized String getBaseUrl(final String testName) {
        String baseUrl = null;
        if (testName != null) {
            for (final TestParam p : param.testParam) {
                if (p.getTestName().equals(testName)) {
                    baseUrl = p.getBaseUrl();
                }
            }
        }
        return baseUrl;
    }

    public synchronized List<String> getSuite(final String testName) {
        List<String> testSuite = new ArrayList<String>();
        if (testName != null) {
            for (final TestParam p : param.testParam) {
                if (p.getTestName().equals(testName)) {
                    testSuite = p.getSuite();
                }
            }
        }
        return testSuite;
    }

    public synchronized List<File> getTestReportDirectory() {
        final List<File> dir = new ArrayList<File>();
        for (final TestParam p : param.testParam) {
            dir.add(p.getTestReportDirectory());
        }
        return dir;
    }

    public synchronized String getTestReportDirectoryPath(final String testName) {
        for (final TestParam p : param.testParam) {
            if (p.getTestName().equals(testName)) {
                return p.getTestReportDirectoryPath();
            }
        }
        return null;
    }

    public synchronized WebDriver getWebDriver(final String testName)
            throws MalformedURLException {

        WebDriver driver = null;

        if (testName != null) {

            for (final TestParam p : param.testParam) {
                if (p.getTestName().equals(testName)) {

                    // DesiredCapabilities
                    // https://code.google.com/p/selenium/wiki/DesiredCapabilities

                    // !! It is *not* necessary to start *any* of the browser driver
                    // profiles to start 'private/icognito' as each new driver
                    // instance starts with a *fresh* profile that does not persist
                    // after the driver is quit. !!

                    if (p.getBrowser().equals(Browser.FIREFOX)) {
                        final FirefoxProfile profile = new FirefoxProfile();
                        final DesiredCapabilities dc = DesiredCapabilities.firefox();
                        if (p.getPlatform().equals(Platform.LINUX)) {
                            // Explicitly enable native events(this is mandatory on Linux system,
                            // since they are not enabled by default.
                            profile.setEnableNativeEvents(true);
                            dc.setPlatform(p.getPlatform());
                            dc.setCapability(Common.BROWSER_CAPABILITY_FIREFOX_PROFILE,
                                    profile);
                        }
                        if (p.getSpot().equals(Spot.LOCAL)) {
                            driver = new FirefoxDriver(profile);
                        } else {
                            driver = new RemoteWebDriver(new URL(
                                    p.getSeleniumGridUrl()),
                                    dc);
                        }
                    } else if (p.getBrowser().equals(Browser.CHROME)) {
                        final DesiredCapabilities dc = DesiredCapabilities.chrome();
                        dc.setPlatform(p.getPlatform());
                        if (p.getSpot().equals(Spot.LOCAL)) {
                            driver = new ChromeDriver();
                        } else {
                            driver = new RemoteWebDriver(new URL(
                                    p.getSeleniumGridUrl()),
                                    dc);
                        }
                    } else if (p.getBrowser().toString().toLowerCase()
                            .startsWith("ie")) {
                        final DesiredCapabilities dc = DesiredCapabilities
                                .internetExplorer();
                        dc.setBrowserName(Common.BROWSER_NAME_INTERNET_EXPLORER);
                        dc.setPlatform(p.getPlatform());
                        // IE8 and newer; Make sure
                        // HKEY_USERS\.Default\Software\Microsoft\Internet
                        // Explorer has DWORD TabProcGrowth set 0
                        // Launch separate process in 'private' mode.
                        dc.setCapability(
                                InternetExplorerDriver.FORCE_CREATE_PROCESS,
                                true);
                        dc.setCapability(InternetExplorerDriver.IE_SWITCHES,
                                "-private");
                        //dc.setCapability(InternetExplorerDriver.LOG_FILE,
                        //        "C:/iedriver.log");
                        //dc.setCapability(InternetExplorerDriver.LOG_LEVEL,
                        //        "DEBUG");
                        dc.setCapability(InternetExplorerDriver.NATIVE_EVENTS,
                                true);
                        // dc.setCapability(
                        // InternetExplorerDriver.REQUIRE_WINDOW_FOCUS,
                        // true);

                        // If *is* a specific IE9, IE10, IE11 (not IE) set the version
                        if (!p.getBrowser().equals(Browser.IE)) {
                            dc.setVersion(p.getBrowser().getVersion());
                        }

                        if (p.getSpot().equals(Spot.LOCAL)) {
                            driver = new InternetExplorerDriver();
                        } else {
                            driver = new RemoteWebDriver(new URL(
                                    p.getSeleniumGridUrl()),
                                    dc);
                        }
                    }
                }
                // Collect the driver for quit after tests complete
                if (driver != null) {
                    collectDriver(driver);
                }
            }
        }
        return driver;
    }

    public synchronized void startService(final Spot spot,
            final WebDriver driver) throws IOException {
        if ((spot != null) && spot.equals(Spot.LOCAL) && (driver != null)) {
            // WebDriver is built into FIREFOX on Linux and Windows
            if (driver instanceof ChromeDriver) {
                // If service class isn't already running start the service
                if (!isServiceRunning(ChromeDriverService.class)) {
                    final ChromeDriverService service = new ChromeDriverService.Builder()
                    .usingDriverExecutable(param.localParam.getChromeDriver())
                    .usingAnyFreePort().build();
                    service.start();
                    // Collect started service for cleanup after tests exhausted
                    collectService(service);
                }
            } else if (driver instanceof InternetExplorerDriver) {
                if (!isServiceRunning(InternetExplorerDriverService.class)) {
                    final InternetExplorerDriverService service = new InternetExplorerDriverService.Builder()
                    .usingDriverExecutable(
                            param.localParam.getIeDriver())
                            .usingAnyFreePort().build();
                    service.start();
                    // Collect started service for cleanup after tests exhausted
                    collectService(service);
                }
            }
        }
    }

    public synchronized void quitDriver() {
        for (final WebDriver driver : Common.driverCollector) {
            driver.quit();
        }
    }

    public synchronized void stopService() {
        for (final Object service : Common.serviceCollector) {
            if (service instanceof ChromeDriverService) {
                ((ChromeDriverService) service).stop();
            }
        }
    }

    public synchronized String getThreadTestName(final long threadId) {
        // Fetch testName associated with a testSuite
        // and remove it from the list.
        String testName = null;
        if (!param.testParam.isEmpty()) {
            for (final TestParam p : param.testParam) {
                if (threadId == p.getThreadId()) {
                    testName = p.getTestName();
                    break;
                }
            }
        }
        return testName;
    }

    public synchronized Collection<TestParam> getParam() {
        return param.testParam;
    }

    public synchronized boolean isEmpty() {
        boolean isEmpty = true;
        if ((param != null) && (param.testParam.size() > 0)) {
            isEmpty = false;
        }
        return isEmpty;
    }

    private synchronized void collectService(final Object service) {
        Common.serviceCollector.add(service);
    }

    private synchronized void collectDriver(final WebDriver driver) {
        Common.driverCollector.add(driver);
    }

    private synchronized boolean isServiceRunning(final Class<?> serviceClass) {
        Boolean isRunning = false;
        for (final Object s : Common.serviceCollector) {
            if ((s.getClass()).equals(serviceClass)) {
                isRunning = true;
            }
        }
        return isRunning;
    }

}
