package com.oracle.truffle.sl.tracer;

import java.util.Stack;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.parser.SLNodeFactory;

/**
 * A read argument looks up for its origin in the operand stack of the caller's frame and pushes the
 * result on top of the operand stack of the current frame.
 *
 */
public class ReadArgumentShadowInstrument implements ShadowGeneratorInstrument {

    private final int index;

    public ReadArgumentShadowInstrument(int index) {
        this.index = index;
    }

    public void beforeExecuteGeneric(VirtualFrame frame, SLExpressionNode wrappedNode) {
    }

    public ShadowTree afterExecuteGeneric(VirtualFrame frame, SLExpressionNode wrappedNode, Object result) {
        VirtualFrame callerFrame = (VirtualFrame) Truffle.getRuntime().getCallerFrame().getFrame(FrameAccess.READ_ONLY, true);
        FrameSlot slot = callerFrame.getFrameDescriptor().findFrameSlot(callerFrame);

        ShadowTree argumentShadowTree = null;

        try {
            ShadowTree[] shadowTrees = (ShadowTree[]) callerFrame.getObject(slot);
            argumentShadowTree = shadowTrees[index];
        } catch (FrameSlotTypeException e) {
            throw new IllegalStateException(e);
        }
        return argumentShadowTree;
    }

}
