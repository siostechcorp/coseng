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

import com.sios.stc.coseng.Common;
import com.sios.stc.coseng.integration.Data;
import com.sios.stc.coseng.integration.Integrator.TriggerOn;

/**
 * The Class VersionOneData.
 *
 * @since 3.0
 * @version.coseng
 */
public class VersionOneData extends Data {

    public static final String LINE_BREAK_BEGIN = "<p>";
    public static final String LINE_BREAK_END   = "</p>";
    public static final String BOLD_BEGIN       = "<strong>";
    public static final String BOLD_END         = "</strong>";
    public static final String ITALIC_BEGIN     = "<em>";
    public static final String ITALIC_END       = "</em>";

    protected static final String ATTR_NAME             = "Name";
    protected static final String ATTR_STATUS           = "Status";
    protected static final String ATTR_TIMEBOX          = "Timebox";
    protected static final String ATTR_DESCRIPTION      = "Description";
    protected static final String ATTR_SETUP            = "Setup";
    protected static final String ATTR_INPUTS           = "Inputs";
    protected static final String ATTR_STEPS            = "Steps";
    protected static final String ATTR_EXPECTED_RESULTS = "ExpectedResults";
    protected static final String ATTR_ACTUAL_RESULTS   = "ActualResults";

    /*
     * For each list of Fields the TriggerOn is treated as the unique key to
     * assure only one field of that type in the list
     */
    private Field                 backlogTimebox          = new Field();
    private List<Field>           backlogName             = new ArrayList<Field>();
    private List<Field>           backlogDescription      = new ArrayList<Field>();
    private List<Field>           testName                = new ArrayList<Field>();
    private List<Field>           testDescription         = new ArrayList<Field>();
    private List<Field>           testSetup               = new ArrayList<Field>();
    private List<Field>           testInputs              = new ArrayList<Field>();
    private List<Field>           testSteps               = new ArrayList<Field>();
    private List<Field>           testStepExpectedResults = new ArrayList<Field>();
    private List<Field>           testStepActualResults   = new ArrayList<Field>();
    private com.versionone.Oid    backlogOid              = null;
    private com.versionone.Oid    testOid                 = null;
    private static final String   ASSERT_RESULT_SEPARATOR = ": ";
    protected static final String PARAM_VALUE_SEPARATOR   = " = ";

    /*
     * (non-Javadoc)
     * 
     * @see com.sios.stc.coseng.integration.Data#deepCopy()
     */
    @Override
    public VersionOneData deepCopy() {
        return new VersionOneData(this);
    }

    /**
     * Instantiates a new version one data.
     * 
     * @since 3.0
     * @version.coseng
     */
    protected VersionOneData() {
        // do nothing
    }

    /**
     * Instantiates a new version one data.
     *
     * @param original
     *            the original
     */
    private VersionOneData(VersionOneData original) {
        this.backlogTimebox = new Field(original.backlogTimebox);
        for (Field field : original.backlogName) {
            this.backlogName.add(new Field(field));
        }
        for (Field field : original.backlogDescription) {
            this.backlogDescription.add(new Field(field));
        }
        for (Field field : original.testName) {
            this.testName.add(new Field(field));
        }
        for (Field field : original.testDescription) {
            this.testDescription.add(new Field(field));
        }
        for (Field field : original.testSetup) {
            this.testSetup.add(new Field(field));
        }
        for (Field field : original.testInputs) {
            this.testInputs.add(new Field(field));
        }
        for (Field field : original.testSteps) {
            this.testSteps.add(new Field(field));
        }
        for (Field field : original.testStepExpectedResults) {
            this.testStepExpectedResults.add(new Field(field));
        }
        for (Field field : original.testStepActualResults) {
            this.testStepActualResults.add(new Field(field));
        }
        this.backlogOid = original.backlogOid;
        this.testOid = original.testOid;
    }

