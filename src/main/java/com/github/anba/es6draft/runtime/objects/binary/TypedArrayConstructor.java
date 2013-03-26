/**
 * Copyright (c) 2012-2013 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.objects.binary;

import static com.github.anba.es6draft.runtime.AbstractOperations.*;
import static com.github.anba.es6draft.runtime.internal.Errors.throwRangeError;
import static com.github.anba.es6draft.runtime.internal.Errors.throwTypeError;
import static com.github.anba.es6draft.runtime.internal.Properties.createProperties;
import static com.github.anba.es6draft.runtime.objects.binary.ArrayBufferConstructor.AllocateArrayBuffer;
import static com.github.anba.es6draft.runtime.objects.binary.ArrayBufferConstructor.CloneArrayBuffer;
import static com.github.anba.es6draft.runtime.objects.binary.ArrayBufferConstructor.SetArrayBufferData;
import static com.github.anba.es6draft.runtime.objects.binary.ArrayBufferConstructor.SetValueInBuffer;
import static com.github.anba.es6draft.runtime.types.Undefined.UNDEFINED;
import static com.github.anba.es6draft.runtime.types.builtins.OrdinaryFunction.AddRestrictedFunctionProperties;
import static com.github.anba.es6draft.runtime.types.builtins.OrdinaryFunction.OrdinaryConstruct;

import com.github.anba.es6draft.runtime.Realm;
import com.github.anba.es6draft.runtime.internal.Initialisable;
import com.github.anba.es6draft.runtime.internal.Messages;
import com.github.anba.es6draft.runtime.internal.Properties.Attributes;
import com.github.anba.es6draft.runtime.internal.Properties.Function;
import com.github.anba.es6draft.runtime.internal.Properties.Prototype;
import com.github.anba.es6draft.runtime.internal.Properties.Value;
import com.github.anba.es6draft.runtime.types.BuiltinSymbol;
import com.github.anba.es6draft.runtime.types.Constructor;
import com.github.anba.es6draft.runtime.types.Intrinsics;
import com.github.anba.es6draft.runtime.types.ScriptObject;
import com.github.anba.es6draft.runtime.types.Type;
import com.github.anba.es6draft.runtime.types.builtins.BuiltinFunction;

/**
 * <h1>15 Standard Built-in ECMAScript Objects</h1><br>
 * <h2>15.13 Binary Data Objects</h2><br>
 * <h3>15.13.6 TypedArray Object Structures</h3>
 * <ul>
 * <li>15.13.6.1 TypedArray Constructors Called as a Function
 * <li>15.13.6.2 The TypedArray Constructors
 * <li>15.13.6.3 Properties of the TypedArray Constructors
 * </ul>
 */
public class TypedArrayConstructor extends BuiltinFunction implements Constructor, Initialisable {
    private final ElementKind elementKind;

    public TypedArrayConstructor(Realm realm, ElementKind elementKind) {
        super(realm);
        this.elementKind = elementKind;
    }

    @Override
    public void initialise(Realm realm) {
        switch (elementKind) {
        case Int8:
            createProperties(this, realm, Properties_Int8Array.class);
            break;
        case Uint8:
            createProperties(this, realm, Properties_Uint8Array.class);
            break;
        case Uint8C:
            createProperties(this, realm, Properties_Uint8Clamped.class);
            break;
        case Int16:
            createProperties(this, realm, Properties_Int16Array.class);
            break;
        case Uint16:
            createProperties(this, realm, Properties_Uint16Array.class);
            break;
        case Int32:
            createProperties(this, realm, Properties_Int32Array.class);
            break;
        case Uint32:
            createProperties(this, realm, Properties_Uint32Array.class);
            break;
        case Float32:
            createProperties(this, realm, Properties_Float32Array.class);
            break;
        case Float64:
            createProperties(this, realm, Properties_Float64Array.class);
            break;
        default:
            throw new IllegalStateException();
        }
        AddRestrictedFunctionProperties(realm, this);
    }

