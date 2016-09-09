package com.oracle.truffle.sl.tracer;

import java.util.Map;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.sl.nodes.SLExpressionNode;

/**
 * In order to create the shadow tree for a local read, we need to use the last shadow subtree
 * stored in the virtual frame for that local variable.
 *
 */
public class LocalReaderWrapperNode extends WrapperNode {

    private final Map<String, ShadowTree> shadowValuesMap;

    private final String name;

    private LocalReaderWrapperNode(SLExpressionNode wrappedNode, Map<String, ShadowTree> shadowValuesMap, String name) {
        super(wrappedNode);
        this.shadowValuesMap = shadowValuesMap;
        this.name = name;
    }

    public static LocalReaderWrapperNode create(SLExpressionNode wrappedNode, Map<String, ShadowTree> shadowValuesMap, String name) {
        return new LocalReaderWrapperNode(wrappedNode, shadowValuesMap, name);
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object result = wrappedNode.executeGeneric(frame);
        this.shadowSubTree = new ShadowTree(this.wrappedNode, result, new ShadowTree[]{shadowValuesMap.get(name)});
        return result;
    }

}
