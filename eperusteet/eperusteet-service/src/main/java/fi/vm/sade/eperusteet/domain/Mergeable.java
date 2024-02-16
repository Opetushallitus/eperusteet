package fi.vm.sade.eperusteet.domain;

public interface Mergeable<T> {
    void mergeState(T updated);
}