    /**
     * 15.13.6.1.1 TypedArray ( length )<br>
     * 15.13.6.1.2 TypedArray ( typedArray )<br>
     * 15.13.6.1.3 TypedArray ( array )<br>
     * 15.13.6.1.4 TypedArray ( buffer, byteOffset=0, length=undefined )<br>
     * 15.13.6.1.5 TypedArray ( binary data stuff ) [TODO]<br>
     */
    @Override
    public Object call(Object thisValue, Object... args) {
        Object arg0 = args.length > 0 ? args[0] : UNDEFINED;
        if (!Type.isObject(arg0)) {
            return callWithLength(realm(), thisValue, arg0, args);
        } else {
            if (arg0 instanceof TypedArrayObject) {
                return callWithTypedArray(realm(), thisValue, (TypedArrayObject) arg0, args);
            } else if (arg0 instanceof ArrayBufferObject) {
                return callWithArrayBuffer(realm(), thisValue, (ArrayBufferObject) arg0, args);
            } else {
                return callWithArray(realm(), thisValue, (ScriptObject) arg0, args);
            }
        }
    }

    /**
     * 15.13.6.1.1 TypedArray ( length )
     */
    private Object callWithLength(Realm realm, Object thisValue, Object length, Object[] args) {
        ElementKind elementType = elementKind;
        Object obj = thisValue;
        if (!(Type.isObject(obj) || Type.isUndefined(obj))) {
            throw throwTypeError(realm, Messages.Key.IncompatibleObject);
        }
        if (Type.isUndefined(obj) || !(obj instanceof TypedArrayObject)) {
            return OrdinaryConstruct(realm, this, args);
        }
        TypedArrayObject array = (TypedArrayObject) obj;
        if (array.getData() != null) {
            throwTypeError(realm, Messages.Key.IncompatibleObject);
        }
        long elementLength = ToUint32(realm, length);
        ArrayBufferObject data = AllocateArrayBuffer(realm,
                realm.getIntrinsic(Intrinsics.ArrayBuffer));
        int elementSize = elementType.size();
        long byteLength = elementSize * elementLength;
        SetArrayBufferData(realm, data, byteLength);
        array.setData(data);
        array.setElementKind(elementType);
        array.setByteLength(byteLength);
        array.setByteOffset(0);
        array.setArrayLength(elementLength);
        return array;
    }

    /**
     * 15.13.6.1.2 TypedArray ( typedArray )
     */
    private Object callWithTypedArray(Realm realm, Object thisValue, TypedArrayObject typedArray,
            Object[] args) {
        TypedArrayObject srcArray = typedArray;
        ElementKind elementType = elementKind;
        Object obj = thisValue;
        if (!(Type.isObject(obj) || Type.isUndefined(obj))) {
            throw throwTypeError(realm, Messages.Key.IncompatibleObject);
        }
        if (Type.isUndefined(obj) || !(obj instanceof TypedArrayObject)) {
            return OrdinaryConstruct(realm, this, args);
        }
        TypedArrayObject array = (TypedArrayObject) obj;
        if (array.getData() != null) {
            throwTypeError(realm, Messages.Key.IncompatibleObject);
        }
        long elementLength = srcArray.getArrayLength();
        ElementKind srcType = srcArray.getElementKind();
        ArrayBufferObject srcData = srcArray.getData();
        ArrayBufferObject data = CloneArrayBuffer(realm, srcData, srcType, elementType,
                elementLength);
        int elementSize = elementType.size();
        long byteLength = elementSize * elementLength;
        // FIXME: spec bug (remove this call <-> CloneArrayBuffer?)
        // data = SetArrayBufferData(realm, data, byteLength);
        array.setData(data);
        array.setElementKind(elementType);
        array.setByteLength(byteLength);
        array.setByteOffset(0);
        array.setArrayLength(elementLength);
        return array;
    }

