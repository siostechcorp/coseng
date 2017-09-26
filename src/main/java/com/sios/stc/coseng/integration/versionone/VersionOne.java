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

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.sios.stc.coseng.Common;
import com.sios.stc.coseng.RunTests;
import com.sios.stc.coseng.integration.Integrator;
import com.sios.stc.coseng.run.CosengException;
import com.sios.stc.coseng.run.Test;
import com.sios.stc.coseng.util.Resource;
import com.versionone.Oid;
import com.versionone.apiclient.Asset;
import com.versionone.apiclient.Query;
import com.versionone.apiclient.Services;
import com.versionone.apiclient.V1Connector;
import com.versionone.apiclient.exceptions.APIException;
import com.versionone.apiclient.exceptions.ConnectionException;
import com.versionone.apiclient.exceptions.OidException;
import com.versionone.apiclient.exceptions.V1Exception;
import com.versionone.apiclient.interfaces.IAssetType;
import com.versionone.apiclient.interfaces.IAttributeDefinition;
import com.versionone.apiclient.interfaces.IServices;
import com.versionone.apiclient.services.QueryResult;

/**
 * The Class VersionOne.
 *
 * @since 3.0
 * @version.coseng
 */
public class VersionOne extends Integrator {

    private static final Logger log = LogManager.getLogger(RunTests.class.getName());

    private static final String INTEGRATOR_NAME = "VersionOne";
    private static final String ASSET_TEST      = "Test";
    private static final String ASSET_BACKLOG   = "Story";
    private static final String ATTR_SEPARATOR  = ":";
    private static final String NAME_SEPARATOR  = " - ";
    private static final String PACKAGE_ROOT    = "com.sios.stc.coseng.tests.";

    private static V1Connector   connector;
    private static IServices     services;
    private static Oid           projectOid;
    private static Oid           sprintOid;
    private static Configuration configuration;
    private static IAssetType    typeBacklog;
    private static IAssetType    typeTest;
    private static String        backlogTitlePrefix = Common.STRING_EMPTY;

