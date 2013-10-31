/**
 * Copyright (c) 2012-2013 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.types.builtins;

import static com.github.anba.es6draft.runtime.AbstractOperations.DefinePropertyOrThrow;
import static com.github.anba.es6draft.runtime.AbstractOperations.Get;
import static com.github.anba.es6draft.runtime.AbstractOperations.IsCallable;
import static com.github.anba.es6draft.runtime.AbstractOperations.OrdinaryCreateFromConstructor;
import static com.github.anba.es6draft.runtime.internal.Errors.throwTypeError;
import static com.github.anba.es6draft.runtime.types.Null.NULL;
import static com.github.anba.es6draft.runtime.types.builtins.ExoticArguments.CreateLegacyArguments;

import com.github.anba.es6draft.runtime.ExecutionContext;
import com.github.anba.es6draft.runtime.LexicalEnvironment;
import com.github.anba.es6draft.runtime.Realm;
import com.github.anba.es6draft.runtime.internal.Messages;
import com.github.anba.es6draft.runtime.internal.RuntimeInfo;
import com.github.anba.es6draft.runtime.internal.RuntimeInfo.Code;
import com.github.anba.es6draft.runtime.internal.TailCallInvocation;
import com.github.anba.es6draft.runtime.types.BuiltinSymbol;
import com.github.anba.es6draft.runtime.types.Callable;
import com.github.anba.es6draft.runtime.types.Constructor;
import com.github.anba.es6draft.runtime.types.Intrinsics;
import com.github.anba.es6draft.runtime.types.PropertyDescriptor;
import com.github.anba.es6draft.runtime.types.ScriptObject;
import com.github.anba.es6draft.runtime.types.Symbol;
import com.github.anba.es6draft.runtime.types.Type;

/**
 * <h1>9 ECMAScript Ordinary and Exotic Objects Behaviours</h1><br>
 * <h2>9.1 Ordinary Object Internal Methods and Internal Data Properties</h2>
 * <ul>
 * <li>9.1.15 Ordinary Function Objects
 * </ul>
 */
public class OrdinaryFunction extends FunctionObject {
    protected OrdinaryFunction(Realm realm) {
        super(realm);
    }

    private static class OrdinaryConstructorFunction extends OrdinaryFunction implements
            Constructor {
        public OrdinaryConstructorFunction(Realm realm) {
            super(realm);
        }

        @Override
        public final boolean isConstructor() {
            return isConstructor;
        }

        /**
         * 9.1.15.2 [[Construct]] (argumentsList)
         */
        @Override
        public ScriptObject construct(ExecutionContext callerContext, Object... args) {
            return OrdinaryConstruct(callerContext, this, args);
        }
    }

    @Override
    public OrdinaryFunction rebind(ExecutionContext cx, ScriptObject newHomeObject) {
        assert isInitialised() : "uninitialised function object";
        Object methodName = getMethodName();
        OrdinaryFunction f;
        if (methodName instanceof String) {
            f = FunctionCreate(cx, getFunctionKind(), getFunction(), getScope(),
                    getPrototypeOf(cx), newHomeObject, (String) methodName);
        } else {
            assert methodName instanceof Symbol;
            f = FunctionCreate(cx, getFunctionKind(), getFunction(), getScope(),
                    getPrototypeOf(cx), newHomeObject, (Symbol) methodName);
        }
        f.isConstructor = this.isConstructor;
        return f;
    }

    /**
     * 9.1.15.1 [[Call]] Internal Method
     */
    @Override
    public Object call(ExecutionContext callerContext, Object thisValue, Object... args) {
        /* step 1 */
        if (!isInitialised()) {
            throw throwTypeError(callerContext, Messages.Key.UninitialisedObject);
        }
        /* steps 2-3 (implicit) */
        Object oldCaller = caller.getValue();
        Object oldArguments = arguments.getValue();
        try {
            /* steps 4-14 */
            ExecutionContext calleeContext = EvaluateArguments(callerContext, this, thisValue, args);
            /* steps 15-16 */
            Object result = EvaluateBody(callerContext, calleeContext, getCode());
            /* step 17 */
            return result;
        } finally {
            if (isLegacy()) {
                restoreLegacyProperties(oldCaller, oldArguments);
            }
        }
    }

