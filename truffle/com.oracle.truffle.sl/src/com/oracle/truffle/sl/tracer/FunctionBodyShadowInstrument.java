package com.oracle.truffle.sl.tracer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.controlflow.SLFunctionBodyNode;
import com.oracle.truffle.sl.parser.SLNodeFactory;

/**
 * Prepares the operand stack and the local variable table for shadow values in callee's frame.
 * After returning from the invocation, we read the shadow value from top of the operand stack of
 * the current frame. It has been pushed by the callee once a return statement is executed.
 *
 */
public class FunctionBodyShadowInstrument implements ShadowGeneratorInstrument {

    public void beforeExecuteGeneric(VirtualFrame frame) {
        // Initialize the shadow tree stack. It simulates the behavior of an operand stack
        final FrameSlot stackSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_OPERAND_STACK_KEY);
        Stack<ShadowTree> stack = new Stack<ShadowTree>();
        frame.setObject(stackSlot, stack);

        // Initialize the mapping of locals to their list of occurrences.
        final FrameSlot localSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_LOCAL_KEY);
        frame.setObject(localSlot, new HashMap<String, List<ShadowTree>>());

    }

    public ShadowTree afterExecuteGeneric(VirtualFrame frame, SLExpressionNode wrappedNode, Object result) {
        // The shadow value is already pushed on top of this frame's operand stack by the wrapper of
        // return statement in the callee. We just pop it here and return it to be consistent with
        // the semantics of an expression wrapper node.
        ShadowTree returnValueShadow = null;
        try {
            final FrameSlot stackSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_OPERAND_STACK_KEY);
            Stack<ShadowTree> stack = (Stack<ShadowTree>) frame.getObject(stackSlot);
            returnValueShadow = stack.pop();
        } catch (FrameSlotTypeException e) {
            throw new IllegalStateException(e);
        }

        return returnValueShadow;
    }

}
