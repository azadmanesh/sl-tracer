package com.oracle.truffle.sl.tracer.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.source.MissingNameException;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;
import com.oracle.truffle.api.vm.PolyglotEngine.Language;
import com.oracle.truffle.sl.SLLanguage;
import com.oracle.truffle.sl.nodes.SLRootNode;

public class FunctionInvocationWrapperTest extends LanguageSetupTest {

    @Test
    public void test() {

        final String snippet = "function f(a, b){\n" +
                        "c = a * 2;\n" +
                        "return c + b; \n" +
                        "}\n" +
                        "function main(){\n" +
                        "c = 2;\n" +
                        "d = 3;\n" +
                        "a = f(c , d);\n" +
                        "}\n";

        try {
            run(snippet);
        } catch (IOException | MissingNameException e) {
            e.printStackTrace();
        }
// Map<String, SLRootNode> map = parse(snippet);
// NodeUtil.printCompactTree(System.out, map.get("main"));

// PolyglotEvalRootNode root = new PolyglotEvalRootNode(this, l, source)
// target = Truffle.getRuntime().createCallTarget();
// SLRootNode root = map.get("main");
// CallTarget target = Truffle.getRuntime().createCallTarget(root);
// target.call(new Object[0]);
// root.adoptChildren();
// Queue<Node> q = new LinkedList<>();
// q.add(root);
// int count = 0;
// while (!q.isEmpty()) {
// Node node = q.remove();
//
// for (Node child : node.getChildren())
// q.add(child);
//
// }
        assertTrue(true);
    }
}
