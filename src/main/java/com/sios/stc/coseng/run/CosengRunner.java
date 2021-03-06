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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;
import org.testng.asserts.Assertion;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;

import com.paulhammant.ngwebdriver.NgWebDriver;
import com.sios.stc.coseng.Common;
import com.sios.stc.coseng.RunTests;
import com.sios.stc.coseng.integration.Integrator;
import com.sios.stc.coseng.run.Browsers.Browser;
import com.sios.stc.coseng.run.Matcher.MatchBy;
import com.sios.stc.coseng.util.Http;
import com.sios.stc.coseng.util.Resource;

/**
 * The Class CosengRunner. This is the class that each TestNG class under test
 * must extend to access the appropriate web driver at the requested depth of
 * parallelism. This class provides convenience methods to access the available
 * web driver and its derived objects such as Actions and JavascriptExecutor as
 * well as manage Selenium WebElements as objects for the level of parallelism.
 * <b>Note:</b> Call these methods from within test class methods.
 * <b>Caution:</b> Unless you manage the test context, calling these methods
 * outside of the TestNG test class methods will have unintended consequences.
 *
 * @since 2.0
 * @version.coseng
 */
public class CosengRunner {

    protected LogAssert logAssert = new LogAssert();

    private static final Logger                         log                    =
            LogManager.getLogger(RunTests.class.getName());
    private static final String                         DIR_SCREENSHOTS        =
            "coseng-screenshots";
    private static final String                         DIR_ADDITIONAL_REPORTS = "coseng-reports";
    private static int                                  startedWebDriver       = 0;
    private static int                                  stoppedWebDriver       = 0;
    private static Map<Thread, Test>                    threadTest             =
            new HashMap<Thread, Test>();
    private static Map<Thread, WebDriver>               threadWebDriver        =
            new HashMap<Thread, WebDriver>();
    private static Map<Thread, Object>                  threadWebDriverService =
            new HashMap<Thread, Object>();
    private static Map<Thread, WebDriverWait>           threadWebDriverWait    =
            new HashMap<Thread, WebDriverWait>();
    private static Map<Thread, Actions>                 threadActions          =
            new HashMap<Thread, Actions>();
    private static Map<Thread, JavascriptExecutor>      threadJsExecutor       =
            new HashMap<Thread, JavascriptExecutor>();
    private static Map<Thread, NgWebDriver>             threadNgWebDriver      =
            new HashMap<Thread, NgWebDriver>();
    private static final org.apache.logging.log4j.Level defaultLogLevel        =
            org.apache.logging.log4j.Level.INFO;

    /* Global collection of found URLs */
    private static Map<String, String>      allUrlsTag    = new HashMap<String, String>();
    private static Map<String, Set<String>> allUrlsRoutes = new HashMap<String, Set<String>>();
    /*
     * Non static so each extended instance is self-contained for
     * finding/saving/checking URLs
     */
    private Map<String, String>      urlsTag    = new HashMap<String, String>();
    private Map<String, Set<String>> urlsRoutes = new HashMap<String, Set<String>>();

    private static int assertFailureCount = 0;

    /**
     * Sets the selenium tools.
     *
     * @param webDriver
     *            the web driver
     * @param webDriverService
     *            the web driver service
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.WebDriverLifecycle#startWebDriver(Test)
     * @since 2.1
     * @version.coseng
     */
    protected static synchronized void setSeleniumTools(WebDriver webDriver,
            Object webDriverService) throws CosengException {
        Thread thread = Thread.currentThread();
        Test test = getThreadTest(thread);
        if (test != null && webDriver != null) {
            ((RemoteWebDriver) webDriver).setLogLevel(Level.FINE);
            webDriver.manage().timeouts().implicitlyWait(test.getWebDriverTimeoutSeconds(),
                    TimeUnit.SECONDS);
            webDriver.manage().timeouts().pageLoadTimeout(test.getWebDriverTimeoutSeconds(),
                    TimeUnit.SECONDS);
            webDriver.manage().timeouts().setScriptTimeout(test.getWebDriverTimeoutSeconds(),
                    TimeUnit.SECONDS);
        } else {
            throw new CosengException("Error creating selenium tools");
        }
        threadWebDriver.put(thread, webDriver);
        threadWebDriverService.put(thread, webDriverService);
        threadWebDriverWait.put(thread,
                new WebDriverWait(webDriver, test.getWebDriverWaitTimeoutSeconds()));
        threadActions.put(thread, new Actions(webDriver));
        threadJsExecutor.put(thread, (JavascriptExecutor) webDriver);
        threadNgWebDriver.put(thread, new NgWebDriver((JavascriptExecutor) webDriver));
    }

    /**
     * Instantiates a new coseng runner.
     * 
     * @since 2.0
     * @version.coseng
     */
    protected CosengRunner() {
        /*
         * Do nothing; avoids having to declare a default constructor for the
         * TestNG test classes which would be required if instantiating this
         * class as it throws CosengExceptions. Reason why each test class
         * extends this class.
         */
    }

    /**
     * Gets the test associated for a given thread.
     *
     * @param thread
     *            the thread
     * @return the thread test
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized Test getThreadTest(Thread thread) {
        if (threadTest.containsKey(thread)) {
            return threadTest.get(thread);
        }
        return null;
    }

    /**
     * Sets the thread test association.
     *
     * @param thread
     *            the thread
     * @param test
     *            the test
     * @since 2.1
     * @version.coseng
     */
    protected static synchronized void addThreadTest(Thread thread, Test test) {
        if (thread != null && test != null) {
            threadTest.put(thread, test);
        }
    }

