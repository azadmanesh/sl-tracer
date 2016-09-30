package com.oracle.truffle.sl.tracer;

import java.util.Stack;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.parser.SLNodeFactory;

/**
 * Just adds one shadow value on top of stack without reading any value.
 *
 */
public class LiteralShadowInstrument implements ShadowGeneratorInstrument {

    public void beforeExecuteGeneric(VirtualFrame frame) {
    }

    public ShadowTree afterExecuteGeneric(VirtualFrame frame, SLExpressionNode wrappedNode, Object result) {
        return new ShadowTree(wrappedNode, result, new ShadowTree[0]);
    }

}
