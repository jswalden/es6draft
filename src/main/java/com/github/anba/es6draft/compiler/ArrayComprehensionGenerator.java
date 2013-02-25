/**
 * Copyright (c) 2012-2013 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.compiler;

import static com.github.anba.es6draft.semantics.StaticSemantics.BoundNames;

import java.util.Iterator;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import com.github.anba.es6draft.ast.ArrayComprehension;
import com.github.anba.es6draft.ast.ComprehensionFor;
import com.github.anba.es6draft.ast.Expression;
import com.github.anba.es6draft.ast.Node;
import com.github.anba.es6draft.compiler.DefaultCodeGenerator.ValType;
import com.github.anba.es6draft.compiler.InstructionVisitor.MethodDesc;
import com.github.anba.es6draft.compiler.InstructionVisitor.MethodType;
import com.github.anba.es6draft.compiler.ExpressionVisitor.Register;

/**
 * TODO: current draft [rev. 13] does not specify the runtime semantics for array-comprehensions,
 * therefore the translation from
 * http://wiki.ecmascript.org/doku.php?id=harmony:array_comprehensions is used
 */
class ArrayComprehensionGenerator extends DefaultCodeGenerator<ValType, ExpressionVisitor> {
    private static class Methods {
        // class: AbstractOperations
        static final MethodDesc AbstractOperations_CreateArrayFromList = MethodDesc.create(
                MethodType.Static, Types.AbstractOperations, "CreateArrayFromList",
                Type.getMethodType(Types.Scriptable, Types.Realm, Types.List));

        // class: ArrayList
        static final MethodDesc ArrayList_init = MethodDesc.create(MethodType.Special,
                Types.ArrayList, "<init>", Type.getMethodType(Type.VOID_TYPE));

        static final MethodDesc ArrayList_add = MethodDesc.create(MethodType.Virtual,
                Types.ArrayList, "add", Type.getMethodType(Type.BOOLEAN_TYPE, Types.Object));

        // class: EnvironmentRecord
        static final MethodDesc EnvironmentRecord_createMutableBinding = MethodDesc.create(
                MethodType.Interface, Types.EnvironmentRecord, "createMutableBinding",
                Type.getMethodType(Type.VOID_TYPE, Types.String, Type.BOOLEAN_TYPE));

        // class: Iterator
        static final MethodDesc Iterator_hasNext = MethodDesc.create(MethodType.Interface,
                Types.Iterator, "hasNext", Type.getMethodType(Type.BOOLEAN_TYPE));

        static final MethodDesc Iterator_next = MethodDesc.create(MethodType.Interface,
                Types.Iterator, "next", Type.getMethodType(Types.Object));

        // class: LexicalEnvironment
        static final MethodDesc LexicalEnvironment_getEnvRec = MethodDesc.create(
                MethodType.Virtual, Types.LexicalEnvironment, "getEnvRec",
                Type.getMethodType(Types.EnvironmentRecord));

        // class: ScriptRuntime
        static final MethodDesc ScriptRuntime_iterate = MethodDesc.create(MethodType.Static,
                Types.ScriptRuntime, "iterate",
                Type.getMethodType(Types.Iterator, Types.Object, Types.Realm));
    }

    ArrayComprehensionGenerator(CodeGenerator codegen) {
        super(codegen);
    }

    @Override
    protected ValType visit(Node node, ExpressionVisitor mv) {
        throw new IllegalStateException(String.format("node-class: %s", node.getClass()));
    }

    @Override
    public ValType visit(ArrayComprehension node, ExpressionVisitor mv) {
        int result = mv.newVariable(Types.ArrayList);
        mv.anew(Types.ArrayList);
        mv.dup();
        mv.invoke(Methods.ArrayList_init);
        mv.store(result, Types.ArrayList);

        visitArrayComprehension(node, result, node.getList().iterator(), mv);

        mv.load(Register.Realm);
        mv.load(result, Types.ArrayList);
        mv.invoke(Methods.AbstractOperations_CreateArrayFromList);
        mv.freeVariable(result);

        return ValType.Object;
    }

