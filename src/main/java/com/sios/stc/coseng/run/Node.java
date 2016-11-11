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
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Platform;

import com.google.gson.annotations.Expose;
import com.sios.stc.coseng.run.Browsers.Browser;

/**
 * The Class Node provides the deserialization fields for the Node JSON
 * configuration file. If the JSON configuration file is absent Node is created
 * with platform defaults. Node parameters define web driver executable paths,
 * TestNG reports directory and temporary resources directory.
 * 
 * JSON example with Linux Node fields defined:
 * 
 * <pre>
 * <code>
 * {
 *   "chromeDriver": "/usr/local/bin/chromedriver",
 *   "geckoDriver": "/usr/local/bin/geckodriver",
 *   "reportsDirectory": "/tmp/reports",
 *   "resourcesTempDirectory": "/tmp/coseng/resources",
 *   "gridUrl": "http://seleniumgrid.host.com:4444/wd/hub",
 *   "maxTestExecutionMinutes": 60
 * }
 * </pre></code>
 * 
 * JSON fields and default value:
 * <dl>
 * <dt>Linux</dt>
 * <dd>reportsDirectory: "coseng-reports" (in current working directory)</dd>
 * <dd>resourcesTempDirectory: /tmp/coseng</dd>
 * <dd>gridUrl: http://localhost:4444/wd/hub</dd>
 * <dd>chromeDriver: /usr/bin/chromedriver</dd>
 * <dd>geckoDriver: /usr/bin/geckodriver</dd>
 * <dd>maxTestExecutionMinutes: 60</dd>
 * <dt>Windows</dt>
 * <dd>reportsDirectory: "" (the current working directory)</dd>
 * <dd>resourcesTempDirectory: %USERPROFILE%\AppData\Local\Temp</dd>
 * <dd>gridUrl: http://localhost:4444/wd/hub</dd>
 * <dd>chromeDriver: C:\\selenium\\chromedriver.exe</dd>
 * <dd>geckoDriver: C:\\selenium\\geckodriver.exe</dd>
 * <dd>edgeDriver: C:\\selenium\\MicrosoftWebDriver.exe</dd>
 * <dd>ieDriver: C:\\selenium\\IEDriverServer.exe</dd>
 * <dd>maxTestExecutionMinutes: 60</dd>
 * </dl>
 *
 * @since 2.0
 * @version.coseng
 */
class Node {

    private static final String DEFAULT_LINUX_CHROME_DRIVER_PATH   = "/usr/bin/chromedriver";
    private static final String DEFAULT_LINUX_GECKO_DRIVER_PATH    = "/usr/bin/geckodriver";
    private static final String DEFAULT_WINDOWS_CHROME_DRIVER_PATH =
            "C:\\selenium\\chromedriver.exe";
    private static final String DEFAULT_WINDOWS_GECKO_DRIVER_PATH  =
            "C:\\selenium\\geckodriver.exe";
    private static final String DEFAULT_WINDOWS_EDGE_DRIVER_PATH   =
            "C:\\selenium\\MicrosoftWebDriver.exe";
    private static final String DEFAULT_WINDOWS_IE_DRIVER_PATH     =
            "C:\\selenium\\IEDriverServer.exe";
    private String              defaultGridUrl                     = "http://localhost:4444/wd/hub";
    private String              defaultReportsDirectory            = "coseng-reports";
    private int                 defaultTestExecutionMinutes        = 60;

    @Expose
    private final String reportsDirectory        = defaultReportsDirectory;
    @Expose
    private final String resourcesTempDirectory  =
            FileUtils.getTempDirectoryPath() + File.separator + "coseng";
    @Expose
    private final String chromeDriver            = getDefaultWebDriver(Browser.CHROME);
    @Expose
    private final String geckoDriver             = getDefaultWebDriver(Browser.FIREFOX);
    @Expose
    private final String ieDriver                = getDefaultWebDriver(Browser.IE);
    @Expose
    private final String edgeDriver              = getDefaultWebDriver(Browser.EDGE);
    @Expose
    private final String gridUrl                 = defaultGridUrl;
    @Expose
    private final int    maxTestExecutionMinutes = defaultTestExecutionMinutes;

    /**
     * Gets the reports directory. This is the target directory for the TestNG
     * HTML reports. If it doesn't exist an attempt will be made to create it.
     *
     * @return the reports directory
     * @since 2.0
     * @version.coseng
     */
    protected File getReportsDirectory() {
        return new File(reportsDirectory);

    }

    /**
     * Gets the resources temp directory. This is the temporary target for the
     * synthetic JSON Test file(s) to support the COSENG test execution. If it
     * doesn't exist an attempt will be made to create it.
     *
     * @return the resources temp directory
     * @since 2.0
     * @version.coseng
     */
    protected File getResourcesTempDirectory() {
        return new File(resourcesTempDirectory);
    }