    @Override
    public Object tailCall(ExecutionContext callerContext, Object thisValue, Object... args)
            throws Throwable {
        /* step 1 */
        if (!isInitialised()) {
            throw throwTypeError(callerContext, Messages.Key.UninitialisedObject);
        }
        /* steps 2-3 (implicit) */
        Object oldCaller = caller.getValue();
        Object oldArguments = arguments.getValue();
        try {
            /* steps 4-14 */
            ExecutionContext calleeContext = EvaluateArguments(callerContext, this, thisValue, args);
            /* steps 15-17 */
            return getCode().handle().invokeExact(calleeContext);
        } finally {
            if (isLegacy()) {
                restoreLegacyProperties(oldCaller, oldArguments);
            }
        }
    }

    private static ExecutionContext EvaluateArguments(ExecutionContext callerContext,
            OrdinaryFunction f, Object thisValue, Object[] args) {
        /* steps 4-12 */
        ExecutionContext calleeContext = ExecutionContext.newFunctionExecutionContext(f, thisValue);
        /* steps 13-14 */
        ExoticArguments arguments = f.getFunction().functionDeclarationInstantiation(calleeContext,
                f, args);
        if (f.isLegacy()) {
            f.updateLegacyProperties(calleeContext, callerContext.getCurrentFunction(), arguments);
        }
        return calleeContext;
    }

