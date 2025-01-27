package fi.vm.sade.eperusteet.repository.liite.impl;

import fi.vm.sade.eperusteet.domain.liite.Liite;
import fi.vm.sade.eperusteet.domain.liite.LiiteTyyppi;
import fi.vm.sade.eperusteet.repository.liite.LiiteRepositoryCustom;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import org.apache.commons.io.IOUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;

public class LiiteRepositoryImpl implements LiiteRepositoryCustom {

    @Autowired
    EntityManager em;

    @Override
    public Liite add(LiiteTyyppi tyyppi, String mime, String nimi, long length, InputStream is) {
        Blob blob = null;
        try {
            blob = BlobProxy.generateProxy(IOUtils.toByteArray(is));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Liite liite = new Liite(tyyppi, mime, nimi, blob);
        em.persist(liite);
        em.flush();
        return liite;
    }

    @Override
    public Liite add(LiiteTyyppi tyyppi, String mime, String nimi, byte[] bytes) {
        Blob blob = BlobProxy.generateProxy(bytes);
        Liite liite = new Liite(tyyppi, mime, nimi, blob);
        em.persist(liite);
        em.flush();
        return liite;
    }

    @Override
    public Liite add(UUID uuid, LiiteTyyppi tyyppi, String mime, String nimi, byte[] bytes) {
        Blob blob = BlobProxy.generateProxy(bytes);
        Liite liite = new Liite(uuid, tyyppi, mime, nimi, blob);
        em.persist(liite);
        em.flush();
        return liite;
    }
}
