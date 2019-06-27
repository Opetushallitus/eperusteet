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
package fi.vm.sade.eperusteet.repository.liite.impl;

import fi.vm.sade.eperusteet.domain.liite.Liite;
import fi.vm.sade.eperusteet.domain.liite.LiiteTyyppi;
import fi.vm.sade.eperusteet.repository.liite.LiiteRepositoryCustom;
import java.io.InputStream;
import java.sql.Blob;
import javax.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author jhyoty
 */
public class LiiteRepositoryImpl implements LiiteRepositoryCustom {

    @Autowired
    EntityManager em;

    @Override
    public Liite add(LiiteTyyppi tyyppi, String mime, String nimi, long length, InputStream is) {
        Session session = em.unwrap(Session.class);
        Blob blob = Hibernate.getLobCreator(session).createBlob(is, length);
        Liite liite = new Liite(tyyppi, mime, nimi, blob);
        em.persist(liite);
        return liite;
    }

    @Override
    public Liite add(LiiteTyyppi tyyppi, String mime, String nimi, byte[] bytes) {
        Session session = em.unwrap(Session.class);
        Blob blob = Hibernate.getLobCreator(session).createBlob(bytes);
        Liite liite = new Liite(tyyppi, mime, nimi, blob);
        em.persist(liite);
        return liite;
    }

}
