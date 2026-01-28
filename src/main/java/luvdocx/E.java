package luvdocx;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import static luvdocx.A.*;

public class E {
    private static final ObjectFactory $ = Context.getWmlObjectFactory();

    public static P w_p(Object... contents) {
        var p = new P(); //$.createP();
        for (var c : contents) {
            switch (c) {
                case PPr pPr -> p.setPPr(pPr);
                case R run -> p.getContent().add(run);
                case String text -> p.getContent().add(w_r(w_t(text)));
                case WFrags wf -> p.getContent().addAll(wf.items());
                case A attr -> handlePAttr(p, attr);
                default -> err("w:p", c);
            }
        }
        return p;
    }

    public static R w_r(Object... contents) {
        var r = $.createR();
        for (var c : contents) {
            switch (c) {
                case RPr rPr -> r.setRPr(rPr);
                case Text text -> r.getContent().add(text);
                case Drawing drawing -> r.getContent().add(drawing);
                case Br br -> r.getContent().add(br);
                case String str -> r.getContent().add(w_t(str));
                case WFrags wf -> r.getContent().addAll(wf.items());
                case A attr -> handleRAttr(r, attr);
                default -> err("w:r", c);
            }
        }
        return r;
    }

    public static Text w_t(String text) {
        var t = $.createText();
        t.setValue(text);
        t.setSpace("preserve");
        return t;
    }

    public static Br w_br(String type) {
        var br = $.createBr();
        if ("page".equals(type)) {
            br.setType(STBrType.PAGE);
        }
        return br;
    }

    public static PPr w_pPr(Object... props) {
        var pPr = $.createPPr();
        for (var prop : props) {
            switch (prop) {
                case PPrBase.Spacing spacing -> pPr.setSpacing(spacing);
                case CTShd shd -> pPr.setShd(shd);
                case PPrBase.Ind ind -> pPr.setInd(ind);
                case PPrBase.PBdr pBdr -> pPr.setPBdr(pBdr);
                case PPrBase.PStyle pStyle -> pPr.setPStyle(pStyle);
                case A attr -> handlePPrAttr(pPr, attr);
                default -> err("w:pPr", prop);
            }
        }
        return pPr;
    }

    public static PPrBase.Spacing w_spacing(Object... attrs) {
        var spacing = $.createPPrBaseSpacing();
        for (var attr : attrs) {
            if (attr instanceof A(String name, String value)) {
                switch (name) {
                    case "before" -> spacing.setBefore(parseBigInt(value));
                    case "after" -> spacing.setAfter(parseBigInt(value));
                    default -> throw new UnsupportedOperationException("Unknown spacing attr: " + name);
                }
            }
        }
        return spacing;
    }

    public static CTShd w_shd(Object... attrs) {
        var shd = $.createCTShd();
        for (var attr : attrs) {
            if (attr instanceof A(String name, String value)) {
                switch (name) {
                    case "fill" -> shd.setFill(value);
                    default -> throw new UnsupportedOperationException("Unknown shd attr: " + name);
                }
            }
        }
        return shd;
    }

    public static PPrBase.Ind w_ind(Object... attrs) {
        var ind = $.createPPrBaseInd();
        for (var attr : attrs) {
            if (attr instanceof A(String name, String value)) {
                switch (name) {
                    case "left" -> ind.setLeft(parseBigInt(value));
                    case "right" -> ind.setRight(parseBigInt(value));
                    default -> throw new UnsupportedOperationException("Unknown ind attr: " + name);
                }
            }
        }
        return ind;
    }

    public static PPrBase.PBdr w_pBdr(Object... borders) {
        var pBdr = $.createPPrBasePBdr();
        for (var b : borders) {
            if (b instanceof A(String name, String value)) {
                var border = createBorder(value);
                switch (name) {
                    case "top" -> pBdr.setTop(border);
                    case "left" -> pBdr.setLeft(border);
                    case "bottom" -> pBdr.setBottom(border);
                    case "right" -> pBdr.setRight(border);
                    default -> throw new UnsupportedOperationException("Unknown border: " + name);
                }
            }
        }
        return pBdr;
    }

    private static CTBorder createBorder(String value) {
        var b = $.createCTBorder();
        // Parse color:size from value like "3B82F6:24"
        var parts = value.split(":");
        if (parts.length == 2) {
            b.setColor(parts[0]);
            b.setSz(BigInteger.valueOf(Integer.parseInt(parts[1])));
            b.setSpace(BigInteger.valueOf(1));
            b.setVal(STBorder.SINGLE);
        }
        return b;
    }

