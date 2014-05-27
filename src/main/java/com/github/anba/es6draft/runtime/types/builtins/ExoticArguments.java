/**
 * Copyright (c) 2012-2014 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.types.builtins;

import static com.github.anba.es6draft.runtime.AbstractOperations.CreateDataProperty;
import static com.github.anba.es6draft.runtime.AbstractOperations.DefinePropertyOrThrow;
import static com.github.anba.es6draft.runtime.internal.Errors.newTypeError;
import static com.github.anba.es6draft.runtime.types.builtins.FunctionObject.isStrictFunction;

import com.github.anba.es6draft.runtime.DeclarativeEnvironmentRecord;
import com.github.anba.es6draft.runtime.ExecutionContext;
import com.github.anba.es6draft.runtime.LexicalEnvironment;
import com.github.anba.es6draft.runtime.Realm;
import com.github.anba.es6draft.runtime.internal.CompatibilityOption;
import com.github.anba.es6draft.runtime.internal.Messages;
import com.github.anba.es6draft.runtime.types.BuiltinSymbol;
import com.github.anba.es6draft.runtime.types.Callable;
import com.github.anba.es6draft.runtime.types.Intrinsics;
import com.github.anba.es6draft.runtime.types.Property;
import com.github.anba.es6draft.runtime.types.PropertyDescriptor;

/**
 * <h1>9 Ordinary and Exotic Objects Behaviours</h1><br>
 * <h2>9.4 Built-in Exotic Object Internal Methods and Data Fields</h2>
 * <ul>
 * <li>9.4.4 Arguments Exotic Objects
 * </ul>
 */
public final class ExoticArguments extends OrdinaryObject {
    /** [[ParameterMap]] */
    private ParameterMap parameterMap = null;

    public ExoticArguments(Realm realm) {
        super(realm);
    }

    /**
     * [[ParameterMap]]
     *
     * @return the parameter map
     */
    ParameterMap getParameterMap() {
        return parameterMap;
    }

    /**
     * 9.4.4.1 [[GetOwnProperty]] (P)
     */
    @Override
    protected Property getProperty(ExecutionContext cx, long propertyKey) {
        /* step 1 (not applicable) */
        /* step 2 */
        Property desc = ordinaryGetOwnProperty(propertyKey);
        /* step 3 */
        if (desc == null) {
            return desc;
        }
        /* step 4 */
        ParameterMap map = this.parameterMap;
        /* steps 5-6 */
        boolean isMapped = map != null ? map.hasOwnProperty(propertyKey, false) : false;
        /* step 7 */
        if (isMapped) {
            PropertyDescriptor d = desc.toPropertyDescriptor();
            d.setValue(map.get(propertyKey));
            desc = d.toProperty();
        }
        /* step 8 (not applicable) */
        /* step 9 */
        return desc;
    }

    /**
     * 9.4.4.1 [[GetOwnProperty]] (P)
     */
    @Override
    protected Property getProperty(ExecutionContext cx, String propertyKey) {
        /* step 1 (not applicable) */
        /* step 2 */
        Property desc = ordinaryGetOwnProperty(propertyKey);
        /* step 3 */
        if (desc == null) {
            return desc;
        }
        /* steps 4-7 (not applicable) */
        /* step 8 */
        if (desc.isDataDescriptor() && "caller".equals(propertyKey)
                && isStrictFunction(desc.getValue())
                && cx.getRealm().isEnabled(CompatibilityOption.FunctionPrototype)) {
            throw newTypeError(cx, Messages.Key.StrictModePoisonPill);
        }
        /* step 9 */
        return desc;
    }

    /**
     * 9.4.4.2 [[DefineOwnProperty]] (P, Desc)
     */
    @Override
    protected boolean defineProperty(ExecutionContext cx, long propertyKey, PropertyDescriptor desc) {
        /* step 1 (not applicable) */
        /* step 2 */
        ParameterMap map = this.parameterMap;
        /* step 3 */
        boolean isMapped = map != null ? map.hasOwnProperty(propertyKey, false) : false;
        /* steps 4-5 */
        boolean allowed = ordinaryDefineOwnProperty(cx, propertyKey, desc);
        /* step 6 */
        if (!allowed) {
            return false;
        }
        /* step 7 */
        if (isMapped) {
            if (desc.isAccessorDescriptor()) {
                map.delete(propertyKey);
            } else {
                if (desc.hasValue()) {
                    map.put(propertyKey, desc.getValue());
                }
                if (desc.hasWritable() && !desc.isWritable()) {
                    map.delete(propertyKey);
                }
            }
        }
        /* step 8 */
        return true;
    }

    /**
     * 9.4.4.3 [[Get]] (P, Receiver)
     */
    @Override
    protected Object getValue(ExecutionContext cx, long propertyKey, Object receiver) {
        /* steps 1-2 */
        ParameterMap map = this.parameterMap;
        /* steps 3-4 */
        boolean isMapped = map != null ? map.hasOwnProperty(propertyKey, false) : false;
        /* steps 5-7 */
        Object v;
        if (!isMapped) {
            /* step 5 */
            v = super.getValue(cx, propertyKey, receiver);
        } else {
            /* step 6 */
            v = map.get(propertyKey);
        }
        /* step 8 (not applicable) */
        /* step 9 */
        return v;
    }

