/**
 * Copyright (c) 2012-2013 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.compiler;

import static com.github.anba.es6draft.compiler.DefaultCodeGenerator.ToPropertyKey;
import static com.github.anba.es6draft.runtime.AbstractOperations.ToString;
import static com.github.anba.es6draft.semantics.StaticSemantics.BoundNames;
import static com.github.anba.es6draft.semantics.StaticSemantics.PropName;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import com.github.anba.es6draft.ast.*;
import com.github.anba.es6draft.compiler.DefaultCodeGenerator.ValType;
import com.github.anba.es6draft.compiler.InstructionVisitor.FieldDesc;
import com.github.anba.es6draft.compiler.InstructionVisitor.FieldType;
import com.github.anba.es6draft.compiler.InstructionVisitor.MethodDesc;
import com.github.anba.es6draft.compiler.InstructionVisitor.MethodType;
import com.github.anba.es6draft.runtime.LexicalEnvironment;

/**
 * <h1>13 Functions and Generators</h1><br>
 * <h2>13.1 Function Definitions</h2>
 * <ul>
 * <li>Runtime Semantics: Binding Initialisation
 * </ul>
 */
class BindingInitialisationGenerator {
    private static class Fields {
        static final FieldDesc Undefined_UNDEFINED = FieldDesc.create(FieldType.Static,
                Types.Undefined, "UNDEFINED", Types.Undefined);
    }

    private static class Methods {
        // class: AbstractOperations
        static final MethodDesc AbstractOperations_Get = MethodDesc.create(MethodType.Static,
                Types.AbstractOperations, "Get", Type.getMethodType(Types.Object,
                        Types.ExecutionContext, Types.ScriptObject, Types.Object));

        static final MethodDesc AbstractOperations_Get_String = MethodDesc.create(
                MethodType.Static, Types.AbstractOperations, "Get", Type.getMethodType(
                        Types.Object, Types.ExecutionContext, Types.ScriptObject, Types.String));

        static final MethodDesc AbstractOperations_HasProperty = MethodDesc.create(
                MethodType.Static, Types.AbstractOperations, "HasProperty", Type
                        .getMethodType(Type.BOOLEAN_TYPE, Types.ExecutionContext,
                                Types.ScriptObject, Types.Object));

        static final MethodDesc AbstractOperations_HasProperty_String = MethodDesc.create(
                MethodType.Static, Types.AbstractOperations, "HasProperty", Type
                        .getMethodType(Type.BOOLEAN_TYPE, Types.ExecutionContext,
                                Types.ScriptObject, Types.String));

        // class: EnvironmentRecord
        static final MethodDesc EnvironmentRecord_initialiseBinding = MethodDesc.create(
                MethodType.Interface, Types.EnvironmentRecord, "initialiseBinding",
                Type.getMethodType(Type.VOID_TYPE, Types.String, Types.Object));

        // class: ExoticArguments
        static final MethodDesc ExoticArguments_getArgument = MethodDesc.create(MethodType.Virtual,
                Types.ExoticArguments, "getArgument",
                Type.getMethodType(Types.Object, Types.ExecutionContext, Types.String));

        // class: LexicalEnvironment
        static final MethodDesc LexicalEnvironment_getEnvRec = MethodDesc.create(
                MethodType.Virtual, Types.LexicalEnvironment, "getEnvRec",
                Type.getMethodType(Types.EnvironmentRecord));

        // class: Reference
        static final MethodDesc Reference_PutValue = MethodDesc.create(MethodType.Virtual,
                Types.Reference, "PutValue",
                Type.getMethodType(Type.VOID_TYPE, Types.Object, Types.ExecutionContext));

        // class: ScriptRuntime
        static final MethodDesc ScriptRuntime_createRestArray = MethodDesc.create(
                MethodType.Static, Types.ScriptRuntime, "createRestArray", Type.getMethodType(
                        Types.ScriptObject, Types.ScriptObject, Type.INT_TYPE,
                        Types.ExecutionContext));

        static final MethodDesc ScriptRuntime_ensureObject = MethodDesc.create(MethodType.Static,
                Types.ScriptRuntime, "ensureObject",
                Type.getMethodType(Types.ScriptObject, Types.Object, Types.ExecutionContext));

        static final MethodDesc ScriptRuntime_throw = MethodDesc.create(MethodType.Static,
                Types.ScriptRuntime, "_throw",
                Type.getMethodType(Types.ScriptException, Types.Object));

