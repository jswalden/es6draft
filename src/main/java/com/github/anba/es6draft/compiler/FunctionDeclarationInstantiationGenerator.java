/**
 * Copyright (c) 2012-2013 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.compiler;

import static com.github.anba.es6draft.semantics.StaticSemantics.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Type;

import com.github.anba.es6draft.ast.BindingElement;
import com.github.anba.es6draft.ast.BindingIdentifier;
import com.github.anba.es6draft.ast.Declaration;
import com.github.anba.es6draft.ast.FormalParameter;
import com.github.anba.es6draft.ast.FormalParameterList;
import com.github.anba.es6draft.ast.FunctionDeclaration;
import com.github.anba.es6draft.ast.FunctionNode;
import com.github.anba.es6draft.ast.GeneratorDeclaration;
import com.github.anba.es6draft.ast.StatementListItem;
import com.github.anba.es6draft.compiler.CodeGenerator.FunctionName;
import com.github.anba.es6draft.compiler.InstructionVisitor.FieldDesc;
import com.github.anba.es6draft.compiler.InstructionVisitor.FieldType;
import com.github.anba.es6draft.compiler.InstructionVisitor.MethodDesc;
import com.github.anba.es6draft.compiler.InstructionVisitor.MethodType;
import com.github.anba.es6draft.compiler.InstructionVisitor.TypedVariable;
import com.github.anba.es6draft.compiler.InstructionVisitor.Variable;
import com.github.anba.es6draft.runtime.EnvironmentRecord;
import com.github.anba.es6draft.runtime.ExecutionContext;
import com.github.anba.es6draft.runtime.LexicalEnvironment;
import com.github.anba.es6draft.runtime.types.Undefined;

/**
 * <h1>10 Executable Code and Execution Contexts</h1><br>
 * <h2>10.5 Declaration Binding Instantiation</h2>
 * <ul>
 * <li>10.5.3 Function Declaration Instantiation
 * </ul>
 */
class FunctionDeclarationInstantiationGenerator extends DeclarationBindingInstantiationGenerator {
    private static class Fields {
        static final FieldDesc Undefined_UNDEFINED = FieldDesc.create(FieldType.Static,
                Types.Undefined, "UNDEFINED", Types.Undefined);
    }

    private static class Methods {
        // class: ExecutionContext
        static final MethodDesc ExecutionContext_getVariableEnvironment = MethodDesc.create(
                MethodType.Virtual, Types.ExecutionContext, "getVariableEnvironment",
                Type.getMethodType(Types.LexicalEnvironment));

        // class: ExoticArguments
        static final MethodDesc ExoticArguments_InstantiateArgumentsObject = MethodDesc.create(
                MethodType.Static, Types.ExoticArguments, "InstantiateArgumentsObject",
                Type.getMethodType(Types.ExoticArguments, Types.ExecutionContext, Types.Object_));

        static final MethodDesc ExoticArguments_CompleteStrictArgumentsObject = MethodDesc.create(
                MethodType.Static, Types.ExoticArguments, "CompleteStrictArgumentsObject",
                Type.getMethodType(Type.VOID_TYPE, Types.ExecutionContext, Types.ExoticArguments));

        static final MethodDesc ExoticArguments_CompleteMappedArgumentsObject = MethodDesc.create(
                MethodType.Static, Types.ExoticArguments, "CompleteMappedArgumentsObject", Type
                        .getMethodType(Type.VOID_TYPE, Types.ExecutionContext,
                                Types.ExoticArguments, Types.FunctionObject, Types.String_,
                                Types.LexicalEnvironment));

        // class: LexicalEnvironment
        static final MethodDesc LexicalEnvironment_getEnvRec = MethodDesc.create(
                MethodType.Virtual, Types.LexicalEnvironment, "getEnvRec",
                Type.getMethodType(Types.EnvironmentRecord));
    }

    private static class FunctionDeclInitMethodGenerator extends ExpressionVisitor {
        static final Type methodDescriptor = Type.getMethodType(Types.ExoticArguments,
                Types.ExecutionContext, Types.FunctionObject, Types.Object_);

