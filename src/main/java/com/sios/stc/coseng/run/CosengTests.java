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

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sios.stc.coseng.Common;
import com.sios.stc.coseng.RunTests;
import com.sios.stc.coseng.integration.versionone.VersionOne;
import com.sios.stc.coseng.util.Resource;

/**
 * The Class CosengTests contains the entry method for executing Concurrent
 * Selenium TestNG (COSENG) suites.
 *
 * @since 2.0
 * @version.coseng
 */
public class CosengTests {

    private static final Logger                log                      =
            LogManager.getLogger(RunTests.class.getName());
    private static final int                   EXIT_SUCCESS             = 0;
    private static final int                   EXIT_FAILURE             = 1;
    private static Tests                       tests;
    private static final Map<String, Class<?>> availableIntegrators     =
            new HashMap<String, Class<?>>();
    private static final Map<String, String>   availableIntegratorsHelp =
            new HashMap<String, String>();

    /**
     * Instantiates a new coseng. Marked protected to prevent other classes
     * outside of the package from instantiating.
     *
     * @since 2.0
     * @version.coseng
     */
    protected CosengTests() {
        // do nothing;
    }

    /**
     * The method for executing Concurrent Selenium TestNG (COSENG) suites.
     * Requires a Tests JSON resource and an optional Node JSON resource. If all
     * tests are successful the exit value is [0]. On failure the exit value is
     * [1]. The concurrent executor timeout is [5] minutes.
     *
     * @param args
     *            the command line arguments to configure a COSENG test
     *            execution; -help for usage and options
     * @see com.sios.stc.coseng.RunTests#main(String[])
     * @see com.sios.stc.coseng.run.Node
     * @see com.sios.stc.coseng.run.Test
     * @see com.sios.stc.coseng.run.Tests
     * @since 2.0
     * @version.coseng
     */
    protected static void with(final String[] args) {
        /* Exit if unsupported operating system */
        if (!OperatingSystem.isSupported()) {
            log.fatal("Operating system [" + OperatingSystem.nameAndVersion()
                    + "] is not supported; supported " + OperatingSystem.getSupported());
            System.exit(EXIT_FAILURE);
        }
        /* Get the tests from requested Node and Tests JSON */
        try {
            parseCliArguments(args);
            log.info(GetTests.configuration());
            tieLogging();
        } catch (CosengException e) {
            log.fatal("Unable to get tests", e);
            System.exit(EXIT_FAILURE);
        }
        /*
         * Create the concurrent workers for each test; execute tests in
         * parallel and shutdown
         */
        boolean executionFailure = false;
        ExecutorService executorPool = Executors.newCachedThreadPool();
        StopWatch stopWatch = new StopWatch();
        try {
            log.info("Testing started");
            stopWatch.start();
            for (String name : tests.getNames()) {
                Test test = tests.getTest(name);
                final Runnable worker = new Concurrent(test);
                executorPool.execute(worker);
            }
            executorPool.shutdown();
            executorPool.awaitTermination(tests.getMaxTestExecutionMinutes(), TimeUnit.MINUTES);
        } catch (CosengException | RejectedExecutionException e) {
            executionFailure = true;
            log.error("Unable to create test worker", e);
        } catch (InterruptedException e) {
            executionFailure = true;
            executorPool.shutdownNow();
            log.error("Test execution time exceeded", e);
        } catch (NullPointerException e) {
            log.error("No tests to execute");
        }
        stopWatch.stop();
        log.info("Elapsed time (hh:mm:ss:ms) [{}]", stopWatch.toString());
        /* Report the test results */
        log.info("Reports @ " + tests.getReportDirectories());
        log.info("Total web driver started [{}]; stopped [{}]",
                CosengRunner.getStartedWebDriverCount(), CosengRunner.getStoppedWebDriverCount());
        List<String> failedTests = tests.getFailed();
        if (executionFailure || !failedTests.isEmpty()) {
            log.error("Testing completed; with failures "
                    + (!failedTests.isEmpty() ? failedTests : ""));
            System.exit(EXIT_FAILURE);
        } else {
            log.info("Testing completed; all tests completed successfully");
            System.exit(EXIT_SUCCESS);
        }
    }

