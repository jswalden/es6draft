/**
 * Copyright (c) 2012-2016 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.compiler;

import com.github.anba.es6draft.runtime.internal.RuntimeInfo;

/**
 * Base class for compiled functions.
 */
public class CompiledFunction {
    private final RuntimeInfo.Function function;

    protected CompiledFunction(RuntimeInfo.Function function) {
        this.function = function;
    }

    /**
     * Returns the runtime information for this function.
     * 
     * @return the runtime information object
     */
    public final RuntimeInfo.Function getFunction() {
        return function;
    }
}
