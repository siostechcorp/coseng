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
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.Platform;
import org.testng.xml.XmlSuite;

import com.google.gson.annotations.Expose;
import com.sios.stc.coseng.run.Browsers.Browser;
import com.sios.stc.coseng.run.Locations.Location;

/**
 * The Class Test provides the deserialization fields for the Tests JSON
 * configuration file. Test parameters primarily define the platform, browser
 * environment.
 * 
 * JSON example with Linux Test fields defined:
 * 
 * <pre>
 * <code>
 * {
 *   "name": "demo1",
 *   "platform": "linux",
 *   "location": "grid",
 *   "browser": "chrome",
 *   "browserVersion": "11.0",
 *   "oneWebDriver": true,
 *   "suites": [
 *     "Demo.xml"
 *    ],
 *   "baseUrl": "https://minion25",
 *   "gridUrl": "http://fvorge.sc.steeleye.com:4444/wd/hub"
 }
 * </pre></code>
 * 
 * JSON fields and default value:
 * <dl>
 * <dt>Test</dt>
 * <dd>name: null</dd>
 * <dd>verbosity: 0</dd>
 * <dd>location: node</dd>
 * <dd>platform: any</dd>
 * <dd>browser: all</dd>
 * <dd>browserVersion:
 * {@value com.sios.stc.coseng.run.Browsers#BROWSER_VERSION_DEFAULT}</dd>
 * <dd>incognito: false (private/incognito for any supported browser)</dd>
 * <dd>acceptInvalidCerts: false</dd>
 * <dd>suites: []</dd>
 * <dd>baseUrl: null</dd>
 * <dd>gridUrl: null</dd>
 * <dd>oneWebDriver: false</dd>
 * <dd>webDriverTimeoutSeconds: 5</dd>
 * <dd>webDriverWaitTimeoutSeconds: 5</dd>
 * </dl>
 *
 * @since 2.0
 * @version.coseng
 */
public class Test {

    private static final int WEB_DRIVER_WAIT_TIMEOUT_SECONDS_DEFAULT     = 5;
    private static final int WEB_DRIVER_IMPLICIT_TIMEOUT_SECONDS_DEFAULT = 5;
    private static boolean   incognitoDefault                            = false;
    private static boolean   acceptInvalidCertsDefault                   = false;
    private static boolean   oneWebDriverDefault                         = false;
    private List<XmlSuite>   xmlSuites                                   =
            new ArrayList<XmlSuite>();
    private String           reportDirectory                             = null;
    private File             resourceDirectory                           = null;
    private boolean          failed                                      = false;
    private boolean          synthetic                                   = false;
    private File             webDriver                                   = null;

    @Expose
    private String             name                        = null;
    @Expose
    private final Integer      verbosity                   = 0;
    @Expose
    private final Location     location                    = Location.NODE;
    @Expose
    private Platform           platform                    = Platform.ANY;
    @Expose
    private Browser            browser                     = Browser.ALL;
    @Expose
    private String             browserVersion              = Browsers.BROWSER_VERSION_DEFAULT;
    @Expose
    private final boolean      incognito                   = incognitoDefault;
    @Expose
    private final boolean      acceptInvalidCerts          = acceptInvalidCertsDefault;
    @Expose
    private final List<String> suites                      = new ArrayList<String>();
    @Expose
    private final String       baseUrl                     = null;
    @Expose
    private String             gridUrl                     = null;
    @Expose
    private final boolean      oneWebDriver                = oneWebDriverDefault;
    @Expose
    private final Integer      webDriverTimeoutSeconds     =
            WEB_DRIVER_WAIT_TIMEOUT_SECONDS_DEFAULT;
    @Expose
    private final Integer      webDriverWaitTimeoutSeconds =
            WEB_DRIVER_IMPLICIT_TIMEOUT_SECONDS_DEFAULT;

    /**
     * Gets the test name.
     *
     * @return the test name
     * @since 2.0
     * @version.coseng
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the test name.
     *
     * @param name
     *            the new test name; may not be null or empty
     * @since 2.0
     * @version.coseng
     */
    protected void setName(String name) {
        if (name != null && !name.isEmpty()) {
            this.name = name;
        }
    }

    /**
     * Gets the location.
     *
     * @return the location
     * @since 2.0
     * @version.coseng
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Gets the platform.
     *
     * @return the platform
     * @since 2.0
     * @version.coseng
     */
    public Platform getPlatform() {
        return platform;
    }

    /**
     * Sets the platform.
     *
     * @param platform
     *            the new platform; may not be null
     * @since 2.0
     * @version.coseng
     */
    protected void setPlatform(Platform platform) {
        if (platform != null) {
            this.platform = platform;
        }
    }

