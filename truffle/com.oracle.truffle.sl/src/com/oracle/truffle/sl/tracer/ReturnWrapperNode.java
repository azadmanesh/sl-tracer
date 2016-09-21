package com.oracle.truffle.sl.tracer;

import java.util.Stack;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;
import com.oracle.truffle.sl.nodes.controlflow.SLReturnException;
import com.oracle.truffle.sl.nodes.controlflow.SLReturnNode;
import com.oracle.truffle.sl.parser.SLNodeFactory;
import com.oracle.truffle.sl.runtime.SLNull;

/**
 * May read a value from the operand stack of the callee and writes it on top of the operand stack
 * of the caller
 *
 */
public class ReturnWrapperNode extends SLStatementNode {

    @Child private SLReturnNode wrappedNode;

    public ReturnWrapperNode(SLReturnNode wrappedNode) {
        this.wrappedNode = wrappedNode;
        this.setSourceSection(this.wrappedNode.getSourceSection());
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        SLReturnException result = null;
        try {
            wrappedNode.executeVoid(frame);
        } catch (SLReturnException e) {
            FrameSlot stackSlot = frame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_OPERAND_STACK_KEY);

            try {
                Stack<ShadowTree> operandStack = (Stack<ShadowTree>) frame.getObject(stackSlot);

                if (e.getResult() != SLNull.SINGLETON) {

                    if (operandStack.size() < 1)
                        throw new IllegalStateException("Operand stack's size should be at least 1!");

                    ShadowTree origin = operandStack.pop();
                    VirtualFrame callerFrame = (VirtualFrame) Truffle.getRuntime().getCallerFrame().getFrame(FrameAccess.READ_ONLY, true);
                    FrameSlot callerStackSlot = callerFrame.getFrameDescriptor().findFrameSlot(SLNodeFactory.SHADOW_OPERAND_STACK_KEY);
                    Stack<ShadowTree> callerStack = (Stack<ShadowTree>) callerFrame.getObject(callerStackSlot);
                    callerStack.push(origin);
                }
            } catch (FrameSlotTypeException fste) {
                e.printStackTrace();
            }

            result = e;
        }

        throw result;
    }

}
