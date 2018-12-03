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

package fi.vm.sade.eperusteet.domain;


import fi.vm.sade.eperusteet.dto.Reference;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 * Rajapinnan toteuttava entity on "viitattavissa" ja sillä pitää olla yksikäsitteinen avain.
 *
 * @author mikkom
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