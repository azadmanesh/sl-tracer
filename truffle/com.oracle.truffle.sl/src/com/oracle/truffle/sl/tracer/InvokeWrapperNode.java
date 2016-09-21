package com.oracle.truffle.sl.tracer;

import java.util.Stack;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.call.SLDispatchNode;
import com.oracle.truffle.sl.nodes.call.SLInvokeNode;
import com.oracle.truffle.sl.parser.SLNodeFactory;

public class InvokeWrapperNode extends SLDispatchNode {

    @Children private final SLExpressionNode[] argumentNodes;
    @Child private SLDispatchNode wrappedNode;

    public InvokeWrapperNode(SLDispatchNode wrappedNode, SLExpressionNode[] argumentNodes) {
        this.wrappedNode = wrappedNode;
        this.argumentNodes = argumentNodes;
    }

    @Override
    public Object executeDispatch(VirtualFrame frame, Object function, Object[] arguments) {
        // find the shadow sub tree corresponding to each argument from the operand stack
        FrameSlot stackSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_OPERAND_STACK_KEY);

        try {
            Stack<ShadowTree> operandStack = (Stack<ShadowTree>) frame.getObject(stackSlot);

            if (operandStack.size() < arguments.length)
                throw new IllegalStateException("Operand stack's size should be at least " + arguments.length);

            ShadowTree[] shadowTree = new ShadowTree[arguments.length];
            for (int i = 0; i < shadowTree.length; i++) {
                shadowTree[i] = operandStack.pop();
            }

            // We pass the argument shadow trees through the frame. In order for the callee to find
            // the array of argument shadow trees in the caller's frame, we need a runtime entity to
            // be used as the key. This key should be such that it can uniquely and correctly
            // identify the shadow subtrees in the caller's frame. The only runtime entity we have
            // available at this point is the frame object itself.

            FrameSlot slot = frame.getFrameDescriptor().findOrAddFrameSlot(frame);
            frame.setObject(slot, shadowTree);

        } catch (FrameSlotTypeException e) {
            e.printStackTrace();
        }

        // We do this in the last step when the shadow data is already stored in the caller's frame
        // and the callee can access it
        return wrappedNode.executeDispatch(frame, function, arguments);
    }

}