    /**
     * 9.4.4.3 [[Get]] (P, Receiver)
     */
    @Override
    protected Object getValue(ExecutionContext cx, String propertyKey, Object receiver) {
        /* steps 1-4, 6 (not applicable) */
        /* steps 5, 7 */
        Object v = super.getValue(cx, propertyKey, receiver);
        /* step 8 */
        if ("caller".equals(propertyKey) && isStrictFunction(v)
                && cx.getRealm().isEnabled(CompatibilityOption.FunctionPrototype)) {
            throw newTypeError(cx, Messages.Key.StrictModePoisonPill);
        }
        /* step 9 */
        return v;
    }

    /**
     * 9.4.4.4 [[Set]] ( P, V, Receiver)
     */
    @Override
    protected boolean setValue(ExecutionContext cx, long propertyKey, Object value, Object receiver) {
        /* steps 1, 3.a */
        ParameterMap map = this.parameterMap;
        /* steps 2-3 */
        boolean isMapped;
        if (this != receiver) {
            /* step 2 */
            isMapped = false;
        } else {
            /* step 3 */
            isMapped = map != null ? map.hasOwnProperty(propertyKey, false) : false;
        }
        /* steps 4-5 */
        if (!isMapped) {
            /* step 4 */
            return super.setValue(cx, propertyKey, value, receiver);
        } else {
            /* step 5 */
            map.put(propertyKey, value);
            return true;
        }
    }

    /**
     * 9.4.4.5 [[Delete]] (P)
     */
    @Override
    protected boolean deleteProperty(ExecutionContext cx, long propertyKey) {
        /* step 1 */
        ParameterMap map = this.parameterMap;
        /* steps 2-3 */
        boolean isMapped = map != null ? map.hasOwnProperty(propertyKey, false) : false;
        /* steps 4-5 */
        boolean result = super.deleteProperty(cx, propertyKey);
        /* step 6 */
        if (result && isMapped) {
            map.delete(propertyKey);
        }
        /* step 7 */
        return result;
    }

    /**
     * 9.4.4.6 CreateUnmappedArgumentsObject(argumentsList) Abstract Operation
     * <p>
     * [Called from generated code]
     * 
     * @param cx
     *            the execution context
     * @param argumentsList
     *            the function arguments
     * @return the strict mode arguments object
     */
    public static ExoticArguments CreateUnmappedArgumentsObject(ExecutionContext cx,
            Object[] argumentsList) {
        /* step 1 */
        int len = argumentsList.length;
        /* steps 2-3 */
        ExoticArguments obj = new ExoticArguments(cx.getRealm());
        obj.setPrototype(cx.getIntrinsic(Intrinsics.ObjectPrototype));
        /* step 4 */
        DefinePropertyOrThrow(cx, obj, "length", new PropertyDescriptor(len, true, false, true));
        /* steps 5-6 */
        for (int index = 0; index < len; ++index) {
            Object val = argumentsList[index];
            CreateDataProperty(cx, obj, index, val);
        }
        Callable thrower = cx.getRealm().getThrowTypeError();
        /* step 7 */
        DefinePropertyOrThrow(cx, obj, BuiltinSymbol.iterator.get(),
                new PropertyDescriptor(cx.getIntrinsic(Intrinsics.ArrayProto_values), true, false,
                        true));
        /* step 8 */
        DefinePropertyOrThrow(cx, obj, "caller", new PropertyDescriptor(thrower, thrower, false,
                false));
        /* step 9 */
        DefinePropertyOrThrow(cx, obj, "callee", new PropertyDescriptor(thrower, thrower, false,
                false));
        /* step 10 (not applicable) */
        /* step 11 */
        return obj;
    }

    /**
     * 9.4.4.7 CreateMappedArgumentsObject ( func, formals, argumentsList, env ) Abstract Operation
     * <p>
     * [Called from generated code]
     * 
     * @param cx
     *            the execution context
     * @param func
     *            the function callee
     * @param formals
     *            the formal parameter names
     * @param argumentsList
     *            the function arguments
     * @param env
     *            the current lexical environment
     * @return the mapped arguments object
     */
    public static ExoticArguments CreateMappedArgumentsObject(ExecutionContext cx,
            FunctionObject func, String[] formals, Object[] argumentsList,
            LexicalEnvironment<? extends DeclarativeEnvironmentRecord> env) {
        /* step 1 (not applicable) */
        /* step 2 */
        int len = argumentsList.length;
        /* steps 3-11 */
        ExoticArguments obj = new ExoticArguments(cx.getRealm());
        obj.setPrototype(cx.getIntrinsic(Intrinsics.ObjectPrototype));
        /* steps 12-13 (not applicable) */
        /* steps 14-15 */
        for (int index = 0; index < len; ++index) {
            Object val = argumentsList[index];
            CreateDataProperty(cx, obj, index, val);
        }
        /* step 16 */
        DefinePropertyOrThrow(cx, obj, "length", new PropertyDescriptor(len, true, false, true));
        /* steps 17-20 */
        ParameterMap map = ParameterMap.create(len, formals, env);
        /* step 21 */
        obj.parameterMap = map;
        /* step 22 */
        DefinePropertyOrThrow(cx, obj, BuiltinSymbol.iterator.get(),
                new PropertyDescriptor(cx.getIntrinsic(Intrinsics.ArrayProto_values), true, false,
                        true));
        /* steps 23-24 */
        DefinePropertyOrThrow(cx, obj, "callee", new PropertyDescriptor(func, true, false, true));
        /* step 25 */
        return obj;
    }
}
