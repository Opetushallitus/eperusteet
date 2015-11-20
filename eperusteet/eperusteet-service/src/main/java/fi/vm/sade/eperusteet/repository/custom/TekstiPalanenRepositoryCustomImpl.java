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

import com.google.common.collect.Lists;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiHakuDto;
import fi.vm.sade.eperusteet.repository.TekstiPalanenRepositoryCustom;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.type.EnumType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

import static java.util.stream.Collectors.toList;

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
        // Make sure PostgreSQL's max parameter size 34464 won't affect us:
        return Lists.partition(new ArrayList<Long>(tekstiPalanenIds), 34460).stream()
                .map(this::getTekstipalaset).flatMap(Collection::stream).collect(toList());
    }

    private List<LokalisoituTekstiHakuDto> getTekstipalaset(List<Long> tekstiPalanenIds) {
        // In JPA 2.0 we can not select DTOs with native queries and LokalisoituTeksti is not an entity
        // (which can therefore neither be selected with JPA query language) -> doing in Hibernate level API:
        Session session = em.unwrap(Session.class);
        StringBuilder or = new StringBuilder();
        // Just to make sure that https://hibernate.atlassian.net/browse/HHH-1123 won't affect us:
        List<List<Long>> idChunks = Lists.partition(tekstiPalanenIds, 1000);
        int i = 0;
        Map<String,List<Long>> params = new HashMap<>(idChunks.size());
        for (List<Long> ids : idChunks) {
            if (or.length() > 0) {
                or.append(" OR ");
            }
            or.append("t.tekstipalanen_id IN (:ids_").append(i).append(") ");
            params.put("ids_"+i, ids);
            ++i;
        }
        Query q =session.createSQLQuery("SELECT " +
                "   t.tekstipalanen_id as id, " +
                "   t.kieli as kieli, " +
                "   t.teksti as teksti " +
                " FROM tekstipalanen_teksti t " +
                " WHERE (" + or + ") ORDER BY t.tekstipalanen_id, t.kieli")
                .addScalar("id", LongType.INSTANCE)
                .addScalar("kieli", enumType(session, Kieli.class))
                .addScalar("teksti", StringType.INSTANCE)
                .setResultTransformer(new AliasToBeanResultTransformer(LokalisoituTekstiHakuDto.class));
        for (Map.Entry<String,List<Long>> p : params.entrySet()) {
            q.setParameterList(p.getKey(), p.getValue());
        }
        return list(q);
    }

    @SuppressWarnings("unchecked")
    protected<T> List<T> list(Query q) {
        return q.list();
    }

    protected<E extends Enum<E>> Type enumType(Session session, Class<E> e) {
        Properties params = new Properties();
        params.put("enumClass", e.getCanonicalName());
        params.put("type", "12");/*type 12 instructs to use the String representation of enum value*/
        return session.getTypeHelper().custom(EnumType.class, params);
    }
}
