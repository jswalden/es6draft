/*
 * Copyright (c) 2012-2014 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */

traceur = {
  get(name) {
    if (name !== "./Options") {
      throw new Error("unknown module: " + name);
    }
    return class Options { };
  }
};