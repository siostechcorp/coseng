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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.sios.stc.coseng.run.Locations.Location;
import com.sios.stc.coseng.util.Resource;

/**
 * The Class HelpParameter provides current platform defaults for Tests and Node
 * JSON parameters, supported values, notes and JSON example.
 *
 * @since 2.0
 * @version.coseng
 */
class Help {

    private static final String SPACER = "    ";

    /**
     * Gets the default Node JSON parameter defaults for current platform and
     * notes.
     *
     * @return the node parameter help and example
     * @see com.sios.stc.coseng.run.Node
     * @since 2.0
     * @version.coseng
     */
    protected static String getNode() {
        Node node = new Node();
        List<String> p = new ArrayList<String>();
        p.add("Supported Node JSON Options & Example");

        p.add(space(1, "reportsDirectory: (optional) String"));
        p.add(space(2, "Default [" + node.getReportsDirectory() + "]"));

        p.add(space(1, "resourcesTempDirectory: (optional) String"));
        p.add(space(2, "Default [" + node.getResourcesTempDirectory() + "]"));
        p.add(space(2, "Platform dependent"));
        final String PLATFORM_DEPENDS = "Platform dependent for location [" + Location.NODE
                + "]; ignored for location [" + Location.GRID + ")";

        p.add(space(1, "chromeDriver: (optional) String"));
        p.add(space(2, "Default [" + node.getChromeDriver() + "]"));
        p.add(space(2, PLATFORM_DEPENDS));

        p.add(space(1, "geckoDriver: (optional) String"));
        p.add(space(2, "Default [" + node.getGeckoDriver() + "]"));
        p.add(space(2, PLATFORM_DEPENDS));

        p.add(space(1, "ieDriver: (optional) String"));
        p.add(space(2, "Default [" + node.getIeDriver() + "]"));
        p.add(space(2, PLATFORM_DEPENDS));

        p.add(space(1, "edgeDriver: (optional) String"));
        p.add(space(2, "Default [" + node.getEdgeDriver() + "]"));
        p.add(space(2, PLATFORM_DEPENDS));
        String gridUrl;
        try {
            if (node.getGridUrl() != null) {
                gridUrl = node.getGridUrl().toExternalForm();
            } else {
                gridUrl = null;
            }
        } catch (CosengException e) {
            gridUrl = e.getCause().toString();
        }
        p.add(space(1, "gridUrl: (optional) String"));
        p.add(space(2, "Default [" + gridUrl + "]"));
        p.add(space(2, "Note: provided for undefined Test gridUrl"));

        p.add(space(1, "maxTestExecutionMinutes: (optional) Integer > 0"));
        p.add(space(2, "Default [" + node.getMaxTestExecutionMinutes() + "]"));
        p.add(space(2, "Note: timeout for executor pool"));

        p.add(space(1, ""));
        p.add(Resource.getJsonFromObject(node));
        return StringUtils.join(p, System.lineSeparator());
    }

