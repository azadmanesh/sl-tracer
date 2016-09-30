package com.oracle.truffle.sl.tracer;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.LocationFactory;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.parser.SLNodeFactory;
import com.oracle.truffle.sl.runtime.SLUndefinedNameException;

/**
 * A write into a property has three origins: The receiver object, the name of the property, and the
 * value to be written. The result of writing into a property is wrapped as a shadow and written as
 * a property into a shadow object. The shadow object is accessible from the application object
 * through the field SHADOW_PROPERTY_KEY.
 *
 */
public class WritePropertyShadowInstrument implements ShadowGeneratorInstrument {

    public final static Object SHADOW_PROPERTY_KEY = new Object();

    public void beforeExecuteGeneric(VirtualFrame frame) {
    }

    public ShadowTree afterExecuteGeneric(VirtualFrame frame, SLExpressionNode wrappedNode, Object result) {
        ShadowTree newShadowTree = null;
        try {
            FrameSlot stackSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_OPERAND_STACK_KEY);
            Stack<ShadowTree> stack = (Stack<ShadowTree>) frame.getObject(stackSlot);

            if (stack.size() < 3)
                throw new IllegalStateException("The operand stack size should be at least 3!");

            final ShadowTree writtenValueShadow = stack.pop();
            final ShadowTree nameShadow = stack.pop();
            final ShadowTree receiverShadow = stack.pop();

            final DynamicObject receiver = (DynamicObject) receiverShadow.getRootValue();

            if (!receiver.containsKey(SHADOW_PROPERTY_KEY)) {
                receiver.define(SHADOW_PROPERTY_KEY, new HashMap<Object, ShadowTree>());
            }

            Map<Object, ShadowTree> shadowObject = (Map<Object, ShadowTree>) receiver.get(SHADOW_PROPERTY_KEY);
            newShadowTree = new ShadowTree(wrappedNode, result, new ShadowTree[]{writtenValueShadow, nameShadow, receiverShadow});
            shadowObject.put(nameShadow.getRootValue(), newShadowTree);

        } catch (FrameSlotTypeException e) {
            e.printStackTrace();
        }

        return newShadowTree;
    }

}
