package com.oracle.truffle.sl.tracer;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.junit.Test;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLRootNode;
import com.oracle.truffle.sl.nodes.expression.SLAddNode;
import com.oracle.truffle.sl.nodes.expression.SLAddNodeGen;
import com.oracle.truffle.sl.nodes.expression.SLDivNodeGen;
import com.oracle.truffle.sl.nodes.expression.SLMulNode;
import com.oracle.truffle.sl.nodes.expression.SLMulNodeGen;
import com.oracle.truffle.sl.nodes.expression.SLSubNode;
import com.oracle.truffle.sl.nodes.expression.SLSubNodeGen;
import com.oracle.truffle.sl.nodes.local.SLReadLocalVariableNodeGen;

public class NodeWrapperTest extends LanguageSetupTest {

    @Test
    public void wrapAddOpTest() {
        String snippet = "function main(){" +
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
            while (!q.isEmpty()) {
                Node node = q.remove();

                for (Node child : node.getChildren())
                    q.add(child);

                if (node.getClass() == SLAddNodeGen.class) {
                    // The parent of an SLAddNode has to be a WrapperNode
                    assertEquals(node.getParent().getClass(), WrapperNode.class);
                }
            }
        }

    }

    @Test
    public void wrapVariableReadTest() {
        String snippet = "function main(){" +
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
            while (!q.isEmpty()) {
                Node node = q.remove();

                for (Node child : node.getChildren())
                    q.add(child);

                if (node.getClass() == SLReadLocalVariableNodeGen.class) {
                    // The parent of an SLReadLocalVariableNodeGen has to be a WrapperNode
                    assertEquals(node.getParent().getClass(), WrapperNode.class);
                }
            }
        }

    }

    @Test
    public void wrapSubOpTest() {
        String snippet = "function main(){" +
                        "b = 1;" +
                        "c = 2;" +
                        "d = 3;" +
                        "a = (b - c) - d;}";

        Map<String, SLRootNode> map = parse(snippet);
        for (String key : map.keySet()) {
            SLRootNode root = map.get(key);
            root.adoptChildren();
            Queue<Node> q = new LinkedList<>();
            q.add(root);
            while (!q.isEmpty()) {
                Node node = q.remove();

                for (Node child : node.getChildren())
                    q.add(child);

                if (node.getClass() == SLSubNodeGen.class) {
                    // The parent of an SLSubNode has to be a WrapperNode
                    assertEquals(node.getParent().getClass(), WrapperNode.class);
                }
            }
        }

    }

    @Test
    public void wrapDivOpTest() {
        String snippet = "function main(){" +
                        "b = 1;" +
                        "c = 2;" +
                        "d = 3;" +
                        "a = (b * c) / d;}";

        Map<String, SLRootNode> map = parse(snippet);
        for (String key : map.keySet()) {
            SLRootNode root = map.get(key);
            root.adoptChildren();
            Queue<Node> q = new LinkedList<>();
            q.add(root);
            while (!q.isEmpty()) {
                Node node = q.remove();

                for (Node child : node.getChildren())
                    q.add(child);

                if (node.getClass() == SLDivNodeGen.class) {
                    // The parent of an SLDivNode has to be a WrapperNode
                    assertEquals(node.getParent().getClass(), WrapperNode.class);
                }
            }
        }
    }

    @Test
    public void wrapMulOpTest() {
        String snippet = "function main(){" +
                        "b = 1;" +
                        "c = 2;" +
                        "d = 3;" +
                        "a = (b * c) / d;}";

        Map<String, SLRootNode> map = parse(snippet);
        for (String key : map.keySet()) {
            SLRootNode root = map.get(key);
            root.adoptChildren();
            Queue<Node> q = new LinkedList<>();
            q.add(root);
            while (!q.isEmpty()) {
                Node node = q.remove();

                for (Node child : node.getChildren())
                    q.add(child);

                if (node.getClass() == SLMulNodeGen.class) {
                    System.out.println("OK");
                    // The parent of an SLMULNode has to be a WrapperNode
                    assertEquals(node.getParent().getClass(), WrapperNode.class);
                }
            }
        }
    }

}
