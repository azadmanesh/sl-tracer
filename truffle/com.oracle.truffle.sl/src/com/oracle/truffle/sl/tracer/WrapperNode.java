package com.oracle.truffle.sl.tracer;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.sl.nodes.SLExpressionNode;

/**
 * A wrapper for each expression node.
 *
 */
public class WrapperNode extends SLExpressionNode {

    @Child protected SLExpressionNode wrappedNode;

    protected ShadowTree shadowSubTree;

    public WrapperNode(SLExpressionNode wrappedNode) {
        this.wrappedNode = wrappedNode;
        // we cannot override getSourceSection method because it is final, so we set source section
        // here to avoid NPE
        this.setSourceSection(this.wrappedNode.getSourceSection());
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object result = wrappedNode.executeGeneric(frame);

        // find the shadow subtrees of the children nodes of the wrapped node
        final List<ShadowTree> subTreeRoots = new ArrayList<>();
        for (final Node child : wrappedNode.getChildren()) {

            if (child instanceof WrapperNode) {
                subTreeRoots.add(((WrapperNode) child).getShadowSubTree());
            }

        }

        ShadowTree[] tree = subTreeRoots.toArray(new ShadowTree[0]);
        this.shadowSubTree = new ShadowTree(wrappedNode, result, tree);

        // Incrementally dump the tree
        ShadowTree.dumpTree(this.shadowSubTree);
        return result;
    }

    @TruffleBoundary
    public void println(String msg) {
        System.out.println(msg);
    }

    public ShadowTree getShadowSubTree() {
        return this.shadowSubTree;
    }

}
