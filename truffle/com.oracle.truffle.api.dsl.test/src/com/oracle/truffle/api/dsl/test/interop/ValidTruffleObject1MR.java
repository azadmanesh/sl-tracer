/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.api.dsl.test.interop;

import com.oracle.truffle.api.dsl.test.ExpectError;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.nodes.Node;

@SuppressWarnings("unused")
@MessageResolution(receiverType = ValidTruffleObject1.class, language = TestTruffleLanguage.class)
public class ValidTruffleObject1MR {
    @Resolve(message = "INVOKE")
    public abstract static class Invoke4 extends Node {
        @ExpectError({"The 3 argument must be a java.lang.String- but is java.lang.Object"})
        protected int access(VirtualFrame frame, ValidTruffleObject0 receiver, Object name, Object[] args) {
            return 0;
        }
    }

    @Resolve(message = "READ")
    public abstract static class ReadNode5 extends Node {

        @ExpectError({"Wrong number of arguments. Expected signature: ([frame: VirtualFrame], receiverObject: TruffleObject, identifier: String)"})
        protected int access(String string, Object receiver, Object name) {
            return 0;
        }
    }

}
