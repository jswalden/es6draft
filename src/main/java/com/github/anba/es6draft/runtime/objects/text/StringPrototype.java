/**
 * Copyright (c) 2012-2016 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.objects.text;

import static com.github.anba.es6draft.runtime.AbstractOperations.*;
import static com.github.anba.es6draft.runtime.internal.Errors.newRangeError;
import static com.github.anba.es6draft.runtime.internal.Errors.newTypeError;
import static com.github.anba.es6draft.runtime.internal.Properties.createProperties;
import static com.github.anba.es6draft.runtime.objects.intl.CollatorPrototype.CompareStrings;
import static com.github.anba.es6draft.runtime.objects.intl.IntlAbstractOperations.BestAvailableLocale;
import static com.github.anba.es6draft.runtime.objects.intl.IntlAbstractOperations.CanonicalizeLocaleList;
import static com.github.anba.es6draft.runtime.objects.intl.IntlAbstractOperations.DefaultLocale;
import static com.github.anba.es6draft.runtime.objects.intl.IntlAbstractOperations.RemoveUnicodeLocaleExtension;
import static com.github.anba.es6draft.runtime.objects.text.RegExpConstructor.RegExpCreate;
import static com.github.anba.es6draft.runtime.objects.text.RegExpStringIteratorPrototype.CreateRegExpStringIterator;
import static com.github.anba.es6draft.runtime.objects.text.StringIteratorPrototype.CreateStringIterator;
import static com.github.anba.es6draft.runtime.types.Undefined.UNDEFINED;
import static com.github.anba.es6draft.runtime.types.builtins.ArrayObject.ArrayCreate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.github.anba.es6draft.runtime.ExecutionContext;
import com.github.anba.es6draft.runtime.Realm;
import com.github.anba.es6draft.runtime.internal.CompatibilityOption;
import com.github.anba.es6draft.runtime.internal.Initializable;
import com.github.anba.es6draft.runtime.internal.Messages;
import com.github.anba.es6draft.runtime.internal.Properties.Attributes;
import com.github.anba.es6draft.runtime.internal.Properties.CompatibilityExtension;
import com.github.anba.es6draft.runtime.internal.Properties.Function;
import com.github.anba.es6draft.runtime.internal.Properties.Prototype;
import com.github.anba.es6draft.runtime.internal.Properties.Value;
import com.github.anba.es6draft.runtime.internal.Strings;
import com.github.anba.es6draft.runtime.objects.intl.CollatorConstructor;
import com.github.anba.es6draft.runtime.objects.intl.CollatorObject;
import com.github.anba.es6draft.runtime.types.BuiltinSymbol;
import com.github.anba.es6draft.runtime.types.Callable;
import com.github.anba.es6draft.runtime.types.Intrinsics;
import com.github.anba.es6draft.runtime.types.Type;
import com.github.anba.es6draft.runtime.types.builtins.ArrayObject;
import com.github.anba.es6draft.runtime.types.builtins.NativeFunction;
import com.github.anba.es6draft.runtime.types.builtins.StringObject;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.Normalizer2;
import com.ibm.icu.util.ULocale;

/**
 * <h1>21 Text Processing</h1><br>
 * <h2>21.1 String Objects</h2>
 * <ul>
 * <li>21.1.3 Properties of the String Prototype Object
 * <li>21.1.4 Properties of String Instances
 * </ul>
 */
public final class StringPrototype extends StringObject implements Initializable {
    /**
     * Constructs a new String prototype object.
     * 
     * @param realm
     *            the realm object
     */
    public StringPrototype(Realm realm) {
        super(realm, "");
    }

    @Override
    public void initialize(Realm realm) {
        createProperties(realm, this, Properties.class);
        createProperties(realm, this, AdditionalProperties.class);
        createProperties(realm, this, TrimFunctions.class);
        createProperties(realm, this, PadFunctions.class);
        createProperties(realm, this, MatchAllFunction.class);
    }

    /**
     * Marker class for {@code String.prototype.iterator}.
     */
    private static final class StringPrototypeIterator {
    }

    /**
     * Returns {@code true} if <var>iterator</var> is the built-in {@code %IteratorPrototype%[@@iterator]} function for
     * the requested realm.
     * 
     * @param realm
     *            the function realm
     * @param iterator
     *            the iterator function
     * @return {@code true} if <var>iterator</var> is the built-in {@code %IteratorPrototype%[@@iterator]} function
     */
    public static boolean isBuiltinIterator(Realm realm, Object iterator) {
        return NativeFunction.isNative(realm, iterator, StringPrototypeIterator.class);
    }

    /**
     * 21.1.3 Properties of the String Prototype Object
     */
    public enum Properties {
        ;

        /**
         * Abstract operation thisStringValue(value)
         * 
         * @param cx
         *            the execution context
         * @param object
         *            the object value
         * @return the string value
         */
        private static CharSequence thisStringValue(ExecutionContext cx, Object object) {
            /* step 1 */
            if (Type.isString(object)) {
                return Type.stringValue(object);
            }
            /* step 2 */
            if (object instanceof StringObject) {
                return ((StringObject) object).getStringData();
            }
            /* step 3 */
            throw newTypeError(cx, Messages.Key.IncompatibleObject);
        }

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.ObjectPrototype;

        /**
         * String.prototype.length
         */
        // FIXME: spec issue - explicitly define length for String.prototype in 21.1.3?
        @Value(name = "length", attributes = @Attributes(writable = false, enumerable = false, configurable = false))
        public static final int length = 0;

        /**
         * 21.1.3.5 String.prototype.constructor
         */
        @Value(name = "constructor")
        public static final Intrinsics constructor = Intrinsics.String;