    public static Object EvaluateBody(ExecutionContext callerContext,
            ExecutionContext calleeContext, Code code) {
        try {
            Object result = code.handle().invokeExact(calleeContext);
            // tail-call with trampoline
            while (result instanceof TailCallInvocation) {
                TailCallInvocation tc = (TailCallInvocation) result;
                result = tc.getFunction().tailCall(callerContext, tc.getThisValue(),
                        tc.getArgumentsList());
            }
            return result;
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void updateLegacyProperties(ExecutionContext cx, FunctionObject caller,
            ExoticArguments arguments) {
        if (caller == null || caller.isStrict()) {
            this.caller.applyValue(NULL);
        } else {
            this.caller.applyValue(caller);
        }
        ExoticArguments args = CreateLegacyArguments(cx, arguments, this);
        this.arguments.applyValue(args);
    }

    private void restoreLegacyProperties(Object oldCaller, Object oldArguments) {
        this.caller.applyValue(oldCaller);
        this.arguments.applyValue(oldArguments);
    }

    /* ***************************************************************************************** */

    /**
     * 9.1.15.2.1 OrdinaryConstruct (F, argumentsList)
     */
    public static <FUNCTION extends ScriptObject & Callable & Constructor> ScriptObject OrdinaryConstruct(
            ExecutionContext cx, FUNCTION f, Object[] args) {
        /* steps 1-2 */
        Object creator = Get(cx, f, BuiltinSymbol.create.get());
        /* steps 3-5 */
        Object obj;
        if (!Type.isUndefined(creator)) {
            if (!IsCallable(creator)) {
                throw throwTypeError(cx, Messages.Key.NotCallable);
            }
            obj = ((Callable) creator).call(cx, f);
        } else {
            obj = OrdinaryCreateFromConstructor(cx, f, Intrinsics.ObjectPrototype);
        }
        /* step 6 */
        if (!Type.isObject(obj)) {
            throw throwTypeError(cx, Messages.Key.NotObjectType);
        }
        /* steps 7-8 */
        Object result = f.call(cx, obj, args);
        /* step 9 */
        if (Type.isObject(result)) {
            return Type.objectValue(result);
        }
        /* step 10 */
        return Type.objectValue(obj);
    }

    /**
     * 9.1.15.5 FunctionAllocate Abstract Operation
     */
    public static OrdinaryFunction FunctionAllocate(ExecutionContext cx,
            ScriptObject functionPrototype, boolean strict, FunctionKind kind) {
        Realm realm = cx.getRealm();
        /* steps 1-3 (implicit) */
        /* steps 4-6 */
        OrdinaryFunction f;
        if (kind == FunctionKind.Normal || kind == FunctionKind.ConstructorMethod) {
            f = new OrdinaryConstructorFunction(realm);
        } else {
            f = new OrdinaryFunction(realm);
        }
        /* step 13 (moved) */
        f.realm = realm;
        /* step 9 */
        f.setStrict(strict);
        /* step 10 */
        f.functionKind = kind;
        /* step 11 */
        f.setPrototype(functionPrototype);
        /* step 12 */
        // f.[[Extensible]] = true (implicit)
        /* step 14 */
        return f;
    }

    /**
     * 9.1.15.6 FunctionInitialise Abstract Operation
     */
    public static <FUNCTION extends FunctionObject> FUNCTION FunctionInitialise(
            ExecutionContext cx, FUNCTION f, FunctionKind kind, RuntimeInfo.Function function,
            LexicalEnvironment scope) {
        return FunctionInitialise(cx, f, kind, function, scope, null, (String) null);
    }

    /**
     * 9.1.15.6 FunctionInitialise Abstract Operation
     */
    public static <FUNCTION extends FunctionObject> FUNCTION FunctionInitialise(
            ExecutionContext cx, FUNCTION f, FunctionKind kind, RuntimeInfo.Function function,
            LexicalEnvironment scope, ScriptObject homeObject, String methodName) {
        /* step 1 */
        int len = function.expectedArgumentCount();
        /* step 2 */
        boolean strict = f.strict;
        /* steps 3-4 */
        DefinePropertyOrThrow(cx, f, "length", new PropertyDescriptor(len, false, false, true));
        /* step 5 */
        if (strict) {
            AddRestrictedFunctionProperties(cx, f);
        }
        /* step 6 */
        f.scope = scope;
        /* steps 7-8 */
        f.function = function;
        /* step 9 */
        f.homeObject = homeObject;
        f.methodName = methodName;
        /* steps 10-12 */
        if (kind == FunctionKind.Arrow) {
            f.thisMode = ThisMode.Lexical;
        } else if (strict) {
            f.thisMode = ThisMode.Strict;
        } else {
            f.thisMode = ThisMode.Global;
        }
        /* step 13 */
        return f;
    }

    /**
     * 9.1.15.6 FunctionInitialise Abstract Operation
     */
    public static <FUNCTION extends FunctionObject> FUNCTION FunctionInitialise(
            ExecutionContext cx, FUNCTION f, FunctionKind kind, RuntimeInfo.Function function,
            LexicalEnvironment scope, ScriptObject homeObject, Symbol methodName) {
        /* step 1 */
        int len = function.expectedArgumentCount();
        /* step 2 */
        boolean strict = f.strict;
        /* steps 3-4 */
        DefinePropertyOrThrow(cx, f, "length", new PropertyDescriptor(len, false, false, true));
        /* step 5 */
        if (strict) {
            AddRestrictedFunctionProperties(cx, f);
        }
        /* step 6 */
        f.scope = scope;
        /* steps 7-8 */
        f.function = function;
        /* step 9 */
        f.homeObject = homeObject;
        f.methodName = methodName;
        /* steps 10-12 */
        if (kind == FunctionKind.Arrow) {
            f.thisMode = ThisMode.Lexical;
        } else if (strict) {
            f.thisMode = ThisMode.Strict;
        } else {
            f.thisMode = ThisMode.Global;
        }
        /* step 13 */
        return f;
    }

    /**
     * 9.1.15.7 FunctionCreate Abstract Operation
     */
    public static OrdinaryFunction FunctionCreate(ExecutionContext cx, FunctionKind kind,
            RuntimeInfo.Function function, LexicalEnvironment scope) {
        return FunctionCreate(cx, kind, function, scope, null, null, (String) null);
    }

    /**
     * 9.1.15.7 FunctionCreate Abstract Operation
     */
    public static OrdinaryFunction FunctionCreate(ExecutionContext cx, FunctionKind kind,
            RuntimeInfo.Function function, LexicalEnvironment scope, ScriptObject functionPrototype) {
        return FunctionCreate(cx, kind, function, scope, functionPrototype, null, (String) null);
    }

    /**
     * 9.1.15.7 FunctionCreate Abstract Operation
     */
    public static OrdinaryFunction FunctionCreate(ExecutionContext cx, FunctionKind kind,
            RuntimeInfo.Function function, LexicalEnvironment scope,
            ScriptObject functionPrototype, ScriptObject homeObject, String methodName) {
        assert !function.isGenerator();
        /* step 1 */
        if (functionPrototype == null) {
            functionPrototype = cx.getIntrinsic(Intrinsics.FunctionPrototype);
        }
        /* step 2 */
        OrdinaryFunction f = FunctionAllocate(cx, functionPrototype, function.isStrict(), kind);
        /* step 3 */
        return FunctionInitialise(cx, f, kind, function, scope, homeObject, methodName);
    }

    /**
     * 9.1.15.7 FunctionCreate Abstract Operation
     */
    public static OrdinaryFunction FunctionCreate(ExecutionContext cx, FunctionKind kind,
            RuntimeInfo.Function function, LexicalEnvironment scope,
            ScriptObject functionPrototype, ScriptObject homeObject, Symbol methodName) {
        assert !function.isGenerator();
        /* step 1 */
        if (functionPrototype == null) {
            functionPrototype = cx.getIntrinsic(Intrinsics.FunctionPrototype);
        }
        /* step 2 */
        OrdinaryFunction f = FunctionAllocate(cx, functionPrototype, function.isStrict(), kind);
        /* step 3 */
        return FunctionInitialise(cx, f, kind, function, scope, homeObject, methodName);
    }

    /**
     * 9.1.15.9 AddRestrictedFunctionProperties Abstract Operation
     */
    public static void AddRestrictedFunctionProperties(ExecutionContext cx, ScriptObject obj) {
        /* step 1 */
        Callable thrower = cx.getRealm().getThrowTypeError();
        /* step 2 */
        DefinePropertyOrThrow(cx, obj, "caller", new PropertyDescriptor(thrower, thrower, false,
                false));
        /* step 3 */
        DefinePropertyOrThrow(cx, obj, "arguments", new PropertyDescriptor(thrower, thrower, false,
                false));
    }

    /**
     * 9.1.15.9 The %ThrowTypeError% Function Object
     */
    private static class TypeErrorThrower extends BuiltinFunction {
        TypeErrorThrower(Realm realm) {
            super(realm);
        }

        @Override
        public Object call(ExecutionContext callerContext, Object thisValue, Object... args) {
            throw throwTypeError(calleeContext(), Messages.Key.StrictModePoisonPill);
        }
    }

    /**
     * 9.1.15.9 The %ThrowTypeError% Function Object
     */
    public static Callable createThrowTypeError(ExecutionContext cx) {
        /* step 1 */
        assert cx.getIntrinsic(Intrinsics.FunctionPrototype) != null;
        /* step 2 */
        ScriptObject functionPrototype = cx.getIntrinsic(Intrinsics.FunctionPrototype);
        /* steps 3-8 (implicit) */
        // inlined FunctionAllocate()
        TypeErrorThrower f = new TypeErrorThrower(cx.getRealm());
        f.setPrototype(functionPrototype);
        // inlined FunctionInitialise()
        DefinePropertyOrThrow(cx, f, "length", new PropertyDescriptor(0, false, false, true));
        DefinePropertyOrThrow(cx, f, "name", new PropertyDescriptor("ThrowTypeError", false, false,
                false));
        // inlined AddRestrictedFunctionProperties()
        DefinePropertyOrThrow(cx, f, "caller", new PropertyDescriptor(f, f, false, false));
        DefinePropertyOrThrow(cx, f, "arguments", new PropertyDescriptor(f, f, false, false));
        /* step 9 */
        f.preventExtensions(cx);
        /* step 10 */
        return f;
    }

    /**
     * 9.1.15.10 MakeConstructor Abstract Operation
     */
    public static void MakeConstructor(ExecutionContext cx, FunctionObject f) {
        /* step 2 */
        boolean installNeeded = true;
        ScriptObject prototype = ObjectCreate(cx, Intrinsics.ObjectPrototype);
        /* step 3 */
        boolean writablePrototype = true;
        MakeConstructor(cx, f, writablePrototype, prototype, installNeeded);
    }

    /**
     * 9.1.15.10 MakeConstructor Abstract Operation
     */
    public static void MakeConstructor(ExecutionContext cx, FunctionObject f,
            boolean writablePrototype, ScriptObject prototype) {
        /* step 1 */
        boolean installNeeded = false;
        MakeConstructor(cx, f, writablePrototype, prototype, installNeeded);
    }

    /**
     * 9.1.15.10 MakeConstructor Abstract Operation
     */
    private static void MakeConstructor(ExecutionContext cx, FunctionObject f,
            boolean writablePrototype, ScriptObject prototype, boolean installNeeded) {
        assert f instanceof Constructor : "MakeConstructor applied on non-Constructor";
        /* step 4 */
        f.isConstructor = true;
        /* step 5 */
        if (installNeeded) {
            prototype.defineOwnProperty(cx, "constructor", new PropertyDescriptor(f,
                    writablePrototype, false, writablePrototype));
        }
        /* step 7 */
        f.defineOwnProperty(cx, "prototype", new PropertyDescriptor(prototype, writablePrototype,
                false, false));
    }

    /**
     * 9.2.11 SetFunctionName Abstract Operation
     */
    public static void SetFunctionName(ExecutionContext cx, FunctionObject f, String name) {
        SetFunctionName(cx, f, name, null);
    }

    /**
     * 9.2.11 SetFunctionName Abstract Operation
     */
    public static void SetFunctionName(ExecutionContext cx, FunctionObject f, String name,
            String prefix) {
        /* step 1 */
        assert f.isExtensible(cx) && !f.hasOwnProperty(cx, "name");
        /* step 2 (implicit) */
        /* step 3 (not applicable) */
        /* step 4 */
        if (prefix != null) {
            name = prefix + " " + name;
        }
        /* step 5 */
        boolean success = f.defineOwnProperty(cx, "name", new PropertyDescriptor(name, false,
                false, true));
        /* step 6 */
        assert success;
        /* step 7 (return) */
    }

    /**
     * 9.2.11 SetFunctionName Abstract Operation
     */
    public static void SetFunctionName(ExecutionContext cx, FunctionObject f, Symbol name) {
        SetFunctionName(cx, f, name, null);
    }

    /**
     * 9.2.11 SetFunctionName Abstract Operation
     */
    public static void SetFunctionName(ExecutionContext cx, FunctionObject f, Symbol name,
            String prefix) {
        /* step 3 */
        String description = name.getDescription();
        String sname = description == null ? "" : "[" + description + "]";
        /* steps 1-2, 4-7 */
        SetFunctionName(cx, f, sname, prefix);
    }

    /**
     * 9.1.15.11 GetSuperBinding(obj) Abstract Operation
     */
    public static ScriptObject GetSuperBinding(Object obj) {
        /* steps 1-2 */
        if (!(obj instanceof FunctionObject)) {
            return null;
        }
        /* step 3 */
        return ((FunctionObject) obj).getHomeObject();
    }

    /**
     * 9.1.15.12 RebindSuper(function, newHome) Abstract Operation
     */
    public static FunctionObject RebindSuper(ExecutionContext cx, FunctionObject function,
            ScriptObject newHome) {
        /* step 1 */
        assert function.getHomeObject() != null;
        /* step 2 */
        assert newHome != null;
        /* steps 3-6 */
        return function.rebind(cx, newHome);
    }
}
