/*
 * Copyright (c) 2015 SIOS Technology Corp. All rights reserved.
 * This file is part of COSENG (Concurrent Selenium TestNG Runner).
 * 
 * COSENG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * COSENG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with COSENG. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sios.stc.coseng.test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

//import org.openqa.selenium.support.ui.Select;

public class Google extends Base {

    @Test(description = "Verify connect to Google")
    public void connect()
            throws Exception {

        driver.get("http://www.google.com");
        saveScreenshot(driver, "landingpage-google");
        final WebElement googleSearchInput = driver.findElement(By.id("gbqfq"));
        Assert.assertTrue(googleSearchInput.isDisplayed());
    }

}
