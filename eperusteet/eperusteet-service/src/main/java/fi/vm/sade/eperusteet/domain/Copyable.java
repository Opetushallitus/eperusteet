package fi.vm.sade.eperusteet.domain;

public interface Copyable<T> {
    default T copy() {
        return copy(true);
    }

    T copy(boolean deep);
}