    /**
     * Gets the browser.
     *
     * @return the browser
     * @since 2.0
     * @version.coseng
     */
    public Browser getBrowser() {
        return browser;
    }

    /**
     * Sets the browser.
     *
     * @param browser
     *            the new browser; may not be null
     * @since 2.0
     * @version.coseng
     */
    protected void setBrowser(Browser browser) {
        if (browser != null) {
            this.browser = browser;
        }
    }

    /**
     * Gets the browser version. Browser version is ignored when the location is
     * {@code NODE}.
     *
     * @return the browser version
     * @since 2.0
     * @version.coseng
     */
    public String getBrowserVersion() {
        return browserVersion;
    }

    /**
     * Checks if is incognito. Applies to all browsers that support a similar
     * mode. eg. Firefox's {@code private} mode.
     *
     * @return true, if is incognito
     * @since 2.0
     * @version.coseng
     */
    public boolean isIncognito() {
        return incognito;
    }

    /**
     * Checks if is accept invalid certs.
     *
     * @return true, if is accept invalid certs
     * @since 2.0
     * @version.coseng
     */
    public boolean isAcceptInvalidCerts() {
        return acceptInvalidCerts;
    }

    /**
     * Gets the verbosity for TestNG logging level. Valid 0..10.
     *
     * @return the verbosity
     * @since 2.0
     * @version.coseng
     */
    protected int getVerbosity() {
        return verbosity;
    }

    /**
     * Gets the base url. A base url is used for tests accessing a common
     * resource. eg. {@code http://minion13}
     *
     * @return the base url
     * @since 2.0
     * @version.coseng
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Gets the collection of TestNG suites that will be executed. Suites are
     * executed in order.
     *
     * @return the testng xml suites
     * @since 2.0
     * @version.coseng
     */
    protected List<String> getSuites() {
        return suites;
    }

