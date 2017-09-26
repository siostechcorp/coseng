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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.sios.stc.coseng.RunTests;
import com.sios.stc.coseng.integration.Integrator;
import com.sios.stc.coseng.integration.Integrator.TriggerOn;
import com.sios.stc.coseng.integration.versionone.Configuration;
import com.sios.stc.coseng.integration.versionone.VersionOne;
import com.sios.stc.coseng.util.Resource;

/**
 * The Class GetIntegrators for deserializing integrator configuration resources
 * and validating the parameters.
 * 
 * @since 3.0
 * @version.coseng
 */
class GetIntegrators {

    private static final Logger log = LogManager.getLogger(RunTests.class.getName());

    private static final List<Integrator> wiredIntegrators = new ArrayList<Integrator>();

    protected static void with(Class<?> clazz, String file) throws CosengException {
        if (clazz != null && file != null) {
            if (clazz.equals(VersionOne.class)) {
                versionOne(file);
            }
        }
    }

    /**
     * Wired.
     *
     * @return the list
     * @since 3.0
     * @version.coseng
     */
    protected static List<Integrator> wired() {
        return wiredIntegrators;
    }

    /**
     * With the given VersionOne JSON file deserialize the JSON into the
     * corresponding VersionOne class.
     *
     * @param jsonVersionOne
     *            the json versionone file
     * @param jsonVersionOneInput
     *            the json versionone input stream
     * @return the tests
     * @throws CosengException
     *             the coseng exception for caught JsonIOException,
     *             JsonSyntaxException, IllegalArgumentException,
     *             NullPointerException or CosengException
     * @see com.sios.stc.coseng.integration.Data
     * @see com.sios.stc.coseng.integration.Integrator
     * @see com.sios.stc.coseng.integration.IntegratorData
     * @see com.sios.stc.coseng.integration.versionone.Backlog
     * @see com.sios.stc.coseng.integration.versionone.Configuration
     * @see com.sios.stc.coseng.integration.versionone.Field
     * @see com.sios.stc.coseng.integration.versionone.Oid
     * @see com.sios.stc.coseng.integration.versionone.ParamFields
     * @see com.sios.stc.coseng.integration.versionone.Test
     * @see com.sios.stc.coseng.integration.versionone.VersionOne
     * @see com.sios.stc.coseng.integration.versionone.VersionOneData
     * @since 3.0
     * @version.coseng
     */
    private static void versionOne(String jsonVersionOne) throws CosengException {
        try {
            // Deserialize any case of TriggerOn
            JsonDeserializer<TriggerOn> triggerOnTypeDeserializer =
                    new JsonDeserializer<TriggerOn>() {
                        public TriggerOn deserialize(JsonElement json, Type typeOfT,
                                JsonDeserializationContext context) throws JsonParseException {
                            return TriggerOn.valueOf(json.getAsString().toUpperCase());
                        }
                    };
            Map<Class<?>, JsonDeserializer<?>> typeAdapters =
                    new HashMap<Class<?>, JsonDeserializer<?>>();
            typeAdapters.put(TriggerOn.class, triggerOnTypeDeserializer);
            Configuration configuration = (Configuration) Resource.getObjectFromJson(jsonVersionOne,
                    typeAdapters, Configuration.class);
            addIntegrator(new VersionOne(configuration));
            log.info("Using integrator [VersionOne]");
        } catch (JsonIOException | JsonSyntaxException | IllegalArgumentException
                | NullPointerException | CosengException e) {
            throw new CosengException("Exception reading VersionOne JSON [" + jsonVersionOne + "]",
                    e);
        }
    }

    /**
     * Adds the integrator.
     *
     * @param integrator
     *            the integrator
     * @since 3.0
     * @version.coseng
     */
    private static void addIntegrator(Integrator integrator) {
        if (integrator != null) {
            wiredIntegrators.add(integrator);
        }
    }

}