    /**
     * 15.13.6.1.3 TypedArray ( array )
     */
    private Object callWithArray(Realm realm, Object thisValue, ScriptObject _array, Object[] args) {
        Object obj = thisValue;
        if (!(Type.isObject(obj) || Type.isUndefined(obj))) {
            throw throwTypeError(realm, Messages.Key.IncompatibleObject);
        }
        // FIXME: spec bug (variable srcArray unused)
        @SuppressWarnings("unused")
        ScriptObject srcArray = _array;
        ElementKind elementType = elementKind;
        if (Type.isUndefined(obj) || !(obj instanceof TypedArrayObject)) {
            return OrdinaryConstruct(realm, this, args);
        }
        TypedArrayObject array = (TypedArrayObject) obj;
        if (array.getData() != null) {
            throwTypeError(realm, Messages.Key.IncompatibleObject);
        }
        Object arrayLength = Get(realm, _array, "length");
        long elementLength = ToUint32(realm, arrayLength);
        ArrayBufferObject data = AllocateArrayBuffer(realm,
                realm.getIntrinsic(Intrinsics.ArrayBuffer));
        int elementSize = elementType.size();
        long byteLength = elementSize * elementLength;
        SetArrayBufferData(realm, data, byteLength);
        for (long k = 0; k < elementLength; ++k) {
            String pk = ToString(k);
            // FIXME: spec bug (`Get(array, Pk)` instead of `Get(O, Pk)`)
            Object kValue = Get(realm, _array, pk);
            double kNumber = ToNumber(realm, kValue);
            SetValueInBuffer(data, k * elementSize, elementType, kNumber, false);
        }
        array.setData(data);
        array.setElementKind(elementType);
        array.setByteLength(byteLength);
        array.setByteOffset(0);
        array.setArrayLength(elementLength);
        return array;
    }

    /**
     * 15.13.6.1.4 TypedArray ( buffer, byteOffset=0, length=undefined )
     */
    private Object callWithArrayBuffer(Realm realm, Object thisValue, ArrayBufferObject buffer,
            Object[] args) {
        Object byteOffset = args.length > 1 ? args[1] : 0;
        Object length = args.length > 2 ? args[2] : UNDEFINED;

        Object obj = thisValue;
        if (!(Type.isObject(obj) || Type.isUndefined(obj))) {
            throw throwTypeError(realm, Messages.Key.IncompatibleObject);
        }
        {
            // FIXME: spec bug (this check is not in spec)
            if (Type.isUndefined(obj) || !(obj instanceof TypedArrayObject)) {
                return OrdinaryConstruct(realm, this, args);
            }
        }

        ElementKind elementType = elementKind;
        int elementSize = elementType.size();
        long offset = ToUint32(realm, byteOffset);
        if (offset % elementSize != 0) {
            throwRangeError(realm, Messages.Key.InvalidByteOffset);
        }
        long bufferByteLength = buffer.getByteLength();
        long newByteLength;
        if (Type.isUndefined(length)) {
            if (bufferByteLength % elementSize != 0) {
                throwRangeError(realm, Messages.Key.InvalidBufferSize);
            }
            newByteLength = bufferByteLength - offset;
        } else {
            long newLength = ToUint32(realm, length);
            newByteLength = newLength * elementSize;
            if (newByteLength > bufferByteLength) {
                throwRangeError(realm, Messages.Key.InvalidBufferSize);
            }
        }
        TypedArrayObject array = (TypedArrayObject) obj;
        if (array.getData() != null) {
            throwTypeError(realm, Messages.Key.IncompatibleObject);
        }
        array.setData(buffer);
        array.setElementKind(elementType);
        array.setByteLength(newByteLength);
        array.setByteOffset(offset);
        array.setArrayLength(newByteLength / elementSize);
        return array;
    }

    /**
     * 15.13.6.2.1 new TypedArray (...args)
     */
    @Override
    public Object construct(Object... args) {
        return OrdinaryConstruct(realm(), this, args);
    }