    /**
     * Gets the Selenium GRID Hub URL. If defined, overrides any defined
     * {@code gridUrl} in {@code Node}.
     *
     * @return the grid url
     * @throws CosengException
     *             the coseng exception for {@code MalformedURLException}
     * @since 2.0
     * @version.coseng
     */
    public URL getGridUrl() throws CosengException {
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
     * Sets the Selenium GRID Hub URL. If undefined, is set from
     * {@code Node getGridUrl}.
     *
     * @param gridUrl
     *            the new grid url; may not be null
     * @see com.sios.stc.coseng.run.Node#getGridUrl()
     * @since 2.0
     * @version.coseng
     */
    protected void setGridUrl(URL gridUrl) {
        if (gridUrl != null) {
            this.gridUrl = gridUrl.toExternalForm();
        }

    }

    /**
     * Checks if is one web driver. One web driver instantiates one web driver
     * that is used from beginning to end of a test's TestNG suite(s). If
     * {@code true} all of the TestNG Suite XML must have
     * {@code parallel="false"}.
     *
     * @return true, if is one web driver
     * @since 2.0
     * @version.coseng
     */
    protected boolean isOneWebDriver() {
        return oneWebDriver;
    }

    /**
     * Gets the web driver. If location {@code NODE} then the web driver must
     * exist and be executable.
     *
     * @return the web driver
     * @see com.sios.stc.coseng.run.Node#getChromeDriver()
     * @see com.sios.stc.coseng.run.Node#getGeckoDriver()
     * @see com.sios.stc.coseng.run.Node#getEdgeDriver()
     * @see com.sios.stc.coseng.run.Node#getIeDriver()
     * @since 2.0
     * @version.coseng
     */
    protected File getWebDriver() {
        return webDriver;
    }

    /**
     * Sets the web driver.
     *
     * @param webDriver
     *            the new web driver; may not be null
     * @since 2.0
     * @version.coseng
     */
    protected void setWebDriver(File webDriver) {
        if (webDriver != null) {
            this.webDriver = webDriver;
        }
    }

    /**
     * Gets the web driver timeout seconds.
     *
     * @return the web driver timeout seconds
     * @since 2.0
     * @version.coseng
     */
    protected Integer getWebDriverTimeoutSeconds() {
        return webDriverTimeoutSeconds;
    }

    /**
     * Gets the web driver wait timeout seconds.
     *
     * @return the web driver wait timeout seconds
     * @since 2.0
     * @version.coseng
     */
    protected Integer getWebDriverWaitTimeoutSeconds() {
        return webDriverWaitTimeoutSeconds;
    }

    /**
     * Gets the report directory. This will be a subdirectory under Node
     * {@code reportsDirectory}.
     *
     * @return the report directory
     * @see com.sios.stc.coseng.run.Node#getReportsDirectory()
     * @see com.sios.stc.coseng.run.Validate
     * @see com.sios.stc.coseng.run.Concurrent#run()
     * @since 2.0
     * @version.coseng
     */
    protected String getReportDirectory() {
        return reportDirectory;
    }

    /**
     * Sets the report directory.
     *
     * @param reportDirectory
     *            the new report directory; may not be null or empty
     * @see com.sios.stc.coseng.run.Node#getReportsDirectory()
     * @see com.sios.stc.coseng.run.Validate
     * @see com.sios.stc.coseng.run.Concurrent#run()
     * @since 2.0
     * @version.coseng
     */
    protected void setReportDirectory(String reportDirectory) {
        if (reportDirectory != null && !reportDirectory.isEmpty()) {
            this.reportDirectory = reportDirectory;
        }
    }

    /**
     * Gets the resource directory. This will be subdirectory under Node
     * {@code resourcesTempDirectory}.
     *
     * @return the report directory
     * @see com.sios.stc.coseng.run.Node#getResourcesTempDirectory()
     * @see com.sios.stc.coseng.run.Validate
     * @since 2.0
     * @version.coseng
     */
    protected File getResourceDirectory() {
        return resourceDirectory;
    }

    /**
     * Sets the resource directory.
     *
     * @param resourceDirectory
     *            the new resource directory; may not be null or not a directory
     * @see com.sios.stc.coseng.run.Node#getResourcesTempDirectory()
     * @see com.sios.stc.coseng.run.Validate
     * @since 2.0
     * @version.coseng
     */
    protected void setResourceDirectory(File resourceDirectory) {
        if (resourceDirectory != null && resourceDirectory.isDirectory()) {
            this.resourceDirectory = resourceDirectory;
        }
    }

    /**
     * Gets the report directory file. This is a convenience method.
     *
     * @return the report directory file
     * @since 2.0
     * @version.coseng
     */
    protected File getReportDirectoryFile() {
        return new File(reportDirectory);
    }

    /**
     * Gets the collection of TestNG XML suites.
     *
     * @return the xml suites
     * @since 2.0
     * @version.coseng
     */
    protected List<XmlSuite> getXmlSuites() {
        return xmlSuites;
    }

    /**
     * Sets the collection of TestNG XML suites.
     *
     * @param xmlSuites
     *            the new xml suites; may not be null
     * @since 2.0
     * @version.coseng
     */
    protected void setXmlSuites(List<XmlSuite> xmlSuites) {
        if (xmlSuites != null) {
            this.xmlSuites = xmlSuites;
        }
    }

    /**
     * Checks if this test is synthetic.
     *
     * @return true, if is synthetic
     * @since 2.0
     * @version.coseng
     */
    protected boolean isSynthetic() {
        return synthetic;
    }

    /**
     * Sets the checks if this test is synthetic. Synthetic is set {@code true}
     * when explicit tests are generated from a broadly defined test. eg. If
     * test {@code browser=any} and {@code platform=linux} then two explicit
     * synthetic tests will be generated; one for {@code browser=chrome} and
     * another for {@code browser=firefox}.
     *
     * @param synthetic
     *            the new checks if is synthetic
     * @since 2.0
     * @version.coseng
     */
    protected void setIsSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
    }

    /**
     * Checks if this test is failed.
     *
     * @return true, if is failed
     * @since 2.0
     * @version.coseng
     */
    protected boolean isFailed() {
        return failed;
    }

    /**
     * Sets the checks if is failed.
     *
     * @param failed
     *            the new checks if is failed
     * @since 2.0
     * @version.coseng
     */
    protected void setIsFailed(boolean failed) {
        this.failed = failed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "name [" + name + "], location [" + location + "], baseUrl [" + baseUrl
                + "], gridUrl [" + gridUrl + "], platform [" + platform + "], suites " + suites
                + ", browser [" + browser + "], browserVersion [" + browserVersion + "] incognito ["
                + incognito + "], acceptInvalidCerts [" + acceptInvalidCerts + "], oneWebDriver ["
                + oneWebDriver + "], verbosity [" + verbosity + "], webDriver [" + webDriver
                + "], webDriverTimeoutSeconds [" + webDriverTimeoutSeconds
                + "], webDriverWaitTimeoutSeconds [" + webDriverWaitTimeoutSeconds
                + "], reportDirectory [" + reportDirectory + "]";
    }

}
