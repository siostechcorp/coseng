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

public class Google extends CosengRunner {

    private static final Logger log = LogManager.getLogger(RunTests.class.getName());

    @Test(description = "Verify connect to Google and search")
    public void connect1() throws CosengException {
        String searchForm = "searchform";
        String url = "http://www.google.com";
        String redirectedUrl = "https://www.google.com";

        /* Make sure a web driver for this thread */
        Assert.assertTrue(hasWebDriver(), "there should be a web driver");
        WebDriver webDriver = getWebDriver();
        log.debug("Test [{}], web driver [{}], thread [{}]", getTest().getName(),
                webDriver.hashCode(), Thread.currentThread().getId());

        /* Get the url and assure on correct route. */
        logTestStep("navigating to url [" + url + "] and assuring search form available");
        webDriver.get(url);
        logAssert.assertTrue(currentUrlContains(redirectedUrl),
                "Current URL should contain [" + redirectedUrl + "]");

        /* Get a COSENG WebElement object, find it and assure displayed */
        WebElement weSearchForm = newWebElement(By.id(searchForm));
        weSearchForm.find();
        logAssert.assertTrue(weSearchForm.isDisplayed(), "search form element should be displayed");

        /* Take a screenshot while were here */
        logMessage("saving screenshot [google-connect1]");
        saveScreenshot("google-connect1");

        /* Find and save URLs on this route */
        logMessage("finding URLs");
        findUrls();
        logMessage("saving URLs");
        saveUrls();
        // urlsAccessible();
    }

    @Test(description = "Verify connect to Google About and Carrers link")
    public void connect2() throws CosengException {
        String carrers = "//*[@id=\"footer-sitemap-about-content\"]/div/ul/li[3]/a";
        String url = "https://www.google.com/intl/en/about/";

        /* Make sure a web driver for this thread */
        Assert.assertTrue(hasWebDriver(), "there should be an available webdriver");
        WebDriver webDriver = getWebDriver();
        log.debug("Test [{}], web driver [{}], thread [{}]", getTest().getName(),
                webDriver.hashCode(), Thread.currentThread().getId());

        /* Get the url and assure on correct route. */
        logTestStep("navigating to url [" + url + "] and assuring Carrers links available");
        webDriver.get(url);
        logAssert.assertTrue(currentUrlContains(url), "current URL should contain [" + url + "]");

        /* Get a COSENG WebElement object, find it and assure displayed */
        WebElement weCarrers = newWebElement(By.xpath(carrers));
        weCarrers.find();
        logAssert.assertTrue(weCarrers.isDisplayed(), "carrers web element should be displayed");

        /* Take a screenshot while were here */
        logMessage("saving screenshot [google-connect2]");
        saveScreenshot("google-connect2");

        /* Find and save URLs on this route */
        logMessage("finding URLs");
        findUrls();
        logMessage("saving URLs");
        saveUrls();
        // urlsAccessible();
    }

}