    /**
     * Instantiates a new version one.
     *
     * @param configuration
     *            the configuration
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    public VersionOne(Configuration configuration) throws CosengException {
        /* Validate the connector */
        if (configuration == null || configuration.getAccessToken().isEmpty()
                || configuration.getApplicationName().isEmpty()
                || configuration.getInstanceUrl().isEmpty()
                || configuration.getProjectName().isEmpty()
                || configuration.getSprintName().isEmpty() || configuration.getVersion().isEmpty()
                || !hasRequiredFields(configuration)) {
            throw new CosengException(
                    "Integrator [VersionOne] configuration parameters misconfigured");
        }
        VersionOne.configuration = configuration;
        String version = configuration.getVersion();
        String instanceUrl = configuration.getInstanceUrl();
        String accessToken = configuration.getAccessToken();
        String applicationName = configuration.getApplicationName();
        String projectName = configuration.getProjectName();
        String sprintName = configuration.getSprintName();
        if (!configuration.getBacklog().getTitlePrefix().isEmpty()) {
            backlogTitlePrefix = configuration.getBacklog().getTitlePrefix();
        }
        try {
            connector = V1Connector.withInstanceUrl(instanceUrl)
                    .withUserAgentHeader(applicationName, version).withAccessToken(accessToken)
                    .build();
            services = new Services(connector);
            typeBacklog = services.getMeta().getAssetType(ASSET_BACKLOG);
            typeTest = services.getMeta().getAssetType(ASSET_TEST);
        } catch (MalformedURLException | V1Exception | NullArgumentException e) {
            throw new CosengException("Unable to connect to VersionOne [" + instanceUrl
                    + "], application [" + applicationName + "], project [" + projectName
                    + "], sprint [" + sprintName + "]", e);
        }
        try {
            projectOid = getProjectOid(projectName);
            sprintOid = getSprintOid(sprintName);
        } catch (CosengException e) {
            throw new CosengException("Unable to get VersionOne project [" + projectName
                    + "] OID and/or sprint [" + sprintName + "] OID", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sios.stc.coseng.integration.Integrator#attachReports(com.sios.stc.
     * coseng.run.Test, java.io.File, java.io.File)
     */
    public synchronized void attachReports(com.sios.stc.coseng.run.Test test, File reportDirectory,
            File resourceDirectory) throws CosengException {
        try {
            /* Attach TestNG reports */
            VersionOneData data = (VersionOneData) test.getIntegratorData(VersionOneData.class);
            Asset backlog = getBacklogAsset(data.getBacklogOid());
            Path sourcePath = Paths.get(reportDirectory.getAbsolutePath());
            String zipFile = resourceDirectory.getAbsolutePath() + File.separator
                    + reportDirectory.getName() + ".zip";
            Path zipPath = Paths.get(zipFile);
            Resource.zipFolder(sourcePath, zipPath);
            services.saveAttachment(zipFile, backlog, "TestNG Reports");
            /* Attach Log4J log file */
            File log4jFile = Resource.getLog4jFile();
            services.saveAttachment(log4jFile.getAbsolutePath(), backlog, "COSENG Log");
        } catch (Exception e) {
            throw new CosengException("Unable to attach TestNG reports", e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sios.stc.coseng.integration.Integrator#onExecutionStart(com.sios.stc.
     * coseng.run.Test)
     */
    @Override
    public synchronized void onExecutionStart(com.sios.stc.coseng.run.Test test)
            throws CosengException {
        try {
            TriggerOn trigger = TriggerOn.EXECUTIONSTART;
            attachIntegratorData(test, new VersionOneData());
            VersionOneData data = (VersionOneData) test.getIntegratorData(VersionOneData.class);
            data.setBacklogTimebox(sprintOid.getToken());
            data.setBacklogName(backlogTitlePrefix + test.getName());
            data.setBacklogDescription(test.toString());
            /* Create a new backlog */
            Asset backlog = services.createNew(typeBacklog, projectOid);
            List<Field> fields = new ArrayList<Field>();
            fields.addAll(configuration.getBacklog().getFields(trigger));
            fields.addAll(data.getBacklogFields(trigger));
            fields.addAll(matchParamFields(test, trigger));
            log.debug(
                    "Integrator [{}], triggerOn [{]], thread [{}], test [{}], testHashCode [{}], integratorDataHashCode [{}]",
                    INTEGRATOR_NAME, trigger.toString(), Thread.currentThread().getId(),
                    test.getName(), test.hashCode(), data.hashCode());
            setFields(typeBacklog, backlog, fields);
            services.save(backlog);
            /* Important to get after save; otherwise "NULL" */
            data.setBacklogOid(backlog.getOid());
        } catch (V1Exception | NullPointerException e) {
            throw new CosengException("Unable to create VersionOne backlog", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sios.stc.coseng.integration.Integrator#onExecutionFinish(com.sios.stc
     * .coseng.run.Test)
     */
    @Override
    public synchronized void onExecutionFinish(com.sios.stc.coseng.run.Test test)
            throws CosengException {
        try {
            TriggerOn trigger = TriggerOn.EXECUTIONFINISH;
            VersionOneData data = (VersionOneData) test.getIntegratorData(VersionOneData.class);
            Asset backlog = getBacklogAsset(data.getBacklogOid());
            List<Field> fields = new ArrayList<Field>();
            fields.addAll(configuration.getBacklog().getFields(trigger));
            for (Field f : data.getBacklogFields(trigger)) {
                log.debug("field {}", f.toString());
            }
            fields.addAll(data.getBacklogFields(trigger));
            fields.addAll(matchParamFields(test, trigger));
            log.debug(
                    "Integrator [{}], triggerOn [{]], thread [{}], test [{}], testHashCode [{}], integratorDataHashCode [{}]",
                    INTEGRATOR_NAME, trigger.toString(), Thread.currentThread().getId(),
                    test.getName(), test.hashCode(), data.hashCode());
            setFields(typeBacklog, backlog, fields);
            services.save(backlog);
        } catch (V1Exception | NullPointerException e) {
            throw new CosengException("Unable to update VersionOne backlog", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sios.stc.coseng.integration.Integrator#onSuiteStart(com.sios.stc.
     * coseng.run.Test)
     */
    @Override
    public synchronized void onSuiteStart(Test test) throws CosengException {
        // do nothing for now
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sios.stc.coseng.integration.Integrator#onSuiteFinish(com.sios.stc.
     * coseng.run.Test)
     */
    @Override
    public synchronized void onSuiteFinish(Test test) throws CosengException {
        // do nothing for now
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sios.stc.coseng.integration.Integrator#onTestStart(com.sios.stc.
     * coseng.run.Test)
     */
    @Override
    public synchronized void onTestStart(Test test) throws CosengException {
        // do nothing for now
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sios.stc.coseng.integration.Integrator#onTestFinish(com.sios.stc.
     * coseng.run.Test)
     */
    @Override
    public synchronized void onTestFinish(Test test) throws CosengException {
        // do nothing for now
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sios.stc.coseng.integration.Integrator#onClassStart(com.sios.stc.
     * coseng.run.Test)
     */
    @Override
    public synchronized void onClassStart(Test test) throws CosengException {
        // do nothing for now
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sios.stc.coseng.integration.Integrator#onClassFinish(com.sios.stc.
     * coseng.run.Test)
     */
    @Override
    public synchronized void onClassFinish(Test test) throws CosengException {
        // do nothing for now
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sios.stc.coseng.integration.Integrator#onMethodStart(com.sios.stc.
     * coseng.run.Test)
     */
    @Override
    public synchronized void onMethodStart(com.sios.stc.coseng.run.Test test)
            throws CosengException {
        try {
            TriggerOn trigger = TriggerOn.METHODSTART;
            String description = test.getTestNgMethod().getTestMethod().getDescription();
            String relevantMethodName = getRelevantMethodName(
                    test.getTestNgMethod().getTestMethod().getQualifiedName());
            VersionOneData data = (VersionOneData) test.getIntegratorData(VersionOneData.class);
            data.setTestName(relevantMethodName + NAME_SEPARATOR + description);
            data.setTestDescription(relevantMethodName + NAME_SEPARATOR + description);
            data.setTestInputs(getTestInputParams(test));
            IAssetType testType = services.getMeta().getAssetType(ASSET_TEST);
            Asset v1Test = services.createNew(testType, data.getBacklogOid());
            List<Field> fields = new ArrayList<Field>();
            fields.addAll(configuration.getTest().getFields(trigger));
            fields.addAll(data.getTestFields(trigger));
            fields.addAll(matchParamFields(test, trigger));
            log.debug(
                    "Integrator [{}], triggerOn [{]], thread [{}], test [{}], testHashCode [{}], integratorDataHashCode [{}]",
                    INTEGRATOR_NAME, trigger.toString(), Thread.currentThread().getId(),
                    test.getName(), test.hashCode(), data.hashCode());
            setFields(typeTest, v1Test, fields);
            services.save(v1Test);
            /* Important to get after save; otherwise "NULL" */
            data.setTestOid(v1Test.getOid());
        } catch (V1Exception | NullPointerException e) {
            throw new CosengException("Unable to create VersionOne test", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sios.stc.coseng.integration.Integrator#onMethodFinish(com.sios.stc.
     * coseng.run.Test)
     */
    @Override
    public synchronized void onMethodFinish(com.sios.stc.coseng.run.Test test)
            throws CosengException {
        try {
            TriggerOn trigger = TriggerOn.METHODFINISH;
            VersionOneData data = (VersionOneData) test.getIntegratorData(VersionOneData.class);
            Query query = new Query(data.getTestOid());
            /* Adding the 'status' is required; otherwise no results */
            IAttributeDefinition status =
                    typeTest.getAttributeDefinition(VersionOneData.ATTR_STATUS);
            query.getSelection().add(status);
            QueryResult result = services.retrieve(query);
            Asset v1Test = result.getAssets()[0];
            List<Field> fields = new ArrayList<Field>();
            fields.addAll(configuration.getTest().getFields(trigger));
            fields.addAll(data.getTestFields(trigger));
            fields.addAll(matchParamFields(test, trigger));
            if (test.getTestNgMethod().getTestResult().isSuccess()) {
                log.debug("Test [{}] method [{}] passed", test.getName(),
                        test.getTestNgMethod().getTestMethod().getMethodName());
                fields.addAll(configuration.getTest().getFields(TriggerOn.METHODPASS));
            } else {
                log.debug("Test [{}] method [{}] failed", test.getName(),
                        test.getTestNgMethod().getTestMethod().getMethodName());
                fields.addAll(configuration.getTest().getFields(TriggerOn.METHODFAIL));
            }
            log.debug(
                    "Integrator [{}], triggerOn [{]], thread [{}], test [{}], testHashCode [{}], integratorDataHashCode [{}]",
                    INTEGRATOR_NAME, trigger.toString(), Thread.currentThread().getId(),
                    test.getName(), test.hashCode(), data.hashCode());
            setFields(typeTest, v1Test, fields);
            services.save(v1Test);
        } catch (V1Exception | NullPointerException e) {
            throw new CosengException("Unable to update VersionOne test", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sios.stc.coseng.integration.Integrator#addTestStep(com.sios.stc.
     * coseng.run.Test, java.lang.String)
     */
    public synchronized void addTestStep(com.sios.stc.coseng.run.Test test, String stepMessage)
            throws CosengException {
        try {
            VersionOneData data = (VersionOneData) test.getIntegratorData(VersionOneData.class);
            data.addTestStep(stepMessage);
        } catch (Exception e) {
            throw new CosengException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sios.stc.coseng.integration.Integrator#addTestStepExpectedResult(com.
     * sios.stc.coseng.run.Test, java.lang.String, java.lang.String)
     */
    public synchronized void addTestStepExpectedResult(com.sios.stc.coseng.run.Test test,
            String stepMessage, String expectedResult) throws CosengException {
        try {
            VersionOneData data = (VersionOneData) test.getIntegratorData(VersionOneData.class);
            data.addTestStepExpectedResult(stepMessage, expectedResult);
        } catch (Exception e) {
            throw new CosengException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sios.stc.coseng.integration.Integrator#addTestStepActualResult(com.
     * sios.stc.coseng.run.Test, java.lang.String, java.lang.String)
     */
    public synchronized void addTestStepActualResult(com.sios.stc.coseng.run.Test test,
            String stepMessage, String actualResult) throws CosengException {
        try {
            VersionOneData data = (VersionOneData) test.getIntegratorData(VersionOneData.class);
            data.addTestStepActualResult(stepMessage, actualResult);
        } catch (Exception e) {
            throw new CosengException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sios.stc.coseng.integration.Integrator#clearAllTestSteps(com.sios.stc
     * .coseng.run.Test)
     */
    public synchronized void clearAllTestSteps(com.sios.stc.coseng.run.Test test)
            throws CosengException {
        try {
            VersionOneData data = (VersionOneData) test.getIntegratorData(VersionOneData.class);
            data.clearAllTestSteps();
        } catch (Exception e) {
            throw new CosengException(e);
        }
    }

    /**
     * Gets the backlog asset.
     *
     * @param backlogOid
     *            the backlog oid
     * @return the backlog asset
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    private synchronized Asset getBacklogAsset(Oid backlogOid) throws CosengException {
        try {
            Query query = new Query(backlogOid);
            /* Adding the 'status' is required; otherwise no results */
            IAttributeDefinition status =
                    typeBacklog.getAttributeDefinition(VersionOneData.ATTR_STATUS);
            query.getSelection().add(status);
            QueryResult result = services.retrieve(query);
            Asset backlog = result.getAssets()[0];
            return backlog;
        } catch (APIException | OidException | NullPointerException | ConnectionException e) {
            throw new CosengException("Unable to query for VersionOne backlog", e);
        }
    }

    /**
     * Gets the project oid.
     *
     * @param project
     *            the project
     * @return the project oid
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    private synchronized Oid getProjectOid(String project) throws CosengException {
        String query = "{" + " \"from\": \"Scope\","
                + " \"select\": [\"ID\"], \"where\": { \"Name\": \"" + project + "\"}" + "}";
        return getOidFromQuery(query);
    }

    /**
     * Gets the sprint oid.
     *
     * @param sprint
     *            the sprint
     * @return the sprint oid
     * @throws CosengException
     *             the coseng exception
     * @since 3.0
     * @version.coseng
     */
    private synchronized Oid getSprintOid(String sprint) throws CosengException {
        String query = "{" + " \"from\": \"Timebox\","
                + " \"select\": [\"Name\", \"ID\", \"State\"], \"where\": {\"Name\": \"" + sprint
                + "\"} }";
        return getOidFromQuery(query);
    }

    /**
     * Gets the oid from query.
     *
     * @param query
     *            the query
     * @return the oid from query
     * @throws CosengException
     *             the coseng exception for caught OidException,
     *             JsonIOException, JsonSyntaxException,
     *             IllegalArgumentException, NullPointerException or
     *             ArrayIndexOutOfBoundsException
     * @since 3.0
     * @version.coseng
     */
    private synchronized Oid getOidFromQuery(String query) throws CosengException {
        try {
            String result = services.executePassThroughQuery(query);
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            com.sios.stc.coseng.integration.versionone.Oid[][] oid =
                    gson.fromJson(result, com.sios.stc.coseng.integration.versionone.Oid[][].class);
            return services.getOid(oid[0][0].getId());
        } catch (OidException | JsonIOException | JsonSyntaxException | IllegalArgumentException
                | NullPointerException | ArrayIndexOutOfBoundsException e) {
            throw new CosengException("Exception on VersionOne query [" + query + "]", e);
        }
    }

    /**
     * Sets the fields.
     *
     * @param assetType
     *            the asset type
     * @param asset
     *            the asset
     * @param fields
     *            the fields
     * @throws CosengException
     *             the coseng exception for caught APIException or
     *             NullPointerException
     * @since 3.0
     * @version.coseng
     */
    private synchronized void setFields(IAssetType assetType, Asset asset, List<Field> fields)
            throws CosengException {
        /*
         * Set backlog field attributes in addition to provided config. Non
         * empty 'name' suggests that value will be 'NAME:VALUE'. Empty 'name'
         * will have value 'VALUE'
         */
        try {
            for (Field field : fields) {
                String attribute = field.getAttribute();
                String name = field.getName();
                String value = field.getValue();
                if (!name.isEmpty()) {
                    value = name + ATTR_SEPARATOR + value;
                }
                IAttributeDefinition attr = assetType.getAttributeDefinition(attribute);
                asset.setAttributeValue(attr, value);
            }
        } catch (APIException | NullPointerException e) {
            throw new CosengException("Unable to set VersionOne attribute value", e);
        }
    }

    /**
     * Match param fields.
     *
     * @param test
     *            the test
     * @param trigger
     *            the trigger
     * @return the list
     * @since 3.0
     * @version.coseng
     */
    private synchronized List<Field> matchParamFields(com.sios.stc.coseng.run.Test test,
            TriggerOn trigger) {
        List<Field> matches = new ArrayList<Field>();
        /* Not all TestNG execution phases may have test parameters */
        try {
            Map<String, String> testParams = getTestParams(test);
            for (String paramName : testParams.keySet()) {
                String paramValue = testParams.get(paramName);
                List<Field> match =
                        configuration.getTest().getParamFields(paramName, paramValue, trigger);
                matches.addAll(match);
            }
        } catch (Exception e) {
            // do nothing
        }
        return matches;
    }

    /**
     * Gets the relevant method name.
     *
     * @param qualifiedMethodName
     *            the qualified method name
     * @return the relevant method name
     * @since 3.0
     * @version.coseng
     */
    private synchronized String getRelevantMethodName(String qualifiedMethodName) {
        String relevantName = Common.STRING_EMPTY;
        try {
            relevantName = qualifiedMethodName.replace(PACKAGE_ROOT, Common.STRING_EMPTY);
        } catch (Exception e) {
            // do nothing
        }
        return relevantName;
    }

    /**
     * Gets the test params.
     *
     * @param test
     *            the test
     * @return the test params
     * @since 3.0
     * @version.coseng
     */
    private synchronized Map<String, String> getTestParams(com.sios.stc.coseng.run.Test test) {
        Map<String, String> testParams = new HashMap<String, String>();
        try {
            Map<String, String> params =
                    test.getTestNgMethod().getTestMethod().getXmlTest().getAllParameters();
            if (params != null) {
                testParams = params;
            }
        } catch (Exception e) {
            // do nothing
        }
        return testParams;
    }

    /**
     * Gets the test input params.
     *
     * @param test
     *            the test
     * @return the test input params
     * @since 3.0
     * @version.coseng
     */
    private synchronized String getTestInputParams(com.sios.stc.coseng.run.Test test) {
        List<String> testParams = new ArrayList<String>();
        try {
            Map<String, String> fetchedTestParams = getTestParams(test);
            for (String paramName : fetchedTestParams.keySet()) {
                String paramValue = fetchedTestParams.get(paramName);
                testParams.add(VersionOneData.ITALIC_BEGIN + paramName + VersionOneData.ITALIC_END
                        + VersionOneData.PARAM_VALUE_SEPARATOR + VersionOneData.BOLD_BEGIN
                        + paramValue + VersionOneData.BOLD_END);
            }
        } catch (Exception e) {
            // do nothing
        }
        return VersionOneData.LINE_BREAK_BEGIN
                + StringUtils.join(testParams,
                        VersionOneData.LINE_BREAK_END + VersionOneData.LINE_BREAK_BEGIN)
                + VersionOneData.LINE_BREAK_END;
    }

    /**
     * Checks for required fields.
     *
     * @param configuration
     *            the configuration
     * @return true, if successful
     * @since 3.0
     * @version.coseng
     */
    private boolean hasRequiredFields(Configuration configuration) {
        try {
            Field field = configuration.getBacklog().getField(VersionOneData.ATTR_STATUS,
                    TriggerOn.EXECUTIONSTART);
            if (!hasAllFields(field)) {
                return false;
            }
            field = configuration.getBacklog().getField(VersionOneData.ATTR_STATUS,
                    TriggerOn.EXECUTIONFINISH);
            if (!hasAllFields(field)) {
                return false;
            }
            field = configuration.getTest().getField(VersionOneData.ATTR_STATUS,
                    TriggerOn.METHODSTART);
            if (!hasAllFields(field)) {
                return false;
            }
            field = configuration.getTest().getField(VersionOneData.ATTR_STATUS,
                    TriggerOn.METHODPASS);
            if (!hasAllFields(field)) {
                return false;
            }
            field = configuration.getTest().getField(VersionOneData.ATTR_STATUS,
                    TriggerOn.METHODFAIL);
            if (!hasAllFields(field)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks for all fields.
     *
     * @param field
     *            the field
     * @return true, if successful
     * @since 3.0
     * @version.coseng
     */
    private boolean hasAllFields(Field field) {
        if (field == null || field.getAttribute().isEmpty() || field.getName().isEmpty()
                || field.getValue().isEmpty()) {
            return false;
        }
        return true;
    }

}
