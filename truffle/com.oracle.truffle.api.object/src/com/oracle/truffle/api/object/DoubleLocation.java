/*
 * Copyright (c) 2013, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
package com.oracle.truffle.api.object;

/** @since 0.8 or earlier */
public interface DoubleLocation extends TypedLocation {
    /**
     * @see #get(DynamicObject, Shape)
     * @since 0.8 or earlier
     */
    double getDouble(DynamicObject store, Shape shape);

    /**
     * @see #get(DynamicObject, boolean)
     * @since 0.8 or earlier
     */
    double getDouble(DynamicObject store, boolean condition);

    /**
     * @see #set(DynamicObject, Object)
     * @since 0.8 or earlier
     */
    void setDouble(DynamicObject store, double value) throws FinalLocationException;

    /**
     * @see #set(DynamicObject, Object, Shape)
     * @since 0.8 or earlier
     */
    void setDouble(DynamicObject store, double value, Shape shape) throws FinalLocationException;

    /**
     * @see #set(DynamicObject, Object, Shape, Shape)
     * @since 0.8 or earlier
     */
    void setDouble(DynamicObject store, double value, Shape oldShape, Shape newShape);

    /** @since 0.8 or earlier */
    Class<Double> getType();
}
