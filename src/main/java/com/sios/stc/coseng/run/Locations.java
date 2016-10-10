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

/**
 * The Class Locations defines the execution of Selenium tests; either locally
 * with {@code NODE} or via the Selenium GRID Hub with {@code GRID}.
 *
 * @since 2.0
 * @version.coseng
 */
class Locations {
    /**
     * The Enum Location.
     *
     * @since 2.0
     * @version.coseng
     */
    protected static enum Location {
        NODE, GRID
    };

    protected static List<String> get() {
        List<String> locations = new ArrayList<String>();
        for (Location location : Location.values()) {
            locations.add(location.toString());
        }
        return locations;
    }
}