        FunctionDeclInitMethodGenerator(CodeGenerator codegen, String methodName, boolean strict) {
            super(codegen.publicStaticMethod(methodName, methodDescriptor.getInternalName()),
                    methodName, methodDescriptor, strict, false);
        }
    }

    private static final int EXECUTION_CONTEXT = 0;
    private static final int FUNCTION = 1;
    private static final int ARGUMENTS = 2;

    FunctionDeclarationInstantiationGenerator(CodeGenerator codegen) {
        super(codegen);
    }

    void generate(FunctionNode func) {
        String methodName = codegen.methodName(func, FunctionName.Init);
        ExpressionVisitor mv = new FunctionDeclInitMethodGenerator(codegen, methodName,
                IsStrict(func));

        mv.lineInfo(func);
        mv.begin();
        mv.enterScope(func);
        generate(func, mv);
        mv.exitScope();
        mv.end();
    }

    private void generate(FunctionNode func, ExpressionVisitor mv) {
        TypedVariable<ExecutionContext> context = mv.getParameter(EXECUTION_CONTEXT,
                ExecutionContext.class);

        TypedVariable<LexicalEnvironment> env = mv.newVariable("env", LexicalEnvironment.class);
        mv.loadExecutionContext();
        mv.invoke(Methods.ExecutionContext_getVariableEnvironment);
        mv.store(env);

        TypedVariable<EnvironmentRecord> envRec = mv.newVariable("envRec", EnvironmentRecord.class);
        mv.load(env);
        mv.invoke(Methods.LexicalEnvironment_getEnvRec);
        mv.store(envRec);

        TypedVariable<Undefined> undef = mv.newVariable("undef", Undefined.class);
        mv.get(Fields.Undefined_UNDEFINED);
        mv.store(undef);

        Set<String> bindings = new HashSet<>();
        /* [10.5.3] step 1 */
        // RuntimeInfo.Code code = func.getCode();
        /* [10.5.3] step 2 */
        boolean strict = IsStrict(func);
        /* [10.5.3] step 3 */
        FormalParameterList formals = func.getParameters();
        /* [10.5.3] step 4 */
        List<String> parameterNames = BoundNames(formals);
        /* [10.5.3] step 5 */
        List<StatementListItem> varDeclarations = VarScopedDeclarations(func);
        /* [10.5.3] step 6 */
        List<Declaration> functionsToInitialize = new ArrayList<>();
        /* [10.5.3] step 7 */
        boolean argumentsObjectNotNeeded = false;
        /* [10.5.3] step 8 */
        for (StatementListItem item : reverse(varDeclarations)) {
            if (item instanceof FunctionDeclaration) {
                FunctionDeclaration d = (FunctionDeclaration) item;
                String fn = BoundName(d);
                if ("arguments".equals(fn)) {
                    argumentsObjectNotNeeded = true;
                }
                boolean alreadyDeclared = bindings.contains(fn);
                if (!alreadyDeclared) {
                    bindings.add(fn);
                    functionsToInitialize.add(d);
                    createMutableBinding(envRec, fn, false, mv);
                }
            }
        }
        /* [10.5.3] step 9 */
        for (String paramName : parameterNames) {
            boolean alreadyDeclared = bindings.contains(paramName);
            if (!alreadyDeclared) {
                if ("arguments".equals(paramName)) {
                    argumentsObjectNotNeeded = true;
                }
                bindings.add(paramName);
                createMutableBinding(envRec, paramName, false, mv);
                // stack: [undefined] -> []
                mv.load(undef);
                initialiseBinding(envRec, paramName, mv);
            }
        }
        /* [10.5.3] step 10-11 */
        if (!argumentsObjectNotNeeded) {
            bindings.add("arguments");
            if (strict) {
                createImmutableBinding(envRec, "arguments", mv);
            } else {
                createMutableBinding(envRec, "arguments", false, mv);
            }
        }
        /* [10.5.3] step 12 */
        Set<String> varNames = VarDeclaredNames(func);
        /* [10.5.3] step 13 */
        for (String varName : varNames) {
            boolean alreadyDeclared = bindings.contains(varName);
            if (!alreadyDeclared) {
                bindings.add(varName);
                createMutableBinding(envRec, varName, false, mv);
                // FIXME: spec bug (partially reported in Bug 1420)
                mv.load(undef);
                initialiseBinding(envRec, varName, mv);
            }
        }
        /* [10.5.3] step 14 */
        List<Declaration> lexDeclarations = LexicallyScopedDeclarations(func);
        /* [10.5.3] step 15 */
        for (Declaration d : lexDeclarations) {
            for (String dn : BoundNames(d)) {
                if (d.isConstDeclaration()) {
                    createImmutableBinding(envRec, dn, mv);
                } else {
                    createMutableBinding(envRec, dn, false, mv);
                }
            }
            if (d instanceof GeneratorDeclaration) {
                functionsToInitialize.add(d);
            }
        }
        /* [10.5.3] step 16 */
        for (Declaration f : functionsToInitialize) {
            String fn = BoundName(f);
            // stack: [] -> [fo]
            if (f instanceof GeneratorDeclaration) {
                InstantiateGeneratorObject(context, env, (GeneratorDeclaration) f, mv);
            } else {
                InstantiateFunctionObject(context, env, (FunctionDeclaration) f, mv);
            }
            // stack: [fo] -> []
            // setMutableBinding(envRec, fn, false, mv);
            initialiseBinding(envRec, fn, mv);
        }
        /* [10.5.3] step 17-19 */
        // stack: [] -> [ao]
        InstantiateArgumentsObject(mv);
        /* [10.5.3] step 20-21 */
        BindingInitialisation(func, mv);
        /* [10.5.3] step 22 */
        if (!argumentsObjectNotNeeded) {
            if (strict) {
                CompleteStrictArgumentsObject(mv);
            } else {
                CompleteMappedArgumentsObject(env, formals, mv);
            }
            // stack: [ao] -> [ao]
            mv.dup();
            initialiseBinding(envRec, "arguments", mv);
        }
        /* [10.5.3] step 23 */
        // stack: [ao] -> []
        mv.areturn();
    }

