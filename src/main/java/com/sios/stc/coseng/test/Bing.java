/*
 * This file is part of COSENG (Concurrent Selenium TestNG Runner).
 * 
 * Copyright (c) 2015 SIOS Technology Corp. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sios.stc.coseng.test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

//import org.openqa.selenium.support.ui.Select;

public class Bing extends Base {

    @Test(description = "Verify connect to Google")
    public void connect()
            throws Exception {

        driver.get("http://www.bing.com");
        saveScreenshot(driver, "landingpage-bing");
        final WebElement bingSearchInput = driver.findElement(By.id("sbox"));
        Assert.assertTrue(bingSearchInput.isDisplayed());
    }

}
