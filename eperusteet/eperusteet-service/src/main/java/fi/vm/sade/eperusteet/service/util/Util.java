/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.domain.ReferenceableEntity;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.Optional.fromNullable;

/**
 * @author jhyoty
 */
public final class Util {

    private Util() {
        //util
    }

    /**
     * vertailee kahta samantyyppistä viitettä ja palauttaa true jos kumpikin on null tai kumpikin on ei-null
     */
    public static <T> boolean refXnor(T l, T r) {
        return (l == null && r == null) || (l != null && r != null);
    }

    public static <T extends ReferenceableEntity> boolean identityEquals(T l, T r) {
        return (l != null && r != null && l.getId() != null && l.getId().equals(r.getId()));
    }

    /**
     * Utility for boolean method references
     *
     * @param p
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> not(Predicate<T> p) {
        return p.negate();
    }

    public static <F, E, T extends Collection<E>> Predicate<F> empty(Function<F, T> src) {
        return from -> fromNullable(src.apply(from)).transform(Collection::isEmpty).or(true);
    }

    public static <F> Predicate<F> emptyString(Function<F, String> src) {
        return from -> fromNullable(src.apply(from)).transform(str -> str.trim().isEmpty()).or(true);
    }

    public static <T> Predicate<T> and(Predicate<T> a, Predicate<? super T> b) {
        return a.and(b);
    }

    public static <T> Predicate<T> or(Predicate<T> a, Predicate<? super T> b) {
        return a.or(b);
    }
}
