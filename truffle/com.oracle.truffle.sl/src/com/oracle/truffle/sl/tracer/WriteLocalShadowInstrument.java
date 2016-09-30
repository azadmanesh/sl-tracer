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
 * The shadow will be read from top of the stack and the new tree will be written in the current
 * frame for the given local variable.
 *
 */
public class WriteLocalShadowInstrument implements ShadowGeneratorInstrument {

    private final String name;

    public WriteLocalShadowInstrument(String name) {
        this.name = name;
    }

    public void beforeExecuteGeneric(VirtualFrame frame) {
    }

    public ShadowTree afterExecuteGeneric(VirtualFrame frame, SLExpressionNode wrappedNode, Object result) {
        ShadowTree newShadowTree = null;
        try {
            // Read the shadow tree of the current value from top of operand stack
            FrameSlot stackSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_OPERAND_STACK_KEY);
            Stack<ShadowTree> operandStack = (Stack<ShadowTree>) frame.getObject(stackSlot);
            ShadowTree origin = null;
            if (!operandStack.isEmpty()) {
                origin = operandStack.pop();
            }

            newShadowTree = new ShadowTree(wrappedNode, result, origin == null ? new ShadowTree[0] : new ShadowTree[]{origin});

            // Add the new shadow tree for the given local variable to the current frame to be
            // accessible for further reads
            FrameSlot localSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_LOCAL_KEY);
            Map<String, List<ShadowTree>> localOccurrencesMap = (Map<String, List<ShadowTree>>) frame.getObject(localSlot);
            List<ShadowTree> localOccurrences = localOccurrencesMap.get(this.name);
            if (localOccurrences == null) {
                localOccurrences = new ArrayList<ShadowTree>();
                localOccurrencesMap.put(this.name, localOccurrences);
            }

            localOccurrences.add(newShadowTree);

            ShadowTree.dumpTree(newShadowTree);

        } catch (FrameSlotTypeException e) {
            throw new IllegalStateException(e);
        }

        return newShadowTree;
    }

}
