package com.oracle.truffle.sl.tracer;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.Node.Child;
import com.oracle.truffle.sl.nodes.SLExpressionNode;

/**
 * A wrapper for each expression node.
 *
 */
public class WrapperNode extends SLExpressionNode {

    @Child private SLExpressionNode wrappedNode;

    public WrapperNode(SLExpressionNode wrappedNode) {
        this.wrappedNode = wrappedNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object result = wrappedNode.executeGeneric(frame);
        println("TRACER:\tWrapped Node with value " + result);
        return result;
    }

    @TruffleBoundary
    public void println(String msg) {
        System.out.println(msg);
    }

}
