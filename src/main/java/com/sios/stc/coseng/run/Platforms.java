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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.Platform;

import com.sios.stc.coseng.run.Browsers.Browser;
import com.sios.stc.coseng.run.Locations.Location;

/**
 * The Class Platforms.
 *
 * @since 2.0
 * @version.coseng
 */
class Platforms {

    private static final Set<Platform>               platforms        = setSupported();
    private static final Map<Platform, Set<Browser>> platformBrowsers = setSupportedBrowsers();

    /**
     * Sets the supported subset of Selenium Platform
     *
     * @return the platforms
     * @see org.openqa.selenium.Platform
     * @since 2.0
     * @version.coseng
     */
    private static Set<Platform> setSupported() {
        Set<Platform> platforms = new HashSet<Platform>();
        platforms.add(Platform.ANY);
        platforms.add(Platform.LINUX);
        platforms.add(Platform.WIN10);
        return platforms;
    }

    /**
     * Sets the supported browsers.
     *
     * @return the map
     * @see org.openqa.selenium.Platform
     * @see com.sios.stc.coseng.run.Browsers.Browser
     * @since 2.0
     * @version.coseng
     */
    private static Map<Platform, Set<Browser>> setSupportedBrowsers() {
        Map<Platform, Set<Browser>> platformBrowsers = new HashMap<Platform, Set<Browser>>();
        for (Platform platform : platforms) {
            Set<Browser> browsers = new HashSet<Browser>();
            if (Platform.LINUX.equals(platform)) {
                browsers.add(Browser.CHROME);
                browsers.add(Browser.FIREFOX);
            } else if (Platform.WIN10.equals(platform)) {
                browsers.add(Browser.CHROME);
                browsers.add(Browser.FIREFOX);
                browsers.add(Browser.IE);
                browsers.add(Browser.EDGE);
            } else if (Platform.ANY.equals(platform)) {
                browsers.add(Browser.CHROME);
                browsers.add(Browser.FIREFOX);
                browsers.add(Browser.IE);
                browsers.add(Browser.EDGE);
            }
            platformBrowsers.put(platform, browsers);
        }
        return platformBrowsers;
    }

    /**
     * Checks if platform is supported.
     *
     * @param platform
     *            the platform
     * @return true, if platform supported
     * @see org.openqa.selenium.Platform
     * @since 2.0
     * @version.coseng
     */
    protected static boolean isSupported(Platform platform) {
        if (platforms.contains(platform)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if is supported based on execution location and platform.
     *
     * @param location
     *            the location
     * @param platform
     *            the platform
     * @return true, if is supported
     * @see org.openqa.selenium.Platform
     * @see com.sios.stc.coseng.run.Platforms#isSupported(Platform)
     * @since 2.0
     * @version.coseng
     */
    protected static boolean isSupported(Location location, Platform platform) {
        if (Location.GRID.equals(location) && isSupported(platform)) {
            return true;
        }
        if (Location.NODE.equals(location) && !Platform.ANY.equals(platform)
                && isSupported(platform)) {
            return true;
        }
        return false;
    }

    /**
     * Gets the supported platform.
     *
     * @return the supported
     * @since 2.0
     * @version.coseng
     */
    protected static Set<Platform> get() {
        return platforms;
    }

    /**
     * Gets the supported platform for a given location.
     *
     * @param location
     *            the location
     * @return the supported platform
     * @see org.openqa.selenium.Platform
     * @see com.sios.stc.coseng.run.Location
     * @since 2.0
     * @version.coseng
     */
    protected static Set<Platform> getSupported(Location location) {
        Set<Platform> platforms = new HashSet<Platform>();
        if (Location.GRID.equals(location)) {
            platforms = Platforms.platforms;
        } else {
            Platform osPlatform = OperatingSystem.getPlatform();
            if (platforms.contains(osPlatform)) {
                platforms.add(osPlatform);
            }
        }
        return platforms;
    }

    /**
     * Checks if is supported browser.
     *
     * @param location
     *            the location
     * @param platform
     *            the platform
     * @param browser
     *            the browser
     * @return true, if is supported browser
     * @see com.sios.stc.coseng.run.Location
     * @see org.openqa.selenium.Platform
     * @see com.sios.stc.coseng.run.Platforms#isSupported(Location, Platform)
     * @see com.sios.stc.coseng.run.Browsers
     * @since 2.0
     * @version.coseng
     */
    protected static boolean isSupportedBrowser(Location location, Platform platform,
            Browser browser) {
        if (platformBrowsers.containsKey(platform)) {
            Set<Browser> browsers = platformBrowsers.get(platform);
            if (isSupported(location, platform) && browsers.contains(browser)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the supported browsers.
     *
     * @param location
     *            the location
     * @param platform
     *            the platform
     * @return the supported browsers
     * @see com.sios.stc.coseng.run.Location
     * @see org.openqa.selenium.Platform
     * @see com.sios.stc.coseng.run.Platforms#isSupported(Location, Platform)
     * @since 2.0
     * @version.coseng
     */
    protected static Set<Browser> getSupportedBrowsers(Location location, Platform platform) {
        if (platformBrowsers.containsKey(platform)) {
            if (isSupported(location, platform)) {
                return platformBrowsers.get(platform);
            }
        }
        return new HashSet<Browser>();
    }

}
