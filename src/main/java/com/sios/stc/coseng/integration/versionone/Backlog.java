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
import com.sios.stc.coseng.integration.Integrator.TriggerOn;

/**
 * The Class Backlog.
 *
 * @since 3.0
 * @version.coseng
 */
public class Backlog {

    @Expose
    private final String      titlePrefix = null;
    @Expose
    private final List<Field> fields      = null;

    /**
     * Gets the title prefix.
     *
     * @return the title prefix
     * @since 3.0
     * @version.coseng
     */
    public String getTitlePrefix() {
        if (titlePrefix != null) {
            return titlePrefix;
        }
        return Common.STRING_EMPTY;
    }

    /**
     * Gets the fields.
     *
     * @param trigger
     *            the trigger
     * @return the fields
     * @since 3.0
     * @version.coseng
     */
    public List<Field> getFields(TriggerOn trigger) {
        List<Field> matchedFields = new ArrayList<Field>();
        if (trigger != null && fields != null) {
            for (Field field : fields) {
                if (trigger.equals(field.getTriggerOn())) {
                    matchedFields.add(field);
                }
            }
        }
        return matchedFields;
    }

    /**
     * Gets the field.
     *
     * @param attribute
     *            the attribute
     * @param trigger
     *            the trigger
     * @return the field
     * @since 3.0
     * @version.coseng
     */
    public Field getField(String attribute, TriggerOn trigger) {
        if (attribute != null && !attribute.isEmpty()) {
            for (Field field : getFields(trigger)) {
                if (attribute.equals(field.getAttribute())) {
                    return field;
                }
            }
        }
        return new Field();
    }

}
