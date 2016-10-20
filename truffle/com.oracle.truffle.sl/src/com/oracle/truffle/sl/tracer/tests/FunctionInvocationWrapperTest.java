package com.oracle.truffle.sl.tracer.tests;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.source.MissingNameException;
import com.oracle.truffle.sl.nodes.SLRootNode;
import com.oracle.truffle.sl.tracer.InvokeWrapperNode;

public class FunctionInvocationWrapperTest extends LanguageSetupTest {

    @Test
    public void test() {

        final String snippet = "" +
                        "function foo() {}\n" +

                        "function bar() {" +
                        "foo();}" +

                        "function main() {" +
                        "i = 0;" +
                        "while (i < 1000) {" +
                        "bar();" +
                        "i = i + 1;" +
                        "}" +
                        "println(i);}";

        try {
            System.out.println("Output:\n" + run(snippet));
        } catch (IOException | MissingNameException e) {
            e.printStackTrace();
        }

// Map<String, SLRootNode> map = parse(snippet);
// NodeUtil.printCompactTree(System.out, map.get("main"));

    }

}
