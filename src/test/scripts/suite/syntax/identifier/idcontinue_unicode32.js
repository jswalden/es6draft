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

// Delta compared to Unicode 3.1
test(0x01c4, 0x0220);
test(0x0300, 0x034f);
test(0x0360, 0x036f);
test(0x03d0, 0x03f5);
test(0x048a, 0x04ce);
test(0x0500, 0x050f);
test(0x066e, 0x066f);
test(0x07b1, 0x07b1);
test(0x10d0, 0x10f8);
test(0x1700, 0x170c);
test(0x170e, 0x1711);
test(0x1712, 0x1714);
test(0x1720, 0x1731);
test(0x1732, 0x1734);
test(0x1740, 0x1751);
test(0x1752, 0x1753);
test(0x1760, 0x176c);
test(0x176e, 0x1770);
test(0x1772, 0x1773);
test(0x17d7, 0x17d7);
test(0x17dc, 0x17dc);
test(0x180b, 0x180d);
test(0x2071, 0x2071);
test(0x20e5, 0x20ea);
test(0x213d, 0x213f);
test(0x2145, 0x2149);
test(0x303b, 0x303b);
test(0x303c, 0x303c);
test(0x3041, 0x3096);
test(0x309f, 0x309f);
test(0x30ff, 0x30ff);
test(0x31f0, 0x31ff);
test(0xfa30, 0xfa6a);
test(0xfe00, 0xfe0f);
test(0xfe70, 0xfe74);