        static final MethodDesc ScriptRuntime_throwTypeErrorKeyNotPresent = MethodDesc.create(
                MethodType.Static, Types.ScriptRuntime, "throwTypeErrorKeyNotPresent",
                Type.getMethodType(Types.ScriptException, Types.Object, Types.ExecutionContext));

        // class: Type
        static final MethodDesc Type_isUndefined = MethodDesc.create(MethodType.Static,
                Types._Type, "isUndefined", Type.getMethodType(Type.BOOLEAN_TYPE, Types.Object));
    }

    private final CodeGenerator codegen;

    BindingInitialisationGenerator(CodeGenerator codegen) {
        this.codegen = codegen;
    }

    void generate(FunctionNode node, ExpressionVisitor mv) {
        BindingInitialisation init = new BindingInitialisation(codegen, mv,
                EnvironmentType.NoEnvironment, true);

        node.getParameters().accept(init, null);
    }

    void generate(Binding node, ExpressionVisitor mv) {
        BindingInitialisation init = new BindingInitialisation(codegen, mv,
                EnvironmentType.NoEnvironment, false);
        node.accept(init, null);
    }

    void generateWithEnvironment(Binding node, ExpressionVisitor mv) {
        BindingInitialisation init = new BindingInitialisation(codegen, mv,
                EnvironmentType.EnvironmentFromStack, false);
        node.accept(init, null);
    }

    private enum EnvironmentType {
        NoEnvironment, EnvironmentFromStack, EnvironmentFromParameter
    }

    private abstract static class RuntimeSemantics<R, V> extends DefaultNodeVisitor<R, V> {
        protected final CodeGenerator codegen;
        protected final ExpressionVisitor mv;
        protected final EnvironmentType environment;
        protected final boolean parameterInitialisation;

        protected RuntimeSemantics(CodeGenerator codegen, ExpressionVisitor mv,
                EnvironmentType environment, boolean parameterInitialisation) {
            this.codegen = codegen;
            this.mv = mv;
            this.environment = environment;
            this.parameterInitialisation = parameterInitialisation;
        }

        protected final void BindingInitialisation(Node node) {
            BindingInitialisation init = new BindingInitialisation(codegen, mv, environment, false);
            node.accept(init, null);
        }

        protected final void IndexedBindingInitialisation(Node node, int index) {
            IndexedBindingInitialisation init = new IndexedBindingInitialisation(codegen, mv,
                    environment, false);
            node.accept(init, index);
        }

        protected final void IndexedBindingInitialisation(FormalParameterList node, int index) {
            IndexedBindingInitialisation init = new IndexedBindingInitialisation(codegen, mv,
                    environment, true);
            node.accept(init, index);
        }

        protected final void KeyedBindingInitialisation(Node node, String key) {
            KeyedBindingInitialisation init = new KeyedBindingInitialisation(codegen, mv,
                    environment, false);
            node.accept(init, key);
        }

        protected final void KeyedBindingInitialisation(Node node, String key,
                boolean parameterInitialisation) {
            KeyedBindingInitialisation init = new KeyedBindingInitialisation(codegen, mv,
                    environment, parameterInitialisation);
            node.accept(init, key);
        }

        protected final void ComputedKeyedBindingInitialisation(Node node, ComputedPropertyName key) {
            ComputedKeyedBindingInitialisation init = new ComputedKeyedBindingInitialisation(
                    codegen, mv, environment, false);
            node.accept(init, key);
        }

        @Override
        protected final R visit(Node node, V value) {
            throw new IllegalStateException();
        }

        protected final ValType expressionValue(Expression node, ExpressionVisitor mv) {
            return codegen.expressionValue(node, mv);
        }

        protected final void dupArgs() {
            if (environment == EnvironmentType.EnvironmentFromStack) {
                mv.dup2();
            } else {
                mv.dup();
            }
        }

        protected final void popArgs() {
            if (environment == EnvironmentType.EnvironmentFromStack) {
                mv.pop2();
            } else {
                mv.pop();
            }
        }
    }

    private static final class BindingInitialisation extends RuntimeSemantics<Void, Void> {
        private static IdentifierResolution identifierResolution = new IdentifierResolution();

        protected BindingInitialisation(CodeGenerator codegen, ExpressionVisitor mv,
                EnvironmentType environment, boolean parameterInitialisation) {
            super(codegen, mv, environment, parameterInitialisation);
        }