    /**
     * Gets the test.
     *
     * @return the test
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized Test getTest() {
        return getTest(null);
    }

    /**
     * Gets the test.
     *
     * @param thread
     *            the thread
     * @return the test
     * @since 2.1
     * @version.coseng
     */
    protected static synchronized Test getTest(Thread thread) {
        if (thread == null) {
            thread = Thread.currentThread();
        }
        return getThreadTest(thread);
    }

    /**
     * Checks for web driver.
     *
     * @return true, if successful
     * @see com.sios.stc.coseng.run.CosengRunner#hasWebDriver(Thread)
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized boolean hasWebDriver() {
        return hasWebDriver(null);
    }

    /**
     * Checks for web driver.
     *
     * @param thread
     *            the thread
     * @return true, if successful
     * @since 2.1
     * @version.coseng
     */
    protected static synchronized boolean hasWebDriver(Thread thread) {
        if (thread == null) {
            thread = Thread.currentThread();
        }
        if (threadWebDriver.containsKey(thread) && threadWebDriver.get(thread) != null) {
            return true;
        }
        return false;
    }

    /**
     * Gets the started web driver.
     *
     * @return the web driver
     * @see com.sios.stc.coseng.run.CosengRunner#getWebDriver(Thread)
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized WebDriver getWebDriver() {
        return getWebDriver(null);
    }

    /**
     * Gets the started web driver.
     *
     * @param thread
     *            the thread
     * @return the web driver
     * @since 2.1
     * @version.coseng
     */
    protected static synchronized WebDriver getWebDriver(Thread thread) {
        if (thread == null) {
            thread = Thread.currentThread();
        }
        return threadWebDriver.get(thread);
    }

    /**
     * Gets the browser version.
     *
     * @return the browser version
     * @since 3.0
     * @version.coseng
     */
    protected static synchronized String getBrowserVersion() {
        WebDriver wd = getWebDriver();
        Capabilities caps = ((RemoteWebDriver) wd).getCapabilities();
        return caps.getVersion();
    }

    /**
     * Gets the started web driver.
     *
     * @return the web driver
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized Object getWebDriverService() {
        return getWebDriverService(null);
    }

    /**
     * Gets the started web driver service.
     *
     * @param thread
     *            the thread
     * @return the web driver service
     * @since 2.1
     * @version.coseng
     */
    protected static synchronized Object getWebDriverService(Thread thread) {
        if (thread == null) {
            thread = Thread.currentThread();
        }
        return threadWebDriverService.get(thread);
    }

    /**
     * Gets the web driver wait.
     *
     * @return the web driver wait
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized WebDriverWait getWebDriverWait() {
        Thread thread = Thread.currentThread();
        return threadWebDriverWait.get(thread);
    }

    /**
     * Gets the actions.
     *
     * @return the actions
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized Actions getActions() {
        Thread thread = Thread.currentThread();
        return threadActions.get(thread);
    }

    /**
     * Gets the javascript executor.
     *
     * @return the javascript executor
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized JavascriptExecutor getJavascriptExecutor() {
        Thread thread = Thread.currentThread();
        return threadJsExecutor.get(thread);
    }

    /**
     * Gets the ng web driver.
     *
     * @return the ng web driver
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized NgWebDriver getNgWebDriver() {
        Thread thread = Thread.currentThread();
        return threadNgWebDriver.get(thread);
    }

    /**
     * Increment started web driver count.
     *
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized void incrementStartedWebDriverCount() {
        startedWebDriver++;
    }

    /**
     * Gets the started web driver count.
     *
     * @return the started web driver count
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized int getStartedWebDriverCount() {
        return startedWebDriver;
    }

    /**
     * Increment stopped web driver count.
     *
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized void incrementStoppedWebDriverCount() {
        stoppedWebDriver++;
    }

    /**
     * Gets the stopped web driver count.
     *
     * @return the stopped web driver count
     * @since 2.0
     * @version.coseng
     */
    protected static synchronized int getStoppedWebDriverCount() {
        return stoppedWebDriver;
    }

    /* BEGIN - Convenience methods */

    /**
     * New web element.
     *
     * @return the web element
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.WebElement
     * @see com.sios.stc.coseng.run.CosengRunner#newWebElement(By)
     * @since 2.1
     * @version.coseng
     */
    protected static com.sios.stc.coseng.run.WebElement newWebElement() throws CosengException {
        return newWebElement(null);
    }

    /**
     * New web element.
     *
     * @param by
     *            the by
     * @return the web element
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.WebElement
     * @see org.openqa.selenium.By
     * @since 2.1
     * @version.coseng
     */
    protected static com.sios.stc.coseng.run.WebElement newWebElement(By by)
            throws CosengException {
        return new com.sios.stc.coseng.run.WebElement(by);
    }

    /**
     * New web elements.
     *
     * @return the web elements
     * @throws CosengException
     *             the coseng exception
     * @since 2.2
     * @version.coseng
     */
    protected static WebElements newWebElements() throws CosengException {
        return newWebElements(null);
    }

    /**
     * New web elements.
     *
     * @param by
     *            the by
     * @return the web elements
     * @throws CosengException
     *             the coseng exception
     * @since 2.2
     * @version.coseng
     */
    protected static WebElements newWebElements(By by) throws CosengException {
        return new WebElements(by);
    }

