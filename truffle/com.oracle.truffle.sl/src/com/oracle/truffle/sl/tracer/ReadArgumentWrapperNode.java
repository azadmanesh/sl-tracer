package com.oracle.truffle.sl.tracer;

import java.util.Stack;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.parser.SLNodeFactory;

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

            FrameSlot stackSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_OPERAND_STACK_KEY);
            Stack<ShadowTree> operandStack = (Stack<ShadowTree>) frame.getObject(stackSlot);

            for (ShadowTree shadowTree : shadowTrees) {
                operandStack.push(shadowTree);
            }

        } catch (FrameSlotTypeException e) {
            e.printStackTrace();
        }

        return result;
    }

}
