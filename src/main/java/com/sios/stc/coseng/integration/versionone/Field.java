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

import com.google.gson.annotations.Expose;
import com.sios.stc.coseng.Common;
import com.sios.stc.coseng.integration.Integrator;

/**
 * The Class Field.
 *
 * @since 3.0
 * @version.coseng
 */
public class Field {

    @Expose
    private String               attribute = null;
    @Expose
    private String               name      = null;
    @Expose
    private String               value     = null;
    @Expose
    private Integrator.TriggerOn triggerOn = null;

    /**
     * Instantiates a new field.
     *
     * @since 3.0
     * @version.coseng
     */
    protected Field() {
        // do nothing
    }

    /**
     * Instantiates a new field.
     *
     * @param field
     *            the field
     * @since 3.0
     * @version.coseng
     */
    protected Field(Field field) {
        this.attribute = field.attribute;
        this.name = field.name;
        this.value = field.value;
        this.triggerOn = field.triggerOn;
    }

    /**
     * Instantiates a new field.
     *
     * @param attribute
     *            the attribute
     * @param name
     *            the name
     * @param value
     *            the value
     * @param triggerOn
     *            the triggerOn
     * @since 3.0
     * @version.coseng
     */
    protected Field(String attribute, String name, String value, Integrator.TriggerOn triggerOn) {
        this.attribute = attribute;
        this.name = name;
        this.value = value;
        this.triggerOn = triggerOn;
    }

    /**
     * Gets the attribute.
     *
     * @return the attribute
     * @since 3.0
     * @version.coseng
     */
    public String getAttribute() {
        if (attribute != null) {
            return attribute;
        }
        return Common.STRING_EMPTY;
    }

    /**
     * Gets the name.
     *
     * @return the name
     * @since 3.0
     * @version.coseng
     */
    public String getName() {
        if (name != null) {
            return name;
        }
        return Common.STRING_EMPTY;
    }

    /**
     * Gets the value.
     *
     * @return the value
     * @since 3.0
     * @version.coseng
     */
    public String getValue() {
        if (value != null) {
            return value;
        }
        return Common.STRING_EMPTY;
    }

    /**
     * Sets the value.
     *
     * @param value
     *            the new value
     * @since 3.0
     * @version.coseng
     */
    protected void setValue(String value) {
        if (value != null && !value.isEmpty()) {
            this.value = value;
        }
    }

    /**
     * Gets the triggerOn.
     *
     * @return the triggerOn
     * @since 3.0
     * @version.coseng
     */
    public Integrator.TriggerOn getTriggerOn() {
        if (triggerOn != null) {
            return triggerOn;
        }
        return Integrator.TriggerOn.UNKNOWN;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "attribute [" + this.getAttribute() + "], name [" + this.getName() + "], value ["
                + this.getValue() + "], triggerOn [" + this.getTriggerOn().toString() + "]";
    }

}
