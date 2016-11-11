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
package com.sios.stc.coseng.tests.demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sios.stc.coseng.RunTests;
import com.sios.stc.coseng.run.CosengException;
import com.sios.stc.coseng.run.CosengRunner;
import com.sios.stc.coseng.run.WebElement;

public class Ask extends CosengRunner {

    private static final Logger log = LogManager.getLogger(RunTests.class.getName());

    @Test(description = "Verify connect to Ask and search")
    public void connect1() throws CosengException {
        String url = "http://www.ask.com";
        String searchform = "search-box";

        /* Make sure a web driver for this thread */
        Assert.assertTrue(hasWebDriver(), "No web driver");
        log.debug("Test [{}], web driver [{}], thread [{}]", getTest().getName(),
                getWebDriver().hashCode(), Thread.currentThread().getId());

        /*
         * Get the url and assure on correct route. Note: Using the convenience
         * method. Can always get the web driver with WebDriver webDriver =
         * getWebDriver();
         */
        webDriverGet(url);
        Assert.assertTrue(currentUrlContains(url));

        /* Get a COSENG WebElement object, find it and assure displayed */
        WebElement weSearchForm = newWebElement(By.id(searchform));
        weSearchForm.find();
        Assert.assertTrue(weSearchForm.isDisplayed());

        /* Take a screenshot while were here */
        saveScreenshot("ask-connect1");

        /* Find and save URLs on this route */
        findUrls();
        saveUrls();
        // urlsAccessible();

    }

    @Test(description = "Verify connect to Ask About and help button")
    public void connect2() throws CosengException {
        String url = "http://about.ask.com";
        String helpButton = "/html/body/section/aside/button";

        /* Make sure a web driver for this thread */
        Assert.assertTrue(hasWebDriver(), "No web driver");
        log.debug("Test [{}], web driver [{}], thread [{}]", getTest().getName(),
                getWebDriver().hashCode(), Thread.currentThread().getId());

        /*
         * Get the url and assure on correct route. Note: Using the convenience
         * method. Can always get the web driver with WebDriver webDriver =
         * getWebDriver();
         */
        webDriverGet(url);
        Assert.assertTrue(currentUrlContains(url));

        /* Get a COSENG WebElement object, find it and assure displayed */
        WebElement weHelpButton = newWebElement(By.xpath(helpButton));
        weHelpButton.find();
        Assert.assertTrue(weHelpButton.isDisplayed());

        /* Take a screenshot while were here */
        saveScreenshot("ask-connect2");

        /* Find and save URLs on this route */
        findUrls();
        saveUrls();
        // urlsAccessible();
    }

}
