package com.oracle.truffle.sl.tracer;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.sl.nodes.SLExpressionNode;

public class ReadArgumentWrapperNode extends WrapperNode {

    private final int index;

    public ReadArgumentWrapperNode(SLExpressionNode wrappedNode, int index) {
        super(wrappedNode);
        this.index = index;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object result = wrappedNode.executeGeneric(frame);
        VirtualFrame callerFrame = (VirtualFrame) Truffle.getRuntime().getCallerFrame().getFrame(FrameAccess.READ_ONLY, true);
        FrameSlot slot = callerFrame.getFrameDescriptor().findFrameSlot(callerFrame);

        try {
            ShadowTree[] shadowTrees = (ShadowTree[]) callerFrame.getObject(slot);
            this.shadowSubTree = shadowTrees[this.index];
        } catch (FrameSlotTypeException e) {
            e.printStackTrace();
        }

        // Incrementally dump the tree
        ShadowTree.dumpTree(this.shadowSubTree);

        return result;
    }

}
