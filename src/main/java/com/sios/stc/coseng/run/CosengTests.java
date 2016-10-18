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

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sios.stc.coseng.RunTests;
import com.sios.stc.coseng.util.Resource;

/**
 * The Class CosengTests contains the entry method for executing Concurrent
 * Selenium TestNG (COSENG) suites.
 *
 * @since 2.0
 * @version.coseng
 */
public class CosengTests {

    private static final int    EXIT_SUCCESS             = 0;
    private static final int    EXIT_FAILURE             = 1;
    private static final int    EXECUTOR_TIMEOUT_MINUTES = 5;
    private static final Logger log                      =
            LogManager.getLogger(RunTests.class.getName());
    private static String       jsonTests;
    private static InputStream  jsonTestsInput;
    private static String       jsonNode;
    private static InputStream  jsonNodeInput;
    private static Tests        tests;

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
            tests = GetTests.with(jsonNode, jsonNodeInput, jsonTests, jsonTestsInput);
            log.info(GetTests.configuration());
            tieLogging();
        } catch (CosengException e) {
            log.fatal("Getting tests", e);
            log.info(Help.getNode());
            log.info(Help.getTest());
            System.exit(EXIT_FAILURE);
        }
        /*
         * Create the concurrent workers for each test; execute tests in
         * parallel and shutdown
         */
        boolean executionFailure = false;
        ExecutorService executor = Executors.newCachedThreadPool();
        try {
            log.info("Testing started");
            for (String name : tests.getNames()) {
                Test test = tests.getTest(name);
                final Runnable worker = new Concurrent(test);
                executor.execute(worker);
            }
        } catch (CosengException e) {
            executionFailure = true;
            log.error("Unable to create test worker; not all tests executed", e);
        }
        executor.shutdown();
        try {
            executor.awaitTermination(EXECUTOR_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            executionFailure = true;
            log.error("Executor timeout reached; testing interrupted {}", e);
        }
        /* Report the test results */
        log.info("Reports @ " + tests.getReportDirectories());
        log.debug("Total local node web driver started [{}]; stopped [{}]",
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
        final String jsonTestsDemo = "coseng/tests/demo/suite-files.json";
        Boolean exitFailure = null;
        HelpFormatter formatter = new HelpFormatter();
        final String optHelp = "help";
        final String optNode = "node";
        final String optTests = "tests";
        final String optDemo = "demo";
        final String helpUsage = "Valid COSENG command line options";
        /* Define the accepted command line options */
        Options options = new Options();
        options.addOption(optHelp, "print this message");
        options.addOption(optNode, true, "[optional] Node JSON configuration resource");
        options.addOption(optTests, true,
                "[required; unless -demo] Tests JSON configuration resource");
        options.addOption(optDemo, "run COSENG demonstration; mutually exclusive of -tests");
        try {
            /*
             * Attempt to parse the command line arguments with the expected
             * options
             */
            CommandLineParser parser = new DefaultParser();
            CommandLine cli = parser.parse(options, args);
            /* Get the option values */
            jsonNode = cli.getOptionValue(optNode);
            jsonTests = cli.getOptionValue(optTests);
            /* Check option expectations */
            if (cli.hasOption(optHelp)) {
                exitFailure = false;
            } else {
                if (cli.hasOption(optNode)) {
                    if (!cli.getOptionValue(optNode).isEmpty()) {
                        jsonNodeInput = Resource.get(jsonNode);
                    } else {
                        log.fatal("-" + optNode + " <arg> empty");
                        exitFailure = true;
                    }
                }
                if (cli.hasOption(optTests)) {
                    if (cli.hasOption(optDemo)) {
                        log.fatal("-" + optTests + " or -" + optDemo + "; not both");
                        exitFailure = true;
                    } else if (!cli.getOptionValue(optTests).isEmpty()) {
                        jsonTestsInput = Resource.get(jsonTests);
                    } else {
                        log.fatal("-" + optTests + " <arg> empty");
                        exitFailure = true;
                    }
                } else {
                    if (cli.hasOption(optDemo)) {
                        jsonTestsInput = Resource.get(jsonTestsDemo);
                    } else {
                        log.fatal("-" + optTests + " <arg> required");
                        exitFailure = true;
                    }
                }
            }
            if (exitFailure != null) {
                final String separator = "-----------------------------------";
                formatter.printHelp(helpUsage, options);
                System.out.println(separator);
                System.out.println(Help.getNode());
                System.out.println(separator);
                System.out.println(Help.getTest());
                if (exitFailure) {
                    System.exit(EXIT_FAILURE);
                }
                System.exit(EXIT_SUCCESS);
            }
        } catch (ParseException | CosengException e) {
            if (e instanceof ParseException) {
                formatter.printHelp(helpUsage, options);
                throw new CosengException("Error parsing command line arguements", e);
            } else {
                throw new CosengException(e);
            }
        }
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
