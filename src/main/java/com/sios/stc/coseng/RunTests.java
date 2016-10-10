package com.sios.stc.coseng;

import com.sios.stc.coseng.run.Coseng;

public class RunTests extends Coseng {
    /**
     * The main method for executing Concurrent Selenium TestNG (COSENG) suites.
     * Requires a Tests JSON resource and an optional Node JSON resource.
     *
     * @param args
     *            the command line arguments to configure a COSENG test
     *            execution; -help for usage and options
     * @see com.sios.stc.coseng.run.Coseng
     * @see com.sios.stc.coseng.run.Node
     * @see com.sios.stc.coseng.run.Test
     * @see com.sios.stc.coseng.run.Tests
     * @since 2.0
     * @version.coseng
     */
    public static void main(final String[] args) {
        with(args);
    }
}