    private ValType expression(Expression node, ExpressionVisitor mv) {
        return codegen.expression(node, mv);
    }

    private void visitArrayComprehension(ArrayComprehension node, int result, ExpressionVisitor mv) {
        Label l0 = null;
        if (node.getTest() != null) {
            l0 = new Label();
            ValType type = expression(node.getTest(), mv);
            invokeGetValue(node.getTest(), mv);
            ToBoolean(type, mv);
            mv.ifeq(l0);
        }

        ValType type = expression(node.getExpression(), mv);
        mv.toBoxed(type);
        invokeGetValue(node.getExpression(), mv);
        mv.load(result, Types.ArrayList);
        mv.swap();
        mv.invoke(Methods.ArrayList_add);
        mv.pop();

        if (node.getTest() != null) {
            mv.mark(l0);
        }
    }

    private void visitArrayComprehension(ArrayComprehension comprehension, int result,
            Iterator<ComprehensionFor> iterator, ExpressionVisitor mv) {
        Label lblContinue = new Label(), lblBreak = new Label();
        Label loopstart = new Label();

        assert iterator.hasNext();
        ComprehensionFor comprehensionFor = iterator.next();

        ValType type = expression(comprehensionFor.getExpression(), mv);
        mv.toBoxed(type);
        invokeGetValue(comprehensionFor.getExpression(), mv);

        // FIXME: translation into for-of per
        // http://wiki.ecmascript.org/doku.php?id=harmony:array_comprehensions means adding
        // additional isUndefinedOrNull() check, but Spidermonkey reports an error in this case!

        mv.dup();
        isUndefinedOrNull(mv);
        mv.ifeq(loopstart);
        mv.pop();
        mv.goTo(lblBreak);
        mv.mark(loopstart);
        mv.load(Register.Realm);
        mv.invoke(Methods.ScriptRuntime_iterate);

        int var = mv.newVariable(Types.Iterator);
        mv.store(var, Types.Iterator);

        mv.mark(lblContinue);
        mv.load(var, Types.Iterator);
        mv.invoke(Methods.Iterator_hasNext);
        mv.ifeq(lblBreak);
        mv.load(var, Types.Iterator);
        mv.invoke(Methods.Iterator_next);

        // FIXME: translation into for-of per
        // http://wiki.ecmascript.org/doku.php?id=harmony:array_comprehensions means using a fresh
        // lexical/declarative environment for each inner loop, but Spidermonkey creates a single
        // environment for the whole array comprehension

        // create new declarative lexical environment
        // stack: [nextValue] -> [nextValue, iterEnv]
        mv.enterScope(comprehensionFor);
        newDeclarativeEnvironment(mv);
        {
            // stack: [nextValue, iterEnv] -> [iterEnv, nextValue, envRec]
            mv.dupX1();
            mv.invoke(Methods.LexicalEnvironment_getEnvRec);

            // stack: [iterEnv, nextValue, envRec] -> [iterEnv, envRec, nextValue]
            for (String name : BoundNames(comprehensionFor.getBinding())) {
                mv.dup();
                mv.aconst(name);
                mv.iconst(false);
                mv.invoke(Methods.EnvironmentRecord_createMutableBinding);
            }
            mv.swap();

            // FIXME: spec bug (missing ToObject() call?)

            // stack: [iterEnv, envRec, nextValue] -> [iterEnv]
            BindingInitialisationWithEnvironment(comprehensionFor.getBinding(), mv);
        }
        // stack: [iterEnv] -> []
        pushLexicalEnvironment(mv);

        if (iterator.hasNext()) {
            visitArrayComprehension(comprehension, result, iterator, mv);
        } else {
            visitArrayComprehension(comprehension, result, mv);
        }

        // restore previous lexical environment
        popLexicalEnvironment(mv);
        mv.exitScope();

        mv.goTo(lblContinue);
        mv.mark(lblBreak);
        mv.freeVariable(var);
    }
}
