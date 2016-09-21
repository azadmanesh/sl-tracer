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
public class LiteralWrapperNode extends WrapperNode {

    public LiteralWrapperNode(SLExpressionNode wrappedNode) {
        super(wrappedNode);
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object result = this.wrappedNode.executeGeneric(frame);

        FrameSlot stackSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_OPERAND_STACK_KEY);

        try {
            Stack<ShadowTree> operandStack = (Stack<ShadowTree>) frame.getObject(stackSlot);

            ShadowTree current = new ShadowTree(this.wrappedNode, result, new ShadowTree[0]);

            operandStack.push(current);

        } catch (FrameSlotTypeException e) {
            e.printStackTrace();
        }

        return result;
    }

}
