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

/**
 * The Class Runner.
 *
 * @since 2.0
 * @version.coseng
 */
class Runner extends WebDriverLifecycle {
    private Thread           thread;
    private Test             test;
    private WebDriverToolbox webDriverToolbox;

    /**
     * Instantiates a new runner.
     *
     * @throws CosengException
     *             the coseng exception
     * @since 2.0
     * @version.coseng
     */
    protected Runner() throws CosengException {
        this(Thread.currentThread(), null);
    }

    /**
     * Instantiates a new runner.
     *
     * @param thread
     *            the thread
     * @param test
     *            the test
     * @throws CosengException
     *             the coseng exception
     * @see {@link Test}
     * @since 2.0
     * @version.coseng
     */
    protected Runner(Thread thread, Test test) throws CosengException {
        if (thread != null && test != null) {
            this.thread = thread;
            this.test = test;

        } else {
            throw new CosengException("Thread or test is null; nothing to do");
        }
    }

    /**
     * Gets the thread.
     *
     * @return the thread
     * @since 2.0
     * @version.coseng
     */
    protected Thread getThread() {
        return thread;
    }

    /**
     * Gets the test.
     *
     * @return the test
     * @since 2.0
     * @version.coseng
     */
    protected Test getTest() {
        return test;
    }

    /**
     * Start web driver.
     *
     * @throws CosengException
     *             the coseng exception
     * @see {@link WebDriverLifecycle#startWebDriver(Test)},
     *      {@link WebDriverToolBox}
     * @since 2.0
     * @version.coseng
     */
    protected void startWebDriver() throws CosengException {
        webDriverToolbox = startWebDriver(test);
    }

    /**
     * Stop web driver.
     *
     * @throws CosengException
     *             the coseng exception
     * @see {@link WebDriverLifecycle#stopWebDriver(org.openqa.selenium.WebDriver, Object)},
     *      {@link WebDriverToolBox}
     * @since 2.0
     * @version.coseng
     */
    protected void stopWebDriver() throws CosengException {
        if (webDriverToolbox != null) {
            stopWebDriver(webDriverToolbox.getWebDriver(), webDriverToolbox.getWebDriverService());
        }
    }

    /**
     * Gets the web driver toolbox.
     *
     * @return the web driver toolbox
     * @see {@link {@link WebDriverToolBox}
     * @since 2.0
     * @version.coseng
     */
    protected WebDriverToolbox getWebDriverToolbox() {
        return webDriverToolbox;
    }

    /**
     * Checks for web driver toolbox.
     *
     * @return true, if successful
     * @see {@link {@link WebDriverToolBox}
     * @since 2.0
     * @version.coseng
     */
    protected boolean hasWebDriverToolbox() {
        if (webDriverToolbox != null) {
            return true;
        }
        return false;
    }

}