    /**
     * Gets the default Test JSON parameter defaults for current platform,
     * supported values, notes and Tests JSON example.
     *
     * @return the test parameter help and example
     * @see com.sios.stc.coseng.run.Tests
     * @see com.sios.stc.coseng.run.Test
     * @since 2.0
     * @version.coseng
     */
    protected static String getTest() {
        Test test = new Test();
        List<String> p = new ArrayList<String>();
        p.add("Supported Tests JSON Options & Example");
        p.add(space(1, "name: (required) String"));
        p.add(space(2, "Default [" + test.getName() + "]"));

        p.add(space(1, "location: (optional) Location " + Locations.get()));
        p.add(space(2, "Default [" + test.getLocation() + "]"));

        p.add(space(1, "platform: (optional) Platform " + Platforms.get()));
        p.add(space(2, "Default [" + test.getPlatform() + "]"));
        p.add(space(2, "Operating system dependent for location [" + Location.NODE + "]"));

        p.add(space(1, "browser: (optional) Browser " + Browsers.get()));
        p.add(space(2, "Default [" + test.getBrowser() + "]"));
        p.add(space(2, "Platform dependent for location [" + Location.NODE + "]"));

        p.add(space(1, "browserRequestedVersion: (optional) String"));
        p.add(space(2, "Default [" + Browsers.BROWSER_VERSION_DEFAULT + "]"));
        p.add(space(2, "Ignored for location [" + Location.NODE + "]"));

        p.add(space(1, "browserHeadless: (optional) boolean"));
        p.add(space(2, "Default [" + test.getBrowserHeadless() + "]"));

        p.add(space(1, "browserWidth: (optional) Integer > 0"));
        p.add(space(2, "Default [" + test.getBrowserWidth() + "]"));
        p.add(space(2, "If defined, browserHeight must also be defined"));

        p.add(space(1, "browserHeight: (optional) Integer > 0"));
        p.add(space(2, "Default [" + test.getBrowserHeight() + "]"));
        p.add(space(2, "If defined, browserWidth must also be defined"));

        p.add(space(1, "browserMaximize: (optional) boolean"));
        p.add(space(2, "Default [" + test.getBrowserMaximize() + "]"));
        p.add(space(2, "If defined, overrides browser width and height"));

        p.add(space(1, "incognito: (optional) boolean"));
        p.add(space(2, "Default [" + test.isIncognito() + "]"));

        p.add(space(1, "acceptInvalidCerts: (optional) boolean"));
        p.add(space(2, "Default [" + test.isAcceptInvalidCerts() + "]"));

        p.add(space(1, "angular2App: (optional) boolean"));
        p.add(space(2, "Default [" + test.isAngular2App() + "]"));

        p.add(space(1, "allowFindUrls: (optional) boolean"));
        p.add(space(2, "Default [" + test.isAllowFindUrls() + "]"));

        p.add(space(1, "allowScreenshots: (optional) boolean"));
        p.add(space(2, "Default [" + test.isAllowScreenshots() + "]"));

        p.add(space(1, "suites: (required) list of String"));
        p.add(space(2, "Default " + test.getSuites()));

        p.add(space(1, "baseUrl: (optional) String"));
        p.add(space(2, "Default [" + test.getBaseUrl() + "]"));

        final String GRIDURL_DEPENDS1 = "(required) String for location [" + Location.GRID + "]";
        final String GRIDURL_DEPENDS2 = "(optional) String for location [" + Location.NODE + "]";
        final String GRIDURL_NOTE =
                "If defined this overrides any defined Node JSON gridUrl; ignored for location ["
                        + Location.NODE + "]";
        String gridUrl;
        try {
            if (test.getGridUrl() != null) {
                gridUrl = test.getGridUrl().toExternalForm();
            } else {
                gridUrl = null;
            }
        } catch (CosengException e) {
            gridUrl = e.getCause().toString();
        }
        p.add(space(1, "gridUrl: (optional) String"));
        p.add(space(2, "Default [" + gridUrl + "]"));
        p.add(space(2, GRIDURL_DEPENDS1));
        p.add(space(2, GRIDURL_DEPENDS2));
        p.add(space(2, GRIDURL_NOTE));

        p.add(space(1, "oneWebDriver: (optional) boolean"));
        p.add(space(2, "Default [" + test.isOneWebDriver() + "]"));
        p.add(space(2, "If oneWebDriver [true] all suites must have parallel=\"false\""));

        p.add(space(1, "webDriverTimeoutSeconds: (optional) Integer"));
        p.add(space(2, "Default [" + test.getWebDriverTimeoutSeconds() + "]"));

        p.add(space(1, "webDriverWaitTimeoutSeconds: (optional) Integer"));
        p.add(space(2, "Default [" + test.getWebDriverWaitTimeoutSeconds() + "]"));

        p.add(space(1, "verbosity: (optional) Integer 0..10"));
        p.add(space(2, "Default [" + test.getVerbosity() + "]"));
        p.add(space(2, "TestNG logging level"));

        p.add(space(1, ""));
        test.setName("testHelp");
        List<String> suites = test.getSuites();
        suites.add("TestNG_suite.xml");
        Tests tests = new Tests();
        tests.add(test);
        p.add(Resource.getJsonFromObject(tests));
        return StringUtils.join(p, System.lineSeparator());
    }

    /**
     * Space message padding helper.
     *
     * @param repeat
     *            the repeat times for the space padding
     * @param message
     *            the space padded message
     * @return the padded string
     * @since 2.0
     * @version.coseng
     */
    private static String space(int repeat, String message) {
        return StringUtils.repeat(SPACER, repeat) + message;
    }

}