    /**
     * Gets the backlog fields.
     *
     * @param trigger
     *            the trigger
     * @return the backlog fields
     * @since 3.0
     * @version.coseng
     */
    protected List<Field> getBacklogFields(TriggerOn trigger) {
        List<Field> fields = new ArrayList<Field>();
        if (trigger != null) {
            List<List<Field>> backlogItems = new ArrayList<List<Field>>();
            backlogItems.add(backlogName);
            backlogItems.add(backlogDescription);
            for (List<Field> bi : backlogItems) {
                Field field = getField(bi, trigger);
                if (field != null) {
                    fields.add(field);
                }
            }
            if (backlogTimebox != null && trigger.equals(backlogTimebox.getTriggerOn())) {
                fields.add(backlogTimebox);
            }
        }
        return fields;
    }

    /**
     * Gets the test fields.
     *
     * @param trigger
     *            the trigger
     * @return the test fields
     * @since 3.0
     * @version.coseng
     */
    protected List<Field> getTestFields(TriggerOn trigger) {
        List<Field> fields = new ArrayList<Field>();
        if (trigger != null) {
            List<List<Field>> testItems = new ArrayList<List<Field>>();
            testItems.add(testName);
            testItems.add(testDescription);
            testItems.add(testSetup);
            testItems.add(testInputs);
            testItems.add(testSteps);
            testItems.add(testStepExpectedResults);
            testItems.add(testStepActualResults);
            for (List<Field> ti : testItems) {
                Field field = getField(ti, trigger);
                if (field != null) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    /**
     * Gets the backlog timebox.
     *
     * @return the backlog timebox
     * @since 3.0
     * @version.coseng
     */
    protected Field getBacklogTimebox() {
        return backlogTimebox;
    }

    /**
     * Sets the backlog timebox.
     *
     * @param sprintOid
     *            the new backlog timebox
     * @since 3.0
     * @version.coseng
     */
    protected void setBacklogTimebox(String sprintOid) {
        if (sprintOid != null) {
            backlogTimebox = new Field(ATTR_TIMEBOX, Common.STRING_EMPTY, sprintOid,
                    TriggerOn.EXECUTIONSTART);
        }
    }

    /**
     * Gets the backlog name.
     *
     * @return the backlog name
     * @since 3.0
     * @version.coseng
     */
    public String getBacklogName() {
        return getValue(backlogName, TriggerOn.EXECUTIONSTART);
    }

    /**
     * Sets the backlog name.
     *
     * @param name
     *            the new backlog name
     * @since 3.0
     * @version.coseng
     */
    public void setBacklogName(String name) {
        if (name != null && !name.isEmpty()) {
            setOrAddField(true, backlogName, name, ATTR_NAME, TriggerOn.EXECUTIONFINISH);
            setOrAddField(true, backlogName, name, ATTR_NAME, TriggerOn.EXECUTIONSTART);
        }
    }

    /**
     * Gets the backlog description.
     *
     * @return the backlog description
     * @since 3.0
     * @version.coseng
     */
    public String getBacklogDescription() {
        return getValue(backlogDescription, TriggerOn.EXECUTIONSTART);
    }

    /**
     * Sets the backlog description.
     *
     * @param description
     *            the new backlog description
     * @since 3.0
     * @version.coseng
     */
    public void setBacklogDescription(String description) {
        if (description != null && !description.isEmpty()) {
            setOrAddField(true, backlogDescription, description, ATTR_DESCRIPTION,
                    TriggerOn.EXECUTIONFINISH);
            setOrAddField(true, backlogDescription, description, ATTR_DESCRIPTION,
                    TriggerOn.EXECUTIONSTART);
        }
    }

    /**
     * Gets the test name.
     *
     * @return the test name
     * @since 3.0
     * @version.coseng
     */
    public String getTestName() {
        return getValue(testName, TriggerOn.METHODSTART);
    }

    /**
     * Sets the test name.
     *
     * @param name
     *            the new test name
     * @since 3.0
     * @version.coseng
     */
    public void setTestName(String name) {
        if (name != null && !name.isEmpty()) {
            setOrAddField(true, testName, name, ATTR_NAME, TriggerOn.METHODFINISH);
            setOrAddField(true, testName, name, ATTR_NAME, TriggerOn.METHODSTART);
        }
    }

    /**
     * Gets the test description.
     *
     * @return the test description
     * @since 3.0
     * @version.coseng
     */
    public String getTestDescription() {
        return getValue(testDescription, TriggerOn.METHODSTART);
    }

    /**
     * Sets the test description.
     *
     * @param description
     *            the new test description
     * @since 3.0
     * @version.coseng
     */
    public void setTestDescription(String description) {
        if (description != null && !description.isEmpty()) {
            setOrAddField(true, testDescription, description, ATTR_DESCRIPTION,
                    TriggerOn.METHODFINISH);
            setOrAddField(true, testDescription, description, ATTR_DESCRIPTION,
                    TriggerOn.METHODSTART);
        }
    }

    /**
     * Gets the test setup.
     *
     * @return the test setup
     * @since 3.0
     * @version.coseng
     */
    public String getTestSetup() {
        return getValue(testSetup, TriggerOn.METHODSTART);
    }

    /**
     * Sets the test setup.
     *
     * @param setup
     *            the new test setup
     * @since 3.0
     * @version.coseng
     */
    public void setTestSetup(String setup) {
        if (setup != null && !setup.isEmpty()) {
            setOrAddField(true, testSetup, setup, ATTR_SETUP, TriggerOn.METHODFINISH);
            setOrAddField(true, testSetup, setup, ATTR_SETUP, TriggerOn.METHODSTART);
        }
    }

    /**
     * Gets the test inputs.
     *
     * @return the test inputs
     * @since 3.0
     * @version.coseng
     */
    public String getTestInputs() {
        return getValue(testInputs, TriggerOn.METHODSTART);
    }

    /**
     * Sets the test inputs.
     *
     * @param inputs
     *            the new test inputs
     * @since 3.0
     * @version.coseng
     */
    public void setTestInputs(String inputs) {
        if (inputs != null && !inputs.isEmpty()) {
            setOrAddField(true, testInputs, inputs, ATTR_INPUTS, TriggerOn.METHODFINISH);
            setOrAddField(true, testInputs, inputs, ATTR_INPUTS, TriggerOn.METHODSTART);
        }
    }

    /**
     * Gets the test steps.
     *
     * @return the test steps
     * @since 3.0
     * @version.coseng
     */
    public String getTestSteps() {
        return getValue(testSteps, TriggerOn.METHODSTART);
    }

    /**
     * Adds the test step.
     *
     * @param stepMessage
     *            the step message
     * @since 3.0
     * @version.coseng
     */
    public void addTestStep(String stepMessage) {
        if (stepMessage != null && !stepMessage.isEmpty()) {
            String message = LINE_BREAK_BEGIN + stepMessage + LINE_BREAK_END;
            setOrAddField(false, testSteps, message, ATTR_STEPS, TriggerOn.METHODFINISH);
            setOrAddField(false, testSteps, message, ATTR_STEPS, TriggerOn.METHODSTART);
        }
    }

    /**
     * Gets the test step expected results.
     *
     * @return the test step expected results
     * @since 3.0
     * @version.coseng
     */
    public String getTestStepExpectedResults() {
        return getValue(testStepExpectedResults, TriggerOn.METHODSTART);
    }

    /**
     * Adds the test step expected result.
     *
     * @param stepMessage
     *            the step message
     * @param expectedResult
     *            the expected result
     * @since 3.0
     * @version.coseng
     */
    public void addTestStepExpectedResult(String stepMessage, String expectedResult) {
        if (stepMessage == null || stepMessage.isEmpty()) {
            stepMessage = Common.STRING_UNKNOWN;
        }
        String message = LINE_BREAK_BEGIN + stepMessage
                + (expectedResult != null ? ASSERT_RESULT_SEPARATOR + expectedResult : "")
                + LINE_BREAK_END;
        setOrAddField(false, testStepExpectedResults, message, ATTR_EXPECTED_RESULTS,
                TriggerOn.METHODFINISH);
        setOrAddField(false, testStepExpectedResults, message, ATTR_EXPECTED_RESULTS,
                TriggerOn.METHODSTART);
    }

    /**
     * Gets the test step actual results.
     *
     * @return the test step actual results
     * @since 3.0
     * @version.coseng
     */
    public String getTestStepActualResults() {
        return getValue(testStepActualResults, TriggerOn.METHODSTART);
    }

    /**
     * Adds the test step actual result.
     *
     * @param stepMessage
     *            the step message
     * @param actualResult
     *            the actual result
     * @since 3.0
     * @version.coseng
     */
    protected void addTestStepActualResult(String stepMessage, String actualResult) {
        if (stepMessage == null || stepMessage.isEmpty()) {
            stepMessage = Common.STRING_UNKNOWN;
        }
        String message = LINE_BREAK_BEGIN + stepMessage
                + (actualResult != null ? ASSERT_RESULT_SEPARATOR + actualResult : "")
                + LINE_BREAK_END;
        setOrAddField(false, testStepActualResults, message, ATTR_ACTUAL_RESULTS,
                TriggerOn.METHODFINISH);
        setOrAddField(false, testStepActualResults, message, ATTR_ACTUAL_RESULTS,
                TriggerOn.METHODSTART);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sios.stc.coseng.integration.Data#clearAllTestSteps()
     */
    @Override
    protected void clearAllTestSteps() {
        testSteps.clear();
        testStepExpectedResults.clear();
        testStepActualResults.clear();
    }

    /**
     * Gets the backlog oid.
     *
     * @return the backlog oid
     * @since 3.0
     * @version.coseng
     */
    protected com.versionone.Oid getBacklogOid() {
        return backlogOid;
    }

    /**
     * Sets the backlog oid.
     *
     * @param backlogOid
     *            the new backlog oid
     * @since 3.0
     * @version.coseng
     */
    protected void setBacklogOid(com.versionone.Oid backlogOid) {
        this.backlogOid = backlogOid;
    }

    /**
     * Gets the test oid.
     *
     * @return the test oid
     * @since 3.0
     * @version.coseng
     */
    protected com.versionone.Oid getTestOid() {
        return testOid;
    }

    /**
     * Sets the test oid.
     *
     * @param testOid
     *            the new test oid
     * @since 3.0
     * @version.coseng
     */
    protected void setTestOid(com.versionone.Oid testOid) {
        this.testOid = testOid;
    }

    /**
     * Sets the or add field.
     *
     * @param isSet
     *            the is set
     * @param fields
     *            the fields
     * @param value
     *            the value
     * @param attribute
     *            the attribute
     * @param trigger
     *            the trigger
     * @since 3.0
     * @version.coseng
     */
    private synchronized void setOrAddField(boolean isSet, List<Field> fields, String value,
            String attribute, TriggerOn trigger) {
        if (fields != null && value != null && !value.isEmpty() && attribute != null
                && !attribute.isEmpty() && trigger != null) {
            if (fields.isEmpty()) {
                fields.add(new Field(attribute, Common.STRING_EMPTY, value, trigger));
            } else if (isSet) {
                Field field = getField(fields, trigger);
                if (field != null) {
                    field.setValue(value);
                } else {
                    fields.add(new Field(attribute, Common.STRING_EMPTY, value, trigger));
                }
            } else {
                for (Field field : fields) {
                    /* Append/add to the value of same trigger */
                    if (trigger.equals(field.getTriggerOn())) {
                        field.setValue(
                                field.getValue() + LINE_BREAK_BEGIN + value + LINE_BREAK_END);
                    }
                }
            }
        }
    }

    /**
     * Gets the field.
     *
     * @param fields
     *            the fields
     * @param trigger
     *            the trigger
     * @return the field
     * @since 3.0
     * @version.coseng
     */
    private synchronized Field getField(List<Field> fields, TriggerOn trigger) {
        if (fields != null && trigger != null) {
            for (Field field : fields) {
                if (trigger.equals(field.getTriggerOn())) {
                    return field;
                }
            }
        }
        return null;
    }

    /**
     * Gets the value.
     *
     * @param fields
     *            the fields
     * @param trigger
     *            the trigger
     * @return the value
     * @since 3.0
     * @version.coseng
     */
    private synchronized String getValue(List<Field> fields, TriggerOn trigger) {
        String value = Common.STRING_EMPTY;
        Field field = getField(fields, trigger);
        if (field != null) {
            value = field.getValue();
        }
        return value;
    }

}
