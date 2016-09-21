package com.oracle.truffle.sl.tracer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.parser.SLNodeFactory;

/**
 * Reads two operands from top of the operand stack and writes the result on top of the operand
 * stack
 *
 */
public class BinaryOperatorWrapperNode extends WrapperNode {

    public BinaryOperatorWrapperNode(SLExpressionNode wrappedNode) {
        super(wrappedNode);
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object result = this.wrappedNode.executeGeneric(frame);

        FrameSlot stackSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_OPERAND_STACK_KEY);

        try {
            Stack<ShadowTree> operandStack = (Stack<ShadowTree>) frame.getObject(stackSlot);

            if (operandStack.size() < 2)
                throw new IllegalStateException("Operand stack's size should be at least 2!");

            ShadowTree origin1 = operandStack.pop();
            ShadowTree origin2 = operandStack.pop();

            ShadowTree current = new ShadowTree(this.wrappedNode, result, new ShadowTree[]{origin1, origin2});

            operandStack.push(current);

        } catch (FrameSlotTypeException e) {
            e.printStackTrace();
        }

        return result;
    }

}