        @Override
        public Void visit(FormalParameterList node, Void value) {
            IndexedBindingInitialisation(node, 0);

            return null;
        }

        @Override
        public Void visit(ArrayBindingPattern node, Void _) {
            // step 1: Assert: Type(value) is Object

            // FIXME: spec bug -> internal `Assert` should never occur
            Label assertion = new Label();
            mv.dup();
            mv.instanceOf(Types.ScriptObject);
            mv.ifne(assertion);
            mv.aconst("Type(value) is Object");
            mv.invoke(Methods.ScriptRuntime_throw);
            mv.pop(); // explicit pop required
            mv.mark(assertion);

            // step 2:
            // stack: [(env), value] -> []
            IndexedBindingInitialisation(node, 0);

            return null;
        }

        @Override
        public Void visit(ObjectBindingPattern node, Void _) {
            // step 1: Assert: Type(value) is Object

            // FIXME: spec bug -> internal `Assert` should never occur
            Label assertion = new Label();
            mv.dup();
            mv.instanceOf(Types.ScriptObject);
            mv.ifne(assertion);
            mv.aconst("Type(value) is Object");
            mv.invoke(Methods.ScriptRuntime_throw);
            mv.pop(); // explicit pop required
            mv.mark(assertion);

            // step 2: [...]
            for (BindingProperty property : node.getProperties()) {
                // stack: [(env), value] -> [(env), value, (env), value]
                dupArgs();
                if (property.getPropertyName() == null) {
                    // BindingProperty : SingleNameBinding
                    String name = BoundNames(property.getBinding()).get(0);
                    // stack: [(env), value, (env), value] -> [(env), value]
                    KeyedBindingInitialisation(property, name);
                } else {
                    // BindingProperty : PropertyName : BindingElement
                    String name = PropName(property.getPropertyName());
                    // stack: [(env), value, (env), value] -> [(env), value]
                    if (name != null) {
                        KeyedBindingInitialisation(property, name);
                    } else {
                        PropertyName propertyName = property.getPropertyName();
                        assert propertyName instanceof ComputedPropertyName;
                        ComputedKeyedBindingInitialisation(property,
                                (ComputedPropertyName) propertyName);
                    }
                }
            }
            // stack: [(env), value] -> []
            popArgs();

            return null;
        }

        @Override
        public Void visit(BindingIdentifier node, Void _) {
            if (environment == EnvironmentType.EnvironmentFromParameter) {
                // stack: [value] -> [value, envRec, id]
                assert false : "unused";

                mv.loadParameter(2, LexicalEnvironment.class);
                mv.invoke(Methods.LexicalEnvironment_getEnvRec);
                mv.aconst(node.getName());

                // [value, envRec, id] -> [envRec, id, value]
                mv.dup2X1();
                mv.pop2();

                // stack: [envRec, id, value] -> []
                mv.invoke(Methods.EnvironmentRecord_initialiseBinding);
            } else if (environment == EnvironmentType.EnvironmentFromStack) {
                // stack: [envRec, value] -> [envRec, id, value]
                mv.aconst(node.getName());
                mv.swap();

                // stack: [envRec, id, value] -> []
                mv.invoke(Methods.EnvironmentRecord_initialiseBinding);
            } else {
                assert environment == EnvironmentType.NoEnvironment;
                // stack: [value] -> [ref, value]
                identifierResolution.resolve(node, mv);
                mv.swap();
                // stack: [ref, value] -> []
                mv.loadExecutionContext();
                mv.invoke(Methods.Reference_PutValue);
            }

            return null;
        }
    }

    private static final class IndexedBindingInitialisation extends RuntimeSemantics<Void, Integer> {
        protected IndexedBindingInitialisation(CodeGenerator codegen, ExpressionVisitor mv,
                EnvironmentType environment, boolean parameterInitialisation) {
            super(codegen, mv, environment, parameterInitialisation);
        }

        @Override
        public Void visit(FormalParameterList node, Integer index) {
            assert environment != EnvironmentType.EnvironmentFromStack;

            // stack: [value]
            for (FormalParameter formal : node) {
                // stack: [value] -> [value, value]
                mv.dup();
                // stack: [value, value] -> [value]
                formal.accept(this, index);
                index = index + 1;
            }
            // stack: [value] -> []
            mv.pop();

            return null;
        }

