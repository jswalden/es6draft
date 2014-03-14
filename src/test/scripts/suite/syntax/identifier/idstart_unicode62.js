/*
 * Copyright (c) 2012-2014 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */

const {
  assertInstanceOf
} = Assert;

const unicode62Supported = (() => {
  try {
    Function(`var \u08A0;`);
    return true;
  } catch (e) {
    assertInstanceOf(SyntaxError, e);
    return false;
  }
})();

function test(start, end) {
  if (!unicode62Supported) return;
  let source = "";
  for (let cp = start; cp <= end; ++cp) {
    source += `obj.${String.fromCodePoint(cp)};\n`;
  }
  Function(source);
}

// test(0x0041, 0x005a);
// test(0x0061, 0x007a);
// test(0x00aa, 0x00aa);
// test(0x00b5, 0x00b5);
// test(0x00ba, 0x00ba);
// test(0x00c0, 0x00d6);
// test(0x00d8, 0x00f6);
// test(0x00f8, 0x01ba);
// test(0x01bb, 0x01bb);
// test(0x01bc, 0x01bf);
// test(0x01c0, 0x01c3);
// test(0x01c4, 0x0293);
// test(0x0294, 0x0294);
// test(0x0295, 0x02af);
// test(0x02b0, 0x02c1);
// test(0x02c6, 0x02d1);
// test(0x02e0, 0x02e4);
// test(0x02ec, 0x02ec);
// test(0x02ee, 0x02ee);
// test(0x0370, 0x0373);
// test(0x0374, 0x0374);
// test(0x0376, 0x0377);
// test(0x037a, 0x037a);
// test(0x037b, 0x037d);
// test(0x0386, 0x0386);
// test(0x0388, 0x038a);
// test(0x038c, 0x038c);
// test(0x038e, 0x03a1);
// test(0x03a3, 0x03f5);
// test(0x03f7, 0x0481);
// test(0x048a, 0x0527);
// test(0x0531, 0x0556);
// test(0x0559, 0x0559);
// test(0x0561, 0x0587);
// test(0x05d0, 0x05ea);
// test(0x05f0, 0x05f2);
// test(0x0620, 0x063f);
// test(0x0640, 0x0640);
// test(0x0641, 0x064a);
// test(0x066e, 0x066f);
// test(0x0671, 0x06d3);
// test(0x06d5, 0x06d5);
// test(0x06e5, 0x06e6);
// test(0x06ee, 0x06ef);
// test(0x06fa, 0x06fc);
// test(0x06ff, 0x06ff);
// test(0x0710, 0x0710);
// test(0x0712, 0x072f);
// test(0x074d, 0x07a5);
// test(0x07b1, 0x07b1);
// test(0x07ca, 0x07ea);
// test(0x07f4, 0x07f5);
// test(0x07fa, 0x07fa);
// test(0x0800, 0x0815);
// test(0x081a, 0x081a);
// test(0x0824, 0x0824);
// test(0x0828, 0x0828);
// test(0x0840, 0x0858);
test(0x08a0, 0x08a0);
test(0x08a2, 0x08ac);
// test(0x0904, 0x0939);
// test(0x093d, 0x093d);
// test(0x0950, 0x0950);
// test(0x0958, 0x0961);
// test(0x0971, 0x0971);
// test(0x0972, 0x0977);
// test(0x0979, 0x097f);
// test(0x0985, 0x098c);
// test(0x098f, 0x0990);
// test(0x0993, 0x09a8);
// test(0x09aa, 0x09b0);
// test(0x09b2, 0x09b2);
// test(0x09b6, 0x09b9);
// test(0x09bd, 0x09bd);
// test(0x09ce, 0x09ce);
// test(0x09dc, 0x09dd);
// test(0x09df, 0x09e1);
// test(0x09f0, 0x09f1);
// test(0x0a05, 0x0a0a);
// test(0x0a0f, 0x0a10);
// test(0x0a13, 0x0a28);
// test(0x0a2a, 0x0a30);
// test(0x0a32, 0x0a33);
// test(0x0a35, 0x0a36);
// test(0x0a38, 0x0a39);
// test(0x0a59, 0x0a5c);
// test(0x0a5e, 0x0a5e);
// test(0x0a72, 0x0a74);
// test(0x0a85, 0x0a8d);
// test(0x0a8f, 0x0a91);
// test(0x0a93, 0x0aa8);
// test(0x0aaa, 0x0ab0);
// test(0x0ab2, 0x0ab3);
// test(0x0ab5, 0x0ab9);
// test(0x0abd, 0x0abd);
// test(0x0ad0, 0x0ad0);
// test(0x0ae0, 0x0ae1);
// test(0x0b05, 0x0b0c);
// test(0x0b0f, 0x0b10);
// test(0x0b13, 0x0b28);
// test(0x0b2a, 0x0b30);
// test(0x0b32, 0x0b33);
// test(0x0b35, 0x0b39);
// test(0x0b3d, 0x0b3d);
// test(0x0b5c, 0x0b5d);
// test(0x0b5f, 0x0b61);
// test(0x0b71, 0x0b71);
// test(0x0b83, 0x0b83);
// test(0x0b85, 0x0b8a);
// test(0x0b8e, 0x0b90);
// test(0x0b92, 0x0b95);
// test(0x0b99, 0x0b9a);
// test(0x0b9c, 0x0b9c);
// test(0x0b9e, 0x0b9f);
// test(0x0ba3, 0x0ba4);
// test(0x0ba8, 0x0baa);
// test(0x0bae, 0x0bb9);
// test(0x0bd0, 0x0bd0);
// test(0x0c05, 0x0c0c);
// test(0x0c0e, 0x0c10);
// test(0x0c12, 0x0c28);
// test(0x0c2a, 0x0c33);
// test(0x0c35, 0x0c39);
// test(0x0c3d, 0x0c3d);
// test(0x0c58, 0x0c59);
// test(0x0c60, 0x0c61);
// test(0x0c85, 0x0c8c);
// test(0x0c8e, 0x0c90);
// test(0x0c92, 0x0ca8);
// test(0x0caa, 0x0cb3);
// test(0x0cb5, 0x0cb9);
// test(0x0cbd, 0x0cbd);
// test(0x0cde, 0x0cde);
// test(0x0ce0, 0x0ce1);
// test(0x0cf1, 0x0cf2);
// test(0x0d05, 0x0d0c);
// test(0x0d0e, 0x0d10);
// test(0x0d12, 0x0d3a);
// test(0x0d3d, 0x0d3d);
// test(0x0d4e, 0x0d4e);
// test(0x0d60, 0x0d61);
// test(0x0d7a, 0x0d7f);
// test(0x0d85, 0x0d96);
// test(0x0d9a, 0x0db1);
// test(0x0db3, 0x0dbb);
// test(0x0dbd, 0x0dbd);
// test(0x0dc0, 0x0dc6);
// test(0x0e01, 0x0e30);
// test(0x0e32, 0x0e33);
// test(0x0e40, 0x0e45);
// test(0x0e46, 0x0e46);
// test(0x0e81, 0x0e82);
// test(0x0e84, 0x0e84);
// test(0x0e87, 0x0e88);
// test(0x0e8a, 0x0e8a);
// test(0x0e8d, 0x0e8d);
// test(0x0e94, 0x0e97);
// test(0x0e99, 0x0e9f);
// test(0x0ea1, 0x0ea3);
// test(0x0ea5, 0x0ea5);
// test(0x0ea7, 0x0ea7);
// test(0x0eaa, 0x0eab);
// test(0x0ead, 0x0eb0);
// test(0x0eb2, 0x0eb3);
// test(0x0ebd, 0x0ebd);
// test(0x0ec0, 0x0ec4);
// test(0x0ec6, 0x0ec6);
test(0x0edc, 0x0edf);
// test(0x0f00, 0x0f00);
// test(0x0f40, 0x0f47);
// test(0x0f49, 0x0f6c);
// test(0x0f88, 0x0f8c);
// test(0x1000, 0x102a);
// test(0x103f, 0x103f);
// test(0x1050, 0x1055);
// test(0x105a, 0x105d);
// test(0x1061, 0x1061);
// test(0x1065, 0x1066);
// test(0x106e, 0x1070);
// test(0x1075, 0x1081);
// test(0x108e, 0x108e);
// test(0x10a0, 0x10c5);
test(0x10c7, 0x10c7);
test(0x10cd, 0x10cd);
// test(0x10d0, 0x10fa);
// test(0x10fc, 0x10fc);
test(0x10fd, 0x1248);
// test(0x124a, 0x124d);
// test(0x1250, 0x1256);
// test(0x1258, 0x1258);
// test(0x125a, 0x125d);
// test(0x1260, 0x1288);
// test(0x128a, 0x128d);
// test(0x1290, 0x12b0);
// test(0x12b2, 0x12b5);
// test(0x12b8, 0x12be);
// test(0x12c0, 0x12c0);
// test(0x12c2, 0x12c5);
// test(0x12c8, 0x12d6);
// test(0x12d8, 0x1310);
// test(0x1312, 0x1315);
// test(0x1318, 0x135a);
// test(0x1380, 0x138f);
// test(0x13a0, 0x13f4);
// test(0x1401, 0x166c);
// test(0x166f, 0x167f);
// test(0x1681, 0x169a);
// test(0x16a0, 0x16ea);
// test(0x16ee, 0x16f0);
// test(0x1700, 0x170c);
// test(0x170e, 0x1711);
// test(0x1720, 0x1731);
// test(0x1740, 0x1751);
// test(0x1760, 0x176c);
// test(0x176e, 0x1770);
// test(0x1780, 0x17b3);
// test(0x17d7, 0x17d7);
// test(0x17dc, 0x17dc);
// test(0x1820, 0x1842);
// test(0x1843, 0x1843);
// test(0x1844, 0x1877);
// test(0x1880, 0x18a8);
// test(0x18aa, 0x18aa);
// test(0x18b0, 0x18f5);
// test(0x1900, 0x191c);
// test(0x1950, 0x196d);
// test(0x1970, 0x1974);
// test(0x1980, 0x19ab);
// test(0x19c1, 0x19c7);
// test(0x1a00, 0x1a16);
// test(0x1a20, 0x1a54);
// test(0x1aa7, 0x1aa7);
// test(0x1b05, 0x1b33);
// test(0x1b45, 0x1b4b);
// test(0x1b83, 0x1ba0);
// test(0x1bae, 0x1baf);
test(0x1bba, 0x1be5);
// test(0x1c00, 0x1c23);
// test(0x1c4d, 0x1c4f);
// test(0x1c5a, 0x1c77);
// test(0x1c78, 0x1c7d);
// test(0x1ce9, 0x1cec);
// test(0x1cee, 0x1cf1);
test(0x1cf5, 0x1cf6);
// test(0x1d00, 0x1d2b);
test(0x1d2c, 0x1d6a);
test(0x1d6b, 0x1d77);
// test(0x1d78, 0x1d78);
// test(0x1d79, 0x1d9a);
// test(0x1d9b, 0x1dbf);
// test(0x1e00, 0x1f15);
// test(0x1f18, 0x1f1d);
// test(0x1f20, 0x1f45);
// test(0x1f48, 0x1f4d);
// test(0x1f50, 0x1f57);
// test(0x1f59, 0x1f59);
// test(0x1f5b, 0x1f5b);
// test(0x1f5d, 0x1f5d);
// test(0x1f5f, 0x1f7d);
// test(0x1f80, 0x1fb4);
// test(0x1fb6, 0x1fbc);
// test(0x1fbe, 0x1fbe);
// test(0x1fc2, 0x1fc4);
// test(0x1fc6, 0x1fcc);
// test(0x1fd0, 0x1fd3);
// test(0x1fd6, 0x1fdb);
// test(0x1fe0, 0x1fec);
// test(0x1ff2, 0x1ff4);
// test(0x1ff6, 0x1ffc);
// test(0x2071, 0x2071);
// test(0x207f, 0x207f);
// test(0x2090, 0x209c);
// test(0x2102, 0x2102);
// test(0x2107, 0x2107);
// test(0x210a, 0x2113);
// test(0x2115, 0x2115);
// test(0x2118, 0x2118);
// test(0x2119, 0x211d);
// test(0x2124, 0x2124);
// test(0x2126, 0x2126);
// test(0x2128, 0x2128);
// test(0x212a, 0x212d);
// test(0x212e, 0x212e);
// test(0x212f, 0x2134);
// test(0x2135, 0x2138);
// test(0x2139, 0x2139);
// test(0x213c, 0x213f);
// test(0x2145, 0x2149);
// test(0x214e, 0x214e);
// test(0x2160, 0x2182);
// test(0x2183, 0x2184);
// test(0x2185, 0x2188);
// test(0x2c00, 0x2c2e);
// test(0x2c30, 0x2c5e);
test(0x2c60, 0x2c7b);
test(0x2c7c, 0x2c7d);
// test(0x2c7e, 0x2ce4);
// test(0x2ceb, 0x2cee);
test(0x2cf2, 0x2cf3);
// test(0x2d00, 0x2d25);
test(0x2d27, 0x2d27);
test(0x2d2d, 0x2d2d);
test(0x2d30, 0x2d67);
// test(0x2d6f, 0x2d6f);
// test(0x2d80, 0x2d96);
// test(0x2da0, 0x2da6);
// test(0x2da8, 0x2dae);
// test(0x2db0, 0x2db6);
// test(0x2db8, 0x2dbe);
// test(0x2dc0, 0x2dc6);
// test(0x2dc8, 0x2dce);
// test(0x2dd0, 0x2dd6);
// test(0x2dd8, 0x2dde);
// test(0x3005, 0x3005);
// test(0x3006, 0x3006);
// test(0x3007, 0x3007);
// test(0x3021, 0x3029);
// test(0x3031, 0x3035);
// test(0x3038, 0x303a);
// test(0x303b, 0x303b);
// test(0x303c, 0x303c);
// test(0x3041, 0x3096);
// test(0x309b, 0x309c);
// test(0x309d, 0x309e);
// test(0x309f, 0x309f);
// test(0x30a1, 0x30fa);
// test(0x30fc, 0x30fe);
// test(0x30ff, 0x30ff);
// test(0x3105, 0x312d);
// test(0x3131, 0x318e);
// test(0x31a0, 0x31ba);
// test(0x31f0, 0x31ff);
// test(0x3400, 0x4db5);
test(0x4e00, 0x9fcc);
// test(0xa000, 0xa014);
// test(0xa015, 0xa015);
// test(0xa016, 0xa48c);
// test(0xa4d0, 0xa4f7);
// test(0xa4f8, 0xa4fd);
// test(0xa500, 0xa60b);
// test(0xa60c, 0xa60c);
// test(0xa610, 0xa61f);
// test(0xa62a, 0xa62b);
// test(0xa640, 0xa66d);
// test(0xa66e, 0xa66e);
// test(0xa67f, 0xa67f);
// test(0xa680, 0xa697);
// test(0xa6a0, 0xa6e5);
// test(0xa6e6, 0xa6ef);
// test(0xa717, 0xa71f);
// test(0xa722, 0xa76f);
// test(0xa770, 0xa770);
// test(0xa771, 0xa787);
// test(0xa788, 0xa788);
// test(0xa78b, 0xa78e);
test(0xa790, 0xa793);
test(0xa7a0, 0xa7aa);
test(0xa7f8, 0xa7f9);
// test(0xa7fa, 0xa7fa);
// test(0xa7fb, 0xa801);
// test(0xa803, 0xa805);
// test(0xa807, 0xa80a);
// test(0xa80c, 0xa822);
// test(0xa840, 0xa873);
// test(0xa882, 0xa8b3);
// test(0xa8f2, 0xa8f7);
// test(0xa8fb, 0xa8fb);
// test(0xa90a, 0xa925);
// test(0xa930, 0xa946);
// test(0xa960, 0xa97c);
// test(0xa984, 0xa9b2);
// test(0xa9cf, 0xa9cf);
// test(0xaa00, 0xaa28);
// test(0xaa40, 0xaa42);
// test(0xaa44, 0xaa4b);
// test(0xaa60, 0xaa6f);
// test(0xaa70, 0xaa70);
// test(0xaa71, 0xaa76);
// test(0xaa7a, 0xaa7a);
// test(0xaa80, 0xaaaf);
// test(0xaab1, 0xaab1);
// test(0xaab5, 0xaab6);
// test(0xaab9, 0xaabd);
// test(0xaac0, 0xaac0);
// test(0xaac2, 0xaac2);
// test(0xaadb, 0xaadc);
// test(0xaadd, 0xaadd);
test(0xaae0, 0xaaea);
test(0xaaf2, 0xaaf2);
test(0xaaf3, 0xaaf4);
// test(0xab01, 0xab06);
// test(0xab09, 0xab0e);
// test(0xab11, 0xab16);
// test(0xab20, 0xab26);
// test(0xab28, 0xab2e);
// test(0xabc0, 0xabe2);
// test(0xac00, 0xd7a3);
// test(0xd7b0, 0xd7c6);
// test(0xd7cb, 0xd7fb);
test(0xf900, 0xfa6d);
// test(0xfa70, 0xfad9);
// test(0xfb00, 0xfb06);
// test(0xfb13, 0xfb17);
// test(0xfb1d, 0xfb1d);
// test(0xfb1f, 0xfb28);
// test(0xfb2a, 0xfb36);
// test(0xfb38, 0xfb3c);
// test(0xfb3e, 0xfb3e);
// test(0xfb40, 0xfb41);
// test(0xfb43, 0xfb44);
// test(0xfb46, 0xfbb1);
// test(0xfbd3, 0xfd3d);
// test(0xfd50, 0xfd8f);
// test(0xfd92, 0xfdc7);
// test(0xfdf0, 0xfdfb);
// test(0xfe70, 0xfe74);
// test(0xfe76, 0xfefc);
// test(0xff21, 0xff3a);
// test(0xff41, 0xff5a);
// test(0xff66, 0xff6f);
// test(0xff70, 0xff70);
// test(0xff71, 0xff9d);
// test(0xff9e, 0xff9f);
// test(0xffa0, 0xffbe);
// test(0xffc2, 0xffc7);
// test(0xffca, 0xffcf);
// test(0xffd2, 0xffd7);
// test(0xffda, 0xffdc);
// test(0x10000, 0x1000b);
// test(0x1000d, 0x10026);
// test(0x10028, 0x1003a);
// test(0x1003c, 0x1003d);
// test(0x1003f, 0x1004d);
// test(0x10050, 0x1005d);
// test(0x10080, 0x100fa);
// test(0x10140, 0x10174);
// test(0x10280, 0x1029c);
// test(0x102a0, 0x102d0);
// test(0x10300, 0x1031e);
// test(0x10330, 0x10340);
// test(0x10341, 0x10341);
// test(0x10342, 0x10349);
// test(0x1034a, 0x1034a);
// test(0x10380, 0x1039d);
// test(0x103a0, 0x103c3);
// test(0x103c8, 0x103cf);
// test(0x103d1, 0x103d5);
// test(0x10400, 0x1044f);
// test(0x10450, 0x1049d);
// test(0x10800, 0x10805);
// test(0x10808, 0x10808);
// test(0x1080a, 0x10835);
// test(0x10837, 0x10838);
// test(0x1083c, 0x1083c);
// test(0x1083f, 0x10855);
// test(0x10900, 0x10915);
// test(0x10920, 0x10939);
test(0x10980, 0x109b7);
test(0x109be, 0x109bf);
// test(0x10a00, 0x10a00);
// test(0x10a10, 0x10a13);
// test(0x10a15, 0x10a17);
// test(0x10a19, 0x10a33);
// test(0x10a60, 0x10a7c);
// test(0x10b00, 0x10b35);
// test(0x10b40, 0x10b55);
// test(0x10b60, 0x10b72);
// test(0x10c00, 0x10c48);
// test(0x11003, 0x11037);
// test(0x11083, 0x110af);
test(0x110d0, 0x110e8);
test(0x11103, 0x11126);
test(0x11183, 0x111b2);
test(0x111c1, 0x111c4);
test(0x11680, 0x116aa);
// test(0x12000, 0x1236e);
// test(0x12400, 0x12462);
// test(0x13000, 0x1342e);
// test(0x16800, 0x16a38);
test(0x16f00, 0x16f44);
test(0x16f50, 0x16f50);
test(0x16f93, 0x16f9f);
// test(0x1b000, 0x1b001);
// test(0x1d400, 0x1d454);
// test(0x1d456, 0x1d49c);
// test(0x1d49e, 0x1d49f);
// test(0x1d4a2, 0x1d4a2);
// test(0x1d4a5, 0x1d4a6);
// test(0x1d4a9, 0x1d4ac);
// test(0x1d4ae, 0x1d4b9);
// test(0x1d4bb, 0x1d4bb);
// test(0x1d4bd, 0x1d4c3);
// test(0x1d4c5, 0x1d505);
// test(0x1d507, 0x1d50a);
// test(0x1d50d, 0x1d514);
// test(0x1d516, 0x1d51c);
// test(0x1d51e, 0x1d539);
// test(0x1d53b, 0x1d53e);
// test(0x1d540, 0x1d544);
// test(0x1d546, 0x1d546);
// test(0x1d54a, 0x1d550);
// test(0x1d552, 0x1d6a5);
// test(0x1d6a8, 0x1d6c0);
// test(0x1d6c2, 0x1d6da);
// test(0x1d6dc, 0x1d6fa);
// test(0x1d6fc, 0x1d714);
// test(0x1d716, 0x1d734);
// test(0x1d736, 0x1d74e);
// test(0x1d750, 0x1d76e);
// test(0x1d770, 0x1d788);
// test(0x1d78a, 0x1d7a8);
// test(0x1d7aa, 0x1d7c2);
// test(0x1d7c4, 0x1d7cb);
test(0x1ee00, 0x1ee03);
test(0x1ee05, 0x1ee1f);
test(0x1ee21, 0x1ee22);
test(0x1ee24, 0x1ee24);
test(0x1ee27, 0x1ee27);
test(0x1ee29, 0x1ee32);
test(0x1ee34, 0x1ee37);
test(0x1ee39, 0x1ee39);
test(0x1ee3b, 0x1ee3b);
test(0x1ee42, 0x1ee42);
test(0x1ee47, 0x1ee47);
test(0x1ee49, 0x1ee49);
test(0x1ee4b, 0x1ee4b);
test(0x1ee4d, 0x1ee4f);
test(0x1ee51, 0x1ee52);
test(0x1ee54, 0x1ee54);
test(0x1ee57, 0x1ee57);
test(0x1ee59, 0x1ee59);
test(0x1ee5b, 0x1ee5b);
test(0x1ee5d, 0x1ee5d);
test(0x1ee5f, 0x1ee5f);
test(0x1ee61, 0x1ee62);
test(0x1ee64, 0x1ee64);
test(0x1ee67, 0x1ee6a);
test(0x1ee6c, 0x1ee72);
test(0x1ee74, 0x1ee77);
test(0x1ee79, 0x1ee7c);
test(0x1ee7e, 0x1ee7e);
test(0x1ee80, 0x1ee89);
test(0x1ee8b, 0x1ee9b);
test(0x1eea1, 0x1eea3);
test(0x1eea5, 0x1eea9);
test(0x1eeab, 0x1eebb);
// test(0x20000, 0x2a6d6);
// test(0x2a700, 0x2b734);
// test(0x2b740, 0x2b81d);
// test(0x2f800, 0x2fa1d);
