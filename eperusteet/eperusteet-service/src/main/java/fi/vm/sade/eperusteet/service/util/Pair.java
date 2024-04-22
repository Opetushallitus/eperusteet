package fi.vm.sade.eperusteet.service.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Yksinkertainen "pari"
 */
@Getter
@EqualsAndHashCode
public final class Pair<F,S> {
    private final F first;
    private final S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public static <F,S> Pair<F,S> of(F f, S s) {
        return new Pair<>(f,s);
    }
}