    public static PPrBase.PStyle w_pStyle(Object... attrs) {
        var style = $.createPPrBasePStyle();
        for (var attr : attrs) {
            switch (attr) {
                case A(var name, var value) when "val".equals(name) -> style.setVal(value);
                default -> {} // Ignore other types
            }
        }
        return style;
    }

    public static Jc w_jc(Object... attrs) {
        var jc = $.createJc();
        for (var attr : attrs) {
            switch (attr) {
                case A(var name, var value) when "val".equals(name) -> {
                    jc.setVal(JcEnumeration.fromValue(value));
                }
                case String value -> {
                    jc.setVal(JcEnumeration.fromValue(value));
                }
                default -> {} // Ignore other types
            }
        }
        return jc;
    }

    public static RPr w_rPr(Object... props) {
        var rPr = $.createRPr();
        for (var prop : props) {
            switch (prop) {
                case W_Bold b -> rPr.setB(b.value());
                case W_Italic i -> rPr.setI(i.value());
                case Color color -> rPr.setColor(color);
                case HpsMeasure sz -> rPr.setSz(sz);
                case U u -> rPr.setU(u);
                case Highlight highlight -> rPr.setHighlight(highlight);
                case RFonts rFonts -> rPr.setRFonts(rFonts);
                case CTShd shd -> rPr.setShd(shd);  // Background color (shading) at run level
                case A attr -> handleRPrAttr(rPr, attr);
                default -> err("w:rPr", prop);
            }
        }
        return rPr;
    }

    // Property markers
    public record W_Bold(BooleanDefaultTrue value) {}
    public record W_Italic(BooleanDefaultTrue value) {}

    public static W_Bold w_b() {
        return new W_Bold($.createBooleanDefaultTrue());
    }

    public static W_Italic w_i() {
        return new W_Italic($.createBooleanDefaultTrue());
    }

    public static Color w_color(Object... attrs) {
        var c = $.createColor();
        for (var attr : attrs) {
            switch (attr) {
                case A(var name, var value) when "val".equals(name) -> c.setVal(value);
                case String hex -> c.setVal(hex);
                default -> {} // Ignore other types
            }
        }
        return c;
    }

    public static HpsMeasure w_sz(Object... attrs) {
        var sz = $.createHpsMeasure();
        for (var attr : attrs) {
            switch (attr) {
                case A(var name, var value) when "val".equals(name) -> sz.setVal(parseBigInt(value));
                case Integer i -> sz.setVal(BigInteger.valueOf(i));
                default -> {} // Ignore other types
            }
        }
        return sz;
    }

    public static U w_u() {
        var u = $.createU();
        u.setVal(UnderlineEnumeration.SINGLE);
        return u;
    }

    public static Highlight w_highlight(Object... attrs) {
        var h = $.createHighlight();
        for (var attr : attrs) {
            switch (attr) {
                case A(var name, var value) when "val".equals(name) -> h.setVal(value);
                case String val -> h.setVal(val);
                default -> {} // Ignore other types
            }
        }
        return h;
    }

    public static RFonts w_rFonts(Object... attrs) {
        var f = $.createRFonts();
        for (var attr : attrs) {
            switch (attr) {
                case A(String name, String value) -> {
                    switch (name) {
                        case "ascii" -> f.setAscii(value);
                        case "hAnsi" -> f.setHAnsi(value);
                        default -> throw new UnsupportedOperationException("Unknown font attr: " + name);
                    }
                }
                case String fontName -> {
                    f.setAscii(fontName);
                    f.setHAnsi(fontName);
                }
                default -> {} // Ignore other types
            }
        }
        return f;
    }

    public static WFrags wfrags(Object... initial) {
        var f = new WFrags();
        for (var obj : initial) {
            f.add(obj);
        }
        return f;
    }

    public static String center() {
        return "center";
    }

    public static String colorHex(String hex) {
        return hex;
    }

    public static void addTo(WordprocessingMLPackage doc, Object... elements) {
        var content = doc.getMainDocumentPart().getContent();
        for (var el : elements) {
            switch (el) {
                case WFrags wf -> content.addAll(wf.items());
                default -> content.add(el);
            }
        }
    }

    private static void handlePAttr(P p, A attr) {}
    private static void handleRAttr(R r, A attr) {}
    private static void handlePPrAttr(PPr pPr, A attr) {}
    private static void handleRPrAttr(RPr rPr, A attr) {}

    private static BigInteger parseBigInt(String value) {
        return BigInteger.valueOf(Integer.parseInt(value));
    }

    public static String formatNumber(java.math.BigInteger number) {
        return String.format("%,d", number);
    }

    private static void err(String context, Object c) {
        throw new UnsupportedOperationException(
            "Cannot add " + c.getClass().getSimpleName() + " to " + context
        );
    }
}
