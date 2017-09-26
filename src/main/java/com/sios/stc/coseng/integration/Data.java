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
package com.sios.stc.coseng.integration;

/**
 * The Class Data. Stub for integrator data exchange. All integrator data
 * classes should extend Data and provide a robust deepCopy. Remaining fields
 * and methods will be implementation specific.
 *
 * @since 3.0
 * @version.coseng
 */
public abstract class Data {

    /**
     * Deep copy.
     *
     * @return the data
     * @since 3.0
     * @version.coseng
     */
    abstract public Data deepCopy();

    /**
     * Clear all test steps.
     *
     * @since 3.0
     * @version.coseng
     */
    abstract protected void clearAllTestSteps();

}
