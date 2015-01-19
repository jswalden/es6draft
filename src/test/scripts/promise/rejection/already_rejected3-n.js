/*
 * Copyright (c) 2012-2015 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */

{
  let p1 = Promise.reject(new Error("rejected promise"));
  let p2 = p1.then(() => {});
  let p3 = p2.then(() => {});
}

triggerGC();