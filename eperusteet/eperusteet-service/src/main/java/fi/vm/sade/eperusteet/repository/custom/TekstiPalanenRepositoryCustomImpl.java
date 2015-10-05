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

package fi.vm.sade.eperusteet.repository.custom;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiHakuDto;
import fi.vm.sade.eperusteet.repository.TekstiPalanenRepositoryCustom;
import org.hibernate.Session;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.type.EnumType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * User: tommiratamaa
 * Date: 5.10.15
 * Time: 21.00
 */
@Repository
public class TekstiPalanenRepositoryCustomImpl implements TekstiPalanenRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<LokalisoituTekstiHakuDto> findLokalisoitavatTekstit(Set<Long> tekstiPalanenIds) {
        // In JPA 2.0 we can not select DTOs with native queries and LokalisoituTeksti is not an entity
        // (which can therefore neither be selected with JPA query language) -> doing in Hibernate level API:
        Session session = em.unwrap(Session.class);
        return list(session.createSQLQuery("SELECT " +
                    "   t.tekstipalanen_id as id, " +
                    "   t.kieli as kieli, " +
                    "   t.teksti as teksti " +
                    " FROM tekstipalanen_teksti t " +
                    " WHERE t.tekstipalanen_id IN (:ids) ORDER BY t.tekstipalanen_id, t.kieli")
                .addScalar("id", LongType.INSTANCE)
                .addScalar("kieli", enumType(session, Kieli.class))
                .addScalar("teksti", StringType.INSTANCE)
                .setResultTransformer(new AliasToBeanResultTransformer(LokalisoituTekstiHakuDto.class))
                .setParameterList("ids", tekstiPalanenIds));
    }

    @SuppressWarnings("unchecked")
    protected<T> List<T> list(org.hibernate.Query q) {
        return q.list();
    }

    protected<E extends Enum<E>> Type enumType(Session session, Class<E> e) {
        Properties params = new Properties();
        params.put("enumClass", e.getCanonicalName());
        params.put("type", "12");/*type 12 instructs to use the String representation of enum value*/
        return session.getTypeHelper().custom(EnumType.class, params);
    }
}
