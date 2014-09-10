/**
 * Copyright (c) 2012-2014 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.types.builtins;

import static com.github.anba.es6draft.runtime.AbstractOperations.IsConstructor;
import static com.github.anba.es6draft.runtime.internal.Errors.newRangeError;

import com.github.anba.es6draft.runtime.ExecutionContext;
import com.github.anba.es6draft.runtime.Realm;
import com.github.anba.es6draft.runtime.internal.Messages;
import com.github.anba.es6draft.runtime.objects.FunctionPrototype;
import com.github.anba.es6draft.runtime.types.Callable;
import com.github.anba.es6draft.runtime.types.Constructor;
import com.github.anba.es6draft.runtime.types.Intrinsics;
import com.github.anba.es6draft.runtime.types.ScriptObject;

/**
 * <h1>9 Ordinary and Exotic Objects Behaviours</h1><br>
 * <h2>9.4 Built-in Exotic Object Internal Methods and Data Fields</h2>
 * <ul>
 * <li>9.4.1 Bound Function Exotic Objects
 * </ul>
 */
public class BoundFunctionObject extends OrdinaryObject implements Callable {
    /** [[BoundTargetFunction]] */
    private Callable boundTargetFunction;

    /** [[BoundThis]] */
    private Object boundThis;

    /** [[BoundArguments]] */
    private Object[] boundArguments;

    private static final class ConstructorBoundFunctionObject extends BoundFunctionObject implements
            Constructor {
        /**
         * Constructs a new Bound Function object.
         * 
         * @param realm
         *            the realm object
         */
        public ConstructorBoundFunctionObject(Realm realm) {
            super(realm);
        }

        /**
         * 9.4.1.2 [[Construct]]
         */
        @Override
        public ScriptObject construct(ExecutionContext callerContext, Object... extraArgs) {
            /* step 1 */
            Callable target = getBoundTargetFunction();
            /* step 2 */
            assert IsConstructor(target);
            /* step 3 */
            Object[] boundArgs = getBoundArguments();
            /* step 4 */
            int argsLen = boundArgs.length + extraArgs.length;
            if (argsLen > FunctionPrototype.getMaxArguments()) {
                throw newRangeError(callerContext, Messages.Key.FunctionTooManyArguments);
            }
            Object[] args = new Object[argsLen];
            System.arraycopy(boundArgs, 0, args, 0, boundArgs.length);
            System.arraycopy(extraArgs, 0, args, boundArgs.length, extraArgs.length);
            /* step 5 */
            return ((Constructor) target).construct(callerContext, args);
        }

        /**
         * 9.4.1.2 [[Construct]]
         */
        @Override
        public ScriptObject tailConstruct(ExecutionContext callerContext, Object... args) {
            return construct(callerContext, args);
        }
    }

    /**
     * Constructs a new Bound Function object.
     * 
     * @param realm
     *            the realm object
     */
    public BoundFunctionObject(Realm realm) {
        super(realm);
    }

    /**
     * [[BoundTargetFunction]]
     * 
     * @return the bound target function
     */
    public Callable getBoundTargetFunction() {
        return boundTargetFunction;
    }

    /**
     * [[BoundThis]]
     * 
     * @return the bound this-value
     */
    public Object getBoundThis() {
        return boundThis;
    }

    /**
     * [[BoundArguments]]
     * 
     * @return the bound function arguments
     */
    public Object[] getBoundArguments() {
        return boundArguments;
    }

    @Override
    public String toSource(SourceSelector selector) {
        return FunctionSource.nativeCode(selector, "BoundFunction");
    }

    /**
     * 9.4.1.1 [[Call]]
     */
    @Override
    public Object call(ExecutionContext callerContext, Object thisValue, Object... argumentsList) {
        /* step 1 */
        Object[] boundArgs = getBoundArguments();
        /* step 2 */
        Object boundThis = getBoundThis();
        /* step 3 */
        Callable target = getBoundTargetFunction();
        /* step 4 */
        int argsLen = boundArgs.length + argumentsList.length;
        if (argsLen > FunctionPrototype.getMaxArguments()) {
            throw newRangeError(callerContext, Messages.Key.FunctionTooManyArguments);
        }
        Object[] args = new Object[argsLen];
        System.arraycopy(boundArgs, 0, args, 0, boundArgs.length);
        System.arraycopy(argumentsList, 0, args, boundArgs.length, argumentsList.length);
        /* step 5 */
        return target.call(callerContext, boundThis, args);
    }

    /**
     * 9.4.1.1 [[Call]]
     */
    @Override
    public Object tailCall(ExecutionContext callerContext, Object thisValue, Object... args) {
        return call(callerContext, thisValue, args);
    }

    @Override
    public BoundFunctionObject clone(ExecutionContext cx) {
        /* step 1 (not applicable) */
        /* steps 2-4 */
        BoundFunctionObject clone;
        if (this instanceof ConstructorBoundFunctionObject) {
            clone = new ConstructorBoundFunctionObject(cx.getRealm());
        } else {
            clone = new BoundFunctionObject(cx.getRealm());
        }
        clone.setPrototype(getPrototype());
        clone.boundTargetFunction = boundTargetFunction;
        clone.boundThis = boundThis;
        clone.boundArguments = boundArguments;
        /* step 5 */
        return clone;
    }

    @Override
    public Realm getRealm(ExecutionContext cx) {
        /* 7.3.21 GetFunctionRealm ( obj ) Abstract Operation */
        return getBoundTargetFunction().getRealm(cx);
    }

    /**
     * 9.4.1.3 BoundFunctionCreate (targetFunction, boundThis, boundArgs) Abstract Operation
     * 
     * @param cx
     *            the execution context
     * @param targetFunction
     *            the target function
     * @param boundThis
     *            the bound this-value
     * @param boundArgs
     *            the bound function arguments
     * @return the new bound function object
     */
    public static BoundFunctionObject BoundFunctionCreate(ExecutionContext cx,
            Callable targetFunction, Object boundThis, Object[] boundArgs) {
        /* step 1 */
        ScriptObject proto = cx.getIntrinsic(Intrinsics.FunctionPrototype);
        /* steps 2-5 (implicit) */
        BoundFunctionObject obj;
        if (IsConstructor(targetFunction)) {
            obj = new ConstructorBoundFunctionObject(cx.getRealm());
        } else {
            obj = new BoundFunctionObject(cx.getRealm());
        }
        /* step 6 */
        obj.setPrototype(proto);
        /* step 7 (implicit) */
        /* step 8 */
        obj.boundTargetFunction = targetFunction;
        /* step 9 */
        obj.boundThis = boundThis;
        /* step 10 */
        obj.boundArguments = boundArgs;
        /* step 11 */
        return obj;
    }

    /**
     * 9.4.1.4 BoundFunctionClone ( function ) Abstract Operation
     * 
     * @param cx
     *            the execution context
     * @param function
     *            the bound function object
     * @return the new cloned bound function object
     */
    public static BoundFunctionObject BoundFunctionClone(ExecutionContext cx,
            BoundFunctionObject function) {
        return function.clone(cx);
    }
}