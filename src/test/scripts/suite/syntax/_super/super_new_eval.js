/*
 * Copyright (c) 2012-2016 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
const {
  assertThrows, assertSyntaxError, assertInstanceOf, fail
} = Assert;

System.load("lib/promises.jsm");
const {
  reportFailure
} = System.get("lib/promises.jsm");


function SuperConstructor() { }

// 14.1 FunctionDeclaration
function fdecl() {
  eval("new super()");
}
Object.setPrototypeOf(fdecl, SuperConstructor);
assertThrows(ReferenceError, () => fdecl());
new fdecl();

// 14.1 FunctionExpression
var fexpr = function() {
  eval("new super()");
};
Object.setPrototypeOf(fexpr, SuperConstructor);
assertThrows(ReferenceError, () => fexpr());
new fexpr();

// 14.3 Method Definitions [Method]
var obj = {
  m() {
    eval("new super()");
  }
};
Object.setPrototypeOf(obj.m, SuperConstructor);
assertThrows(ReferenceError, () => obj.m());
assertThrows(TypeError, () => new obj.m());

var obj = class {
  m() {
    eval("new super()");
  }
};
Object.setPrototypeOf(obj.prototype.m, SuperConstructor);
assertThrows(ReferenceError, () => obj.prototype.m());
assertThrows(TypeError, () => new obj.prototype.m());

var obj = class {
  static m() {
    eval("new super()");
  }
};
Object.setPrototypeOf(obj.m, SuperConstructor);
assertThrows(ReferenceError, () => obj.m());
assertThrows(TypeError, () => new obj.m());

// 14.3 Method Definitions [ConstructorMethod]
var obj = class {
  constructor() {
    eval("new super()");
  }
};
Object.setPrototypeOf(obj, SuperConstructor);
assertThrows(TypeError, () => obj());
new obj();

var obj = class extends class {} {
  constructor() {
    super();
    eval("new super()");
  }
};
assertThrows(TypeError, () => obj());
new obj();

// 14.3 Method Definitions [Getter]
var obj = {
  get x() {
    eval("new super()");
  }
};
Object.setPrototypeOf(Object.getOwnPropertyDescriptor(obj, "x").get, SuperConstructor);
assertThrows(ReferenceError, () => Object.getOwnPropertyDescriptor(obj, "x").get());
assertThrows(TypeError, () => new (Object.getOwnPropertyDescriptor(obj, "x").get)());

var obj = class {
  get x() {
    eval("new super()");
  }
};
Object.setPrototypeOf(Object.getOwnPropertyDescriptor(obj.prototype, "x").get, SuperConstructor);
assertThrows(ReferenceError, () => Object.getOwnPropertyDescriptor(obj.prototype, "x").get());
assertThrows(TypeError, () => new (Object.getOwnPropertyDescriptor(obj.prototype, "x").get)());

var obj = class {
  static get x() {
    eval("new super()");
  }
};
Object.setPrototypeOf(Object.getOwnPropertyDescriptor(obj, "x").get, SuperConstructor);
assertThrows(ReferenceError, () => Object.getOwnPropertyDescriptor(obj, "x").get());
assertThrows(TypeError, () => new (Object.getOwnPropertyDescriptor(obj, "x").get)());

// 14.3 Method Definitions [Setter]
var obj = {
  set x(_) {
    eval("new super()");
  }
};
Object.setPrototypeOf(Object.getOwnPropertyDescriptor(obj, "x").set, SuperConstructor);
assertThrows(ReferenceError, () => Object.getOwnPropertyDescriptor(obj, "x").set());
assertThrows(TypeError, () => new (Object.getOwnPropertyDescriptor(obj, "x").set)());

var obj = class {
  set x(_) {
    eval("new super()");
  }
};
Object.setPrototypeOf(Object.getOwnPropertyDescriptor(obj.prototype, "x").set, SuperConstructor);
assertThrows(ReferenceError, () => Object.getOwnPropertyDescriptor(obj.prototype, "x").set());
assertThrows(TypeError, () => new (Object.getOwnPropertyDescriptor(obj.prototype, "x").set)());

var obj = class {
  static set x(_) {
    eval("new super()");
  }
};
Object.setPrototypeOf(Object.getOwnPropertyDescriptor(obj, "x").set, SuperConstructor);
assertThrows(ReferenceError, () => Object.getOwnPropertyDescriptor(obj, "x").set());
assertThrows(TypeError, () => new (Object.getOwnPropertyDescriptor(obj, "x").set)());

// 14.4 GeneratorDeclaration
function* gdecl() {
  eval("new super()");
}
Object.setPrototypeOf(gdecl, SuperConstructor);
assertThrows(ReferenceError, () => gdecl().next());
assertThrows(TypeError, () => new gdecl());

// 14.4 GeneratorExpression
var gexpr = function*() {
  eval("new super()");
};
Object.setPrototypeOf(gexpr, SuperConstructor);
assertThrows(ReferenceError, () => gexpr().next());
assertThrows(TypeError, () => new gexpr());

// 14.4 GeneratorMethod
var obj = {
  *m() {
    eval("new super()");
  }
};
Object.setPrototypeOf(obj.m, SuperConstructor);
assertThrows(ReferenceError, () => obj.m().next());
assertThrows(TypeError, () => new obj.m());

var obj = class {
  *m() {
    eval("new super()");
  }
};
Object.setPrototypeOf(obj.prototype.m, SuperConstructor);
assertThrows(ReferenceError, () => obj.prototype.m().next());
assertThrows(TypeError, () => new obj.prototype.m());

var obj = class {
  static *m() {
    eval("new super()");
  }
};
Object.setPrototypeOf(obj.m, SuperConstructor);
assertThrows(ReferenceError, () => obj.m().next());
assertThrows(TypeError, () => new obj.m());

// 15.1 Scripts
assertThrows(SyntaxError, () => evalScript(`
  eval("new super()");
`));

// 15.2 Modules
System
.define("parse-new-super-eval", `
  eval("new super()");
`)
.then(() => fail `no SyntaxError`, e => assertInstanceOf(SyntaxError, e))
.catch(reportFailure);
