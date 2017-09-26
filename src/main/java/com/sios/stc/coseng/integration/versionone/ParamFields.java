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
package com.sios.stc.coseng.integration.versionone;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.sios.stc.coseng.Common;

/**
 * The Class ParamFields.
 *
 * @since 3.0
 * @version.coseng
 */
public class ParamFields {

    @Expose
    private final String      name   = null;
    @Expose
    private final String      value  = null;
    @Expose
    private final List<Field> fields = new ArrayList<Field>();

    /**
     * Gets the param name.
     *
     * @return the param name
     * @since 3.0
     * @version.coseng
     */
    public String getParamName() {
        if (name != null) {
            return name;
        }
        return Common.STRING_EMPTY;
    }

    /**
     * Gets the param value.
     *
     * @return the param value
     * @since 3.0
     * @version.coseng
     */
    public String getParamValue() {
        if (value != null) {
            return value;
        }
        return Common.STRING_EMPTY;
    }

    /**
     * Gets the fields.
     *
     * @return the fields
     * @since 3.0
     * @version.coseng
     */
    public List<Field> getFields() {
        return fields;
    }
}
