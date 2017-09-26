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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Platform;
import org.testng.IInvokedMethod;
import org.testng.ISuite;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.xml.XmlSuite;

import com.google.gson.annotations.Expose;
import com.sios.stc.coseng.integration.Data;
import com.sios.stc.coseng.integration.IntegratorData;
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
 *   "browserRequestVersion": "11.0",
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
 * <dd>browserRequestVersion:
 * {@value com.sios.stc.coseng.run.Browsers#BROWSER_VERSION_DEFAULT}</dd>
 * <dd>browserHeadless: false</dd>
 * <dd>incognito: false (private/incognito for any supported browser)</dd>
 * <dd>acceptInvalidCerts: false</dd>
 * <dd>angular2App: false</dd>
 * <dd>allowFindUrl: false</dd>
 * <dd>allowScreenshot: false</dd>
 * <dd>browserWidth: null</dd>
 * <dd>browserHeight: null</dd>
 * <dd>browserMaximize: false</dd>
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
public class Test implements IntegratorData {

    private static final int WEB_DRIVER_WAIT_TIMEOUT_SECONDS_DEFAULT     = 5;
    private static final int WEB_DRIVER_IMPLICIT_TIMEOUT_SECONDS_DEFAULT = 5;

    private boolean        incognitoDefault          = false;
    private boolean        acceptInvalidCertsDefault = false;
    private boolean        oneWebDriverDefault       = false;
    private boolean        angular2AppDefault        = false;
    private boolean        allowFindUrlsDefault      = false;
    private boolean        allowScreenshotsDefault   = false;
    private boolean        browserMaximizeDefault    = false;
    private List<XmlSuite> xmlSuites                 = new ArrayList<XmlSuite>();
    private String         reportDirectory           = null;
    private File           resourceDirectory         = null;
    private boolean        failed                    = false;
    private boolean        synthetic                 = false;
    private File           webDriver                 = null;
    private ISuite         testNgSuite               = null;
    private ITestContext   testNgTest                = null;
    private ITestClass     testNgClass               = null;
    private IInvokedMethod testNgMethod              = null;
    private int            testSuiteCount            = 0;
    private List<Data>     integratorData            = new ArrayList<Data>();

    @Expose
    private String       name                        = null;
    @Expose
    private Location     location                    = Location.NODE;
    @Expose
    private Platform     platform                    = Platform.ANY;
    @Expose
    private Browser      browser                     = Browser.ALL;
    @Expose
    private List<String> suites                      = new ArrayList<String>();
    @Expose
    private String       baseUrl                     = null;
    @Expose
    private String       gridUrl                     = null;
    @Expose
    private boolean      oneWebDriver                = oneWebDriverDefault;
    @Expose
    private Integer      webDriverTimeoutSeconds     = WEB_DRIVER_WAIT_TIMEOUT_SECONDS_DEFAULT;
    @Expose
    private Integer      webDriverWaitTimeoutSeconds = WEB_DRIVER_IMPLICIT_TIMEOUT_SECONDS_DEFAULT;
    @Expose
    private String       browserRequestVersion       = Browsers.BROWSER_VERSION_DEFAULT;
    @Expose
    private boolean      browserHeadless             = false;
    @Expose
    private boolean      incognito                   = incognitoDefault;
    @Expose
    private boolean      acceptInvalidCerts          = acceptInvalidCertsDefault;
    @Expose
    private boolean      angular2App                 = angular2AppDefault;
    @Expose
    private boolean      allowFindUrls               = allowFindUrlsDefault;
    @Expose
    private boolean      allowScreenshots            = allowScreenshotsDefault;
    @Expose
    private Integer      browserWidth                = null;
    @Expose
    private Integer      browserHeight               = null;
    @Expose
    private boolean      browserMaximize             = browserMaximizeDefault;
    @Expose
    private Integer      verbosity                   = 0;

    protected Test deepCopy() {
        return new Test(this);
    }

    protected Test() {
        // do nothing
    }

    protected Test(Test original) {
        this.incognitoDefault = original.incognitoDefault;
        this.acceptInvalidCertsDefault = original.acceptInvalidCertsDefault;
        this.oneWebDriverDefault = original.oneWebDriverDefault;
        this.angular2AppDefault = original.angular2AppDefault;
        this.allowFindUrlsDefault = original.allowFindUrlsDefault;
        this.allowScreenshotsDefault = original.allowScreenshotsDefault;
        this.browserMaximizeDefault = original.browserMaximizeDefault;
        for (XmlSuite xml : original.xmlSuites) {
            this.xmlSuites.add((XmlSuite) xml.clone());
        }
        this.xmlSuites.addAll(original.xmlSuites);
        this.reportDirectory = original.reportDirectory;
        this.resourceDirectory = original.resourceDirectory;
        this.failed = original.failed;
        this.synthetic = original.synthetic;
        this.webDriver = original.webDriver;
        this.testNgSuite = original.testNgSuite;
        this.testNgTest = original.testNgTest;
        this.testNgClass = original.testNgClass;
        this.testNgMethod = original.testNgMethod;
        this.testSuiteCount = original.testSuiteCount;
        for (Data data : original.integratorData) {
            this.integratorData.add(data.deepCopy());
        }
        this.browserRequestVersion = original.browserRequestVersion;
        this.name = original.name;
        this.location = original.location;
        this.platform = original.platform;
        this.browser = original.browser;
        this.browserHeadless = original.browserHeadless;
        this.suites.addAll(original.suites);
        this.baseUrl = original.baseUrl;
        this.gridUrl = original.gridUrl;
        this.oneWebDriver = original.oneWebDriver;
        this.webDriverTimeoutSeconds = original.webDriverTimeoutSeconds;
        this.webDriverWaitTimeoutSeconds = original.webDriverWaitTimeoutSeconds;
        this.incognito = original.incognito;
        this.acceptInvalidCerts = original.acceptInvalidCerts;
        this.angular2App = original.angular2App;
        this.allowFindUrls = original.allowFindUrls;
        this.allowScreenshots = original.allowScreenshots;
        this.browserWidth = original.browserWidth;
        this.browserHeight = original.browserHeight;
        this.browserMaximize = original.browserMaximize;
        this.verbosity = original.verbosity;
    }

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
     * Gets the browser requested version.
     *
     * @return the browser requested version
     * @since 3.0
     * @version.coseng
     */
    public String getBrowserRequestVersion() {
        return browserRequestVersion;
    }

    /**
     * Gets the browser headless.
     *
     * @return the browser headless
     * @since 3.0
     * @version.coseng
     */
    public boolean getBrowserHeadless() {
        return browserHeadless;
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

    /**
     * Gets the test ng suite.
     *
     * @return the test ng suite
     * @see com.sios.stc.coseng.run.CosengListener#onStart(org.testng.ISuite)
     * @since 2.1
     * @version.coseng
     */
    public ISuite getTestNgSuite() {
        return this.testNgSuite;
    }

    /**
     * Sets the test ng suite.
     *
     * @param suite
     *            the new test ng suite
     * @see com.sios.stc.coseng.run.CosengListener#onStart(org.testng.ISuite)
     * @since 2.1
     * @version.coseng
     */
    protected void setTestNgSuite(ISuite suite) {
        if (suite != null) {
            this.testNgSuite = suite;
        }
    }

    /**
     * Gets the test ng test.
     *
     * @return the test ng test
     * @see com.sios.stc.coseng.run.CosengListener#onStart(org.testng.ITestContext)
     * @since 2.1
     * @version.coseng
     */
    public ITestContext getTestNgTest() {
        return this.testNgTest;
    }

    /**
     * Sets the test ng test.
     *
     * @param test
     *            the new test ng test
     * @see com.sios.stc.coseng.run.CosengListener#onStart(org.testng.ITestContext)
     * @since 2.1
     * @version.coseng
     */
    protected void setTestNgTest(ITestContext test) {
        if (test != null) {
            this.testNgTest = test;
        }
    }

    /**
     * Gets the test ng class.
     *
     * @return the test ng class
     * @see com.sios.stc.coseng.run.CosengListener#onBeforeClass(org.testng.ITestClass)
     * @since 2.1
     * @version.coseng
     */
    public ITestClass getTestNgClass() {
        return this.testNgClass;
    }

    /**
     * Sets the test ng class.
     *
     * @param clazz
     *            the new test ng class
     * @see com.sios.stc.coseng.run.CosengListener#onBeforeClass(org.testng.ITestClass)
     * @since 2.1
     * @version.coseng
     */
    protected void setTestNgClass(ITestClass clazz) {
        if (clazz != null) {
            this.testNgClass = clazz;
        }
    }

    /**
     * Gets the test ng method.
     *
     * @return the test ng method
     * @see com.sios.stc.coseng.run.CosengListener#beforeInvocation(org.testng.IInvokedMethod,
     *      org.testng.ITestResult)
     * @since 2.1
     * @version.coseng
     */
    public IInvokedMethod getTestNgMethod() {
        return this.testNgMethod;
    }

    /**
     * Sets the test ng method.
     *
     * @param method
     *            the new test ng method
     * @see com.sios.stc.coseng.run.CosengListener#beforeInvocation(org.testng.IInvokedMethod,
     *      org.testng.ITestResult)
     * @since 2.1
     * @version.coseng
     */
    protected void setTestNgMethod(IInvokedMethod method) {
        if (method != null) {
            this.testNgMethod = method;
        }
    }

    /**
     * Checks if is angular app.
     *
     * @return true, if is angular app
     * @since 2.1
     * @version.coseng
     */
    public boolean isAngular2App() {
        return angular2App;
    }

    /**
     * Checks if is allow find urls.
     *
     * @return true, if is allow find urls
     * @since 2.1
     * @version.coseng
     */
    public boolean isAllowFindUrls() {
        return allowFindUrls;
    }

    /**
     * Checks if is allow screenshots.
     *
     * @return true, if is allow screenshots
     * @since 2.1
     * @version.coseng
     */
    public boolean isAllowScreenshots() {
        return allowScreenshots;
    }

    /**
     * Gets the test suite count. Differs from getXmlSuite().size() in that it
     * counts all recursively occurring <suite-file> containing <test>.
     *
     * @return the suite count
     * @since 2.1
     * @version.coseng
     */
    protected int getTestSuiteCount() {
        return testSuiteCount;
    }

    /**
     * Sets the test suite count.
     *
     * @param suiteCount
     *            the new suite count
     * @since 2.1
     * @version.coseng
     */
    protected void setTestSuiteCount(int suiteCount) {
        this.testSuiteCount = suiteCount;
    }

    /**
     * Gets the browser width.
     *
     * @return the browser width
     * @since 2.1
     * @version.coseng
     */
    protected Integer getBrowserWidth() {
        return browserWidth;
    }

    /**
     * Gets the browser height.
     *
     * @return the browser height
     * @since 2.1
     * @version.coseng
     */
    protected Integer getBrowserHeight() {
        return browserHeight;
    }

    /**
     * Gets the browser dimension.
     *
     * @return the browser dimension
     * @since 2.1
     * @version.coseng
     */
    public Dimension getBrowserDimension() {
        Dimension browserDimension = null;
        /* browserMaximize overrides width and height */
        if (!browserMaximize && browserWidth != null && browserHeight != null && browserWidth > 0
                && browserHeight > 0) {
            browserDimension = new Dimension(browserWidth, browserHeight);
        }
        return browserDimension;
    }

    /**
     * Gets the browser maximize.
     *
     * @return the browser maximize
     * @since 2.1
     * @version.coseng
     */
    protected boolean getBrowserMaximize() {
        return browserMaximize;
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
                + ", browser [" + browser + "], browserRequestVersion [" + browserRequestVersion
                + "], browserHeadless [" + browserHeadless + "], incognito [" + incognito
                + "], acceptInvalidCerts [" + acceptInvalidCerts + "], angular2App [" + angular2App
                + "], allowFindUrls [" + allowFindUrls + "], allowScreenshots [" + allowScreenshots
                + "], browserWidth [" + browserWidth + "], browserHeight [" + browserHeight
                + "], browserMaximize [" + browserMaximize + "], oneWebDriver [" + oneWebDriver
                + "], verbosity [" + verbosity + "], webDriverTimeoutSeconds ["
                + webDriverTimeoutSeconds + "], webDriverWaitTimeoutSeconds ["
                + webDriverWaitTimeoutSeconds + "], reportDirectory [" + reportDirectory + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sios.stc.coseng.integration.IntegratorData#getIntegratorData(java.
     * lang.Class)
     */
    @Override
    public Data getIntegratorData(Class<?> dataClass) {
        if (dataClass != null) {
            Iterator<Data> iterator = integratorData.iterator();
            while (iterator.hasNext()) {
                Data data = iterator.next();
                if (dataClass.equals(data.getClass())) {
                    return data;
                }
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sios.stc.coseng.integration.IntegratorData#hasIntegratorData(java.
     * lang.Class)
     */
    @Override
    public boolean hasIntegratorData(Class<?> dataClass) {
        if (getIntegratorData(dataClass) != null) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sios.stc.coseng.integration.IntegratorData#addIntegratorData(com.sios
     * .stc.coseng.integration.Data)
     */
    @Override
    public boolean addIntegratorData(Data data) {
        if (data != null) {
            /* Only add unique integrator data classes */
            if (!hasIntegratorData(data.getClass())) {
                return integratorData.add(data);
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sios.stc.coseng.integration.IntegratorData#removeIntegratorData(java.
     * lang.Class)
     */
    @Override
    public boolean removeIntegratorData(Class<?> dataClass) {
        Data data = getIntegratorData(dataClass);
        if (data != null) {
            return integratorData.remove(data);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sios.stc.coseng.integration.IntegratorData#clearIntegratorData()
     */
    @Override
    public void clearIntegratorData() {
        integratorData.clear();
    }

}
