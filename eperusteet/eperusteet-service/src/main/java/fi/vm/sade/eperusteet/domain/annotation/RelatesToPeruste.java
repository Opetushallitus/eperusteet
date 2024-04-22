package fi.vm.sade.eperusteet.domain.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for entity properties (getters or fields) leading to or closer to a related
 * owning Peruste. Relation can be direct or via a Collection.
 *
 * @see fi.vm.sade.eperusteet.domain.Peruste
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface RelatesToPeruste {

    /**
     * Marker annotation for an entity to set the path (or paths) leading to (or closer to) an
     * owning related Peruste
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Through {
        /**
         * @return dot chained property paths (or properties leading another entity that RelatesToPeruste)
         * (standard Java bean property names for getter/field properties)
         *
         * E.g. "peruste" or {"liittyva.peruste", "toisenKautta.liittyva.peruste"}
         */
        String[] value();
    }

    /**
     * Marker annotation for entity class that is Identifiable and is related to many locations
     * @see Identifiable
     *
     * Will be looked up if no relation to Peruste is found by other means.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface FromAnywhereReferenced {
    }
}
