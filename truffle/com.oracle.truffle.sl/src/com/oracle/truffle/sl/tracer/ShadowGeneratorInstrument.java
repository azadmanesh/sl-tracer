package com.oracle.truffle.sl.tracer;

import java.util.Stack;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.sl.nodes.SLExpressionNode;

public interface ShadowGeneratorInstrument {

    /**
     * Any housekeeping before executing the wrapped node can be done here
     *
     * @param frame
     * @param operandStack
     */
    public void beforeExecuteGeneric(VirtualFrame frame);

    /**
     * This is invoked right after excuting the wrapped node for retrieving the shadow tree
     * corresponding to this wrapped node. The shadow tree might be stored into some specific space
     * before being returned.
     *
     * @param frame
     * @param wrappedNode
     * @param result
     * @return
     */
    public ShadowTree afterExecuteGeneric(VirtualFrame frame, SLExpressionNode wrappedNode, Object result);
}
