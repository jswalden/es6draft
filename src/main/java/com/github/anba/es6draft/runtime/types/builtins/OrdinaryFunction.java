/**
 * Copyright (c) 2012-2013 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.types.builtins;

import static com.github.anba.es6draft.runtime.AbstractOperations.Get;
import static com.github.anba.es6draft.runtime.AbstractOperations.IsCallable;
import static com.github.anba.es6draft.runtime.AbstractOperations.OrdinaryCreateFromConstructor;
import static com.github.anba.es6draft.runtime.internal.Errors.throwTypeError;

import com.github.anba.es6draft.runtime.ExecutionContext;
import com.github.anba.es6draft.runtime.LexicalEnvironment;
import com.github.anba.es6draft.runtime.Realm;
import com.github.anba.es6draft.runtime.internal.Messages;
import com.github.anba.es6draft.runtime.internal.RuntimeInfo;
import com.github.anba.es6draft.runtime.types.BuiltinSymbol;
import com.github.anba.es6draft.runtime.types.Callable;
import com.github.anba.es6draft.runtime.types.Constructor;
import com.github.anba.es6draft.runtime.types.IntegrityLevel;
import com.github.anba.es6draft.runtime.types.Intrinsics;
import com.github.anba.es6draft.runtime.types.PropertyDescriptor;
import com.github.anba.es6draft.runtime.types.ScriptObject;
import com.github.anba.es6draft.runtime.types.Type;

/**
 * <h1>8 Types</h1><br>
 * <h2>8.3 Ordinary Object Internal Methods and Internal Data Properties</h2>
 * <ul>
 * <li>8.3.19 Ordinary Function Objects
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

        /**
         * 8.3.15.2 [[Construct]] Internal Method
         */
        @Override
        public Object construct(Object... args) {
            return OrdinaryConstruct(realm, this, args);
        }
    }

    /**
     * [13.6 Creating Function Objects and Constructors] FunctionCreate
     */
    public static OrdinaryFunction FunctionCreate(Realm realm, FunctionKind kind,
            RuntimeInfo.Function function, LexicalEnvironment scope) {
        return FunctionCreate(realm, kind, function, scope, null, null, null);
    }

    /**
     * [13.6 Creating Function Objects and Constructors] FunctionCreate
     */
    public static OrdinaryFunction FunctionCreate(Realm realm, FunctionKind kind,
            RuntimeInfo.Function function, LexicalEnvironment scope, ScriptObject prototype) {
        return FunctionCreate(realm, kind, function, scope, prototype, null, null);
    }

    /**
     * [13.6 Creating Function Objects and Constructors] FunctionCreate
     */
    public static OrdinaryFunction FunctionCreate(Realm realm, FunctionKind kind,
            RuntimeInfo.Function function, LexicalEnvironment scope, ScriptObject prototype,
            ScriptObject homeObject, String methodName) {
        assert !function.isGenerator();

        boolean strict = (kind != FunctionKind.Arrow ? function.isStrict() : true);
        /* step 1 */
        OrdinaryFunction f;
        if (kind == FunctionKind.Normal || kind == FunctionKind.ConstructorMethod) {
            f = new OrdinaryConstructorFunction(realm);
        } else {
            f = new OrdinaryFunction(realm);
        }
        /* step 2-4 (implicit) */
        /* step 5 */
        if (prototype == null) {
            prototype = realm.getIntrinsic(Intrinsics.FunctionPrototype);
        }
        /* step 6 */
        f.setPrototype(realm, prototype);
        /* step 7 */
        f.scope = scope;
        /* step 8-9 */
        f.function = function;
        /* step 10 */
        // f.[[Extensible]] = true (implicit)
        /* step 11 */
        f.realm = realm;
        /* step 12 */
        f.home = homeObject;
        /* step 13 */
        f.methodName = methodName;
        /* step 14 */
        f.strict = strict;
        /* step 15-17 */
        f.kind = kind;
        if (kind == FunctionKind.Arrow) {
            f.thisMode = ThisMode.Lexical;
        } else if (strict) {
            f.thisMode = ThisMode.Strict;
        } else {
            f.thisMode = ThisMode.Global;
        }
        /*  step 18 */
        int len = function.expectedArgumentCount();
        /* step 19 */
        f.defineOwnProperty(realm, "length", new PropertyDescriptor(len, false, false, false));
        String name = function.functionName() != null ? function.functionName() : "";
        f.defineOwnProperty(realm, "name", new PropertyDescriptor(name, false, false, false));
        /* step 20 */
        if (strict) {
            AddRestrictedFunctionProperties(realm, f);
        }
        /* step 21 */
        return f;
    }

    /**
     * [13.6 Creating Function Objects and Constructors] MakeConstructor
     */
    public static void MakeConstructor(Realm realm, FunctionObject f) {
        /*  step 2 */
        boolean installNeeded = true;
        ScriptObject prototype = ObjectCreate(realm, Intrinsics.ObjectPrototype);
        /*  step 3 */
        boolean writablePrototype = true;
        MakeConstructor(realm, f, writablePrototype, prototype, installNeeded);
    }

    /**
     * [13.6 Creating Function Objects and Constructors] MakeConstructor
     */
    public static void MakeConstructor(Realm realm, FunctionObject f, boolean writablePrototype,
            ScriptObject prototype) {
        /* step 1 */
        boolean installNeeded = false;
        MakeConstructor(realm, f, writablePrototype, prototype, installNeeded);
    }

    /**
     * [13.6 Creating Function Objects and Constructors] MakeConstructor
     */
    private static void MakeConstructor(Realm realm, FunctionObject f, boolean writablePrototype,
            ScriptObject prototype, boolean installNeeded) {
        assert f instanceof Constructor : "MakeConstructor applied on non-Constructor";
        /* step 4 (implicit) */
        /* step 5 */
        if (installNeeded) {
            prototype.defineOwnProperty(realm, "constructor", new PropertyDescriptor(f,
                    writablePrototype, false, writablePrototype));
        }
        /* step 7 */
        f.defineOwnProperty(realm, "prototype", new PropertyDescriptor(prototype,
                writablePrototype, false, false));
    }

    /**
     * 13.6.3 The [[ThrowTypeError]] Function Object
     */
    private static class TypeErrorThrower extends BuiltinFunction {
        TypeErrorThrower(Realm realm) {
            super(realm);
        }

        @Override
        public Object call(Object thisValue, Object... args) {
            /* step 8 */
            throw throwTypeError(realm(), Messages.Key.StrictModePoisonPill);
        }
    }

    /**
     * [13.6.3] The [[ThrowTypeError]] Function Object
     */
    public static Callable createThrowTypeError(Realm realm) {
        // FIXME: spec bug (section 8.12 does not exist) (bug 1057)
        /* step 1-3 (implicit) */
        TypeErrorThrower f = new TypeErrorThrower(realm);
        /* step 4 */
        f.setPrototype(realm, realm.getIntrinsic(Intrinsics.FunctionPrototype));
        /* step 5-8 (implicit) */
        /* step 9 */
        f.defineOwnProperty(realm, "length", new PropertyDescriptor(0, false, false, false));
        f.defineOwnProperty(realm, "name", new PropertyDescriptor("ThrowTypeError", false, false,
                false));
        /* step 10 */
        f.setIntegrity(realm, IntegrityLevel.NonExtensible);

        return f;
    }

    /**
     * [13.6 Creating Function Objects and Constructors] AddRestrictedFunctionProperties
     */
    public static void AddRestrictedFunctionProperties(Realm realm, ScriptObject obj) {
        /*  step 1  */
        Callable thrower = realm.getThrowTypeError();
        /*  step 2  */
        obj.defineOwnProperty(realm, "caller", new PropertyDescriptor(thrower, thrower, false,
                false));
        /*  step 3  */
        obj.defineOwnProperty(realm, "arguments", new PropertyDescriptor(thrower, thrower, false,
                false));
    }

    /**
     * [Runtime Semantics: InstantiateFunctionObject]
     */
    public static OrdinaryFunction InstantiateFunctionObject(Realm realm, LexicalEnvironment scope,
            RuntimeInfo.Function fd) {
        /* step 1-2 */
        OrdinaryFunction f = FunctionCreate(realm, FunctionKind.Normal, fd, scope);
        /* step 3 */
        MakeConstructor(realm, f);
        /* step 4 */
        return f;
    }

    /**
     * 8.3.15.1 [[Call]] Internal Method
     */
    @Override
    public Object call(Object thisValue, Object... args) {
        /* step 1-11 */
        ExecutionContext calleeContext = ExecutionContext.newFunctionExecutionContext(this,
                thisValue);
        /* step 12-13 */
        getFunction().functionDeclarationInstantiation(calleeContext, this, args);
        /* step 14-15 */
        Object result = getCode().evaluate(calleeContext);
        /* step 16 */
        return result;
    }

    /**
     * 8.3.15.2.1 OrdinaryConstruct (F, argumentsList)
     */
    public static <FUNCTION extends ScriptObject & Callable & Constructor> Object OrdinaryConstruct(
            Realm realm, FUNCTION f, Object[] args) {
        Object creator = Get(realm, f, BuiltinSymbol.create.get());
        Object obj;
        if (!Type.isUndefined(creator)) {
            if (!IsCallable(creator)) {
                throw throwTypeError(realm, Messages.Key.NotCallable);
            }
            obj = ((Callable) creator).call(f);
        } else {
            obj = OrdinaryCreateFromConstructor(realm, f, Intrinsics.ObjectPrototype);
        }
        Object result = f.call(obj, args);
        if (Type.isObject(result)) {
            return result;
        }
        return obj;
    }
}