    private void InstantiateArgumentsObject(ExpressionVisitor mv) {
        mv.loadExecutionContext();
        mv.load(ARGUMENTS, Types.Object_);
        mv.invoke(Methods.ExoticArguments_InstantiateArgumentsObject);
    }

    private void BindingInitialisation(FunctionNode node, ExpressionVisitor mv) {
        // stack: [ao] -> [ao]
        mv.dup();
        new BindingInitialisationGenerator(codegen).generate(node, mv);
    }

    private void CompleteStrictArgumentsObject(ExpressionVisitor mv) {
        // stack: [ao] -> [ao]
        mv.dup();
        mv.loadExecutionContext();
        mv.swap();
        mv.invoke(Methods.ExoticArguments_CompleteStrictArgumentsObject);
    }

    private void CompleteMappedArgumentsObject(Variable env, FormalParameterList formals,
            ExpressionVisitor mv) {
        // stack: [ao] -> [ao]
        mv.dup();
        mv.loadExecutionContext();
        mv.swap();
        mv.load(FUNCTION, Types.FunctionObject);
        astore_string(mv, mappedNames(formals));
        mv.load(env);
        mv.invoke(Methods.ExoticArguments_CompleteMappedArgumentsObject);
    }

    private String[] mappedNames(FormalParameterList formals) {
        List<FormalParameter> list = formals.getFormals();
        int numberOfNonRestFormals = NumberOfParameters(formals);
        assert numberOfNonRestFormals <= list.size();

        Set<String> mappedNames = new HashSet<>();
        String[] names = new String[numberOfNonRestFormals];
        for (int index = numberOfNonRestFormals - 1; index >= 0; --index) {
            assert list.get(index) instanceof BindingElement;
            BindingElement formal = (BindingElement) list.get(index);
            if (formal.getBinding() instanceof BindingIdentifier) {
                String name = ((BindingIdentifier) formal.getBinding()).getName();
                if (!mappedNames.contains(name)) {
                    mappedNames.add(name);
                    names[index] = name;
                }
            }
        }
        return names;
    }

    private void astore_string(InstructionVisitor mv, String[] strings) {
        mv.newarray(strings.length, Types.String);
        int index = 0;
        for (String string : strings) {
            mv.astore(index++, string, Types.String);
        }
    }
}
