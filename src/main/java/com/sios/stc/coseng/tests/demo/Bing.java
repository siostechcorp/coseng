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
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sios.stc.coseng.RunTests;
import com.sios.stc.coseng.run.CosengException;
import com.sios.stc.coseng.run.CosengRunner;
import com.sios.stc.coseng.run.WebElement;

public class Bing extends CosengRunner {

    private static final Logger log = LogManager.getLogger(RunTests.class.getName());

    @Test(description = "Verify connect to Bing and search")
    public void connect1() throws CosengException {
        String searchform = "sb_form_q";
        String url = "http://www.bing.com";

        /* Make sure a web driver for this thread */
        Assert.assertTrue(hasWebDriver(), "there should be a web driver");
        WebDriver webDriver = getWebDriver();
        log.debug("Test [{}], web driver [{}], thread [{}]", getTest().getName(),
                webDriver.hashCode(), Thread.currentThread().getId());

        /* Get the url and assure on correct route. */
        logTestStep("navigating to url [" + url + "] and assuring search form available]");
        webDriver.get(url);
        logAssert.assertTrue(currentUrlContains(url), "current URL should contain [" + url + "]");

        /* Get a COSENG WebElement object, find it and assure displayed */
        WebElement weSearchForm = newWebElement(By.id(searchform));
        weSearchForm.find();
        logAssert.assertTrue(weSearchForm.isDisplayed(), "search form element should be displayed");

        /* Take a screenshot while were here */
        logMessage("saving screenshot [bing-connect1]");
        saveScreenshot("bing-connect1");

        /* Find and save URLs on this route */
        logMessage("finding URLs");
        findUrls();
        logMessage("saving URLs");
        saveUrls();
        // urlsAccessible();
    }

    @Test(description = "Verify connect to Bing Help and search")
    public void connect2() throws CosengException {
        String searchForm = "//*[@id=\"searchquery\"]";
        String url = "http://help.bing.microsoft.com/#apex/18/en-US/n1999/-1/en-US";

        /* Make sure a web driver for this thread */
        Assert.assertTrue(hasWebDriver(), "there should be an available webdriver");
        WebDriver webDriver = getWebDriver();
        log.debug("Test [{}], web driver [{}], thread [{}]", getTest().getName(),
                webDriver.hashCode(), Thread.currentThread().getId());

        /* Get the url and assure on correct route. */
        logTestStep("navigating to url [" + url + "] and assuring help button available");
        webDriver.get(url);
        logAssert.assertTrue(currentUrlContains(url), "current URL should contain [" + url + "]");

        /* Get a COSENG WebElement object, find it and assure displayed */
        WebElement weSearchForm = newWebElement(By.xpath(searchForm));
        weSearchForm.find();
        logAssert.assertTrue(weSearchForm.isDisplayed(), "search form element should be displayed");

        /* Take a screenshot while were here */
        logMessage("saving screenshot [bing-connect2]");
        saveScreenshot("bing-connect2");

        /* Find and save URLs on this route */
        logMessage("finding URLs");
        findUrls();
        logMessage("saving URLs");
        saveUrls();
        // urlsAccessible();
    }

}