    /**
     * Parse the command line arguments and attempt to get the referenced
     * resources. Print help usage and exit with value [0] when -help option
     * set. Print help usage and exit with value [1] for mis-configured options.
     *
     * @param args
     *            the args
     * @throws CosengException
     *             the coseng exception on parse errors and absent or invalid
     *             resources
     * @see com.sios.stc.coseng.run.Node
     * @see com.sios.stc.coseng.run.Test
     * @see com.sios.stc.coseng.run.Tests
     * @since 2.0
     * @version.coseng
     */
    private static void parseCliArguments(String[] args) throws CosengException {
        final String jsonTestsDemo = "coseng/demo/tests/suite-files.json";
        Boolean exitFailure = false;
        HelpFormatter formatter = new HelpFormatter();
        final String optHelp = "help";
        final String optNode = "node";
        final String optTests = "tests";
        final String optDemo = "demo";
        final String optIntegrationVersionOne = "versionone";
        final String helpUsage = "Valid COSENG command line options";
        final String separator = "-----------------------------------";
        /* Spin up the available integrations */
        putAvailableIntegrator(optIntegrationVersionOne, VersionOne.class,
                "coseng/help/integrators/versionone.json");
        /* Define the accepted command line options */
        Options options = new Options();
        options.addOption(optDemo, "Run COSENG demonstration");
        options.addOption(optHelp, false, "Help");
        options.addOption(optNode, true, "Node JSON configuration resource e.g. /path/node.json");
        options.addOption(optTests, true,
                "Tests JSON configuration resource e.g. /path/tests.json");
        for (String integrator : getAvailableIntegrators()) {
            options.addOption(integrator, true, integrator
                    + " integrator configuration resource e.g. /path/" + integrator + ".json");
        }
        try {
            /*
             * Attempt to parse the command line arguments with the expected
             * options
             */
            CommandLineParser parser = new DefaultParser();
            CommandLine cli = parser.parse(options, args);
            /* Get the option values */
            String jsonNodeFileName = null;
            String jsonTestsFileName = null;
            /* Check option expectations */
            if (cli.hasOption(optHelp)) {
                formatter.printHelp(helpUsage, options);
                System.out.println(separator);
                System.out.println(Help.getTest());
                System.out.println(separator);
                System.out.println(Help.getNode());
                for (String integrator : getAvailableIntegrators()) {
                    System.out.println(separator);
                    if (hasAvailableIntegratorHelp(integrator)) {
                        System.out.println("Integrator [" + integrator + "]");
                        String fileName = getAvailableIntegratorHelp(integrator);
                        System.out.println(Resource.getString(fileName));
                    } else {
                        System.out.println("Integrator [" + integrator + "] has no help");
                    }
                }
                System.exit(EXIT_SUCCESS);
            } else {
                if (cli.hasOption(optNode)) {
                    jsonNodeFileName = cli.getOptionValue(optNode);
                    if (jsonNodeFileName.isEmpty()) {
                        log.fatal("-" + optNode + " <arg> empty");
                        exitFailure = true;
                    }
                }
                if (cli.hasOption(optTests)) {
                    jsonTestsFileName = cli.getOptionValue(optTests);
                    if (cli.hasOption(optDemo)) {
                        log.fatal("-" + optTests + " or -" + optDemo + "; not both");
                        exitFailure = true;
                    } else if (jsonTestsFileName.isEmpty()) {
                        log.fatal("-" + optTests + " <arg> empty");
                        exitFailure = true;
                    }
                } else {
                    if (!cli.hasOption(optDemo)) {
                        log.fatal("-" + optTests + " <arg> required");
                        exitFailure = true;
                    } else {
                        jsonTestsFileName = jsonTestsDemo;
                    }
                }
                for (String integration : getAvailableIntegrators()) {
                    if (cli.hasOption(integration)) {
                        String jsonFileName = cli.getOptionValue(integration);
                        if (!jsonFileName.isEmpty()) {
                            Class<?> clazz = getAvailableIntegratorClass(integration);
                            GetIntegrators.with(clazz, jsonFileName);
                        } else {
                            log.fatal("-" + integration + " <arg> required");
                            exitFailure = true;
                        }
                    }
                }
                if (exitFailure) {
                    System.exit(EXIT_FAILURE);
                }
            }
            tests = GetTests.with(jsonNodeFileName, jsonTestsFileName);
        } catch (ParseException | CosengException e) {
            if (e instanceof ParseException) {
                formatter.printHelp(helpUsage, options);
                throw new CosengException("Error parsing command line arguements", e);
            } else {
                throw new CosengException(e);
            }
        }
    }

    private static void putAvailableIntegrator(String integration, Class<?> clazz,
            String helpJsonFileName) {
        if (integration != null && !integration.isEmpty() && clazz != null
                && helpJsonFileName != null && !helpJsonFileName.isEmpty()) {
            availableIntegrators.put(integration, clazz);
            availableIntegratorsHelp.put(integration, helpJsonFileName);
        }
    }

    private static boolean hasAvailableIntegrator(String integration) {
        if (availableIntegrators.containsKey(integration)) {
            return true;
        }
        return false;
    }

    private static boolean hasAvailableIntegratorHelp(String integration) {
        if (availableIntegratorsHelp.containsKey(integration)) {
            return true;
        }
        return false;
    }

    private static Class<?> getAvailableIntegratorClass(String integration) {
        if (hasAvailableIntegrator(integration)) {
            return availableIntegrators.get(integration);
        }
        return Object.class;
    }

    private static String getAvailableIntegratorHelp(String integration) {
        if (hasAvailableIntegratorHelp(integration)) {
            return availableIntegratorsHelp.get(integration);
        }
        return Common.STRING_EMPTY;
    }

    private static Set<String> getAvailableIntegrators() {
        return availableIntegrators.keySet();
    }

    /**
     * Tie logging.
     *
     * @since 2.0
     * @version.coseng
     */
    private static void tieLogging() {
        System.setOut(createOutProxy(System.out));
        System.setErr(createErrorProxy(System.err));
    }

    /**
     * Creates the out proxy.
     *
     * @param printStream
     *            the print stream
     * @return the prints the stream
     * @since 2.0
     * @version.coseng
     */
    private static PrintStream createOutProxy(PrintStream printStream) {
        return new PrintStream(printStream) {
            public void print(String string) {
                printStream.print(string);
                log.info("{}", string);
            }
        };
    }

    /**
     * Creates the error proxy.
     *
     * @param printStream
     *            the print stream
     * @return the prints the stream
     * @since 2.0
     * @version.coseng
     */
    private static PrintStream createErrorProxy(PrintStream printStream) {
        return new PrintStream(printStream) {
            public void print(String string) {
                printStream.print(string);
                log.error("{}", string);
            }
        };
    }

}
