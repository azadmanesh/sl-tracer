package com.oracle.truffle.sl.tracer;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.Node;

/**
 * A shadow tree keeps track of the values generated throughout executing an AST.
 *
 */
public class ShadowTree {

    private final Object value;

    private final ShadowTree[] children;

    private final Node astNode;

    public ShadowTree(Node delegate, Object value, ShadowTree[] children) {
        this.value = value;
        this.children = children;
        this.astNode = delegate;
    }

    @TruffleBoundary
    public static void dumpTree(ShadowTree root) {
        System.out.println("\nDumping Shadow Tree:");
        dumpTree(root, 1);
    }

    @TruffleBoundary
    private static void dumpTree(ShadowTree root, int level) {
        if (root.astNode.getSourceSection() != null) {      // source section can be null, e.g.:
                                                            // formal parameter passing
            pad(level);
            System.out.println("Node:\t" + root.astNode.getClass().getSimpleName() + ", Line:\t" + root.astNode.getSourceSection().getStartLine() + ", Value:\t" + root.value);
        }

        for (ShadowTree child : root.children) {
            dumpTree(child, level + 1);
        }
    }

    @TruffleBoundary
    private static void pad(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("\t");
        }
    }

    public ShadowTree[] getChildren() {
        return children;
    }

    public Object getRootValue() {
        return this.value;
    }

    public Node getASTNode() {
        return this.astNode;
    }
}
