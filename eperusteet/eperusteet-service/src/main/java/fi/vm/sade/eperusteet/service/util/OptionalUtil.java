/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.service.exception.NotExistsException;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;


/**
 * User: tommiratamaa
 * Date: 29.9.15
 * Time: 15.19
 */
public class OptionalUtil {
    private OptionalUtil() {
    }

    public static final Supplier<NotExistsException> NOT_EXISTS = NotExistsException::new;
    private static<T> Predicate<T> truePredicate() {
        return any -> true;
    }

    public static <T,Ex extends Exception> T found(Optional<T> opt, Predicate<? super T> predicate, Supplier<Ex> exception) throws Ex {
        if (!opt.isPresent()) {
            throw exception.get();
        }
        return tested(predicate, exception, opt.get());
    }

    private static <T, Ex extends Exception> T tested(Predicate<? super T> predicate, Supplier<Ex> exception, T value) throws Ex {
        if (!predicate.test(value)) {
            throw exception.get();
        }
        return value;
    }

    public static <T,Ex extends Exception> T found(Optional<T> opt, Supplier<Ex> exception) throws Ex {
        return found(opt, OptionalUtil.<T>truePredicate(), exception);
    }

    public static <T> T found(Optional<T> opt, Predicate<? super T> predicate) throws NotExistsException {
        return found(opt, predicate, NOT_EXISTS);
    }

    public static <T> T found(Optional<T> opt) throws NotExistsException {
        return found(opt, OptionalUtil.<T>truePredicate(), NOT_EXISTS);
    }

    public static <T,Ex extends Exception> T found(Optional<T> opt, Predicate<? super T> predicate, Supplier<Ex> exception) throws Ex{
        if (!opt.isPresent()) {
            throw exception.get();
        }
        return tested(predicate, exception, opt.get());
    }

    public static <T,Ex extends Exception> T found(Optional<T> opt, Predicate<? super T> predicate) throws Ex{
        return found(opt, predicate, NOT_EXISTS);
    }

    public static <T,Ex extends Exception> T found(Optional<T> opt, Supplier<Ex> exception) throws Ex {
        return found(opt, OptionalUtil.<T>truePredicate(), exception);
    }

    public static <T> T found(Optional<T> opt) throws NotExistsException {
        return found(opt, OptionalUtil.<T>truePredicate(), NOT_EXISTS);
    }

    public static <T> T found(T opt) throws NotExistsException {
        return found(opt, NOT_EXISTS);
    }

    public static <T,Ex extends Exception> T found(T opt, Supplier<Ex> exception) throws Ex {
        return found(ofNullable(opt), exception);
    }

    public static <T> T found(T opt, Predicate<T> predicate)  {
        return found(ofNullable(opt), predicate, NOT_EXISTS);
    }

    public static <T,Ex extends Exception> T found(T opt, Predicate<T> predicate, Supplier<Ex> exception) throws Ex {
        return found(ofNullable(opt), predicate, exception);
    }
}