    /**
     * Web driver get.
     *
     * @param url
     *            the url
     * @see org.openqa.selenium.WebDriver#get(String)
     * @since 2.1
     * @version.coseng
     */
    protected void webDriverGet(String url) {
        WebDriver webDriver = getWebDriver();
        if (webDriver != null) {
            webDriver.get(url);
        }
    }

    /**
     * Web driver navigate to.
     *
     * @param url
     *            the url
     * @throws CosengException
     *             the coseng exception
     * @see com.sios.stc.coseng.run.CosengRunner#webDriverNavigateTo(URL)
     * @since 2.1
     * @version.coseng
     */
    protected void webDriverNavigateTo(String url) throws CosengException {
        try {
            URL urlObj = new URL(url);
            webDriverNavigateTo(urlObj);
        } catch (MalformedURLException e) {
            throw new CosengException("MalformedURLException", e);
        }
    }

    /**
     * Web driver navigate to.
     *
     * @param url
     *            the url
     * @see org.openqa.selenium.WebDriver#navigate()
     * @since 2.1
     * @version.coseng
     */
    protected void webDriverNavigateTo(URL url) {
        WebDriver webDriver = getWebDriver();
        if (webDriver != null) {
            webDriver.navigate().to(url);
        }
    }

    /**
     * Gets the current url. Adjusts for Angular2 apps.
     *
     * @return the current url
     * @see org.openqa.selenium.WebDriver#getCurrentUrl()
     * @see com.paulhammant.ngwebdriver.NgWebDriver#getLocationAbsUrl()
     * @since 2.1
     * @version.coseng
     */
    protected static String getCurrentUrl() {
        Test test = getTest();
        WebDriver webDriver = getWebDriver();
        NgWebDriver ngWebDriver = getNgWebDriver();
        if (test != null && webDriver != null && ngWebDriver != null) {
            if (test.isAngular2App()) {
                try {
                    /*
                     * Web driver outpaces (both node or grid); nor does using
                     * ngWebDriver.waitForAngular2RequestsToFinish()
                     */
                    ngWebDriver.waitForAngular2RequestsToFinish();
                    pause(350l);
                    return ngWebDriver.getLocationAbsUrl();
                } catch (Exception e) {
                    /*
                     * May be at about:blank or on non-angular page; best effort
                     */
                    return webDriver.getCurrentUrl();
                }
            } else {
                return webDriver.getCurrentUrl();
            }
        }
        return null;
    }

    /**
     * Current url contains.
     *
     * @param route
     *            the route
     * @return true, if successful
     * @see #currentUrlMatchBy(String, MatchBy)
     * @since 2.1
     * @version.coseng
     */
    protected static boolean currentUrlContains(String route) {
        return currentUrlMatchBy(route, MatchBy.CONTAIN);
    }

    /**
     * Current url equals.
     *
     * @param route
     *            the route
     * @return true, if successful
     * @see #currentUrlMatchBy(String, MatchBy)
     * @since 2.1
     * @version.coseng
     */
    protected static boolean currentUrlEquals(String route) {
        return currentUrlMatchBy(route, MatchBy.EQUAL);
    }

    /**
     * Current url match by.
     *
     * @param route
     *            the route
     * @param matchBy
     *            the match by
     * @return true, if successful
     * @see #currentUrlContains(String)
     * @see #currentUrlEquals(String)
     * @since 2.1
     * @version.coseng
     */
    private static boolean currentUrlMatchBy(String route, MatchBy matchBy) {
        boolean matched = false;
        String currentUrl = getCurrentUrl();
        if (currentUrl != null) {
            if (MatchBy.CONTAIN.equals(matchBy)) {
                if (currentUrl.contains(route)) {
                    matched = true;
                }
            } else {
                // default matcher
                if (currentUrl.equals(route)) {
                    matched = true;
                }
            }
        }
        return matched;
    }

    /**
     * Accept invalid SSL certificate. Uses web driver to click through
     * accepting invalid certs. Note: Only available for Microsoft Internet
     * Explorer (IE) and Edge.
     *
     * @since 2.1
     * @version.coseng
     */
    protected static void acceptInvalidSSLCertificate() {
        /*
         * To accept self-signed or other SSL Certificates Should try with
         * browser profile; this as last resort.
         */
        /*
         * 2016-09-01 Doesn't work with FF 48. Can't do gimick as with IE since
         * geckodriver will bomb before getting chance to 'drive' thru manually
         * accepting the invalid certs. (Awaiting upstream geckodriver fix).
         * Till then import cert into profile or add to browser.
         */
        Test test = getTest();
        WebDriver webDriver = getWebDriver();
        Actions actions = getActions();
        if (test != null && webDriver != null && actions != null) {
            Browser browser = test.getBrowser();
            if (Browser.IE.equals(browser)) {
                boolean title =
                        webDriver.getTitle().equals("Certificate Error: Navigation Blocked");
                int count = webDriver.findElements(By.id("overridelink")).size();
                if (title && (count == 1)) {
                    WebElement overrideLink = webDriver.findElement(By.id("overridelink"));
                    if (overrideLink.isDisplayed()) {
                        actions.moveToElement(overrideLink).click().build().perform();
                    }
                }
            } else if (Browser.EDGE.equals(browser)) {
                boolean title =
                        webDriver.getTitle().equals("Certificate error: Navigation blocked");
                int count = webDriver.findElements(By.id("continueLink")).size();
                if (title && (count == 1)) {
                    WebElement overrideLink = webDriver.findElement(By.id("continueLink"));
                    if (overrideLink.isDisplayed()) {
                        actions.moveToElement(overrideLink).click().build().perform();
                    }
                }
            }
        }
    }

