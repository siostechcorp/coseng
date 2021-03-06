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
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.sios.stc.coseng.RunTests;
import com.sios.stc.coseng.run.Browsers.Browser;
import com.sios.stc.coseng.run.Locations.Location;

/**
 * The Class WebDriverLifecycle manages the start, stop and state of a test's
 * web driver.
 *
 * @since 2.0
 * @version.coseng
 */
class WebDriverLifecycle {

    private static final int    WEB_DRIVER_SERVICE_IS_RUNNING_TIMEOUT_ATTEMPTS = 10;
    private static final Long   WEB_DRIVER_SERVICE_IS_RUNNING_TIMEOUT_SLEEP    = 500L;
    private static final Logger log                                            =
            LogManager.getLogger(RunTests.class.getName());

    /**
     * Start web driver. Based on test location, platform and browser select and
     * start the appropriate web driver.
     *
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.CosengRunner
     * @since 2.0
     * @version.coseng
     */
    protected static void startWebDriver(final Test test) throws CosengException {
        /*
         * Creating the WebDriver object starts the backing browser instance at
         * once. There is no delay of the instantiation. Make sure ready for it.
         */
        try {
            Location location = test.getLocation();
            Platform platform = test.getPlatform();
            Browser browser = test.getBrowser();
            String browserRequestVersion = test.getBrowserRequestVersion();
            File webDriverFile = test.getWebDriver();
            boolean isAcceptInvalidCerts = test.isAcceptInvalidCerts();
            boolean isIncognito = test.isIncognito();
            URL gridUrl = test.getGridUrl();
            Object webDriverService = null;
            WebDriver webDriver = null;
            // DesiredCapabilities
            // https://code.google.com/p/selenium/wiki/DesiredCapabilities
            /*
             * !! It is *not* necessary to start *any* of the browser webDriver
             * profiles to start 'private/icognito' as each new webDriver
             * instance starts with a *fresh* profile that does not persist
             * after the webDriver is quit. But do enable if you need the true
             * 'private' mode.
             */
            if (Browser.FIREFOX.equals(browser)) {
                FirefoxOptions options = new FirefoxOptions();
                FirefoxProfile profile = new FirefoxProfile();
                DesiredCapabilities dc = DesiredCapabilities.firefox();
                if (!Platform.ANY.equals(platform)) {
                    dc.setPlatform(platform);
                }
                if (!Browsers.BROWSER_VERSION_DEFAULT.equals(browserRequestVersion)) {
                    dc.setVersion(browserRequestVersion);
                }
                if (isAcceptInvalidCerts) {
                    dc.setCapability("acceptInsecureCerts", true);
                    profile.setAcceptUntrustedCertificates(true);
                    profile.setAssumeUntrustedCertificateIssuer(false);
                }
                if (isIncognito) {
                    options.addArguments("-private");
                    // Not the same as true "-private" mode
                    profile.setPreference("browser.privatebrowsing.autostart", true);
                    /*
                     * Can only provide "-private" command line if running
                     * locally 2016-09-01 using FirefoxBinary and
                     * 'addCommandLineOptions' is not working with 3.0.0-beta2
                     * and geckodriver
                     */
                } else {
                    profile.setPreference("browser.privatebrowsing.autostart", false);
                }
                options.setProfile(profile);
                options.addTo(dc);
                if (Location.NODE.equals(location)) {
                    GeckoDriverService service = new GeckoDriverService.Builder()
                            .usingDriverExecutable(webDriverFile).usingAnyFreePort().build();
                    service.start();
                    if (webDriverServiceIsRunning(service)) {
                        webDriver =
                                new RemoteWebDriver(((GeckoDriverService) service).getUrl(), dc);
                    }
                    webDriverService = service;
                } else {
                    webDriver = new RemoteWebDriver(gridUrl, dc);
                }
            } else if (Browser.CHROME.equals(browser)) {
                DesiredCapabilities dc = DesiredCapabilities.chrome();
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--test-type");
                options.addArguments("--disable-infobars");
                if (test.getBrowserHeadless()) {
                    options.addArguments("--headless");
                }
                if (!Platform.ANY.equals(platform)) {
                    dc.setPlatform(platform);
                }
                if (!Browsers.BROWSER_VERSION_DEFAULT.equals(browserRequestVersion)) {
                    dc.setVersion(browserRequestVersion);
                }
                if (isAcceptInvalidCerts) {
                    options.addArguments("--ignore-certificate-errors");
                }
                if (isIncognito) {
                    options.addArguments("--incognito");
                }
                dc.setCapability(ChromeOptions.CAPABILITY, options);
                if (Location.NODE.equals(location)) {
                    ChromeDriverService service = new ChromeDriverService.Builder()
                            .usingDriverExecutable(webDriverFile).usingAnyFreePort().build();
                    service.start();
                    if (webDriverServiceIsRunning(service)) {
                        webDriver =
                                new RemoteWebDriver(((ChromeDriverService) service).getUrl(), dc);
                    }
                    webDriverService = service;
                } else {
                    webDriver = new RemoteWebDriver(gridUrl, dc);
                }
            } else if (Browser.EDGE.equals(browser)) {
                DesiredCapabilities dc = DesiredCapabilities.edge();
                if (!Platform.ANY.equals(platform)) {
                    dc.setPlatform(platform);
                }
                if (!Browsers.BROWSER_VERSION_DEFAULT.equals(browserRequestVersion)) {
                    dc.setVersion(browserRequestVersion);
                }
                if (isAcceptInvalidCerts) {
                    dc.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
                }
                if (isIncognito) {
                    /*
                     * nothing to do yet. 2016-09-01 No EdgeDriver options to
                     * set '-private' or otherwise.
                     */
                }
                if (Location.NODE.equals(location)) {
                    EdgeDriverService service = new EdgeDriverService.Builder()
                            .usingDriverExecutable(webDriverFile).usingAnyFreePort().build();
                    service.start();
                    if (webDriverServiceIsRunning(service)) {
                        webDriver = new RemoteWebDriver(((EdgeDriverService) service).getUrl(), dc);
                    }
                    webDriverService = service;
                } else {
                    webDriver = new RemoteWebDriver(gridUrl, dc);
                }
            } else if (Browser.IE.equals(browser)) {
                DesiredCapabilities dc = DesiredCapabilities.internetExplorer();
                if (!Platform.ANY.equals(platform)) {
                    dc.setPlatform(platform);
                }
                if (!Browsers.BROWSER_VERSION_DEFAULT.equals(browserRequestVersion)) {
                    dc.setVersion(browserRequestVersion);
                }
                dc.setBrowserName(Browsers.BROWSER_NAME_INTERNET_EXPLORER);
                if (isAcceptInvalidCerts) {
                    dc.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
                }
                /*
                 * IE8 and newer; Make sure
                 * HKEY_USERS\.Default\Software\Microsoft\Internet Explorer has
                 * DWORD TabProcGrowth set 0
                 */
                dc.setCapability(InternetExplorerDriver.FORCE_CREATE_PROCESS, true);
                if (isIncognito) {
                    dc.setCapability(InternetExplorerDriver.IE_SWITCHES, "-private");
                }
                /*
                 * dc.setCapability(InternetExplorerDriver.LOG_FILE, import
                 * com.sios.stc.coseng.util.Browsers; "C:/iedriver.log");
                 * dc.setCapability(InternetExplorerDriver.LOG_LEVEL, "DEBUG");
                 */
                dc.setCapability(InternetExplorerDriver.NATIVE_EVENTS, true);
                /*
                 * dc.setCapability(
                 * InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, true);
                 */
                if (Location.NODE.equals(location)) {
                    InternetExplorerDriverService service =
                            new InternetExplorerDriverService.Builder()
                                    .usingDriverExecutable(webDriverFile).usingAnyFreePort()
                                    .build();
                    service.start();
                    if (webDriverServiceIsRunning(service)) {
                        webDriver = new RemoteWebDriver(
                                ((InternetExplorerDriverService) service).getUrl(), dc);
                    }
                    webDriverService = service;
                } else {
                    webDriver = new RemoteWebDriver(gridUrl, dc);
                }
            }
            /* Set the file detector for uploads; set early */
            ((RemoteWebDriver) webDriver).setFileDetector(new LocalFileDetector());
            /* Set dimension or maximize */
            if (test.getBrowserDimension() != null) {
                webDriver.manage().window().setSize(test.getBrowserDimension());
            } else if (test.getBrowserMaximize()) {
                webDriver.manage().window().maximize();
            }
            /* Make CosengRunner aware of Selenium tooling */
            CosengRunner.setSeleniumTools(webDriver, webDriverService);
        } catch (Exception e) {
            throw new CosengException(
                    "Error starting web driver; browser/web driver version mismatch?; check for orphaned web driver processes.",
                    e);
        }
    }

