/**
 * Copyright (c) 2012-2016 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.compiler;

import com.github.anba.es6draft.compiler.assembler.Stack;
import com.github.anba.es6draft.compiler.assembler.Type;
import com.github.anba.es6draft.compiler.assembler.Variables;

/**
 * Application specific override of {@link Stack}.
 */
final class StackImpl extends Stack {
    public StackImpl(Variables variables) {
        super(variables);
    }

    @Override
    protected Type intersectionType(Type left, Type right) {
        // Hard coded type relationships to avoid dynamic class loading.
        // TODO: Change to a less brittle approach.
        Type commonType = Types.Object;
        if (isCharSequence(left)) {
            if (isCharSequence(right)) {
                commonType = Types.CharSequence;
            }
        } else if (isNumber(left)) {
            if (isNumber(right)) {
                commonType = Types.Number;
            }
        } else if (isFunctionObject(left)) {
            if (isFunctionObject(right)) {
                commonType = Types.FunctionObject;
            } else if (isOrdinaryObject(right)) {
                commonType = Types.OrdinaryObject;
            } else if (isScriptObject(right)) {
                commonType = Types.ScriptObject;
            } else if (isCallable(right) || isConstructor(right)) {
                commonType = Types.Callable;
            }
        } else if (isOrdinaryObject(left)) {
            if (isFunctionObject(right) || isOrdinaryObject(right)) {
                commonType = Types.OrdinaryObject;
            } else if (isScriptObject(right) || isCallable(right) || isConstructor(right)) {
                commonType = Types.ScriptObject;
            }
        } else if (isScriptObject(left)) {
            if (isFunctionObject(right) || isOrdinaryObject(right) || isScriptObject(right)
                    || isCallable(right) || isConstructor(right)) {
                commonType = Types.ScriptObject;
            }
        } else if (isCallable(left)) {
            if (isFunctionObject(right) || isCallable(right) || isConstructor(right)) {
                commonType = Types.Callable;
            } else if (isOrdinaryObject(right) || isScriptObject(right)) {
                commonType = Types.ScriptObject;
            }
        } else if (isConstructor(left)) {
            if (isConstructor(right)) {
                commonType = Types.Constructor;
            } else if (isFunctionObject(right) || isCallable(right)) {
                commonType = Types.Callable;
            } else if (isOrdinaryObject(right) || isScriptObject(right)) {
                commonType = Types.ScriptObject;
            }
        }
        return commonType;
    }

    private static boolean isCharSequence(Type type) {
        return Types.String.equals(type) || Types.CharSequence.equals(type);
    }

    private static boolean isNumber(Type type) {
        return Types.Integer.equals(type) || Types.Long.equals(type) || Types.Double.equals(type);
    }

    private static boolean isFunctionObject(Type type) {
        return Types.OrdinaryFunction.equals(type) || Types.OrdinaryConstructorFunction.equals(type)
                || Types.OrdinaryGenerator.equals(type) || Types.OrdinaryConstructorGenerator.equals(type)
                || Types.OrdinaryAsyncFunction.equals(type) || Types.LegacyConstructorFunction.equals(type)
                || Types.FunctionObject.equals(type);
    }

    private static boolean isOrdinaryObject(Type type) {
        return Types.OrdinaryObject.equals(type) || Types.ArrayObject.equals(type) || Types.ArgumentsObject.equals(type)
                || Types.RegExpObject.equals(type) || Types.GeneratorObject.equals(type)
                || Types.AsyncGeneratorObject.equals(type) || Types.ModuleNamespaceObject.equals(type);
    }

    private static boolean isConstructor(Type type) {
        return Types.Constructor.equals(type);
    }

    private static boolean isCallable(Type type) {
        return Types.Callable.equals(type);
    }

    private static boolean isScriptObject(Type type) {
        return Types.ScriptObject.equals(type);
    }
}