    /**
     * Save screenshot.
     *
     * @see com.sios.stc.coseng.run.CosengRunner#saveScreenshot(String)
     * @since 2.1
     * @version.coseng
     */
    protected static void saveScreenshot() {
        saveScreenshot(null);
    }

    /**
     * Save screenshot. Without a name the screenshot will be saved as
     * YYYMMddHHmmss.png. Best effort. Will warn if unable to save screenshot.
     *
     * @param name
     *            the name; may not be null or empty
     * @since 2.1
     * @version.coseng
     */
    protected static void saveScreenshot(String name) {
        Test test = getTest();
        WebDriver webDriver = getWebDriver();
        if (test != null && webDriver != null) {
            if (test.isAllowScreenshots()) {
                String logHeader = getLogHeader();
                try {
                    ArrayList<String> dirPaths = new ArrayList<String>();
                    dirPaths.add(test.getReportDirectoryFile().getAbsolutePath());
                    dirPaths.add(DIR_SCREENSHOTS);
                    dirPaths.add(test.getTestNgSuite().getName());
                    dirPaths.add(test.getTestNgTest().getName());
                    dirPaths.add(test.getTestNgClass().getName());
                    dirPaths.add(test.getTestNgMethod().getTestMethod().getMethodName());
                    String dirPath = StringUtils.join(dirPaths, File.separator);
                    if (name == null || name.isEmpty()) {
                        DateFormat dateFormat = new SimpleDateFormat("YYYYMMddHHmmss");
                        Calendar cal = Calendar.getInstance();
                        name = dateFormat.format(cal.getTime());
                    }
                    /* Make directory in report directory */
                    File dir = new File(dirPath);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    /* Save the screenshot */
                    File screenshotIn =
                            ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
                    File screenshotOut = new File(dirPath + File.separator + name + ".png");
                    FileUtils.copyFile(screenshotIn, screenshotOut);
                    log.info("{} Save screenshot [{}] successful", logHeader, name);
                } catch (Exception e) {
                    log.warn("{} Save screenshot [{}] unsuccessful: {}", logHeader, name,
                            e.getMessage());
                }
            }
        }
    }

    /**
     * Upload file.
     *
     * @param uploadElement
     *            the upload element; must exist, be displayed and not have
     *            "readonly" attribute
     * @param fileName
     *            the file name must not be null or empty
     * @throws CosengException
     * @see com.sios.stc.coseng.util.Resource#get(String)
     * @see com.sios.stc.coseng.util.Resource#create(InputStream, File)
     * @see com.sios.stc.coseng.run.WebDriverLifecycle#startWebDriver(Test)
     * @since 2.1
     * @version.coseng
     */
    protected static void uploadFile(WebElement uploadElement, String fileName)
            throws CosengException {
        Test test = getTest();
        if (test != null && fileName != null && !fileName.isEmpty() && uploadElement != null
                && uploadElement.isDisplayed() && uploadElement.getAttribute("readonly") == null) {
            try {
                InputStream fileInput = Resource.getStream(fileName);
                File resourceDir = test.getResourceDirectory();
                File resource = new File(resourceDir + File.separator + fileName);
                Resource.create(fileInput, resource);
                String resourcePath = resource.getAbsolutePath();
                /*
                 * NOTE! ((RemoteWebDriver) webDriver).setFileDetector(new
                 * LocalFileDetector()); in
                 * WebDriverLifecycle.startWebDriver(Test)
                 * 
                 * If set here instead of earlier when starting the web driver
                 * you may get exceptions that the "path is not absolute".
                 */
                uploadElement.sendKeys(resourcePath);
            } catch (CosengException e) {
                throw new CosengException("Unable to upload file [{}]; assure file exists", e);
            }
        } else {
            throw new CosengException("Unable to upload file [" + fileName
                    + "]; fileName may not be empty, uploadElement must exist, be displayed and must not have 'readonly' attribute");
        }
    }

    /**
     * Pause.
     *
     * @see com.sios.stc.coseng.run.CosengRunner#pause(Long)
     * @since 2.1
     * @version.coseng
     */
    protected static void pause() {
        pause(null);
    }

