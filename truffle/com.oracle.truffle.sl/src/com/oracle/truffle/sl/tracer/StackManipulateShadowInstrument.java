package com.oracle.truffle.sl.tracer;

import java.util.Stack;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.parser.SLNodeFactory;

/**
 * This class is applied to any operator which operates on some operands, all residing in the
 * operand stack. Given that the number of operands may differ for different operators, and the fact
 * that we need to make the wrapper nodes language-agnostic, we keep track of the index of the
 * operand stack before executing the wrapped node to find the number of operands prepared for this
 * operator. All of those values on top of the operand stack will be considered as the origins of
 * the result of this operator.
 *
 */
public class StackManipulateShadowInstrument implements ShadowGeneratorInstrument {

    private int stackSizeBefore;

    public void beforeExecuteGeneric(VirtualFrame frame, SLExpressionNode wrappedNode) {
        FrameSlot stackSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_OPERAND_STACK_KEY);
        Stack<ShadowTree> stack;
        try {
            stack = (Stack<ShadowTree>) frame.getObject(stackSlot);
        } catch (FrameSlotTypeException e) {
            throw new IllegalStateException(e);
        }

        /*
         * we keep track of the last index written into operand stack before executing this node.
         * Given that the wrapper nodes are supposed to be language agnostic, we don't know in
         * advance how many operands an operator may have, so we take all the operand values on top
         * of the operand stack to the point before this node was executed.
         */
        this.stackSizeBefore = stack.size();
    }

    public ShadowTree afterExecuteGeneric(VirtualFrame frame, SLExpressionNode wrappedNode, Object result) {
        ShadowTree newShadowTree = null;
        try {
            FrameSlot stackSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_OPERAND_STACK_KEY);
            Stack<ShadowTree> stack = (Stack<ShadowTree>) frame.getObject(stackSlot);

            /*
             * Given that the wrapper nodes are supposed to be language agnostic, we don't know in
             * advance how many operands an operator may have, so we take all the operand values on
             * top of the operand stack to the point before this node was executed.
             */
            final int currStackSize = stack.size();
            final int operandCount = currStackSize - this.stackSizeBefore;

            ShadowTree[] operands = new ShadowTree[operandCount];
            for (int i = 0; i < operandCount; i++) {
                operands[i] = stack.pop();
            }

            newShadowTree = new ShadowTree(wrappedNode, result, operands);
        } catch (FrameSlotTypeException e) {
            throw new IllegalStateException(e);
        }

        return newShadowTree;
    }

}
