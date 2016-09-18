package com.oracle.truffle.sl.tracer;

import java.util.Map;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.sl.nodes.SLExpressionNode;

/**
 * An instance of this class is used for AST nodes which have a side-effect, but not on the stack,
 * e.g., a write into a local variable. This wrapper node writes a mapping of the value into the
 * current frame.
 *
 * TODO A copy wrapper for property writes
 */
public class LocalWriterWrapperNode extends WrapperNode {

    private final Map<String, ShadowTree> shadowValuesMap;

    private final String name;

    private LocalWriterWrapperNode(SLExpressionNode wrappedNode, Map<String, ShadowTree> shadowValuesMap, String name) {
        super(wrappedNode);
        this.shadowValuesMap = shadowValuesMap;
        this.name = name;
    }

    public static LocalWriterWrapperNode create(SLExpressionNode wrappedNode, Map<String, ShadowTree> shadowValuesMap, String name) {
        return new LocalWriterWrapperNode(wrappedNode, shadowValuesMap, name);
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object result = super.executeGeneric(frame);
        shadowValuesMap.put(name, this.shadowSubTree);
        return result;
    }

}
