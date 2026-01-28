package luvdocx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class WFrags implements Iterable<Object> {
    private final List<Object> items = new ArrayList<>();

    public WFrags add(Object... objects) {
        for (var obj : objects) {
            items.add(obj);
        }
        return this;
    }

    public List<Object> items() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public Iterator<Object> iterator() {
        return items.iterator();
    }

    public Object[] __() {
        return items.toArray();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
