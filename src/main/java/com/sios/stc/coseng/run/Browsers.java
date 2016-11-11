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

import java.util.ArrayList;
import java.util.List;

/**
 * The Class Browsers.
 *
 * @since 2.0
 * @version.coseng
 */
public class Browsers {

    /**
     * The Enum Browser.
     *
     * @since 2.0
     * @version.coseng
     */
    public static enum Browser {
        ALL, FIREFOX, CHROME, IE, EDGE;
    }

    protected static final String BROWSER_VERSION_DEFAULT        = "ANY";
    protected static final String BROWSER_NAME_INTERNET_EXPLORER = "internet explorer";

    /**
     * Gets the list of browser values.
     *
     * @return the list
     * @since 2.0
     * @version.coseng
     */
    protected static List<String> get() {
        List<String> browsers = new ArrayList<String>();
        for (Browser browser : Browser.values()) {
            browsers.add(browser.toString());
        }
        return browsers;
    }

}
