package com.oracle.truffle.sl.tracer.tests;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.source.MissingNameException;
import com.oracle.truffle.sl.nodes.SLRootNode;

public class ReadPropertyWrapperTest extends LanguageSetupTest {

    @Test
    public void simpleReadTest() {
        final String snippet = "" +
                        "function main(){\n" +
                        "obj1 = new();\n" +
                        "obj1.s = new();\n" +
                        "obj1.s.y = 66;\n" +
                        "obj1.y = 66;\n" +
                        "obj1.z = new();\n" +
                        "println(obj1.s);\n" +
                        "}\n";

        try {
            System.out.println("Output:\n" + run(snippet));
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

// Map<String, SLRootNode> map = parse(snippet2);
// NodeUtil.printCompactTree(System.out, map.get("utility"));

    }
}
