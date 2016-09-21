package com.oracle.truffle.sl.tracer;

import java.util.Stack;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.parser.SLNodeFactory;

/**
 * Reads one operand from top of the operand stack and writes the result on top of the operand stack
 *
 */
public class UnaryOperatorWrapperNode extends WrapperNode {

    public UnaryOperatorWrapperNode(SLExpressionNode wrappedNode) {
        super(wrappedNode);
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object result = this.wrappedNode.executeGeneric(frame);

        FrameSlot stackSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_OPERAND_STACK_KEY);

        try {
            Stack<ShadowTree> operandStack = (Stack<ShadowTree>) frame.getObject(stackSlot);

            if (operandStack.size() < 1)
                throw new IllegalStateException("Operand stack's size should be at least 1!");

            ShadowTree origin1 = operandStack.pop();

            ShadowTree current = new ShadowTree(this.wrappedNode, result, new ShadowTree[]{origin1});

            operandStack.push(current);

            System.out.println("dumping unary op");
            ShadowTree.dumpTree(current);
            System.out.println("End\n");

        } catch (FrameSlotTypeException e) {
            e.printStackTrace();
        }

        return result;
    }

}
