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

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sios.stc.coseng.RunTests;
import com.sios.stc.coseng.run.CosengException;
import com.sios.stc.coseng.run.CosengRunner;

public class Urls extends CosengRunner {

    private static final Logger log = LogManager.getLogger(RunTests.class.getName());

    @Test(description = "All URLs Accessible")
    public void accessible() throws CosengException {

        /* Make sure a web driver for this thread */
        Assert.assertTrue(hasWebDriver(), "No web driver");

        /* URL Tags to skip */
        Set<String> skipTags = new HashSet<String>();
        skipTags.add("ng-include");

        /* URL to skip */
        Set<String> skipUrls = new HashSet<String>();
        skipUrls.add("http://host/path");
        skipUrls.add("mailto:support@host");

        /* Check all found links accessible */
        saveAllUrls();
        boolean allUrlsAccessible = true;
        // allUrlsAccessible = allUrlsAccessible(skipTags, skipUrls);
        // Assert.assertTrue(allUrlsAccessible, "all URLs not accessible; check
        // logs");
        Assert.assertTrue(allUrlsAccessible,
                "All URLs accessible IGNORED FOR DEMO; to enable uncomment in Urls.java");
        log.warn(
                "NOTE! *not* checking allUrlsAccessible(); would 'HEAD' url check ~300 external 'href' and 'src' elements. Uncomment line 53 to see results");
    }

}
