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
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

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
            LogManager.getLogger(Coseng.class.getName());

    /**
     * Start web driver. Based on test location, platform and browser select and
     * start the appropriate web driver.
     *
     * @param test
     *            the test
     * @return the web driver toolbox
     * @throws CosengException
     *             the coseng exception
     * @since 2.0
     * @version.coseng
     */
    protected static WebDriverToolbox startWebDriver(final Test test) throws CosengException {

        /*
         * Creating the WebDriver object starts the backing browser instance at
         * once. There is no delay of the instantiation. Make sure ready for it.
         */
        try {
            Location location = test.getLocation();
            Platform platform = test.getPlatform();
            Browser browser = test.getBrowser();
            String browserVersion = test.getBrowserVersion();
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
                FirefoxProfile profile = new FirefoxProfile();
                DesiredCapabilities dc = DesiredCapabilities.firefox();
                dc.setPlatform(platform);
                dc.setVersion(browserVersion);
                if (browserVersion != null && browserVersion.equals("48.0")) {
                    dc.setCapability(FirefoxDriver.MARIONETTE, true);
                }
                if (isAcceptInvalidCerts) {
                    /*
                     * 2016-09-01 Doesn't work with FF 48. Can't do * gimick as
                     * with IE since geckodriver will bomb before getting chance
                     * to 'drive' thru // manually accepting the invalid certs.
                     * (Awaiting upstream geckodriver fix). Till then import
                     * cert into profile or add to browser.
                     */

                    profile.setAcceptUntrustedCertificates(true);
                }
                if (isIncognito) {
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
                if (Platform.LINUX.equals(platform)) {
                    /*
                     * Explicitly enable native events(this is mandatory on
                     * Linux system, since they are not enabled by default.
                     */
                    profile.setEnableNativeEvents(true);
                }
                dc.setCapability(FirefoxDriver.PROFILE, profile);
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
                dc.setPlatform(platform);
                dc.setVersion(browserVersion);
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
                dc.setPlatform(platform);
                dc.setVersion(browserVersion);
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
                dc.setPlatform(platform);
                dc.setVersion(browserVersion);
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
            return new WebDriverToolbox(test, webDriver, webDriverService);
        } catch (Exception e) {
            throw new CosengException("Error starting WebDriver", e);
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
     * @since 2.0
     * @version.coseng
     */
    protected static void stopWebDriver(final WebDriver webDriver, final Object webDriverService)
            throws CosengException {
        try {
            webDriver.close();
            webDriver.quit();
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
            throw new CosengException("Error stopping WebDriver");
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
