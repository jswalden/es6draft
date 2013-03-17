/**
 * Copyright (c) 2012-2013 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.types;

/**
 * <h1>8 Types</h1><br>
 * <h2>8.1 ECMAScript Language Types</h2><br>
 * <h3>8.1.6 The Object Type</h3>
 * <ul>
 * <li>8.1.6.2 Object Internal Methods and Internal Data Properties
 * </ul>
 * <p>
 * Internal Method: [[Call]]
 */
public interface Callable extends ScriptObject {
    /**
     * [[BuiltinBrand]]
     */
    BuiltinBrand getBuiltinBrand();

    /**
     * [[Call]]
     */
    Object call(Object thisValue, Object... args);

    /**
     * Source representation of this callable
     */
    String toSource();
}
