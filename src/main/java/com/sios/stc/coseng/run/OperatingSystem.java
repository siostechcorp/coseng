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
import java.util.Locale;

import org.openqa.selenium.Platform;

/**
 * The Class OperatingSystem.
 *
 * @since 2.0
 * @version.coseng
 */
class OperatingSystem {
    /**
     * The Enum SupportedOs.
     *
     * @since 2.0
     * @version.coseng
     */
    protected static enum SupportedOs {
        LINUX, WIN10;
    }

    private static final String OS_NAME_LINUX      = "linux";
    private static final String OS_NAME_WINDOWS    = "windows";
    private static final String OS_VERSION_WINDOWS = "10.";

    private static final String osName    =
            System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
    private static final String osVersion =
            System.getProperty("os.version").toLowerCase(Locale.ENGLISH);

    /**
     * Gets the platform.
     *
     * @return the platform
     * @since 2.0
     * @version.coseng
     */
    protected static Platform getPlatform() {
        if (isSupported()) {
            if (osName.contains(OS_NAME_LINUX)) {
                return Platform.LINUX;
            }
            if (osName.contains(OS_NAME_WINDOWS)) {
                return Platform.WIN10;
            }
        }
        return null;
    }

    /**
     * Checks if is supported.
     *
     * @return true, if is supported
     * @since 2.0
     * @version.coseng
     */
    protected static boolean isSupported() {
        if (osName.contains(OS_NAME_LINUX)) {
            return true;
        }
        if (osName.contains(OS_NAME_WINDOWS)) {
            if (osVersion.contains(OS_VERSION_WINDOWS)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the supported.
     *
     * @return the supported
     * @since 2.0
     * @version.coseng
     */
    protected static List<String> getSupported() {
        List<String> options = new ArrayList<String>();
        for (SupportedOs os : SupportedOs.values()) {
            options.add(os.name());
        }
        return options;
    }

    /**
     * Name and version.
     *
     * @return the string
     * @since 2.0
     * @version.coseng
     */
    protected static String nameAndVersion() {
        return "os [" + osName + "], version [" + osVersion + "]";
    }

}
