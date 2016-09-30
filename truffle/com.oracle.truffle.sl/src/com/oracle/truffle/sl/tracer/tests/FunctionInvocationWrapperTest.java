package com.oracle.truffle.sl.tracer.tests;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.source.MissingNameException;

public class FunctionInvocationWrapperTest extends LanguageSetupTest {

    @Test
    public void test() {

        final String snippet = "" +
                        "function f(a, b){\n" +
                        "d = a * 2;\n" +           // 2
                        "return b + d;\n" +        // 3
                        "}\n" +     // 4

                        "function main(){\n" +      // 4
                        "b = c = 3 + 5 * 6;\n" +    // 5
                        "o = new();\n" +
                        "o.a = 1;\n" +
                        "d = c;\n" +            // 6
                        "d = f(o.a,d);\n" +       // 7
                        "}\n";

        try {
            System.out.println("Output:\n" + run(snippet));
        } catch (IOException | MissingNameException e) {
            e.printStackTrace();
        }
        assertTrue(true);

// Map<String, SLRootNode> map = parse(snippet);
// NodeUtil.printCompactTree(System.out, map.get("main"));

    }
}
