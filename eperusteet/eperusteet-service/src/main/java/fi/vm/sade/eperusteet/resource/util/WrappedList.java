package fi.vm.sade.eperusteet.resource.util;

import java.util.AbstractList;
import java.util.List;

public class WrappedList<T> extends AbstractList<T> {
    private final List<T> wrappedList;

    public WrappedList(List<T> c) {
        wrappedList = c;
    }

    @Override
    public T get(int index) {
        return wrappedList.get(index);
    }

    @Override
    public int size() {
        return wrappedList.size();
    }

    public static <T> List<T> wrap(List<T> source) {
        return new WrappedList<>(source);
    }
}
