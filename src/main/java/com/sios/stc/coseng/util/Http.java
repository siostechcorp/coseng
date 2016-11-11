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
package com.sios.stc.coseng.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.time.StopWatch;

/**
 * The Class Http offers conveniences to discern state of Http resrouces.
 *
 * @since 2.1
 * @version.coseng
 */
public class Http {

    private static final String         sysPropUserAgent     = "http.agent";
    private static final String         requestMethodDefault = "HEAD";
    private static final String         userAgentNew         =
            "Mozilla/5.0 (X11; Linux x86_64; rv:49.0) Gecko/20100101 Firefox/49.0";
    private static int                  millisTimeoutDefault = 3000;
    private static Map<String, Integer> urlsResponseCode     = new HashMap<String, Integer>();

    /**
     * Checks if http url is accessible.
     *
     * @param url
     *            the url
     * @return true, if is accessible
     * @see com.sios.stc.coseng.util.Http#isAccessible(String, Integer)
     * @since 2.1
     * @version.coseng
     */
    public static boolean isAccessible(String url) {
        return isAccessible(url, null);
    }

    /**
     * Checks if http url is accessible.
     *
     * @param url
     *            the url
     * @param millisTimeout
     *            the millisecond timeout
     * @return true, if is accessible
     * @see com.sios.stc.coseng.util.Http#connect(String, Integer, String,
     *      boolean)
     * @see com.sios.stc.coseng.util.Http#accessibleResponseCode(int)
     * @since 2.1
     * @version.coseng
     */
    public static boolean isAccessible(String url, Integer millisTimeout) {
        int responseCode = connect(url, millisTimeout, null, false);
        boolean isAccessible = false;
        /* Any 200 level through 300 level identified as success */
        if (accessibleResponseCode(responseCode)) {
            isAccessible = true;
        } else {
            /* Attempt again with modified user agent */
            responseCode = connect(url, millisTimeout, null, true);
            if (accessibleResponseCode(responseCode)) {
                isAccessible = true;
            }
        }
        urlsResponseCode.put(url, responseCode);
        return isAccessible;
    }

    /**
     * Accessible response code. Response code >= 200 and <= 399 is considered
     * accessible.
     *
     * @param responseCode
     *            the status code
     * @return true, if successful
     * @see com.sios.stc.coseng.util.Http#isAccessible(String, Integer)
     * @since 2.1
     * @version.coseng
     */
    private static boolean accessibleResponseCode(int responseCode) {
        if (responseCode >= 200 && responseCode <= 399) {
            return true;
        }
        return false;
    }

    /**
     * Gets the url response code.
     *
     * @param url
     *            the url
     * @return the url response code
     * @since 2.1
     * @version.coseng
     */
    public static Integer getResponseCode(String url) {
        return urlsResponseCode.get(url);
    }

    /**
     * Connect. Accepts invalid SSL certs. Derived code.
     * 
     * @link http://stackoverflow.com/questions/13778635/checking-status-of-website-in-java
     * @link http://stackexchange.com/users/347553/bhavik-ambani
     * @link https://opensource.org/licenses/MIT
     * @author Bhavik Ambani
     * @author James B. Crocker
     *
     * @param url
     *            the url
     * @param millisTimeout
     *            the millis timeout
     * @return the int
     * @see com.sios.stc.coseng.util.Http#isAccessible(String, Integer)
     * @since 2.1
     * @version.coseng
     */
    private static int connect(String url, Integer millisTimeout, String requestMethod,
            boolean changeUserAgent) {
        if (url != null && !url.isEmpty()) {
            String userAgentOriginal = System.getProperty(sysPropUserAgent);
            if (millisTimeout == null || millisTimeout <= 0) {
                millisTimeout = millisTimeoutDefault;
            }
            if (requestMethod == null || requestMethod.isEmpty()) {
                requestMethod = requestMethodDefault;
            }
            /* Fake out the user agent when asked */
            if (changeUserAgent) {
                System.setProperty(sysPropUserAgent, userAgentNew);
            }
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            /* Accept any/all certs */
            SSLUtilities.trustAllHostnames();
            SSLUtilities.trustAllHttpsCertificates();
            do {
                try {
                    HttpURLConnection connection =
                            (HttpURLConnection) new URL(url).openConnection();
                    connection.setConnectTimeout(millisTimeout);
                    connection.setReadTimeout(millisTimeout);
                    connection.setRequestMethod(requestMethod);
                    int responseCode = connection.getResponseCode();
                    /* Change back to original user agent if changed */
                    if (changeUserAgent && userAgentOriginal != null) {
                        System.setProperty(sysPropUserAgent, userAgentOriginal);
                    }
                    return responseCode;
                } catch (IOException | ClassCastException e) {
                    // do nothing; will be 0
                }
            } while (stopWatch.getTime() < millisTimeout);
            stopWatch.stop();
        }
        return 0;
    }

}