    /**
     * Stop web driver.
     *
     * @param webDriver
     *            the web driver
     * @param webDriverService
     *            the web driver service
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.CosengRunner
     * @since 2.0
     * @version.coseng
     */
    protected static void stopWebDriver(WebDriver webDriver, Object webDriverService)
            throws CosengException {
        try {
            /*
             * Calls 'dispose()'; closes all browser windows and safely ends the
             * session. Don't use 'close()'; it will close the window under
             * focus but may cause timeouts when using Selenium GRID Hub
             */
            webDriver.quit();
            /* WebDriverService is for local/node instances */
            if (webDriverService != null) {
                if (webDriverService instanceof ChromeDriverService) {
                    ((ChromeDriverService) webDriverService).stop();
                } else if (webDriverService instanceof GeckoDriverService) {
                    ((GeckoDriverService) webDriverService).stop();
                } else if (webDriverService instanceof InternetExplorerDriverService) {
                    ((InternetExplorerDriverService) webDriverService).stop();
                } else if (webDriverService instanceof EdgeDriverService) {
                    ((EdgeDriverService) webDriverService).stop();
                }
            }
        } catch (Exception e) {
            throw new CosengException("Error stopping web driver", e);
        }
    }

    /**
     * Web driver service is running.
     *
     * @param webDriverService
     *            the web driver service
     * @return true, if successful
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.WebDriverLifecycle#startWebDriver(Test)
     * @since 2.0
     * @version.coseng
     */
    private static boolean webDriverServiceIsRunning(final Object webDriverService)
            throws CosengException {
        /*
         * Wait for WeDriverService isRunning to be true for the timeout limit.
         * The service may not be fully operational by the time it is referenced
         * for the RemoteWebDriver instantiation.
         */
        try {
            int tryCount = WEB_DRIVER_SERVICE_IS_RUNNING_TIMEOUT_ATTEMPTS;
            while (tryCount > 0) {
                if (webDriverService instanceof ChromeDriverService
                        && ((ChromeDriverService) webDriverService).isRunning()) {
                    return true;
                } else if (webDriverService instanceof GeckoDriverService
                        && ((GeckoDriverService) webDriverService).isRunning()) {
                    return true;
                } else if (webDriverService instanceof InternetExplorerDriverService
                        && ((InternetExplorerDriverService) webDriverService).isRunning()) {
                    return true;
                } else if (webDriverService instanceof EdgeDriverService
                        && ((EdgeDriverService) webDriverService).isRunning()) {
                    return true;
                }
                tryCount--;
                try {
                    Thread.sleep(WEB_DRIVER_SERVICE_IS_RUNNING_TIMEOUT_SLEEP);
                } catch (InterruptedException e) {
                    log.warn("Timeout reached waiting for webdriver service");
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new CosengException("Error checking webdriver running", e);
        }
    }

}
