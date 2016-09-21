package com.oracle.truffle.sl.tracer;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.parser.SLNodeFactory;

/**
 * In order to create the shadow tree for a local read, we need to use the last shadow subtree
 * stored in the virtual frame for that local variable.
 *
 */
public class LocalReaderWrapperNode extends WrapperNode {

    private final String name;

    private LocalReaderWrapperNode(SLExpressionNode wrappedNode, String name) {
        super(wrappedNode);
        this.name = name;
    }

    public static LocalReaderWrapperNode create(SLExpressionNode wrappedNode, String name) {
        return new LocalReaderWrapperNode(wrappedNode, name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object executeGeneric(VirtualFrame frame) {
        Object result = wrappedNode.executeGeneric(frame);
        FrameSlot localSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_LOCAL_KEY);
        FrameSlot stackSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_OPERAND_STACK_KEY);

        try {
            Map<String, List<ShadowTree>> localsOccurrenceMap = (Map<String, List<ShadowTree>>) frame.getObject(localSlot);
            ShadowTree origin = null;

            List<ShadowTree> localOccurrences = localsOccurrenceMap.get(this.name);
            if (localOccurrences != null) {
                // we get the last occurrence
                origin = localOccurrences.get(localOccurrences.size() - 1);
            }

            ShadowTree current = new ShadowTree(this.wrappedNode, result, origin == null ? new ShadowTree[0] : new ShadowTree[]{origin});

            Stack<ShadowTree> operandStack = (Stack<ShadowTree>) frame.getObject(stackSlot);
            operandStack.push(current);

        } catch (FrameSlotTypeException e) {
            e.printStackTrace();
        }

        return result;
    }

}
