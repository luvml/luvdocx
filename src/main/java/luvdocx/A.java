package luvdocx;

import luvx.Attr_I;

public record A(String name, String value) implements Attr_I {

    public static A val(String value) {
        return new A("val", value);
    }

    public static A fill(String value) {
        return new A("fill", value);
    }

    public static A before(int value) {
        return new A("before", String.valueOf(value));
    }

    public static A after(int value) {
        return new A("after", String.valueOf(value));
    }

    public static A left(Object value) {
        return new A("left", String.valueOf(value));
    }

    public static A right(int value) {
        return new A("right", String.valueOf(value));
    }

    public static A top(Object value) {
        return new A("top", String.valueOf(value));
    }

    public static A bottom(Object value) {
        return new A("bottom", String.valueOf(value));
    }

    public static A ascii(String value) {
        return new A("ascii", value);
    }

    public static A hAnsi(String value) {
        return new A("hAnsi", value);
    }

    @Override
    public A self() {
        return this;
    }
}
