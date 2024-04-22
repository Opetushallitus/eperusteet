package fi.vm.sade.eperusteet.domain;

public interface PartialMergeable<T> extends Mergeable<T> {
    void partialMergeState(T updated);
}
