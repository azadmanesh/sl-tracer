package com.oracle.truffle.sl.tracer;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.controlflow.SLReturnException;
import com.oracle.truffle.sl.parser.Parser;
import com.oracle.truffle.sl.parser.SLNodeFactory;

/**
 * A wrapper for each expression node. Wrapper nodes are differentiated based on the source of
 * information they consume. There are different standard sources for each language such as local
 * variables, operand stack, function calls, and global space. An instance of this class represents
 * the case where the source of information is the operand stack.
 *
 */
public class ExpressionWrapperNode extends SLExpressionNode {

    private final ShadowGeneratorInstrument shadowUtility;
    @Child protected SLExpressionNode wrappedNode;

    public ExpressionWrapperNode(SLExpressionNode wrappedNode, ShadowGeneratorInstrument shadowUtility) {
        this.wrappedNode = wrappedNode;
        // we cannot override getSourceSection method because it is final, so we set source section
        // here to avoid NPE
        this.setSourceSection(this.wrappedNode.getSourceSection());
        this.shadowUtility = shadowUtility;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object executeGeneric(VirtualFrame frame) {
        if (!Parser.DO_TRACE)
            return this.wrappedNode.executeGeneric(frame);

        if (frame == null)
            return this.wrappedNode.executeGeneric(frame);

        this.shadowUtility.beforeExecuteGeneric(frame);

        Object result = null;
        try {
            result = this.wrappedNode.executeGeneric(frame);
        } catch (SLReturnException e) {
            result = e.getResult();
        }

        ShadowTree newShadowTree = this.shadowUtility.afterExecuteGeneric(frame, wrappedNode, result);

        // All expression type nodes must write a value on top of the stack.
        try {
            FrameSlot stackSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_OPERAND_STACK_KEY);
            Stack<ShadowTree> stack = (Stack<ShadowTree>) frame.getObject(stackSlot);
            stack.push(newShadowTree);
        } catch (FrameSlotTypeException e) {
            throw new IllegalStateException(e);
        }

        return result;
    }

}
