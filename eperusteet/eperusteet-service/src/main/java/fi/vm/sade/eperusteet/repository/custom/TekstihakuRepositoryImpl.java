package fi.vm.sade.eperusteet.repository.custom;

import fi.vm.sade.eperusteet.domain.views.TekstiHakuTulos;
import fi.vm.sade.eperusteet.domain.views.TekstiHakuTulos_;
import fi.vm.sade.eperusteet.dto.peruste.VapaaTekstiQueryDto;
import fi.vm.sade.eperusteet.repository.TekstihakuRepositoryCustom;
import org.hibernate.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class TekstihakuRepositoryImpl implements TekstihakuRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    private Long getCount(VapaaTekstiQueryDto pquery) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<TekstiHakuTulos> from = countQuery.from(TekstiHakuTulos.class);
        countQuery
                .select(cb.count(from))
                .where(createPredicate(pquery, from));
        return em.createQuery(countQuery).getSingleResult();
    }

    private TypedQuery<TekstiHakuTulos> getResult(VapaaTekstiQueryDto pquery) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TekstiHakuTulos> query = cb.createQuery(TekstiHakuTulos.class);
        Root<TekstiHakuTulos> from = query.from(TekstiHakuTulos.class);
        query.select(from)
             .where(createPredicate(pquery, from));
        return em.createQuery(query);
    }

    private Predicate createPredicate(VapaaTekstiQueryDto pquery, Root<TekstiHakuTulos> from) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        List<Predicate> preds = new ArrayList<>();

        if (pquery.getPerusteprojekti() != null) {
            preds.add(cb.equal(from.get(TekstiHakuTulos_.perusteprojekti), pquery.getPerusteprojekti()));
        }

        if (pquery.getPeruste() != null) {
            preds.add(cb.equal(from.get(TekstiHakuTulos_.peruste), pquery.getPeruste()));
        }

//        if (pquery.getTila() != null) {
//            preds.add(cb.equal(from.get(TekstiHakuTulos_.tila), pquery.getTila()));
//        }

        { // Tekstihaku
            Predicate tekstihakuPred = cb.like(
                    from.get(TekstiHakuTulos_.teksti),
                    cb.literal(RepositoryUtil.kutenCaseSensitive(pquery.getTeksti())));
            preds.add(tekstihakuPred);
        }

        return RepositoryUtil.and(cb, preds);
    }

    @Override
    public Page<TekstiHakuTulos> tekstihaku(VapaaTekstiQueryDto pquery) {
        Long count = getCount(pquery);
        TypedQuery<TekstiHakuTulos> query = getResult(pquery)
            .setFirstResult(pquery.getSivu())
            .setMaxResults(pquery.getSivukoko());

        String querystr = query.unwrap(Query.class).getQueryString();

        return new PageImpl<>(
                query.getResultList(),
                new PageRequest(pquery.getSivu(), pquery.getSivukoko()),
                count);
    }

    @Override
    public void rakennaTekstihaku() {
        em.createStoredProcedureQuery("rakenna_tekstihaku").execute();
    }
}
