package fi.vm.sade.eperusteet.repository.custom;

import com.google.common.collect.Lists;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiHakuDto;
import fi.vm.sade.eperusteet.repository.TekstiPalanenRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Slf4j
@Deprecated // addScalar ei toimi helposti hibernate 6 kanssa: repo käytössä vain vanhan lukion kanssa = voidaan tuhota(?)
@Repository
public class TekstiPalanenRepositoryCustomImpl implements TekstiPalanenRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<LokalisoituTekstiHakuDto> findLokalisoitavatTekstit(Set<Long> tekstiPalanenIds) {
        // Make sure PostgreSQL's max parameter size 34464 won't affect us:
//        return Lists.partition(new ArrayList<Long>(tekstiPalanenIds), 34460).stream()
//                .map(this::getTekstipalaset).flatMap(Collection::stream).collect(toList());
        return Collections.emptyList();
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
        Query q =session.createNativeQuery("SELECT " +
                "   t.tekstipalanen_id as id, " +
                "   t.kieli as kieli, " +
                "   t.teksti as teksti " +
                " FROM tekstipalanen_teksti t " +
                " WHERE (" + or + ") ORDER BY t.tekstipalanen_id, t.kieli")
                .addScalar("id", StandardBasicTypes.LONG)
                .addScalar("kieli", StandardBasicTypes.STRING)
                .addScalar("teksti", StandardBasicTypes.STRING)
                .setResultTransformer(new AliasToBeanResultTransformer(LokalisoituTekstiHakuDto.class));
        for (Map.Entry<String,List<Long>> p : params.entrySet()) {
            q.setParameter(p.getKey(), p.getValue());
        }
        return list(q);
    }

    @SuppressWarnings("unchecked")
    protected<T> List<T> list(Query q) {
        return q.getResultList();
    }

//    protected<E extends Enum<E>> Type enumType(Session session, Class<E> e) {
//        Properties params = new Properties();
//        params.put("enumClass", e.getCanonicalName());
//        params.put("type", "12");/*type 12 instructs to use the String representation of enum value*/
//        return session.getSessionFactory().getTypeHelper().custom(UserType.class, params);
//    }
}
