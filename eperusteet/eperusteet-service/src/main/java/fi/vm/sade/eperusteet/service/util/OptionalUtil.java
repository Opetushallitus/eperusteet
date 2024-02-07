package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.service.exception.NotExistsException;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;

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

    public static <T> T found(Optional<T> opt) throws NotExistsException {
        return found(opt, OptionalUtil.<T>truePredicate(), NOT_EXISTS);
    }

    public static <T,Ex extends Exception> T found(Optional<T> opt, Predicate<? super T> predicate) throws Ex {
        return found(opt, predicate, NOT_EXISTS);
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
