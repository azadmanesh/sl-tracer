package com.oracle.truffle.sl.tracer.tests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.oracle.truffle.api.source.MissingNameException;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;
import com.oracle.truffle.sl.nodes.SLRootNode;
import com.oracle.truffle.sl.parser.Parser;

/**
 * Base class for tracer tests.
 */
public class LanguageSetupTest {

    protected PolyglotEngine engine;

    protected final ByteArrayOutputStream out = new ByteArrayOutputStream();
    protected final ByteArrayOutputStream err = new ByteArrayOutputStream();

    /**
     * Logs each test method's name upon execution
     */
    @Rule public TestWatcher testWatcher = new TestWatcher() {
        @Override
        protected void starting(final Description description) {
            String methodName = description.getMethodName();
            System.out.println("Test Method: " + methodName);
        }
    };

    @Before
    public void setup() {
        engine = PolyglotEngine.newBuilder().setOut(out).setErr(err).build();
    }

    protected String run(Source source) throws IOException {
        this.out.reset();
        this.err.reset();
        engine.eval(source);
        this.out.flush();
        this.err.flush();
        String outText = getOut();
        return outText;
    }

    protected String run(String source) throws IOException, MissingNameException {
        return run(Source.newBuilder(source).name("Test.sl").mimeType("application/x-sl").build());
    }

    protected void assertEvalOut(String source, String output) throws IOException, MissingNameException {
        assertEvalOut(Source.newBuilder(source).name("Test.sl").mimeType("application/x-sl").build(), output);
    }

    protected void assertEvalOut(Source source, String output) throws IOException {
        String actual = run(source);
        String error = getErr();
        if (!error.isEmpty()) {
            throw new AssertionError("Unexpected error printed: %s" + error);
        }
        if (!actual.equals(output)) {
            Assert.assertEquals(output, actual);
        }
    }

    protected final String getOut() {
        return new String(out.toByteArray());
    }

    protected final String getErr() {
        return new String(err.toByteArray());
    }

    @After
    public void teardown() {
        if (engine != null) {
            engine.dispose();
        }
    }

    public Map<String, SLRootNode> parse(String source) {
        return parse(Source.newBuilder(source).name("Test.sl").mimeType("application/x-sl").build());
    }

    public Map<String, SLRootNode> parse(Source source) {
        return Parser.parseSL(source);
    }
}