    /**
     * 15.13.6.3.2 TypedArray[ @@create ] ( )
     */
    private static TypedArrayObject createTypedArray(Realm realm, Object thisValue,
            Intrinsics prototype) {
        Object f = thisValue;
        ScriptObject proto = GetPrototypeFromConstructor(realm, f, prototype);
        TypedArrayObject obj = new TypedArrayObject(realm);
        obj.setPrototype(realm, proto);
        return obj;
    }

    /**
     * 15.13.6.3 Properties of the TypedArray Constructors
     */
    public enum Properties_Int8Array {
        ;

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.FunctionPrototype;

        @Value(name = "length", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final int length = 3;

        @Value(name = "name", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final String name = "Int8Array";

        /**
         * 15.13.6.3.1 TypedArray.prototype
         */
        @Value(name = "prototype", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final Intrinsics prototype = Intrinsics.Int8ArrayPrototype;

        /**
         * 15.13.6.3.2 TypedArray[ @@create ] ( )
         */
        @Function(
                name = "@@create",
                symbol = BuiltinSymbol.create,
                arity = 0,
                attributes = @Attributes(writable = false, enumerable = false, configurable = false))
        public static Object create(Realm realm, Object thisValue) {
            return createTypedArray(realm, thisValue, prototype);
        }
    }

    /**
     * 15.13.6.3 Properties of the TypedArray Constructors
     */
    public enum Properties_Uint8Array {
        ;

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.FunctionPrototype;

        @Value(name = "length", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final int length = 3;

        @Value(name = "name", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final String name = "Uint8Array";

        /**
         * 15.13.6.3.1 TypedArray.prototype
         */
        @Value(name = "prototype", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final Intrinsics prototype = Intrinsics.Uint8ArrayPrototype;

        /**
         * 15.13.6.3.2 TypedArray[ @@create ] ( )
         */
        @Function(
                name = "@@create",
                symbol = BuiltinSymbol.create,
                arity = 0,
                attributes = @Attributes(writable = false, enumerable = false, configurable = false))
        public static Object create(Realm realm, Object thisValue) {
            return createTypedArray(realm, thisValue, prototype);
        }
    }

    /**
     * 15.13.6.3 Properties of the TypedArray Constructors
     */
    public enum Properties_Uint8Clamped {
        ;

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.FunctionPrototype;

        @Value(name = "length", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final int length = 3;

        @Value(name = "name", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final String name = "Uint8Clamped";

        /**
         * 15.13.6.3.1 TypedArray.prototype
         */
        @Value(name = "prototype", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final Intrinsics prototype = Intrinsics.Uint8ClampedArrayPrototype;

        /**
         * 15.13.6.3.2 TypedArray[ @@create ] ( )
         */
        @Function(
                name = "@@create",
                symbol = BuiltinSymbol.create,
                arity = 0,
                attributes = @Attributes(writable = false, enumerable = false, configurable = false))
        public static Object create(Realm realm, Object thisValue) {
            return createTypedArray(realm, thisValue, prototype);
        }
    }

    /**
     * 15.13.6.3 Properties of the TypedArray Constructors
     */
    public enum Properties_Int16Array {
        ;

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.FunctionPrototype;

        @Value(name = "length", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final int length = 3;

        @Value(name = "name", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final String name = "Int16Array";

        /**
         * 15.13.6.3.1 TypedArray.prototype
         */
        @Value(name = "prototype", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final Intrinsics prototype = Intrinsics.Int16ArrayPrototype;

        /**
         * 15.13.6.3.2 TypedArray[ @@create ] ( )
         */
        @Function(
                name = "@@create",
                symbol = BuiltinSymbol.create,
                arity = 0,
                attributes = @Attributes(writable = false, enumerable = false, configurable = false))
        public static Object create(Realm realm, Object thisValue) {
            return createTypedArray(realm, thisValue, prototype);
        }
    }

    /**
     * 15.13.6.3 Properties of the TypedArray Constructors
     */
    public enum Properties_Uint16Array {
        ;

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.FunctionPrototype;

        @Value(name = "length", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final int length = 3;

        @Value(name = "name", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final String name = "Uint16Array";

        /**
         * 15.13.6.3.1 TypedArray.prototype
         */
        @Value(name = "prototype", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final Intrinsics prototype = Intrinsics.Uint16ArrayPrototype;

        /**
         * 15.13.6.3.2 TypedArray[ @@create ] ( )
         */
        @Function(
                name = "@@create",
                symbol = BuiltinSymbol.create,
                arity = 0,
                attributes = @Attributes(writable = false, enumerable = false, configurable = false))
        public static Object create(Realm realm, Object thisValue) {
            return createTypedArray(realm, thisValue, prototype);
        }
    }

    /**
     * 15.13.6.3 Properties of the TypedArray Constructors
     */
    public enum Properties_Int32Array {
        ;

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.FunctionPrototype;

        @Value(name = "length", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final int length = 3;

        @Value(name = "name", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final String name = "Int32Array";

        /**
         * 15.13.6.3.1 TypedArray.prototype
         */
        @Value(name = "prototype", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final Intrinsics prototype = Intrinsics.Int32ArrayPrototype;

        /**
         * 15.13.6.3.2 TypedArray[ @@create ] ( )
         */
        @Function(
                name = "@@create",
                symbol = BuiltinSymbol.create,
                arity = 0,
                attributes = @Attributes(writable = false, enumerable = false, configurable = false))
        public static Object create(Realm realm, Object thisValue) {
            return createTypedArray(realm, thisValue, prototype);
        }
    }

    /**
     * 15.13.6.3 Properties of the TypedArray Constructors
     */
    public enum Properties_Uint32Array {
        ;

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.FunctionPrototype;

        @Value(name = "length", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final int length = 3;

        @Value(name = "name", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final String name = "Uint32Array";

        /**
         * 15.13.6.3.1 TypedArray.prototype
         */
        @Value(name = "prototype", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final Intrinsics prototype = Intrinsics.Uint32ArrayPrototype;

        /**
         * 15.13.6.3.2 TypedArray[ @@create ] ( )
         */
        @Function(
                name = "@@create",
                symbol = BuiltinSymbol.create,
                arity = 0,
                attributes = @Attributes(writable = false, enumerable = false, configurable = false))
        public static Object create(Realm realm, Object thisValue) {
            return createTypedArray(realm, thisValue, prototype);
        }
    }

    /**
     * 15.13.6.3 Properties of the TypedArray Constructors
     */
    public enum Properties_Float32Array {
        ;

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.FunctionPrototype;

        @Value(name = "length", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final int length = 3;

        @Value(name = "name", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final String name = "Float32Array";

        /**
         * 15.13.6.3.1 TypedArray.prototype
         */
        @Value(name = "prototype", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final Intrinsics prototype = Intrinsics.Float32ArrayPrototype;

        /**
         * 15.13.6.3.2 TypedArray[ @@create ] ( )
         */
        @Function(
                name = "@@create",
                symbol = BuiltinSymbol.create,
                arity = 0,
                attributes = @Attributes(writable = false, enumerable = false, configurable = false))
        public static Object create(Realm realm, Object thisValue) {
            return createTypedArray(realm, thisValue, prototype);
        }
    }

    /**
     * 15.13.6.3 Properties of the TypedArray Constructors
     */
    public enum Properties_Float64Array {
        ;

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.FunctionPrototype;

        @Value(name = "length", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final int length = 3;

        @Value(name = "name", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final String name = "Float64Array";

        /**
         * 15.13.6.3.1 TypedArray.prototype
         */
        @Value(name = "prototype", attributes = @Attributes(writable = false, enumerable = false,
                configurable = false))
        public static final Intrinsics prototype = Intrinsics.Float64ArrayPrototype;

        /**
         * 15.13.6.3.2 TypedArray[ @@create ] ( )
         */
        @Function(
                name = "@@create",
                symbol = BuiltinSymbol.create,
                arity = 0,
                attributes = @Attributes(writable = false, enumerable = false, configurable = false))
        public static Object create(Realm realm, Object thisValue) {
            return createTypedArray(realm, thisValue, prototype);
        }
    }
}
