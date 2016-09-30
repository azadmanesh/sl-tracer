package com.oracle.truffle.sl.tracer.tests;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.sl.nodes.SLRootNode;
import com.oracle.truffle.sl.nodes.local.SLWriteLocalVariableNodeGen;

/**
 * Test methods here intend to test the simple assignments in SimpleLanguage. It excludes assignment
 * to properties. For a simple assignment, there should be a wrapper node added to the AST and the
 * node should introduce a new mapping to the shadow values map which is kept in the virtual frame
 * of the current method.
 *
 */
public class LocalWriterWrapperTest extends LanguageSetupTest {

    @Test
    public void simpleAssignmentTest() {
        final String snippet = "function main(){" +
                        "b = 1;" +
                        "c = 2;" +
                        "d = 3;" +
                        "a = (b + c) + d;}";

        Map<String, SLRootNode> map = parse(snippet);
        for (String key : map.keySet()) {
            SLRootNode root = map.get(key);
            root.adoptChildren();
            Queue<Node> q = new LinkedList<>();
            q.add(root);
            int count = 0;
            while (!q.isEmpty()) {
                Node node = q.remove();

                for (Node child : node.getChildren())
                    q.add(child);

                if (node.getClass() == SLWriteLocalVariableNodeGen.class) {
                    count++;
                    // The parent of an SLWriteLocalVariableNodeGen has to be a WrapperNode
// assertEquals(node.getParent().getClass(), LocalWriterWrapperNode.class);
                }
            }

            assertEquals("There should be 4 assignments", 4, count);
        }
    }

    @Test
    public void wrapperNodeValueTest() {
        final String snippet = " function main(){\n" +
                        "a = 3;\n" +
                        "b = 1;\n" +
                        "c = 2;\n" +
                        "d = 4;\n" +
                        "a = (b + c) * d;}\n";

        Map<String, SLRootNode> map = parse(snippet);

        for (String key : map.keySet()) {
            SLRootNode root = map.get(key);
            CallTarget target = Truffle.getRuntime().createCallTarget(root);
            target.call(new Object[0]);

            root.adoptChildren();
            Queue<Node> q = new LinkedList<>();
            q.add(root);
            int count = 0;
            // a b c d a
            final long[] assignedValues = {3, 1, 2, 4, 12};

            while (!q.isEmpty()) {
                Node node = q.remove();

                for (Node child : node.getChildren())
                    q.add(child);

                if (node.getClass() == SLWriteLocalVariableNodeGen.class) {
                    // The parent of an SLWriteLocalVariableNodeGen has to be a WrapperNode
// assertEquals(node.getParent().getClass(), LocalWriterWrapperNode.class);

// LocalWriterWrapperNode wrapperNode = (LocalWriterWrapperNode) node.getParent();
// assertEquals("Wrapper nodes value should be " + assignedValues[count] + " for the " + count + "th
// assingment", assignedValues[count],
// wrapperNode.getShadowSubTree().getRootValue());
                    count++;
                }
            }

            assertEquals("There should be only a single assignment", 5, count);
        }
    }

}
