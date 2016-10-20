package com.oracle.truffle.sl.tracer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLRootNode;
import com.oracle.truffle.sl.nodes.controlflow.SLFunctionBodyNode;
import com.oracle.truffle.sl.parser.SLNodeFactory;
import com.oracle.truffle.sl.runtime.SLNull;

/**
 * Prepares the operand stack and the local variable table for shadow values in callee's frame.
 * After returning from the invocation, we read the shadow value from top of the operand stack of
 * the current frame. It has been pushed by the callee once a return statement is executed.
 *
 */
public class FunctionBodyShadowInstrument implements ShadowGeneratorInstrument {

    public void beforeExecuteGeneric(VirtualFrame frame, SLExpressionNode wrappedNode) {
        // Initialize the shadow tree stack. It simulates the behavior of an operand stack
        final FrameSlot stackSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_OPERAND_STACK_KEY);
        Stack<ShadowTree> stack = new Stack<ShadowTree>();
        frame.setObject(stackSlot, stack);

        // Initialize the mapping of locals to their list of occurrences.
        final FrameSlot localSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_LOCAL_KEY);
        frame.setObject(localSlot, new HashMap<String, List<ShadowTree>>());

    }

    public ShadowTree afterExecuteGeneric(VirtualFrame frame, SLExpressionNode wrappedNode, Object result) {
        // At this point, we pass the shadow return value from the callee's operand stack to the
        // caller's operand stack.The shadow value may have been written on top of the operand stack
        // of the current frame,
        // depending on the existence of a return statement. A return stmt. is not an expression, so
        // in future we may remove the wrapper around a return stmt, that's why we pass the shadow
        // return value to the operand stack of the caller at this point.
        ShadowTree returnValueShadow = null;

        try {
            FrameSlot stackSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_OPERAND_STACK_KEY);
            Stack<ShadowTree> operandStack = (Stack<ShadowTree>) frame.getObject(stackSlot);
            if (operandStack.size() > 0) {

                returnValueShadow = operandStack.pop();
            } else {
                assert result == SLNull.SINGLETON;
                returnValueShadow = new ShadowTree(wrappedNode, SLNull.SINGLETON, new ShadowTree[0]);
            }

            VirtualFrame callerFrame = (VirtualFrame) Truffle.getRuntime().getCallerFrame().getFrame(FrameAccess.READ_ONLY, true);
            FrameSlot callerStackSlot = callerFrame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_OPERAND_STACK_KEY);

            if (callerStackSlot != null) {    // This is the case for the caller of the main method
                Stack<ShadowTree> callerStack = (Stack<ShadowTree>) callerFrame.getObject(callerStackSlot);
                callerStack.push(returnValueShadow);
            }

        } catch (FrameSlotTypeException e) {
            throw new IllegalStateException(e);
        }

        return returnValueShadow;
    }

}
