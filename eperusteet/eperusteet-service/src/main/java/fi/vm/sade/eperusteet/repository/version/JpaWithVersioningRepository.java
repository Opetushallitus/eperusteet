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
package fi.vm.sade.eperusteet.repository.version;

import java.io.Serializable;
import java.util.List;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface JpaWithVersioningRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

    List<Revision> getRevisions(final ID id);

    T findRevision(final ID id, final Integer revisionId);

    Integer getLatestRevisionId(final ID id);

    public class DomainClassNotAuditedException extends BeanCreationException {

        public DomainClassNotAuditedException(Class<?> clazz) {
            super("Defined domain class '" + clazz.getSimpleName() + "' does not contain @audited-annotation");
        }
    }

    /**
     * Lukitsee entiteetin muokkausta varten. Lukitus vapautuu automaattisesti transaktion loppuessa.
     *
     * Enversillä on ongelmia yhtäaikaisten transaktioiden kanssa, joten pessimistisen lukituksen käyttäminen on joissakin tapauksissa tarpeen.
     *
     * @param entity
     * @return päivitetty, lukittu entiteetti
     */
    @Transactional(propagation = Propagation.MANDATORY)
    T lock(T entity);

    /**
     * Asettaa revisiokohtaisen kommentin.
     *
     * Jos revisioon on jo asetettu kommentti, uusi kommentti korvaa aikaisemman. Kommentti on globaali koko revisiolle; jos samassa muutoksessa muokataan
     * useita entiteettejä, kommentti koskee niitä kaikkia.
     *
     * @param kommentti valinnainen kommentti
     */
    void setRevisioKommentti(String kommentti);

}
