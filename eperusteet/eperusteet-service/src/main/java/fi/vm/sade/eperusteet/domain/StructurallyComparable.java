package fi.vm.sade.eperusteet.domain;

public interface StructurallyComparable<T> {
    boolean structureEquals(T other);
}
