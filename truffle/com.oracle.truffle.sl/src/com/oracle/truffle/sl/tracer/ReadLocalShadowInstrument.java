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
 * A read from local finds its origin in the local variable table of the current frame and pushes it
 * on top of the operand stack of the current frame. For a local variable, there is a list of shadow
 * subtrees, each corresponding to an occurrence of that local variable during the execution. During
 * the execution, we need to use the last shadow subtree stored in the virtual frame for the given
 * local variable. In a postmorterm analysis, we may need to access earlier occurrences.
 *
 */
public class ReadLocalShadowInstrument implements ShadowGeneratorInstrument {

    private final String name;

    public ReadLocalShadowInstrument(String name) {
        this.name = name;
    }

    public ShadowTree afterExecuteGeneric(VirtualFrame frame, SLExpressionNode wrappedNode, Object result) {
        ShadowTree newShadowTree = null;
        try {
            final FrameSlot localSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_LOCAL_KEY);
            Map<String, List<ShadowTree>> localsOccurrenceMap = (Map<String, List<ShadowTree>>) frame.getObject(localSlot);
            ShadowTree origin = null;

            List<ShadowTree> localOccurrences = localsOccurrenceMap.get(this.name);
            if (localOccurrences != null) {
                // we get the last occurrence
                origin = localOccurrences.get(localOccurrences.size() - 1);
            }

            newShadowTree = new ShadowTree(wrappedNode, result, origin == null ? new ShadowTree[0] : new ShadowTree[]{origin});

        } catch (FrameSlotTypeException e) {
            throw new IllegalStateException(e);
        }

        return newShadowTree;
    }

    public void beforeExecuteGeneric(VirtualFrame frame) {
    }

}