        @Override
        public Void visit(BindingElement node, Integer index) {
            Binding binding = node.getBinding();
            if (binding instanceof BindingIdentifier) {
                // step 1
                // stack: [(env), array] -> []
                KeyedBindingInitialisation(node, ToString(index), parameterInitialisation);
            } else {
                assert binding instanceof BindingPattern;
                Expression initialiser = node.getInitialiser();

                // stack: [(env), array] -> [(env), array, array]
                mv.dup();

                // step 1
                String name = ToString(index);

                if (parameterInitialisation) {
                    mv.loadExecutionContext();
                    mv.aconst(name);
                    mv.invoke(Methods.ExoticArguments_getArgument);
                } else {
                    // steps 2-3
                    // stack: [(env), array, array] -> [(env), array, exists]
                    mv.loadExecutionContext();
                    mv.swap();
                    mv.aconst(name);
                    mv.invoke(Methods.AbstractOperations_HasProperty_String);

                    // steps 4-5
                    // stack: [(env), array, exists] -> [v]
                    Label exists = new Label(), valueLoaded = new Label();
                    mv.ifne(exists);
                    {
                        mv.pop();
                        if (initialiser == null) {
                            mv.aconst(name);
                            mv.loadExecutionContext();
                            mv.invoke(Methods.ScriptRuntime_throwTypeErrorKeyNotPresent);
                            mv.athrow();
                        } else {
                            mv.get(Fields.Undefined_UNDEFINED);
                            mv.goTo(valueLoaded);
                        }
                    }
                    mv.mark(exists);
                    {
                        mv.loadExecutionContext();
                        mv.swap();
                        mv.aconst(name);
                        mv.invoke(Methods.AbstractOperations_Get_String);
                    }
                    mv.mark(valueLoaded);
                }

                // step 6
                // stack: [(env), v] -> [(env), v']
                if (initialiser != null) {
                    Label undef = new Label();
                    mv.dup();
                    mv.invoke(Methods.Type_isUndefined);
                    mv.ifeq(undef);
                    {
                        mv.pop();
                        ValType type = expressionValue(initialiser, mv);
                        mv.toBoxed(type);
                    }
                    mv.mark(undef);
                }

                // stack: [(env), v'] -> [(env), v']
                mv.loadExecutionContext();
                mv.invoke(Methods.ScriptRuntime_ensureObject);

                // step 7
                // stack: [(env), v'] -> []
                BindingInitialisation(binding);
            }
            return null;
        }

        @Override
        public Void visit(BindingRestElement node, Integer index) {
            mv.iconst(index);
            mv.loadExecutionContext();
            // stack: [(env), array, index, cx] -> [(env), rest]
            mv.invoke(Methods.ScriptRuntime_createRestArray);

            // stack: [(env), rest] -> []
            BindingInitialisation(node.getBindingIdentifier());

            return null;
        }

        @Override
        public Void visit(ArrayBindingPattern node, Integer index) {
            for (BindingElementItem element : node.getElements()) {
                if (element instanceof BindingElision) {
                    index = index + 1;
                    continue;
                }

                // stack: [(env), value] -> [(env), value, (env), value]
                dupArgs();
                // stack: [(env), value, (env), value] -> [(env), value]
                element.accept(this, index);
                index = index + 1;
            }
            // stack: [(env), value] -> []
            popArgs();
            return null;
        }
    }

    private static final class KeyedBindingInitialisation extends RuntimeSemantics<Void, String> {
        protected KeyedBindingInitialisation(CodeGenerator codegen, ExpressionVisitor mv,
                EnvironmentType environment, boolean parameterInitialisation) {
            super(codegen, mv, environment, parameterInitialisation);
        }

        @Override
        public Void visit(BindingElement node, String key) {
            generate(node.getBinding(), node.getInitialiser(), key);
            return null;
        }

        @Override
        public Void visit(BindingProperty node, String key) {
            generate(node.getBinding(), node.getInitialiser(), key);
            return null;
        }

