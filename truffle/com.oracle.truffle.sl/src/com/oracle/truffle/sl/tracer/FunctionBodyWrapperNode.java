package com.oracle.truffle.sl.tracer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.sl.nodes.controlflow.SLFunctionBodyNode;
import com.oracle.truffle.sl.parser.SLNodeFactory;

public class FunctionBodyWrapperNode extends WrapperNode {

    public FunctionBodyWrapperNode(SLFunctionBodyNode wrappedNode) {
        super(wrappedNode);
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        // Initialize the shadow tree stack. It simulates the behavior of an operand stack
        final FrameSlot stackSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_OPERAND_STACK_KEY);
        Stack<ShadowTree> stack = new Stack<ShadowTree>();
        frame.setObject(stackSlot, stack);

        // Initialize the mapping of locals to their list of occurrences.
        final FrameSlot localSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_LOCAL_KEY);
        frame.setObject(localSlot, new HashMap<String, List<ShadowTree>>());

        return wrappedNode.executeGeneric(frame);
    }

}