    /**
     * Pause. Default 1000 milliseconds.
     *
     * @param milliseconds
     *            the milliseconds
     * @since 2.1
     * @version.coseng
     */
    protected static void pause(Long milliseconds) {
        Long millis = new Long(1000);
        if (milliseconds != null && milliseconds >= 0) {
            millis = milliseconds;
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        do {
            // cpu burner
        } while (stopWatch.getTime() < millis);
    }

    /**
     * Find urls. Collects current URL Xpath 'href' and 'src' URLs. Adds found
     * URL to 'all' found URLs. Waits for Angular2 apps to finish requests.
     * Derived code.
     * 
     * @link http://stackoverflow.com/questions/28163618/fetching-all-href-links-from-the-page-source-using-webdriver
     * @link http://stackoverflow.com/users/2897008/vins
     * @link https://opensource.org/licenses/MIT
     * @author Vins
     * @author James B. Crocker
     * 
     * @see com.sios.stc.coseng.run.CosengRunner#getUrls()
     * @see com.sios.stc.coseng.run.CosengRunner#getUrlTag(String)
     * @see com.sios.stc.coseng.run.CosengRunner#getUrlRoutes(String)
     * @see com.sios.stc.coseng.run.CosengRunner#getAllUrls()
     * @see com.sios.stc.coseng.run.CosengRunner#getAllUrlTag(String)
     * @see com.sios.stc.coseng.run.CosengRunner#getAllUrlRoutes(String)
     * @see com.sios.stc.coseng.run.CosengRunner#saveUrls()
     * @see com.sios.stc.coseng.run.CosengRunner#saveAllUrls()
     * @since 2.1
     * @version.coseng
     */
    protected synchronized void findUrls() {
        Test test = getTest();
        WebDriver webDriver = getWebDriver();
        NgWebDriver ngWebDriver = getNgWebDriver();
        if (test != null && test.isAllowFindUrls() && webDriver != null && ngWebDriver != null) {
            if (test.isAngular2App()) {
                ngWebDriver.waitForAngular2RequestsToFinish();
            }
            List<org.openqa.selenium.WebElement> urlList =
                    webDriver.findElements(By.xpath("//*[@href or @src]"));
            for (org.openqa.selenium.WebElement webElement : urlList) {
                String url = webElement.getAttribute("href");
                if (url == null) {
                    url = webElement.getAttribute("src");
                }
                if (url != null && !url.isEmpty()) {
                    /* Record url and tag */
                    String tag = webElement.getTagName();
                    if (tag != null && !tag.isEmpty()) {
                        urlsTag.put(url, webElement.getTagName());
                    }
                    /* Record url and associated routes */
                    Set<String> routes;
                    String route = getCurrentUrl();
                    if (!urlsRoutes.containsKey(url)) {
                        routes = new HashSet<String>();
                        routes.add(route);
                        urlsRoutes.put(url, routes);
                    } else {
                        routes = urlsRoutes.get(url);
                        routes.add(route);
                    }
                }
            }
            CosengRunner.allUrlsTag.putAll(urlsTag);
            CosengRunner.allUrlsRoutes.putAll(urlsRoutes);
        }
    }

    /**
     * Gets the found urls.
     *
     * @return the urls
     * @see com.sios.stc.coseng.run.CosengRunner#findUrls()
     * @since 2.1
     * @version.coseng
     */
    protected synchronized Set<String> getUrls() {
        return urlsTag.keySet();
    }

    /**
     * Gets all the found urls.
     *
     * @return all urls
     * @see com.sios.stc.coseng.run.CosengRunner#findUrls()
     * @since 2.1
     * @version.coseng
     */
    protected static synchronized Set<String> getAllUrls() {
        return allUrlsTag.keySet();
    }

    /**
     * Gets the url tag.
     *
     * @param url
     *            the url
     * @return the url tag
     * @see com.sios.stc.coseng.run.CosengRunner#findUrls()
     * @since 2.1
     * @version.coseng
     */
    protected synchronized String getUrlTag(String url) {
        if (urlsTag.containsKey(url)) {
            return urlsTag.get(url);
        }
        return null;
    }

    /**
     * Gets the url tag from all urls.
     *
     * @param url
     *            the url
     * @return the url tag
     * @see com.sios.stc.coseng.run.CosengRunner#findUrls()
     * @since 2.1
     * @version.coseng
     */
    protected static synchronized String getAllUrlTag(String url) {
        if (allUrlsTag.containsKey(url)) {
            return allUrlsTag.get(url);
        }
        return null;
    }

    /**
     * Gets the url routes.
     *
     * @param url
     *            the url
     * @return the url routes
     * @see com.sios.stc.coseng.run.CosengRunner#findUrls()
     * @since 2.1
     * @version.coseng
     */
    protected synchronized Set<String> getUrlRoutes(String url) {
        Set<String> routes = new HashSet<String>();
        if (urlsRoutes.containsKey(url)) {
            return urlsRoutes.get(url);
        }
        return routes;
    }

    /**
     * Gets routes from all urls.
     *
     * @param url
     *            the url
     * @return the all url routes
     * @see com.sios.stc.coseng.run.CosengRunner#findUrls()
     * @since 2.1
     * @version.coseng
     */
    protected static synchronized Set<String> getAllUrlRoutes(String url) {
        Set<String> routes = new HashSet<String>();
        if (allUrlsRoutes.containsKey(url)) {
            return allUrlsRoutes.get(url);
        }
        return routes;
    }

    /**
     * Save urls.
     *
     * @see com.sios.stc.coseng.run.CosengRunner#saveUrls(boolean)
     * @see com.sios.stc.coseng.run.CosengRunner#findUrls()
     * @since 2.1
     * @version.coseng
     */
    protected void saveUrls() {
        saveUrls(false);
    }

    /**
     * Save all urls.
     *
     * @see com.sios.stc.coseng.run.CosengRunner#saveUrls(boolean)
     * @see com.sios.stc.coseng.run.CosengRunner#findUrls()
     * @since 2.1
     * @version.coseng
     */
    protected void saveAllUrls() {
        saveUrls(true);
    }

    /**
     * Save urls.
     *
     * @param allUrls
     *            either all urls or specific test's urls
     * @see com.sios.stc.coseng.run.CosengRunner#saveUrls()
     * @see com.sios.stc.coseng.run.CosengRunner#saveAllUrls()
     * @see com.sios.stc.coseng.run.CosengRunner#findUrls()
     * @since 2.1
     * @version.coseng
     */
    private synchronized void saveUrls(boolean allUrls) {
        Test test = getTest();
        if (test != null && test.isAllowFindUrls()) {
            String logHeader = getLogHeader();
            String fileName = "foundUrls.txt";
            if (allUrls) {
                fileName = "allFoundUrls.txt";
            }
            try {
                ArrayList<String> dirPaths = new ArrayList<String>();
                dirPaths.add(test.getReportDirectoryFile().getAbsolutePath());
                dirPaths.add(DIR_ADDITIONAL_REPORTS);
                dirPaths.add(test.getTestNgSuite().getName());
                dirPaths.add(test.getTestNgTest().getName());
                dirPaths.add(test.getTestNgClass().getName());
                dirPaths.add(test.getTestNgMethod().getTestMethod().getMethodName());
                String dirPath = StringUtils.join(dirPaths, File.separator);
                File file = new File(dirPath + File.separator + fileName);
                /* Make directory in report directory */
                File dir = new File(dirPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                /* Save the report */
                List<String> report = new ArrayList<String>();
                Set<String> urls = getUrls();
                if (allUrls) {
                    urls = getAllUrls();
                }
                for (String url : urls) {
                    Integer responseCode = Http.getResponseCode(url);
                    report.add("[" + url + "], tag [" + getAllUrlTag(url) + "], response code ["
                            + (responseCode == null || responseCode == 0 ? "n/a" : responseCode)
                            + "]; found in routes " + getAllUrlRoutes(url));
                }
                String longReport = StringUtils.join(report, System.lineSeparator());
                InputStream stream =
                        new ByteArrayInputStream(longReport.getBytes(StandardCharsets.UTF_8));
                FileUtils.copyToFile(stream, file);
            } catch (Exception e) {
                log.warn(logHeader + " Save found URLs [" + fileName + "] unsuccessful", e);
            }
        }
    }

    /**
     * Urls accessible.
     *
     * @return true, if successful
     * @see com.sios.stc.coseng.run.CosengRunner#urlsAccessible(Set, Set)
     * @see com.sios.stc.coseng.run.CosengRunner#findUrls()
     * @since 2.1
     * @version.coseng
     */
    protected boolean urlsAccessible() {
        return urlsAccessible(null, null);
    }

    /**
     * Urls accessible.
     *
     * @param skipTags
     *            the skip tags
     * @param skipUrls
     *            the skip urls
     * @return true, if successful
     * @see com.sios.stc.coseng.run.CosengRunner#urlsAccessible(Set, Set,
     *      boolean)
     * @see com.sios.stc.coseng.run.CosengRunner#findUrls()
     * @since 2.1
     * @version.coseng
     */
    protected boolean urlsAccessible(Set<String> skipTags, Set<String> skipUrls) {
        return urlsAccessible(skipTags, skipUrls, false);
    }

    /**
     * All urls accessible.
     *
     * @return true, if successful
     * @see com.sios.stc.coseng.run.CosengRunner#allUrlsAccessible(Set, Set)
     * @see com.sios.stc.coseng.run.CosengRunner#findUrls()
     * @since 2.1
     * @version.coseng
     */
    protected boolean allUrlsAccessible() {
        return allUrlsAccessible(null, null);
    }

    /**
     * All urls accessbile.
     *
     * @param skipTags
     *            the skip tags
     * @param skipUrls
     *            the skip urls
     * @return true, if successful
     * @see com.sios.stc.coseng.run.CosengRunner#urlsAccessible(Set, Set,
     *      boolean)
     * @see com.sios.stc.coseng.run.CosengRunner#findUrls()
     * @since 2.1
     * @version.coseng
     */
    protected boolean allUrlsAccessible(Set<String> skipTags, Set<String> skipUrls) {
        return urlsAccessible(skipTags, skipUrls, true);
    }

    /**
     * Urls accessible.
     *
     * @param skipTags
     *            the skip tags
     * @param skipUrls
     *            the skip urls
     * @return true, if successful
     * @see com.sios.stc.coseng.run.CosengRunner#urlsAccessible()
     * @see com.sios.stc.coseng.run.CosengRunner#urlsAccessible(Set, Set)
     * @see com.sios.stc.coseng.run.CosengRunner#allUrlsAccessible()
     * @see com.sios.stc.coseng.run.CosengRunner#allUrlsAccessible(Set, Set)
     * @see com.sios.stc.coseng.run.CosengRunner#findUrls()
     * @see com.sios.stc.coseng.run.CosengRunner#getAllUrls()
     * @see com.sios.stc.coseng.run.CosengRunner#getAllUrlTag(String)
     * @see com.sios.stc.coseng.run.CosengRunner#getAllUrlRoutes(String)
     * @see com.sios.stc.coseng.util.Http#isAccessible(String)
     * @see com.sios.stc.coseng.util.Http#getUrlResponseCode(String)
     * @since 2.1
     * @version.coseng
     */
    private synchronized boolean urlsAccessible(Set<String> skipTags, Set<String> skipUrls,
            boolean allUrls) {
        String logHeader = getLogHeader();
        boolean allUrlsAccessible = true;
        Set<String> urls = getUrls();
        if (allUrls) {
            urls = getAllUrls();
        }
        for (String url : urls) {
            boolean skip = false;
            if (skipUrls != null && skipUrls.contains(url)) {
                skip = true;
            }
            String tag = getAllUrlTag(url);
            if (skipTags != null && skipTags.contains(tag)) {
                skip = true;
            }
            if (skip) {
                log.warn(logHeader + " Skipping URL [{}], tag [{}]; found on routes {}", url, tag,
                        getAllUrlRoutes(url));
                continue;
            }
            if (!Http.isAccessible(url)) {
                Integer responseCode = Http.getResponseCode(url);
                log.error(logHeader + " URL [{}], tag [{}], response code [{}]; found on routes {}",
                        url, tag,
                        (responseCode == null || responseCode == 0 ? "n/a" : responseCode),
                        getAllUrlRoutes(url));
                allUrlsAccessible = false;
            }
        }
        return allUrlsAccessible;
    }

    /**
     * Send keyboard.
     *
     * @param key
     *            the key
     * @since 2.2
     * @version.coseng
     */
    protected static void sendKeyboard(Keys key) {
        Actions actions = getActions();
        if (actions != null) {
            actions.sendKeys(key).build().perform();
        }
    }

    /**
     * Log test step result.
     *
     * @param logLevel
     *            the log level
     * @param logMessage
     *            the log message
     * @param expectedResult
     *            the expected result
     * @param actualResult
     *            the actual result
     * @since 3.0
     * @version.coseng
     */
    private static void logTestStepResult(org.apache.logging.log4j.Level logLevel,
            String logMessage, String expectedResult, String actualResult) {
        Test test = getTest();
        String logHeader = getLogHeader();
        String reporterBreak = "<br>";
        String appendResult =
                (expectedResult == null ? "" : ", expected result [" + expectedResult + "]")
                        + (actualResult == null ? "" : ", actual result [" + actualResult + "]");
        if (logLevel == null) {
            logLevel = defaultLogLevel;
        }
        log.log(logLevel, "{} {}", logHeader, logMessage + appendResult);
        Reporter.log(logMessage + appendResult + reporterBreak);
        for (Integrator i : GetIntegrators.wired()) {
            try {
                i.addTestStep(test, logMessage);
                i.addTestStepExpectedResult(test, logMessage, expectedResult);
                i.addTestStepActualResult(test, logMessage, actualResult);
            } catch (CosengException e) {
                log.error(
                        "{} Unable to add integrator [{}],test step result message [{}], expected [{}], actual [{}]: {}",
                        logHeader, i.getClass().getName(), logMessage, expectedResult, actualResult,
                        e.getMessage());
            }
        }
    }

    /**
     * Log test step.
     *
     * @param step
     *            the step
     * @since 3.0
     * @version.coseng
     */
    protected static void logTestStep(String step) {
        String logMessage = "> Test [" + step + "]";
        logTestStepResult(defaultLogLevel, logMessage, null, null);
    }

    /**
     * Log message.
     *
     * @param message
     *            the message
     * @since 3.0
     * @version.coseng
     */
    protected static void logMessage(String message) {
        logMessage(message, defaultLogLevel);
    }

    /**
     * Adds the test step message. Convenience method to log a test message to
     * the log file, as well as any wired integrators, without having to use
     * assertions.
     *
     * @param stepMessage
     *            the step message
     * @since 3.0
     * @version.coseng
     */
    protected static void logMessage(String message, org.apache.logging.log4j.Level logLevel) {
        String logMessage = "# Message [" + message + "]";
        logTestStepResult(logLevel, logMessage, null, null);
    }

    /**
     * Log skip test for browser.
     *
     * @since 3.0
     * @version.coseng
     */
    protected static void logSkipTestForBrowser() {
        logSkipTestForBrowser(getTest().getBrowser(), null,
                Thread.currentThread().getStackTrace()[2].getLineNumber(),
                org.apache.logging.log4j.Level.WARN);
    }

    /**
     * Log skip test for browser.
     *
     * @param message
     *            the message
     * @since 3.0
     * @version.coseng
     */
    protected static void logSkipTestForBrowser(String message) {
        logSkipTestForBrowser(getTest().getBrowser(), message,
                Thread.currentThread().getStackTrace()[2].getLineNumber(),
                org.apache.logging.log4j.Level.WARN);
    }

    /**
     * Log skip test for browser.
     *
     * @param message
     *            the message
     * @param logLevel
     *            the log level
     * @since 3.0
     * @version.coseng
     */
    protected static void logSkipTestForBrowser(String message,
            org.apache.logging.log4j.Level logLevel) {
        logSkipTestForBrowser(getTest().getBrowser(), message,
                Thread.currentThread().getStackTrace()[2].getLineNumber(), logLevel);
    }

    /**
     * Log skip test for browser.
     *
     * @param browser
     *            the browser
     * @param message
     *            the message
     * @param lineNumber
     *            the line number
     * @param logLevel
     *            the log level
     * @since 3.0
     * @version.coseng
     */
    private static void logSkipTestForBrowser(Browser browser, String message, int lineNumber,
            org.apache.logging.log4j.Level logLevel) {
        String logMessage =
                "- Skipping test near line number [" + lineNumber + "], browser [" + browser + "]";
        if (message != null) {
            logMessage += ", message [" + message + "]";
        }
        logTestStepResult(logLevel, logMessage, null, null);
    }

    /**
     * Log skip test for defect.
     *
     * @param defectId
     *            the defect id
     * @since 3.0
     * @version.coseng
     */
    protected static void logSkipTestForDefect(String defectId) {
        logSkipTestForDefect(defectId, null,
                Thread.currentThread().getStackTrace()[2].getLineNumber(),
                org.apache.logging.log4j.Level.WARN);
    }

    /**
     * Log skip test for defect.
     *
     * @param defectId
     *            the defect id
     * @param message
     *            the message
     * @since 3.0
     * @version.coseng
     */
    protected static void logSkipTestForDefect(String defectId, String message) {
        logSkipTestForDefect(defectId, message,
                Thread.currentThread().getStackTrace()[2].getLineNumber(),
                org.apache.logging.log4j.Level.WARN);
    }

    /**
     * Log skip test for defect.
     *
     * @param defectId
     *            the defect id
     * @param message
     *            the message
     * @param logLevel
     *            the log level
     * @since 3.0
     * @version.coseng
     */
    protected static void logSkipTestForDefect(String defectId, String message,
            org.apache.logging.log4j.Level logLevel) {
        logSkipTestForDefect(defectId, message,
                Thread.currentThread().getStackTrace()[2].getLineNumber(), logLevel);
    }

    /**
     * Log skip test for defect.
     *
     * @param defectId
     *            the defect id
     * @param message
     *            the message
     * @param lineNumber
     *            the line number
     * @param logLevel
     *            the log level
     * @since 3.0
     * @version.coseng
     */
    private static void logSkipTestForDefect(String defectId, String message, int lineNumber,
            org.apache.logging.log4j.Level logLevel) {
        String logMessage = "- Skipping test near line number [" + lineNumber + "], defect ["
                + defectId + "], message [" + message + "]";
        logTestStepResult(logLevel, logMessage, null, null);
    }

    /**
     * The Class LogAssert.
     *
     * @since 3.0
     * @version.coseng
     */
    protected class LogAssert extends Assertion {
        /*
         * getExpected and getActual return less than helpful values depending
         * on the assert method. ie. assertNotNull, assertEquals. So, rather
         * than doing a laundry list of reflections just report on success or on
         * failure.
         */

        /*
         * (non-Javadoc)
         * 
         * @see org.testng.asserts.Assertion#onAssertSuccess(org.testng.asserts.
         * IAssert)
         */
        @Override
        public synchronized void onAssertSuccess(IAssert<?> a) {
            if (a != null) {
                String message = Common.STRING_UNKNOWN;
                if (a.getMessage() != null) {
                    message = a.getMessage();
                }
                String logMessage = "+ Assertion [" + message + "]";
                logTestStepResult(org.apache.logging.log4j.Level.INFO, logMessage,
                        Common.STRING_ASSERTION_SUCESS, Common.STRING_ASSERTION_SUCESS);
            } else {
                log.debug("Assertion was null");
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.testng.asserts.Assertion#onAssertFailure(org.testng.asserts.
         * IAssert)
         */
        @Override
        public synchronized void onAssertFailure(IAssert<?> a) {
            if (a != null) {
                String message = Common.STRING_UNKNOWN;
                if (a.getMessage() != null) {
                    message = a.getMessage();
                }
                String logMessage = "+ Assertion [" + message + "]";
                logTestStepResult(org.apache.logging.log4j.Level.ERROR, logMessage,
                        Common.STRING_ASSERTION_SUCESS, Common.STRING_ASSERTION_FAIL);
                saveScreenshot("AssertFailure-" + assertFailureCount++);
            } else {
                log.debug("Assertion was null");
            }
        }
    }

    /**
     * The Class LogSoftAssert.
     *
     * @since 3.0
     * @version.coseng
     */
    protected class LogSoftAssert extends SoftAssert {
        /*
         * getExpected and getActual return less than helpful values depending
         * on the assert method. ie. assertNotNull, assertEquals. So, rather
         * than doing a laundry list of reflections just report on success or on
         * failure.
         */

        /*
         * (non-Javadoc)
         * 
         * @see org.testng.asserts.Assertion#onAssertSuccess(org.testng.asserts.
         * IAssert)
         */
        @Override
        public synchronized void onAssertSuccess(IAssert<?> a) {
            if (a != null) {
                String message = Common.STRING_UNKNOWN;
                if (a.getMessage() != null) {
                    message = a.getMessage();
                }
                String logMessage = "+ SoftAssertion [" + message + "]";
                logTestStepResult(org.apache.logging.log4j.Level.INFO, logMessage,
                        Common.STRING_ASSERTION_SUCESS, Common.STRING_ASSERTION_SUCESS);
            } else {
                log.debug("Assertion was null");
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.testng.asserts.Assertion#onAssertFailure(org.testng.asserts.
         * IAssert)
         */
        @Override
        public synchronized void onAssertFailure(IAssert<?> a) {
            if (a != null) {
                String message = Common.STRING_UNKNOWN;
                if (a.getMessage() != null) {
                    message = a.getMessage();
                }
                String logMessage = "+ SoftAssertion [" + message + "]";
                logTestStepResult(org.apache.logging.log4j.Level.ERROR, logMessage,
                        Common.STRING_ASSERTION_SUCESS, Common.STRING_ASSERTION_FAIL);
                saveScreenshot("AssertFailure-" + assertFailureCount++);
            } else {
                log.debug("Assertion was null");
            }
        }
    }

    /**
     * Gets the log header as a combination of the TestNG test, suite, test,
     * class and method names to prepend logging.
     *
     * @return the log header
     * @since 3.0
     * @version.coseng
     */
    private static String getLogHeader() {
        Test test = CosengRunner.getTest();
        String logHeader = Common.STRING_UNKNOWN;
        try {
            List<String> headers = new ArrayList<String>();
            headers.add("cosengTest [" + test.getName());
            headers.add("suite [" + test.getTestNgSuite().getName());
            headers.add("test [" + test.getTestNgTest().getName());
            headers.add("class [" + test.getTestNgClass().getName());
            headers.add("method [" + test.getTestNgMethod().getTestMethod().getMethodName());
            logHeader = StringUtils.join(headers, "], ") + "]";
        } catch (Exception e) {
            logHeader = ("exception [" + e.getMessage() + "]");
        }
        return "[" + logHeader + "]:";
    }

}
