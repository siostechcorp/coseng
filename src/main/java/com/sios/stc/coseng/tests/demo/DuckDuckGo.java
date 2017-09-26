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
package com.sios.stc.coseng.tests.demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sios.stc.coseng.RunTests;
import com.sios.stc.coseng.run.Browsers.Browser;
import com.sios.stc.coseng.run.CosengException;
import com.sios.stc.coseng.run.CosengRunner;
import com.sios.stc.coseng.run.WebElement;

public class DuckDuckGo extends CosengRunner {

    private static final Logger log = LogManager.getLogger(RunTests.class.getName());

    @Test(description = "Verify connect to DuckDuckGo and search")
    public void connect1() throws CosengException {
        String searchForm = "search_form_input_homepage";
        String url = "https://www.duckduckgo.com";
        String redirectedUrl = "https://duckduckgo.com";

        /* Make sure a web driver for this thread */
        Assert.assertTrue(hasWebDriver(), "there should be a web driver");
        log.debug("Test [{}], web driver [{}], thread [{}]", getTest().getName(),
                getWebDriver().hashCode(), Thread.currentThread().getId());

        Browser browser = getTest().getBrowser();

        /*
         * Get the url and assure on correct route. Note: Using the convenience
         * method. Can always get the web driver with WebDriver webDriver =
         * getWebDriver();
         */
        logTestStep("navigating to url [" + url + "] and assuring search form available");
        webDriverGet(url);
        logAssert.assertTrue(currentUrlContains(redirectedUrl),
                "Current URL contains " + redirectedUrl);

        /* Get a COSENG WebElement object, find it and assure displayed */
        WebElement weSearchForm = newWebElement(By.id(searchForm));
        Assert.assertTrue(weSearchForm.find());
        if (!Browser.EDGE.equals(browser)) {
            logAssert.assertTrue(weSearchForm.isDisplayed(),
                    "search form element should be displayed");
        } else {
            logSkipTestForBrowser();
        }

        /* Take a screenshot while were here */
        logMessage("saving screenshot [duckDuckGo-connect1]");
        saveScreenshot("duckDuckGo-connect1");

        /* Find and save URLs on this route */
        logMessage("finding URLs");
        findUrls();
        logMessage("saving URLs");
        saveUrls();
        // urlsAccessible();
    }

}