        /**
         * 21.1.3.23 String.prototype.toString ( )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @return the string representation
         */
        @Function(name = "toString", arity = 0)
        public static Object toString(ExecutionContext cx, Object thisValue) {
            return thisStringValue(cx, thisValue);
        }

        /**
         * 21.1.3.26 String.prototype.valueOf ( )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @return the string value
         */
        @Function(name = "valueOf", arity = 0)
        public static Object valueOf(ExecutionContext cx, Object thisValue) {
            return thisStringValue(cx, thisValue);
        }

        /**
         * 21.1.3.1 String.prototype.charAt (pos)
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param pos
         *            the string index
         * @return the character or the empty string
         */
        @Function(name = "charAt", arity = 1)
        public static Object charAt(ExecutionContext cx, Object thisValue, Object pos) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            CharSequence s = ToString(cx, obj);
            /* steps 4-5 */
            double position = ToInteger(cx, pos);
            /* step 6 */
            int size = s.length();
            /* step 7 */
            if (position < 0 || position >= size) {
                return "";
            }
            /* step 8 */
            return String.valueOf(s.charAt((int) position));
        }

        /**
         * 21.1.3.2 String.prototype.charCodeAt (pos)
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param pos
         *            the string index
         * @return the character code unit
         */
        @Function(name = "charCodeAt", arity = 1)
        public static Object charCodeAt(ExecutionContext cx, Object thisValue, Object pos) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            CharSequence s = ToString(cx, obj);
            /* steps 4-5 */
            double position = ToInteger(cx, pos);
            /* step 6 */
            int size = s.length();
            /* step 7 */
            if (position < 0 || position >= size) {
                return Double.NaN;
            }
            /* step 8 */
            return (int) s.charAt((int) position);
        }

        /**
         * 21.1.3.3 String.prototype.codePointAt (pos)
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param pos
         *            the start position
         * @return the code point
         */
        @Function(name = "codePointAt", arity = 1)
        public static Object codePointAt(ExecutionContext cx, Object thisValue, Object pos) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            String s = ToFlatString(cx, obj);
            /* steps 4-5 */
            double position = ToInteger(cx, pos);
            /* step 6 */
            int size = s.length();
            /* step 7 */
            if (position < 0 || position >= size) {
                return UNDEFINED;
            }
            /* steps 8-12 */
            return s.codePointAt((int) position);
        }

        /**
         * 21.1.3.4 String.prototype.concat ( ...args )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param args
         *            the additional strings
         * @return the concatenated string
         */
        @Function(name = "concat", arity = 1)
        public static Object concat(ExecutionContext cx, Object thisValue, Object... args) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            CharSequence s = ToString(cx, obj);
            /* step 4 (not applicable) */
            /* step 5 */
            StringBuilder r = new StringBuilder(s);
            /* step 6 */
            for (int i = 0; i < args.length; ++i) {
                CharSequence nextString = ToString(cx, args[i]);
                r.append(nextString);
            }
            /* step 7 */
            return r.toString();
        }

        /**
         * 21.1.3.6 String.prototype.endsWith (searchString [, endPosition] )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param searchString
         *            the search string
         * @param endPosition
         *            the end position
         * @return {@code true} if the string ends with <var>searchString</var>
         */
        @Function(name = "endsWith", arity = 1)
        public static Object endsWith(ExecutionContext cx, Object thisValue, Object searchString,
                Object endPosition) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            String s = ToFlatString(cx, obj);
            /* steps 4-6 */
            if (IsRegExp(cx, searchString)) {
                throw newTypeError(cx, Messages.Key.InvalidRegExpArgument);
            }
            /* steps 7-8 */
            String searchStr = ToFlatString(cx, searchString);
            /* step 9 */
            int len = s.length();
            /* steps 10-11 */
            double pos = Type.isUndefined(endPosition) ? len : ToInteger(cx, endPosition);
            /* step 12 */
            int end = (int) Math.min(Math.max(pos, 0), len);
            /* step 13 */
            int searchLength = searchStr.length();
            /* step 14 */
            int start = end - searchLength;
            /* step 15 */
            if (start < 0) {
                return false;
            }
            /* steps 16-17 */
            return s.startsWith(searchStr, start);
        }

        /**
         * 21.1.3.7 String.prototype.includes ( searchString [ , position ] )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param searchString
         *            the search string
         * @param position
         *            the start position
         * @return {@code true} if the search string was found
         */
        @Function(name = "includes", arity = 1)
        public static Object includes(ExecutionContext cx, Object thisValue, Object searchString,
                Object position /* = 0 */) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            String s = ToFlatString(cx, obj);
            /* steps 4-6 */
            if (IsRegExp(cx, searchString)) {
                throw newTypeError(cx, Messages.Key.InvalidRegExpArgument);
            }
            /* steps 7-8 */
            String searchStr = ToFlatString(cx, searchString);
            /* steps 9-10 */
            double pos = ToInteger(cx, position);
            /* step 11 */
            int len = s.length();
            /* step 12 */
            int start = (int) Math.min(Math.max(pos, 0), len);
            /* step 13 */
            // int searchLen = searchStr.length();
            /* step 14 */
            return s.indexOf(searchStr, start) != -1;
        }

        /**
         * 21.1.3.8 String.prototype.indexOf ( searchString [ , position ] )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param searchString
         *            the search string value
         * @param position
         *            the start position
         * @return the result index
         */
        @Function(name = "indexOf", arity = 1)
        public static Object indexOf(ExecutionContext cx, Object thisValue, Object searchString,
                Object position) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            String s = ToFlatString(cx, obj);
            /* steps 4-5 */
            String searchStr = ToFlatString(cx, searchString);
            /* steps 6-7 */
            double pos = ToInteger(cx, position);
            /* step 8 */
            int len = s.length();
            /* step 9 */
            int start = (int) Math.min(Math.max(pos, 0), len);
            /* steps 10-11 */
            return s.indexOf(searchStr, start);
        }

        /**
         * 21.1.3.9 String.prototype.lastIndexOf ( searchString [ , position ] )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param searchString
         *            the search string value
         * @param position
         *            the start position
         * @return the result index
         */
        @Function(name = "lastIndexOf", arity = 1)
        public static Object lastIndexOf(ExecutionContext cx, Object thisValue,
                Object searchString, Object position) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            String s = ToFlatString(cx, obj);
            /* steps 4-5 */
            String searchStr = ToFlatString(cx, searchString);
            /* steps 6-7 */
            double numPos = ToNumber(cx, position);
            /* step 8 */
            double pos = Double.isNaN(numPos) ? Double.POSITIVE_INFINITY : ToInteger(numPos);
            /* step 9 */
            int len = s.length();
            /* step 10 */
            int start = (int) Math.min(Math.max(pos, 0), len);
            /* steps 11-12 */
            return s.lastIndexOf(searchStr, start);
        }

        /**
         * 21.1.3.10 String.prototype.localeCompare ( that [, reserved1 [ , reserved2 ] ] )<br>
         * 13.1.1 String.prototype.localeCompare (that [, locales [, options ]])
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param that
         *            the other string
         * @param locales
         *            the optional locales array
         * @param options
         *            the optional options object
         * @return the locale specific comparison result
         */
        @Function(name = "localeCompare", arity = 1)
        public static Object localeCompare(ExecutionContext cx, Object thisValue, Object that,
                Object locales, Object options) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            String s = ToFlatString(cx, obj);
            /* steps 4-5 */
            String t = ToFlatString(cx, that);

            // ES5/6
            // return cx.getRealm().getCollator().compare(s, t);

            // ECMA-402
            /* steps 6-7 */
            CollatorConstructor ctor = (CollatorConstructor) cx
                    .getIntrinsic(Intrinsics.Intl_Collator);
            CollatorObject collator = ctor.construct(cx, ctor, locales, options);
            /* step 8 */
            return CompareStrings(collator, s, t);
        }

        /**
         * 21.1.3.11 String.prototype.match (regexp)
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param regexp
         *            the regular expression object
         * @return the match result array
         */
        @Function(name = "match", arity = 1)
        public static Object match(ExecutionContext cx, Object thisValue, Object regexp) {
            /* steps 1-2 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* step 3 */
            if (!Type.isUndefinedOrNull(regexp)) {
                /* steps 3.a-b */
                Callable matcher = GetMethod(cx, regexp, BuiltinSymbol.match.get());
                /* step 3.c */
                if (matcher != null) {
                    return matcher.call(cx, regexp, obj);
                }
            }
            /* steps 4-5 */
            CharSequence s = ToString(cx, obj);
            /* steps 6-7 */
            RegExpObject rx = RegExpCreate(cx, regexp, UNDEFINED);
            /* step 8 */
            return Invoke(cx, rx, BuiltinSymbol.match.get(), s);
        }

        /**
         * 21.1.3.12 String.prototype.normalize ( [ form ] )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param form
         *            the normalisation form
         * @return the normalized string
         */
        @Function(name = "normalize", arity = 0)
        public static Object normalize(ExecutionContext cx, Object thisValue, Object form) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            String s = ToFlatString(cx, obj);
            /* steps 4-6 */
            String f = "NFC";
            if (!Type.isUndefined(form)) {
                f = ToFlatString(cx, form);
            }
            /* steps 7-9 */
            switch (f) {
            case "NFC":
                return Normalizer2.getNFCInstance().normalize(s);
            case "NFD":
                return Normalizer2.getNFDInstance().normalize(s);
            case "NFKC":
                return Normalizer2.getNFKCInstance().normalize(s);
            case "NFKD":
                return Normalizer2.getNFKDInstance().normalize(s);
            default:
                throw newRangeError(cx, Messages.Key.InvalidNormalizationForm, f);
            }
        }

        /**
         * 21.1.3.13 String.prototype.repeat (count)
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param count
         *            the repetition count
         * @return the string repeated <var>count</var> times
         */
        @Function(name = "repeat", arity = 1)
        public static Object repeat(ExecutionContext cx, Object thisValue, Object count) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            String s = ToFlatString(cx, obj);
            /* steps 4-5 */
            double n = ToInteger(cx, count);
            /* steps 6-7 */
            if (n < 0 || n == Double.POSITIVE_INFINITY) {
                throw newRangeError(cx, Messages.Key.InvalidStringRepeat);
            }
            /* step 8 */
            if (n == 0 || s.length() == 0) {
                return "";
            }
            if (n == 1) {
                return s;
            }
            double capacity = s.length() * n;
            if (capacity > 1 << 27) {
                // likely to exceed heap space, follow SpiderMonkey and throw RangeError
                throw newRangeError(cx, Messages.Key.InvalidStringRepeat);
            }
            /* step 8 */
            final int length = s.length();
            char[] ca = new char[(int) capacity];
            if (length == 1) {
                Arrays.fill(ca, s.charAt(0));
                return new String(ca);
            }
            s.getChars(0, length, ca, 0);
            final int N = (int) n;
            final int limit = length * Integer.highestOneBit(N);
            for (int k = length; k < limit; k <<= 1) {
                System.arraycopy(ca, 0, ca, k, k);
            }
            System.arraycopy(ca, 0, ca, limit, (N * length - limit));
            /* step 9 */
            return new String(ca);
        }

        /**
         * 21.1.3.14 String.prototype.replace (searchValue, replaceValue)
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param searchValue
         *            the search string
         * @param replaceValue
         *            the replace string or replacer function
         * @return the new string
         */
        @Function(name = "replace", arity = 2)
        public static Object replace(ExecutionContext cx, Object thisValue, Object searchValue,
                Object replaceValue) {
            /* steps 1-2 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* step 3 */
            if (!Type.isUndefinedOrNull(searchValue)) {
                /* steps 3.a-b */
                Callable replacer = GetMethod(cx, searchValue, BuiltinSymbol.replace.get());
                /* step 3.c */
                if (replacer != null) {
                    return replacer.call(cx, searchValue, obj, replaceValue);
                }
            }
            /* steps 4-5 */
            String string = ToFlatString(cx, obj);
            /* steps 6-7 */
            String searchString = ToFlatString(cx, searchValue);
            /* step 8 */
            boolean functionalReplace = IsCallable(replaceValue);
            /* step 9 */
            String replaceValueString = null;
            Callable replaceValueCallable = null;
            if (!functionalReplace) {
                replaceValueString = ToFlatString(cx, replaceValue);
            } else {
                replaceValueCallable = (Callable) replaceValue;
            }
            /* step 10 */
            int pos = string.indexOf(searchString);
            if (pos < 0) {
                return string;
            }
            String matched = searchString;
            /* steps 11-12 */
            String replStr;
            if (functionalReplace) {
                Object replValue = replaceValueCallable.call(cx, UNDEFINED, matched, pos, string);
                replStr = ToFlatString(cx, replValue);
            } else {
                replStr = GetSubstitution(matched, string, pos, replaceValueString);
            }
            /* step 13 */
            int tailPos = pos + searchString.length();
            /* steps 14-15 */
            return string.substring(0, pos) + replStr + string.substring(tailPos);
        }

        /**
         * 21.1.3.14.1 Runtime Semantics: GetSubstitution(matched, str, position, captures,
         * replacement)
         * 
         * @param matched
         *            the matched substring
         * @param string
         *            the string
         * @param position
         *            the string index
         * @param replacement
         *            the replacement value
         * @return the replacement string
         */
        private static String GetSubstitution(String matched, String string, int position, String replacement) {
            /* step 1 (not applicable) */
            /* step 2 */
            int matchLength = matched.length();
            /* step 3 (not applicable) */
            /* step 4 */
            int stringLength = string.length();
            /* steps 5-6 */
            assert position >= 0 && position <= stringLength;
            /* steps 7-8 (not applicable) */
            /* step 9 */
            int tailPos = position + matchLength;
            assert tailPos >= 0 && tailPos <= stringLength;
            /* step 10 (not applicable) */
            /* step 11 */
            int cursor = replacement.indexOf('$');
            if (cursor < 0) {
                return replacement;
            }
            final int length = replacement.length();
            int lastCursor = 0;
            StringBuilder result = new StringBuilder();
            for (;;) {
                if (lastCursor < cursor) {
                    result.append(replacement, lastCursor, cursor);
                }
                if (++cursor == length) {
                    result.append('$');
                    break;
                }
                assert cursor < length;
                char c = replacement.charAt(cursor++);
                switch (c) {
                case '&':
                    result.append(matched);
                    break;
                case '`':
                    result.append(string, 0, position);
                    break;
                case '\'':
                    result.append(string, tailPos, stringLength);
                    break;
                case '$':
                    result.append('$');
                    break;
                default:
                    result.append('$').append(c);
                    break;
                }
                lastCursor = cursor;
                cursor = replacement.indexOf('$', cursor);
                if (cursor < 0) {
                    if (lastCursor < length) {
                        result.append(replacement, lastCursor, length);
                    }
                    break;
                }
            }
            /* step 12 */
            return result.toString();
        }

        /**
         * 21.1.3.15 String.prototype.search (regexp)
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param regexp
         *            the regular expression object
         * @return the first match index
         */
        @Function(name = "search", arity = 1)
        public static Object search(ExecutionContext cx, Object thisValue, Object regexp) {
            /* steps 1-2 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* step 3 */
            if (!Type.isUndefinedOrNull(regexp)) {
                /* steps 3.a-b */
                Callable searcher = GetMethod(cx, regexp, BuiltinSymbol.search.get());
                /* step 3.c */
                if (searcher != null) {
                    return searcher.call(cx, regexp, obj);
                }
            }
            /* steps 4-5 */
            CharSequence string = ToString(cx, obj);
            /* steps 6-7 */
            RegExpObject rx = RegExpCreate(cx, regexp, UNDEFINED);
            /* step 8 */
            return Invoke(cx, rx, BuiltinSymbol.search.get(), string);
        }

        /**
         * 21.1.3.16 String.prototype.slice (start, end)
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param start
         *            the start position
         * @param end
         *            the end position
         * @return the substring
         */
        @Function(name = "slice", arity = 2)
        public static Object slice(ExecutionContext cx, Object thisValue, Object start, Object end) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            CharSequence s = ToString(cx, obj);
            /* step 4 */
            int len = s.length();
            /* steps 5-6 */
            double intStart = ToInteger(cx, start);
            /* steps 7-8 */
            double intEnd = Type.isUndefined(end) ? len : ToInteger(cx, end);
            /* step 9 */
            int from = (int) (intStart < 0 ? Math.max(len + intStart, 0) : Math.min(intStart, len));
            /* step 10 */
            int to = (int) (intEnd < 0 ? Math.max(len + intEnd, 0) : Math.min(intEnd, len));
            /* step 11 */
            int span = Math.max(to - from, 0);
            /* step 12 */
            return s.subSequence(from, from + span);
        }

        /**
         * 21.1.3.17 String.prototype.split (separator, limit)
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param separator
         *            the string separator
         * @param limit
         *            the optional split array limit
         * @return the split array
         */
        @Function(name = "split", arity = 2)
        public static Object split(ExecutionContext cx, Object thisValue, Object separator,
                Object limit) {
            /* steps 1-2 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* step 3 */
            if (!Type.isUndefinedOrNull(separator)) {
                /* steps 3.a-b */
                Callable splitter = GetMethod(cx, separator, BuiltinSymbol.split.get());
                /* step 3.c */
                if (splitter != null) {
                    return splitter.call(cx, separator, obj, limit);
                }
            }
            /* steps 4-5 */
            String s = ToFlatString(cx, obj);
            /* step 6 */
            ArrayObject a = ArrayCreate(cx, 0);
            /* step 7 */
            int lengthA = 0;
            /* steps 8-9 */
            long lim = Type.isUndefined(limit) ? 0xFFFF_FFFFL : ToUint32(cx, limit);
            /* step 10 */
            int size = s.length();
            /* step 11 */
            int p = 0;
            /* steps 12-13 */
            String r = ToFlatString(cx, separator);
            /* step 14 */
            if (lim == 0) {
                return a;
            }
            /* step 15 */
            if (Type.isUndefined(separator)) {
                CreateDataProperty(cx, a, 0, s);
                return a;
            }
            /* step 16 */
            if (size == 0) {
                if (r.length() == 0) {
                    return a;
                }
                CreateDataProperty(cx, a, 0, s);
                return a;
            }
            /* step 17 */
            int q = p;
            /* step 18 */
            while (q != size) {
                int z = SplitMatch(s, q, r);
                if (z == -1) {
                    break;
                } else {
                    int e = z + r.length();
                    if (e == p) {
                        q = q + 1;
                    } else {
                        String t = s.substring(p, z);
                        CreateDataProperty(cx, a, lengthA, t);
                        lengthA += 1;
                        if (lengthA == lim) {
                            return a;
                        }
                        p = e;
                        q = p;
                    }
                }
            }
            /* step 19 */
            String t = s.substring(p, size);
            /* steps 20-21 */
            CreateDataProperty(cx, a, lengthA, t);
            /* step 22 */
            return a;
        }

        /**
         * 21.1.3.17.1 Runtime Semantics: SplitMatch ( S, q, R )
         * 
         * @param s
         *            the string
         * @param q
         *            the start position
         * @param r
         *            the search string
         * @return the index of the first match
         */
        private static int SplitMatch(String s, int q, String r) {
            // returns start instead of end position
            return s.indexOf(r, q);
        }

        /**
         * 21.1.3.18 String.prototype.startsWith (searchString [, position ] )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param searchString
         *            the search string
         * @param position
         *            the start position
         * @return {@code true} if the string starts with <var>searchString</var>
         */
        @Function(name = "startsWith", arity = 1)
        public static Object startsWith(ExecutionContext cx, Object thisValue, Object searchString,
                Object position) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            String s = ToFlatString(cx, obj);
            /* steps 4-6 */
            if (IsRegExp(cx, searchString)) {
                throw newTypeError(cx, Messages.Key.InvalidRegExpArgument);
            }
            /* steps 7-8 */
            String searchStr = ToFlatString(cx, searchString);
            /* steps 9-10 */
            double pos = ToInteger(cx, position);
            /* step 11 */
            int len = s.length();
            /* step 12 */
            int start = (int) Math.min(Math.max(pos, 0), len);
            /* step 13 */
            int searchLength = searchStr.length();
            /* step 14 */
            if (searchLength + start > len) {
                return false;
            }
            /* steps 15-16 */
            return s.startsWith(searchStr, start);
        }

        /**
         * 21.1.3.19 String.prototype.substring (start, end)
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param start
         *            the start position
         * @param end
         *            the end position
         * @return the substring
         */
        @Function(name = "substring", arity = 2)
        public static Object substring(ExecutionContext cx, Object thisValue, Object start,
                Object end) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            CharSequence s = ToString(cx, obj);
            /* step 4 */
            int len = s.length();
            /* steps 5-6 */
            double intStart = ToInteger(cx, start);
            /* steps 7-8 */
            double intEnd = Type.isUndefined(end) ? len : ToInteger(cx, end);
            /* step 9 */
            int finalStart = (int) Math.min(Math.max(intStart, 0), len);
            /* step 10 */
            int finalEnd = (int) Math.min(Math.max(intEnd, 0), len);
            /* step 11 */
            int from = Math.min(finalStart, finalEnd);
            /* step 12 */
            int to = Math.max(finalStart, finalEnd);
            /* step 13 */
            return s.subSequence(from, to);
        }

        /**
         * 21.1.3.20 String.prototype.toLocaleLowerCase ( [ reserved1 [ , reserved2 ] ] )<br>
         * 13.1.2 String.prototype.toLocaleLowerCase ([locales])
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param locales
         *            the optional locales array
         * @return the lower case string
         */
        @Function(name = "toLocaleLowerCase", arity = 0)
        public static Object toLocaleLowerCase(ExecutionContext cx, Object thisValue, Object locales) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            String s = ToFlatString(cx, obj);

            // ES5/6
            // return s.toLowerCase(cx.getRealm().getLocale());

            /* steps 4-5 */
            Set<String> requestedLocales = CanonicalizeLocaleList(cx, locales);
            /* steps 6-8 */
            String requestedLocale = !requestedLocales.isEmpty() ? requestedLocales.iterator()
                    .next() : DefaultLocale(cx.getRealm());
            /* step 9 */
            String noExtensionsLocale = RemoveUnicodeLocaleExtension(requestedLocale);
            /* step 10 */
            HashSet<String> availableLocales = new HashSet<>(Arrays.asList("az", "lt", "tr"));
            /* step 11 */
            String locale = BestAvailableLocale(availableLocales, noExtensionsLocale);
            /* step 12 */
            String supportedLocale = locale == null ? "und" : locale;
            /* steps 13-18 */
            return UCharacter.toLowerCase(ULocale.forLanguageTag(supportedLocale), s);
        }

        /**
         * 21.1.3.21 String.prototype.toLocaleUpperCase ([ reserved1 [ , reserved2 ] ] )<br>
         * 13.1.3 String.prototype.toLocaleUpperCase ([locales ])
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param locales
         *            the optional locales array
         * @return the upper case string
         */
        @Function(name = "toLocaleUpperCase", arity = 0)
        public static Object toLocaleUpperCase(ExecutionContext cx, Object thisValue, Object locales) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            String s = ToFlatString(cx, obj);

            // ES5/6
            // return s.toUpperCase(cx.getRealm().getLocale());

            /* steps 4-5 */
            Set<String> requestedLocales = CanonicalizeLocaleList(cx, locales);
            /* steps 6-8 */
            String requestedLocale = !requestedLocales.isEmpty() ? requestedLocales.iterator()
                    .next() : DefaultLocale(cx.getRealm());
            /* step 9 */
            String noExtensionsLocale = RemoveUnicodeLocaleExtension(requestedLocale);
            /* step 10 */
            HashSet<String> availableLocales = new HashSet<>(Arrays.asList("az", "lt", "tr"));
            /* step 11 */
            String locale = BestAvailableLocale(availableLocales, noExtensionsLocale);
            /* step 12 */
            String supportedLocale = locale == null ? "und" : locale;
            /* steps 13-18 */
            return UCharacter.toUpperCase(ULocale.forLanguageTag(supportedLocale), s);
        }

        /**
         * 21.1.3.22 String.prototype.toLowerCase ( )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @return the lower case string
         */
        @Function(name = "toLowerCase", arity = 0)
        public static Object toLowerCase(ExecutionContext cx, Object thisValue) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            String s = ToFlatString(cx, obj);
            /* steps 4-9 */
            Latin1: {
                int index = 0;
                Lower: {
                    for (; index < s.length(); index++) {
                        int c = s.charAt(index);
                        if (c > 0xff) {
                            break Latin1;
                        }
                        if (c != Character.toLowerCase(c)) {
                            break Lower;
                        }
                    }
                    return s;
                }
                char[] chars = s.toCharArray();
                for (; index < chars.length; ++index) {
                    int c = chars[index];
                    if (c > 0xff) {
                        break Latin1;
                    }
                    chars[index] = (char) Character.toLowerCase(c);
                }
                return new String(chars);
            }
            return UCharacter.toLowerCase(ULocale.ROOT, s);
        }

        /**
         * 21.1.3.24 String.prototype.toUpperCase ( )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @return the upper case string
         */
        @Function(name = "toUpperCase", arity = 0)
        public static Object toUpperCase(ExecutionContext cx, Object thisValue) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            String s = ToFlatString(cx, obj);
            /* steps 4-9 */
            Latin1: {
                int index = 0;
                Upper: {
                    for (; index < s.length(); index++) {
                        int c = s.charAt(index);
                        if (c > 0xff || c == 0xdf) {
                            break Latin1;
                        }
                        if (c != Character.toUpperCase(c)) {
                            break Upper;
                        }
                    }
                    return s;
                }
                char[] chars = s.toCharArray();
                for (; index < chars.length; ++index) {
                    int c = chars[index];
                    if (c > 0xff || c == 0xdf) {
                        break Latin1;
                    }
                    chars[index] = (char) Character.toUpperCase(c);
                }
                return new String(chars);
            }
            return UCharacter.toUpperCase(ULocale.ROOT, s);
        }

        /**
         * 21.1.3.25 String.prototype.trim ( )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @return the string with leading and trailing whitespace removed
         */
        @Function(name = "trim", arity = 0)
        public static Object trim(ExecutionContext cx, Object thisValue) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            String s = ToFlatString(cx, obj);
            /* steps 4-5 */
            return Strings.trim(s);
        }

        /**
         * 21.1.3.27 String.prototype [ @@iterator ]( )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @return the string iterator
         */
        @Function(name = "[Symbol.iterator]", symbol = BuiltinSymbol.iterator, arity = 0,
                nativeId = StringPrototypeIterator.class)
        public static Object iterator(ExecutionContext cx, Object thisValue) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            String s = ToFlatString(cx, obj);
            /* step 4 */
            return CreateStringIterator(cx, s);
        }
    }

    /**
     * B.2.3 Additional Properties of the String.prototype Object
     */
    @CompatibilityExtension(CompatibilityOption.StringPrototype)
    public enum AdditionalProperties {
        ;

        /**
         * B.2.3.1 String.prototype.substr (start, length)
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param start
         *            the start position
         * @param length
         *            the substring length
         * @return the substring
         */
        @Function(name = "substr", arity = 2)
        public static Object substr(ExecutionContext cx, Object thisValue, Object start,
                Object length) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            CharSequence s = ToString(cx, obj);
            /* steps 4-5 */
            double intStart = ToInteger(cx, start);
            /* steps 6-7 */
            double end = Type.isUndefined(length) ? Double.POSITIVE_INFINITY : ToInteger(cx, length);
            /* step 8 */
            int size = s.length();
            /* step 9 */
            if (intStart < 0) {
                intStart = Math.max(size + intStart, 0);
            }
            /* step 10 */
            double resultLength = Math.min(Math.max(end, 0), size - intStart);
            /* step 11 */
            if (resultLength <= 0) {
                return "";
            }
            assert 0 <= intStart && intStart + resultLength <= size;
            /* step 12 */
            return s.subSequence((int) intStart, (int) (intStart + resultLength));
        }

        /**
         * B.2.3.2.1 Runtime Semantics: CreateHTML ( string, tag, attribute, value )
         * 
         * @param cx
         *            the execution context
         * @param string
         *            the string
         * @param tag
         *            the html tag
         * @param attribute
         *            the html attribute name
         * @param value
         *            the html attribute value
         * @return the html string
         */
        private static String CreateHTML(ExecutionContext cx, Object string, String tag,
                String attribute, Object value) {
            /* step 1 */
            Object str = RequireObjectCoercible(cx, string);
            /* steps 2-3 */
            String s = ToFlatString(cx, str);
            /* steps 4-5 */
            StringBuilder p = new StringBuilder().append('<').append(tag);
            if (!attribute.isEmpty()) {
                String v = ToFlatString(cx, value);
                String escapedV = v.replace("\"", "&quot;");
                p.append(' ').append(attribute).append('=').append('"').append(escapedV)
                        .append('"');
            }
            /* steps 6-9 */
            return p.append('>').append(s).append("</").append(tag).append('>').toString();
        }

        /**
         * B.2.3.2 String.prototype.anchor ( name )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param name
         *            the anchor name
         * @return the html string
         */
        @Function(name = "anchor", arity = 1)
        public static Object anchor(ExecutionContext cx, Object thisValue, Object name) {
            Object s = thisValue;
            return CreateHTML(cx, s, "a", "name", name);
        }

        /**
         * B.2.3.3 String.prototype.big ()
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @return the html string
         */
        @Function(name = "big", arity = 0)
        public static Object big(ExecutionContext cx, Object thisValue) {
            Object s = thisValue;
            return CreateHTML(cx, s, "big", "", "");
        }

        /**
         * B.2.3.4 String.prototype.blink ()
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @return the html string
         */
        @Function(name = "blink", arity = 0)
        public static Object blink(ExecutionContext cx, Object thisValue) {
            Object s = thisValue;
            return CreateHTML(cx, s, "blink", "", "");
        }

        /**
         * B.2.3.5 String.prototype.bold ()
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @return the html string
         */
        @Function(name = "bold", arity = 0)
        public static Object bold(ExecutionContext cx, Object thisValue) {
            Object s = thisValue;
            return CreateHTML(cx, s, "b", "", "");
        }

        /**
         * B.2.3.6 String.prototype.fixed ()
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @return the html string
         */
        @Function(name = "fixed", arity = 0)
        public static Object fixed(ExecutionContext cx, Object thisValue) {
            Object s = thisValue;
            return CreateHTML(cx, s, "tt", "", "");
        }

        /**
         * B.2.3.7 String.prototype.fontcolor ( color )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param color
         *            the font color
         * @return the html string
         */
        @Function(name = "fontcolor", arity = 1)
        public static Object fontcolor(ExecutionContext cx, Object thisValue, Object color) {
            Object s = thisValue;
            return CreateHTML(cx, s, "font", "color", color);
        }

        /**
         * B.2.3.8 String.prototype.fontsize ( size )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param size
         *            the font size
         * @return the html string
         */
        @Function(name = "fontsize", arity = 1)
        public static Object fontsize(ExecutionContext cx, Object thisValue, Object size) {
            Object s = thisValue;
            return CreateHTML(cx, s, "font", "size", size);
        }

        /**
         * B.2.3.9 String.prototype.italics ()
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @return the html string
         */
        @Function(name = "italics", arity = 0)
        public static Object italics(ExecutionContext cx, Object thisValue) {
            Object s = thisValue;
            return CreateHTML(cx, s, "i", "", "");
        }

        /**
         * B.2.3.10 String.prototype.link ( url )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param url
         *            the url
         * @return the html string
         */
        @Function(name = "link", arity = 1)
        public static Object link(ExecutionContext cx, Object thisValue, Object url) {
            Object s = thisValue;
            return CreateHTML(cx, s, "a", "href", url);
        }

        /**
         * B.2.3.11 String.prototype.small ()
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @return the html string
         */
        @Function(name = "small", arity = 0)
        public static Object small(ExecutionContext cx, Object thisValue) {
            Object s = thisValue;
            return CreateHTML(cx, s, "small", "", "");
        }

        /**
         * B.2.3.12 String.prototype.strike ()
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @return the html string
         */
        @Function(name = "strike", arity = 0)
        public static Object strike(ExecutionContext cx, Object thisValue) {
            Object s = thisValue;
            return CreateHTML(cx, s, "strike", "", "");
        }

        /**
         * B.2.3.13 String.prototype.sub ()
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @return the html string
         */
        @Function(name = "sub", arity = 0)
        public static Object sub(ExecutionContext cx, Object thisValue) {
            Object s = thisValue;
            return CreateHTML(cx, s, "sub", "", "");
        }

        /**
         * B.2.3.14 String.prototype.sup ()
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @return the html string
         */
        @Function(name = "sup", arity = 0)
        public static Object sup(ExecutionContext cx, Object thisValue) {
            Object s = thisValue;
            return CreateHTML(cx, s, "sup", "", "");
        }
    }

    /**
     * Extension: String.prototype.trimLeft and trimRight
     */
    @CompatibilityExtension(CompatibilityOption.StringTrim)
    public enum TrimFunctions {
        ;

        /**
         * String.prototype.trimLeft ()
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @return the string with leading whitespace removed
         */
        @Function(name = "trimLeft", arity = 0)
        public static Object trimLeft(ExecutionContext cx, Object thisValue) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            String s = ToFlatString(cx, obj);
            /* steps 4-5 */
            return Strings.trimLeft(s);
        }

        /**
         * String.prototype.trimRight ()
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @return the string with trailing whitespace removed
         */
        @Function(name = "trimRight", arity = 0)
        public static Object trimRight(ExecutionContext cx, Object thisValue) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-3 */
            String s = ToFlatString(cx, obj);
            /* steps 4-5 */
            return Strings.trimRight(s);
        }
    }

    /**
     * Extension: String.prototype.padStart and padEnd
     */
    @CompatibilityExtension(CompatibilityOption.StringPad)
    public enum PadFunctions {
        ;

        /**
         * String.prototype.padStart( maxLength [ , fillString ] )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param maxLength
         *            the maximum length
         * @param fillString
         *            the optional fill string
         * @return the string with leading padding applied
         */
        @Function(name = "padStart", arity = 1)
        public static Object padStart(ExecutionContext cx, Object thisValue, Object maxLength, Object fillString) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* step 2 */
            String s = ToFlatString(cx, obj);
            /* step 3 */
            long intMaxLength = ToLength(cx, maxLength);
            /* step 4 */
            int stringLength = s.length();
            /* step 5 */
            if (intMaxLength <= stringLength) {
                return s;
            }
            /* steps 6-7 */
            CharSequence fillStr = Type.isUndefined(fillString) ? "" : ToString(cx, fillString);
            /* step 8 */
            if (fillStr.length() == 0) {
                fillStr = " ";
            }
            /* step 9 */
            long fillLen = intMaxLength - stringLength;
            if (fillLen > 1 << 27) {
                // Likely to exceed heap space, throw RangeError to match String.prototype.repeat.
                throw newRangeError(cx, Messages.Key.InvalidStringPad);
            }
            /* steps 10-11 */
            return repeatFill(fillStr.toString(), (int) fillLen) + s;
        }

        /**
         * String.prototype.padEnd( maxLength [ , fillString ] )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param maxLength
         *            the maximum length
         * @param fillString
         *            the optional fill string
         * @return the string with trailing padding applied
         */
        @Function(name = "padEnd", arity = 1)
        public static Object padEnd(ExecutionContext cx, Object thisValue, Object maxLength, Object fillString) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* step 2 */
            String s = ToFlatString(cx, obj);
            /* step 3 */
            long intMaxLength = ToLength(cx, maxLength);
            /* step 4 */
            int stringLength = s.length();
            /* step 5 */
            if (intMaxLength <= stringLength) {
                return s;
            }
            /* steps 6-7 */
            CharSequence fillStr = Type.isUndefined(fillString) ? "" : ToString(cx, fillString);
            /* step 8 */
            if (fillStr.length() == 0) {
                fillStr = " ";
            }
            /* step 9 */
            long fillLen = intMaxLength - stringLength;
            if (fillLen > 1 << 27) {
                // Likely to exceed heap space, throw RangeError to match String.prototype.repeat.
                throw newRangeError(cx, Messages.Key.InvalidStringPad);
            }
            /* steps 10-11 */
            return s + repeatFill(fillStr.toString(), (int) fillLen);
        }

        private static String repeatFill(String fillStr, int fillLen) {
            assert !fillStr.isEmpty() && fillLen > 0;
            final int length = fillStr.length();
            int c = fillLen / length;
            if (c == 0) {
                return fillStr.substring(0, fillLen);
            }
            if (c == 1) {
                int r = fillLen - length;
                if (r == 0) {
                    return fillStr;
                }
                return fillStr + fillStr.substring(0, r);
            }
            char[] ca = new char[fillLen];
            if (length == 1) {
                Arrays.fill(ca, fillStr.charAt(0));
                return new String(ca);
            }
            fillStr.getChars(0, length, ca, 0);
            final int limit = length * Integer.highestOneBit(c);
            for (int k = length; k < limit; k <<= 1) {
                System.arraycopy(ca, 0, ca, k, k);
            }
            System.arraycopy(ca, 0, ca, limit, fillLen - limit);
            return new String(ca);
        }
    }

    /**
     * Extension: String.prototype.matchAll
     */
    @CompatibilityExtension(CompatibilityOption.StringMatchAll)
    public enum MatchAllFunction {
        ;

        /**
         * String.prototype.matchAll ( regexp )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param regexp
         *            the regular expression object
         * @return the match iterator
         */
        @Function(name = "matchAll", arity = 1)
        public static Object matchAll(ExecutionContext cx, Object thisValue, Object regexp) {
            /* step 1 */
            Object obj = RequireObjectCoercible(cx, thisValue);
            /* steps 2-4 */
            if (!IsRegExp(cx, regexp)) {
                throw newTypeError(cx, Messages.Key.IncompatibleObject);
            }
            /* steps 5-6 */
            String s = ToFlatString(cx, obj);
            /* steps 7-8 */
            String flags = ToFlatString(cx, Get(cx, Type.objectValue(regexp), "flags"));
            /* step 9 */
            if (flags.indexOf('g') == -1) {
                flags = "g" + flags;
            }
            /* step 10 */
            // FIXME: spec bug? - species not handled.
            // FIXME: spec bug - regexp pattern not extracted.
            String pattern = ToFlatString(cx, Get(cx, Type.objectValue(regexp), "source"));
            RegExpObject rx = RegExpCreate(cx, pattern, flags);
            /* steps 11-12 */
            long lastIndex = ToLength(cx, Get(cx, Type.objectValue(regexp), "lastIndex"));
            /* steps 13-14 */
            Set(cx, rx, "lastIndex", lastIndex, true);
            /* step 15 */
            return CreateRegExpStringIterator(cx, rx, s);
        }
    }
}
