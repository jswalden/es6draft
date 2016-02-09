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

// Delta compared to Unicode 3.2
test(0x01c4, 0x0236);
test(0x0250, 0x02af);
test(0x02b0, 0x02c1);
test(0x02c6, 0x02d1);
test(0x0300, 0x0357);
test(0x035d, 0x036f);
test(0x03f7, 0x03fb);
test(0x0610, 0x0615);
test(0x064b, 0x0658);
test(0x06ee, 0x06ef);
test(0x06ff, 0x06ff);
test(0x0712, 0x072f);
test(0x074d, 0x074f);
test(0x0904, 0x0939);
test(0x09bd, 0x09bd);
test(0x0a01, 0x0a02);
test(0x0a03, 0x0a03);
test(0x0a85, 0x0a8d);
test(0x0ae0, 0x0ae1);
test(0x0ae2, 0x0ae3);
test(0x0b35, 0x0b39);
test(0x0b71, 0x0b71);
test(0x0cbc, 0x0cbc);
test(0x0cbd, 0x0cbd);
test(0x17b6, 0x17b6);
test(0x17dd, 0x17dd);
test(0x1900, 0x191c);
test(0x1920, 0x1922);
test(0x1923, 0x1926);
test(0x1927, 0x1928);
test(0x1929, 0x192b);
test(0x1930, 0x1931);
test(0x1932, 0x1932);
test(0x1933, 0x1938);
test(0x1939, 0x193b);
test(0x1946, 0x194f);
test(0x1950, 0x196d);
test(0x1970, 0x1974);
test(0x1d00, 0x1d2b);
test(0x1d2c, 0x1d61);
test(0x1d62, 0x1d6b);
test(0x2054, 0x2054);
test(0x2118, 0x2118);
test(0x212e, 0x212e);
test(0x309b, 0x309c);
test(0x10000, 0x1000b);
test(0x1000d, 0x10026);
test(0x10028, 0x1003a);
test(0x1003c, 0x1003d);
test(0x1003f, 0x1004d);
test(0x10050, 0x1005d);
test(0x10080, 0x100fa);
test(0x10380, 0x1039d);
test(0x10400, 0x1044f);
test(0x10450, 0x1049d);
test(0x104a0, 0x104a9);
test(0x10800, 0x10805);
test(0x10808, 0x10808);
test(0x1080a, 0x10835);
test(0x10837, 0x10838);
test(0x1083c, 0x1083c);
test(0x1083f, 0x1083f);
test(0x1d4bd, 0x1d4c3);
test(0xe0100, 0xe01ef);