    /**
     * Gets the default web driver. Returns the default browser's web driver for
     * the given operating system platform.
     *
     * @param browser
     *            the browser
     * @return the default, platform dependent, web driver
     * @since 2.0
     * @version.coseng
     */
    private String getDefaultWebDriver(Browser browser) {
        Platform platform = OperatingSystem.getPlatform();
        String webDriver = null;
        if (Browser.CHROME.equals(browser)) {
            if (Platform.LINUX.equals(platform)) {
                webDriver = DEFAULT_LINUX_CHROME_DRIVER_PATH;
            } else if (Platform.WIN10.equals(platform)) {
                webDriver = DEFAULT_WINDOWS_CHROME_DRIVER_PATH;
            }
        } else if (Browser.FIREFOX.equals(browser)) {
            if (Platform.LINUX.equals(platform)) {
                webDriver = DEFAULT_LINUX_GECKO_DRIVER_PATH;
            } else if (Platform.WIN10.equals(platform)) {
                webDriver = DEFAULT_WINDOWS_GECKO_DRIVER_PATH;
            }
        } else if (Browser.IE.equals(browser)) {
            if (Platform.WIN10.equals(OperatingSystem.getPlatform())) {
                webDriver = DEFAULT_WINDOWS_IE_DRIVER_PATH;
            }
        } else if (Browser.EDGE.equals(browser)) {
            if (Platform.WIN10.equals(OperatingSystem.getPlatform())) {
                webDriver = DEFAULT_WINDOWS_EDGE_DRIVER_PATH;
            }
        }
        return webDriver;
    }

    /**
     * Gets the chrome driver. If location {@code NODE} then the web driver must
     * exist and be executable. If location {@code GRID} platform must be one of
     * {@code ANY, LINUX, WINDOWS, WIN10}.
     *
     * @return the chrome driver
     * @see com.sios.stc.coseng.run.Validate#tests(Node, Tests)
     * @since 2.0
     * @version.coseng
     */
    protected File getChromeDriver() {
        if (chromeDriver != null) {
            return new File(chromeDriver);
        }
        return null;
    }

    /**
     * Gets the firefox gecko driver. If location {@code NODE} then the web
     * driver must exist and be executable. If location {@code GRID} platform
     * must be one of {@code ANY, LINUX, WINDOWS, WIN10}.
     *
     * @return the gecko driver
     * @see {@link com.sios.stc.coseng.run.Validate#tests(Node, Tests)
     * @since 2.0
     * @version.coseng
     */
    protected File getGeckoDriver() {
        if (geckoDriver != null) {
            return new File(geckoDriver);
        }
        return null;
    }

    /**
     * Gets the windows ie driver. If location {@code NODE} then the web driver
     * must exist and be executable and platform must be either
     * {@code WINDOWS, WIN10}. If location {@code GRID} platform must be one of
     * {@code ANY, WINDOWS, WIN10}.
     *
     * @return the ie driver
     * @see com.sios.stc.coseng.run.Validate#tests(Node, Tests)
     * @since 2.0
     * @version.coseng
     */
    protected File getIeDriver() {
        if (ieDriver != null) {
            return new File(ieDriver);
        }
        return null;
    }

    /**
     * Gets the windows edge driver. If location {@code NODE} then the web
     * driver must exist and be executable and platform must be either
     * {@code WINDOWS, WIN10}. If location {@code GRID} platform must be one of
     * {@code ANY, WINDOWS, WIN10}.
     *
     * @return the edge driver
     * @see com.sios.stc.coseng.run.Validate#tests(Node, Tests)
     * @since 2.0
     * @version.coseng
     */
    protected File getEdgeDriver() {
        if (edgeDriver != null) {
            return new File(edgeDriver);
        }
        return null;
    }

    /**
     * Gets the Selenium GRID Hub URL. If defined, provides a default value for
     * all undefined {@code gridUrl} in {@code Test}.
     *
     * @return the grid url
     * @throws CosengException
     *             the coseng exception on caught {@code MalformedURLException}
     * @see com.sios.stc.coseng.run.Validate#tests(Node, Tests)
     * @see Test#setGridUrl(URL)
     * @since 2.0
     * @version.coseng
     */
    protected URL getGridUrl() throws CosengException {
        if (gridUrl != null && !gridUrl.isEmpty()) {
            try {
                return new URL(gridUrl);
            } catch (MalformedURLException e) {
                throw new CosengException("MalformedURLException for gridUrl [" + gridUrl + "]", e);
            }
        }
        return null;
    }

    /**
     * Gets the max test execution minutes.
     *
     * @return the max test execution minutes
     * @since 2.0
     * @version.coseng
     */
    protected int getMaxTestExecutionMinutes() {
        return maxTestExecutionMinutes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "reportsDirectory [" + reportsDirectory + "], resourcesTempDirectory ["
                + resourcesTempDirectory + "], chromeDriver [" + chromeDriver + "], ieDriver ["
                + ieDriver + "], geckoDriver [" + geckoDriver + "], edgeDriver [" + edgeDriver
                + "], gridUrl [" + gridUrl + "], maxTestExecutionMinutes ["
                + maxTestExecutionMinutes + "]";
    }

}
