package fi.vm.sade.eperusteet.repository.custom;

import fi.vm.sade.eperusteet.domain.LokalisoituTeksti;
import fi.vm.sade.eperusteet.domain.LokalisoituTeksti_;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.TekstiPalanen_;
import fi.vm.sade.eperusteet.domain.osaamismerkki.Osaamismerkki;
import fi.vm.sade.eperusteet.domain.osaamismerkki.Osaamismerkki_;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiQuery;
import fi.vm.sade.eperusteet.repository.OsaamismerkkiRepositoryCustom;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class OsaamismerkkiRepositoryImpl implements OsaamismerkkiRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Osaamismerkki> findBy(PageRequest page, OsaamismerkkiQuery oquery) {
        TypedQuery<Long> countQuery = getCountQuery(oquery);
        TypedQuery<Tuple> query = getQuery(page, oquery);
        query.setFirstResult(page.getOffset());
        query.setMaxResults(page.getPageSize());

        log.debug(query.unwrap(Query.class).getQueryString());

        List<Osaamismerkki> result = query.getResultList().stream()
                .map(t -> t.get(0, Osaamismerkki.class))
                .collect(Collectors.toList());

        return new PageImpl<>(result, page, countQuery.getSingleResult());
    }

    private TypedQuery<Long> getCountQuery(OsaamismerkkiQuery oquery) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Osaamismerkki> root = query.from(Osaamismerkki.class);

        Predicate pred = buildPredicate(root, cb, oquery);
        query.select(cb.countDistinct(root)).where(pred);

        return em.createQuery(query);
    }

    private TypedQuery<Tuple> getQuery(PageRequest page, OsaamismerkkiQuery oquery) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Osaamismerkki> root = query.from(Osaamismerkki.class);

        Predicate pred = buildPredicate(root, cb, oquery);
        query = query.multiselect(root);

        List<Order> orders = new ArrayList<>();
        Sort sort = page.getSort();
        sort.forEach(order -> {
            if (order.getDirection().equals(Sort.Direction.ASC)) {
                orders.add(cb.asc(root.get(order.getProperty())));
            } else {
                orders.add(cb.desc(root.get(order.getProperty())));
            }
        });

        query.where(pred)
                .orderBy(orders)
                .distinct(true);

        return em.createQuery(query);
    }

    private Predicate buildPredicate(Root<Osaamismerkki> root,
                                     CriteriaBuilder cb,
                                     OsaamismerkkiQuery tq) {
        Predicate pred = cb.conjunction();

        if (!ObjectUtils.isEmpty(tq.getNimi())) {
            Expression<String> nimiLit = cb.literal(RepositoryUtil.kuten(tq.getNimi()));
            Join<TekstiPalanen, LokalisoituTeksti> nimi = root.join(Osaamismerkki_.nimi).join(TekstiPalanen_.teksti);
            Predicate nimessa = cb.like(cb.lower(nimi.get(LokalisoituTeksti_.teksti)), nimiLit);
            pred = cb.and(pred, nimessa);
        }
        return pred;
    }
}
