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
 * An instance of this class is used for AST nodes which have a side-effect, but not on the stack,
 * e.g., a write into a local variable. This wrapper node writes a mapping of the value into the
 * current frame.
 *
 * TODO A copy wrapper for property writes
 */
public class LocalWriterWrapperNode extends WrapperNode {

    private final String name;

    private LocalWriterWrapperNode(SLExpressionNode wrappedNode, String name) {
        super(wrappedNode);
        this.name = name;
    }

    public static LocalWriterWrapperNode create(SLExpressionNode wrappedNode, String name) {
        return new LocalWriterWrapperNode(wrappedNode, name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object executeGeneric(VirtualFrame frame) {
        Object result = this.wrappedNode.executeGeneric(frame);

        FrameSlot localSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_LOCAL_KEY);
        FrameSlot stackSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_OPERAND_STACK_KEY);

        try {
            Map<String, List<ShadowTree>> localOccurrencesMap = (Map<String, List<ShadowTree>>) frame.getObject(localSlot);

            Stack<ShadowTree> operandStack = (Stack<ShadowTree>) frame.getObject(stackSlot);
            ShadowTree origin = null;

            if (!operandStack.isEmpty()) {
                origin = operandStack.pop();
            }

            ShadowTree current = new ShadowTree(this.wrappedNode, result, origin == null ? new ShadowTree[0] : new ShadowTree[]{origin});

            List<ShadowTree> localOccurrences = localOccurrencesMap.get(this.name);
            if (localOccurrences == null) {
                localOccurrences = new ArrayList<ShadowTree>();
                localOccurrencesMap.put(this.name, localOccurrences);
            }

            localOccurrences.add(current);

            ShadowTree.dumpTree(current);

        } catch (FrameSlotTypeException e) {
            e.printStackTrace();
        }

        return result;
    }

}
