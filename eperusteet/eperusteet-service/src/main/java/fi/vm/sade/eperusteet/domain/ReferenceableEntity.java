package fi.vm.sade.eperusteet.domain;


import fi.vm.sade.eperusteet.dto.Reference;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 * Rajapinnan toteuttava entity on "viitattavissa" ja sillä pitää olla yksikäsitteinen avain.
 */
public interface ReferenceableEntity {

    /**
     * Määrittää mitä entiteetin arvoa käytetään referenssinä.
     *
     * @return reference
     */
    Reference getReference();

    /**
     * Palauttaa viitattavissa olevan entityn yksikäsitteisen avaimen. Avain pitää olla mahdollista muuttaa merkkijonoksi (järkevä toString-methodi vaaditaan).
     *
     * @return id
     */
    Serializable getId();

    static <T extends ReferenceableEntity> Predicate<T> idEquals(Serializable id) {
        return e -> id != null && id.equals(e.getId());
    }
}