        private void generate(Binding binding, Expression initialiser, String propertyName) {
            // stack: [(env), obj] -> [(env), obj, obj]
            mv.dup();

            if (parameterInitialisation) {
                mv.loadExecutionContext();
                mv.aconst(propertyName);
                mv.invoke(Methods.ExoticArguments_getArgument);
            } else {
                // steps 1-2
                // stack: [(env), obj, obj] -> [(env), obj, exists]
                mv.loadExecutionContext();
                mv.swap();
                mv.aconst(propertyName);
                mv.invoke(Methods.AbstractOperations_HasProperty_String);

                // steps 3-4
                // stack: [(env), obj, exists] -> [v]
                Label exists = new Label(), valueLoaded = new Label();
                mv.ifne(exists);
                {
                    mv.pop();
                    if (initialiser == null) {
                        mv.aconst(propertyName);
                        mv.loadExecutionContext();
                        mv.invoke(Methods.ScriptRuntime_throwTypeErrorKeyNotPresent);
                        mv.athrow();
                    } else {
                        mv.get(Fields.Undefined_UNDEFINED);
                        mv.goTo(valueLoaded);
                    }
                }
                mv.mark(exists);
                {
                    mv.loadExecutionContext();
                    mv.swap();
                    mv.aconst(propertyName);
                    mv.invoke(Methods.AbstractOperations_Get_String);
                }
                mv.mark(valueLoaded);
            }

            // step 5
            // stack: [(env), v] -> [(env), v']
            if (initialiser != null) {
                Label undef = new Label();
                mv.dup();
                mv.invoke(Methods.Type_isUndefined);
                mv.ifeq(undef);
                {
                    mv.pop();
                    ValType type = expressionValue(initialiser, mv);
                    mv.toBoxed(type);
                }
                mv.mark(undef);
            }

            if (binding instanceof BindingPattern) {
                // step 6
                // stack: [(env), v'] -> [(env), v']
                mv.loadExecutionContext();
                mv.invoke(Methods.ScriptRuntime_ensureObject);
            }

            // step 7
            // stack: [(env), v'] -> []
            BindingInitialisation(binding);
        }
    }

    private static final class ComputedKeyedBindingInitialisation extends
            RuntimeSemantics<Void, ComputedPropertyName> {
        protected ComputedKeyedBindingInitialisation(CodeGenerator codegen, ExpressionVisitor mv,
                EnvironmentType environment, boolean parameterInitialisation) {
            super(codegen, mv, environment, parameterInitialisation);
        }

        @Override
        public Void visit(BindingProperty node, ComputedPropertyName key) {
            generate(node.getBinding(), node.getInitialiser(), key);
            return null;
        }

        private void generate(Binding binding, Expression initialiser,
                ComputedPropertyName propertyName) {
            // stack: [(env), obj] -> [(env), obj, propertyName]
            // Runtime Semantics: Evaluation
            // ComputedPropertyName : [ AssignmentExpression ]
            ValType propType = expressionValue(propertyName.getExpression(), mv);
            ToPropertyKey(propType, mv);

            // stack: [(env), obj, propertyName] -> [(env), obj, propertyName, obj, propertyName]
            mv.dup2();

            // step 1-2
            // stack: [(env), obj, propertyName, obj, propertyName] -> [(env), obj, propertyName,
            // exists]
            mv.loadExecutionContext();
            mv.dupX2();
            mv.pop();
            mv.invoke(Methods.AbstractOperations_HasProperty);

            // steps 3-4
            // stack: [(env), obj, propertyName, exists] -> [(env), v]
            Label exists = new Label(), valueLoaded = new Label();
            mv.ifne(exists);
            {
                if (initialiser == null) {
                    mv.swap();
                    mv.pop();
                    mv.loadExecutionContext();
                    mv.invoke(Methods.ScriptRuntime_throwTypeErrorKeyNotPresent);
                    mv.athrow();
                } else {
                    mv.pop2();
                    mv.get(Fields.Undefined_UNDEFINED);
                    mv.goTo(valueLoaded);
                }
            }
            mv.mark(exists);
            {
                mv.loadExecutionContext();
                mv.dupX2();
                mv.pop();
                mv.invoke(Methods.AbstractOperations_Get);
            }
            mv.mark(valueLoaded);

            // step 5
            // stack: [(env), v] -> [(env), v']
            if (initialiser != null) {
                Label undef = new Label();
                mv.dup();
                mv.invoke(Methods.Type_isUndefined);
                mv.ifeq(undef);
                {
                    mv.pop();
                    ValType type = expressionValue(initialiser, mv);
                    mv.toBoxed(type);
                }
                mv.mark(undef);
            }

            if (binding instanceof BindingPattern) {
                // step 6
                // stack: [(env), v'] -> [(env), v']
                mv.loadExecutionContext();
                mv.invoke(Methods.ScriptRuntime_ensureObject);
            }

            // step 7
            // stack: [(env), v'] -> []
            BindingInitialisation(binding);
        }
    }
}
