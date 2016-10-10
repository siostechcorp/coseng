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

/**
 * The Class Message.
 *
 * @since 2.0
 * @version.coseng
 */
class Message {
    /**
     * Details.
     *
     * @param name
     *            the name
     * @param detail
     *            the detail
     * @return the message string referencing the details by name
     * @since 2.0
     * @version.coseng
     */
    protected static String details(String name, String detail) {
        String log = "Test name [" + name + "]: ";
        if (detail != null) {
            log += detail;
        }
        return log;
    }
}
