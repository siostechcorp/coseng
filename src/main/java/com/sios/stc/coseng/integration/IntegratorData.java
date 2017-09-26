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
 * The Interface IntegratorData.
 *
 * @since 3.0
 * @version.coseng
 */
public interface IntegratorData {

    /**
     * Gets the integrator data.
     *
     * @param dataClass
     *            the data class
     * @return the integrator data
     * @since 3.0
     * @version.coseng
     */
    public Data getIntegratorData(Class<?> dataClass);

    /**
     * Checks for integrator data.
     *
     * @param dataClass
     *            the data class
     * @return true, if successful
     * @since 3.0
     * @version.coseng
     */
    public boolean hasIntegratorData(Class<?> dataClass);

    /**
     * Adds the integrator data.
     *
     * @param data
     *            the data
     * @return true, if successful
     * @since 3.0
     * @version.coseng
     */
    public boolean addIntegratorData(Data data);

    /**
     * Removes the integrator data.
     *
     * @param dataClass
     *            the data class
     * @return true, if successful
     * @since 3.0
     * @version.coseng
     */
    public boolean removeIntegratorData(Class<?> dataClass);

    /**
     * Clear integrator data.
     *
     * @since 3.0
     * @version.coseng
     */
    public void clearIntegratorData();

}
