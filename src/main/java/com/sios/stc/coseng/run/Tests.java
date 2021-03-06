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

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * The Class Tests provides the deserialized collection of Test from the JSON
 * configuration file.
 *
 * @since 2.0
 * @version.coseng
 */
class Tests {

    private int maxTestExecutionMinutes = 0;

    @Expose
    private final List<Test> tests = new ArrayList<Test>();

    /**
     * Adds the test to the collection.
     *
     * @param test
     *            the test; may not be null
     * @see com.sios.stc.coseng.run.Test
     * @since 2.0
     * @version.coseng
     */
    protected void add(Test test) {
        if (test != null) {
            this.tests.add(test);
        }
    }

    /**
     * Adds all the tests to the collection.
     *
     * @param tests
     *            the collection tests; may not be null
     * @see com.sios.stc.coseng.run.Test
     * @since 2.0
     * @version.coseng
     */
    protected void addAll(List<Test> tests) {
        if (tests != null) {
            this.tests.addAll(tests);
        }
    }

    /**
     * Size of the collection of tests.
     *
     * @return the int size
     * @since 2.0
     * @version.coseng
     */
    protected int size() {
        return tests.size();
    }

    /**
     * Removes the all the tests from the collection if they exist in the
     * collection.
     *
     * @param tests
     *            the collection of tests; may not be null
     * @see com.sios.stc.coseng.run.Test
     * @since 2.0
     * @version.coseng
     */
    protected void removeAll(List<Test> tests) {
        if (tests != null) {
            for (Test test : tests) {
                if (this.tests.contains(test)) {
                    this.tests.remove(test);
                }
            }
        }
    }

    /**
     * Gets the all the test.
     *
     * @return the collection of test
     * @see com.sios.stc.coseng.run.Test
     * @since 2.0
     * @version.coseng
     */
    protected List<Test> getAll() {
        return this.tests;
    }

    /**
     * Checks for test.
     *
     * @param test
     *            the test
     * @return true, if successful
     * @see com.sios.stc.coseng.run.Test
     * @since 2.0
     * @version.coseng
     */
    protected boolean hasTest(Test test) {
        return tests.contains(test);
    }

    /**
     * Gets the test by name.
     *
     * @param name
     *            the name; may not be null or empty
     * @return the test
     * @see com.sios.stc.coseng.run.Test#getName()
     * @since 2.0
     * @version.coseng
     */
    protected Test getTest(String name) {
        if (name != null && !name.isEmpty()) {
            for (Test test : tests) {
                if (test != null) {
                    if (name.equals(test.getName())) {
                        return test;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets the names of each test.
     *
     * @return the collection of test names
     * @see com.sios.stc.coseng.run.Test#getName()
     * @since 2.0
     * @version.coseng
     */
    protected List<String> getNames() {
        List<String> names = new ArrayList<String>();
        for (Test test : tests) {
            if (test != null) {
                String name = test.getName();
                if (name != null && !name.isEmpty()) {
                    names.add(name);
                }
            }
        }
        return names;
    }

    /**
     * Gets the report directories for each of the test.
     *
     * @return the collection of report directories
     * @see com.sios.stc.coseng.run.Node
     * @see com.sios.stc.coseng.run.Test#getReportDirectory()
     * @since 2.0
     * @version.coseng
     */
    protected List<String> getReportDirectories() {
        List<String> directories = new ArrayList<String>();
        for (Test test : tests) {
            if (test != null) {
                directories.add(test.getReportDirectory());
            }
        }
        return directories;
    }

    /**
     * Gets the name of each failed test.
     *
     * @return the collection of failed test names
     * @see com.sios.stc.coseng.run.Test#getName()
     * @see com.sios.stc.coseng.run.Test#isFailed()
     * @since 2.0
     * @version.coseng
     */
    protected List<String> getFailed() {
        List<String> failed = new ArrayList<String>();
        for (Test test : tests) {
            if (test != null) {
                if (test.isFailed()) {
                    failed.add(test.getName());
                }
            }
        }
        return failed;
    }

    /**
     * Gets the max test execution minutes.
     *
     * @return the max test execution minutes
     * @since 2.1
     * @version.coseng
     */
    protected int getMaxTestExecutionMinutes() {
        return this.maxTestExecutionMinutes;
    }

    /**
     * Sets the max test execution minutes.
     *
     * @param maxTestExecutionMinutes
     *            the new max test execution minutes
     * @since 2.1
     * @version.coseng
     */
    protected void setMaxTestExecutionMinutes(int maxTestExecutionMinutes) {
        if (maxTestExecutionMinutes > 0) {
            this.maxTestExecutionMinutes = maxTestExecutionMinutes;
        }
    }

}
