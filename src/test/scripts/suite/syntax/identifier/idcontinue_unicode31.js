/*
 * Copyright (c) 2012-2015 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */

function test(start, end) {
  for (let cp = start; cp <= end;) {
    let source = "var obj = {};\n";
    for (let i = 0; cp <= end && i < 1000; ++cp, ++i) {
      source += `obj.A${String.fromCodePoint(cp)};\n`;
    }
    eval(source);
  }
}

// Delta compared to Unicode 3.0
test(0x03da, 0x03f5);
test(0x16ee, 0x16f0);
test(0x10300, 0x1031e);
test(0x10330, 0x10349);
test(0x1034a, 0x1034a);
test(0x10400, 0x10425);
test(0x10428, 0x1044d);
test(0x1d165, 0x1d166);
test(0x1d167, 0x1d169);
test(0x1d16d, 0x1d172);
test(0x1d17b, 0x1d182);
test(0x1d185, 0x1d18b);
test(0x1d1aa, 0x1d1ad);
test(0x1d400, 0x1d454);
test(0x1d456, 0x1d49c);
test(0x1d49e, 0x1d49f);
test(0x1d4a2, 0x1d4a2);
test(0x1d4a5, 0x1d4a6);
test(0x1d4a9, 0x1d4ac);
test(0x1d4ae, 0x1d4b9);
test(0x1d4bb, 0x1d4bb);
test(0x1d4bd, 0x1d4c0);
test(0x1d4c2, 0x1d4c3);
test(0x1d4c5, 0x1d505);
test(0x1d507, 0x1d50a);
test(0x1d50d, 0x1d514);
test(0x1d516, 0x1d51c);
test(0x1d51e, 0x1d539);
test(0x1d53b, 0x1d53e);
test(0x1d540, 0x1d544);
test(0x1d546, 0x1d546);
test(0x1d54a, 0x1d550);
test(0x1d552, 0x1d6a3);
test(0x1d6a8, 0x1d6c0);
test(0x1d6c2, 0x1d6da);
test(0x1d6dc, 0x1d6fa);
test(0x1d6fc, 0x1d714);
test(0x1d716, 0x1d734);
test(0x1d736, 0x1d74e);
test(0x1d750, 0x1d76e);
test(0x1d770, 0x1d788);
test(0x1d78a, 0x1d7a8);
test(0x1d7aa, 0x1d7c2);
test(0x1d7c4, 0x1d7c9);
test(0x1d7ce, 0x1d7ff);
test(0x20000, 0x2a6d6);
test(0x2f800, 0x2fa1d);
