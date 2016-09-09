package com.oracle.truffle.sl.tracer.tests;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.junit.Test;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.sl.nodes.SLRootNode;
import com.oracle.truffle.sl.nodes.local.SLReadLocalVariableNodeGen;
import com.oracle.truffle.sl.nodes.local.SLWriteLocalVariableNodeGen;
import com.oracle.truffle.sl.tracer.LocalReaderWrapperNode;
import com.oracle.truffle.sl.tracer.LocalWriterWrapperNode;
import com.oracle.truffle.sl.tracer.ShadowTree;

public class LocalReaderWrapperTest extends LanguageSetupTest {

    @Test
    public void addWrapperTest() {
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

                if (node.getClass() == SLReadLocalVariableNodeGen.class) {
                    count++;
                    // The parent of an SLReadLocalVariableNodeGen has to be a
                    // LocalReaderWrapperNode
                    assertEquals(node.getParent().getClass(), LocalReaderWrapperNode.class);
                }
            }

            assertEquals("There should be 3 local reads", 3, count);
        }
    }

    @Test
    public void wrapperNodeValueTest() {
        final String snippet = "function main(){" +
                        "a = 3;" +
                        "b = a;}";

        Map<String, SLRootNode> map = parse(snippet);

        for (String key : map.keySet()) {
            SLRootNode root = map.get(key);
            CallTarget target = Truffle.getRuntime().createCallTarget(root);
            target.call(new Object[0]);

            root.adoptChildren();
            Queue<Node> q = new LinkedList<>();
            q.add(root);
            int count = 0;

            while (!q.isEmpty()) {
                Node node = q.remove();

                for (Node child : node.getChildren())
                    q.add(child);

                if (node.getClass() == SLReadLocalVariableNodeGen.class) {
                    // The parent of an SLReadLocalVariableNodeGen has to be a WrapperNode
                    assertEquals(node.getParent().getClass(), LocalReaderWrapperNode.class);

                    LocalReaderWrapperNode wrapperNode = (LocalReaderWrapperNode) node.getParent();
                    assertEquals("Wrapper node's value should be 3", 3L,
                                    wrapperNode.getShadowSubTree().getRootValue());
                    count++;
                }
            }

            assertEquals(1, count);
        }

    }

    @Test
    public void wrapperNodeValueTest2() {
        final String snippet = "function main(){\n" +
                        "a = 3;\n" +
                        "b = a + 1;\n" +
                        "b = a + \n" +
                        "b + 5;}";

        Map<String, SLRootNode> map = parse(snippet);
        for (String key : map.keySet()) {
            SLRootNode root = map.get(key);
            CallTarget target = Truffle.getRuntime().createCallTarget(root);
            target.call(new Object[0]);

            root.adoptChildren();
            Queue<Node> q = new LinkedList<>();
            q.add(root);
            int count = 0;

            while (!q.isEmpty()) {
                Node node = q.remove();

                for (Node child : node.getChildren())
                    q.add(child);

                if (node.getClass() == SLReadLocalVariableNodeGen.class) {
                    final int lineNo = node.getSourceSection().getStartLine();
                    final LocalReaderWrapperNode wrapperNode = (LocalReaderWrapperNode) node.getParent();

                    switch (lineNo) {
                        case 3: {
                            // b = a;
                            assertEquals(3L, wrapperNode.getShadowSubTree().getRootValue());
                            assertEquals(1, wrapperNode.getShadowSubTree().getChildren().length);
                            ShadowTree child = wrapperNode.getShadowSubTree().getChildren()[0];
                            assertEquals(child.getASTNode().getClass(), SLWriteLocalVariableNodeGen.class);
                            // The read's value should come from the assignment in line 2
                            assertEquals(2, child.getASTNode().getSourceSection().getStartLine());
                            assertEquals(3L, child.getRootValue());
                            break;
                        }
                        case 4: {
                            // b = a
                            assertEquals(3L, wrapperNode.getShadowSubTree().getRootValue());
                            assertEquals(1, wrapperNode.getShadowSubTree().getChildren().length);
                            ShadowTree child = wrapperNode.getShadowSubTree().getChildren()[0];
                            assertEquals(child.getASTNode().getClass(), SLWriteLocalVariableNodeGen.class);
                            // The read's value should come from the assignment in line 2
                            assertEquals(2, child.getASTNode().getSourceSection().getStartLine());
                            assertEquals(3L, child.getRootValue());
                            break;
                        }
                        case 5: {
                            // b = b + 5 + ...
                            assertEquals(4L, wrapperNode.getShadowSubTree().getRootValue());
                            assertEquals(1, wrapperNode.getShadowSubTree().getChildren().length);
                            ShadowTree child = wrapperNode.getShadowSubTree().getChildren()[0];
                            assertEquals(child.getASTNode().getClass(), SLWriteLocalVariableNodeGen.class);
                            // The read's value should come from the assignment in line 3
                            assertEquals(3, child.getASTNode().getSourceSection().getStartLine());
                            assertEquals(4L, child.getRootValue());
                            break;
                        }
                    }
                }
            }

        }

    }

}
