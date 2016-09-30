package com.oracle.truffle.sl.tracer;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.access.SLReadPropertyNode;
import com.oracle.truffle.sl.nodes.access.SLReadPropertyNodeGen;
import com.oracle.truffle.sl.nodes.expression.SLStringLiteralNode;
import com.oracle.truffle.sl.parser.SLNodeFactory;

/**
 * A read property has three origins: the reciever object, the name of the property, and the current
 * value of that property. In order to find the origin of the current value for a property, we find
 * the shadow object which keeps track of the shadow trees for each property of this object. This
 * shadow object is accessible from the application object through the property SHADOW_PROPERTY_KEY.
 *
 */
public class ReadPropertyShadowInstrument implements ShadowGeneratorInstrument {

    public void beforeExecuteGeneric(VirtualFrame frame) {
    }

    @SuppressWarnings("unchecked")
    public ShadowTree afterExecuteGeneric(VirtualFrame frame, SLExpressionNode wrappedNode, Object result) {
        ShadowTree newShadowTree = null;
        try {
            FrameSlot stackSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_OPERAND_STACK_KEY);
            Stack<ShadowTree> stack = (Stack<ShadowTree>) frame.getObject(stackSlot);

            if (stack.size() < 2)
                throw new IllegalStateException("The operand stack size should be at least 2!");

            final ShadowTree nameShadow = stack.pop();
            final ShadowTree receiverShadow = stack.pop();

            final DynamicObject receiver = (DynamicObject) receiverShadow.getRootValue();

            if (!receiver.containsKey(WritePropertyShadowInstrument.SHADOW_PROPERTY_KEY) ||
                            !((Map<Object, ShadowTree>) receiver.get(WritePropertyShadowInstrument.SHADOW_PROPERTY_KEY)).containsKey(nameShadow.getRootValue())) {
                // There is no information about the origin of the read value, so the newly created
                // shadow tree skips that.
                newShadowTree = new ShadowTree(wrappedNode, result, new ShadowTree[]{nameShadow, receiverShadow});
            } else {
                Map<Object, ShadowTree> shadowObject = (Map<Object, ShadowTree>) receiver.get(WritePropertyShadowInstrument.SHADOW_PROPERTY_KEY);
                newShadowTree = new ShadowTree(wrappedNode, result, new ShadowTree[]{shadowObject.get(nameShadow.getRootValue()), nameShadow, receiverShadow});
            }
        } catch (FrameSlotTypeException e) {
            e.printStackTrace();
        }

        return newShadowTree;

    }

}
