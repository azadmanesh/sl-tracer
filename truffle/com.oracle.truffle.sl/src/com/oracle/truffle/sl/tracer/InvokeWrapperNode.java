package com.oracle.truffle.sl.tracer;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.call.SLDispatchNode;
import com.oracle.truffle.sl.nodes.call.SLInvokeNode;

public class InvokeWrapperNode extends SLDispatchNode {

    @Children private final SLExpressionNode[] argumentNodes;
    @Child private SLDispatchNode wrappedNode;

    public InvokeWrapperNode(SLDispatchNode wrappedNode, SLExpressionNode[] argumentNodes) {
        this.wrappedNode = wrappedNode;
        this.argumentNodes = argumentNodes;
    }

    @Override
    public Object executeDispatch(VirtualFrame frame, Object function, Object[] arguments) {
        ShadowTree[] shadowTree = new ShadowTree[this.argumentNodes.length];

        // find the shadow sub tree corresponding to each argument
        for (int i = 0; i < argumentNodes.length; i++) {
            SLExpressionNode arg = argumentNodes[i];

            assert (arg instanceof WrapperNode);
            WrapperNode argWrapper = (WrapperNode) arg;
            shadowTree[i] = argWrapper.getShadowSubTree();
        }

        // We pass the argument shadow trees through the frame. In order for the callee to find the
        // array of argument shadow trees in the caller's frame, we need a runtime entity to be used
        // as the key. This key should be such that it can uniquely and correctly identify the
        // shadow subtrees in the caller's frame. The only runtime entity we have available at this
        // point is the frame object itself.
        FrameSlot slot = frame.getFrameDescriptor().findOrAddFrameSlot(frame);
        frame.setObject(slot, shadowTree);

        // We do this in the last step when the shadow data is already stored in the caller's frame
        // and the callee can access it
        return wrappedNode.executeDispatch(frame, function, arguments);
    }

}
