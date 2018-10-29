package fi.vm.sade.eperusteet.repository.custom;

import fi.vm.sade.eperusteet.domain.tekstihaku.*;
import fi.vm.sade.eperusteet.dto.peruste.VapaaTekstiQueryDto;
import fi.vm.sade.eperusteet.repository.TekstihakuRepositoryCustom;
import org.hibernate.Query;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static fi.vm.sade.eperusteet.repository.custom.RepositoryUtil.ESCAPE_CHAR;

public class TekstihakuRepositoryImpl implements TekstihakuRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    private TypedQuery<TekstiHakuTulos> getResult(VapaaTekstiQueryDto pquery) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TekstiHakuTulos> query = cb.createQuery(TekstiHakuTulos.class);
        Root<TekstiHakuTulos> from = query.from(TekstiHakuTulos.class);
        query.select(from)
             .where(createPredicate(pquery, from));
        return em.createQuery(query);
    }

    public static String tekstihakuMatch(String teksti) {
        if (teksti == null) {
            teksti = "";
        }
        StringBuilder b = new StringBuilder("%");
        b.append(teksti
                .replace("" + ESCAPE_CHAR, "" + ESCAPE_CHAR + ESCAPE_CHAR)
                .replace("_", ESCAPE_CHAR + "_")
                .replace("%", ESCAPE_CHAR + "%"));
        b.append("%");
        return b.toString();
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

        if (pquery.getTila() != null) {
//            Set<TekstihakuTyyppi> kohteet = pquery.getKohteet();
//            if (!kohteet.equals(TekstihakuTyyppi.kaikki())) {
//                preds.add(from.get(TekstiHakuTulos_.tyyppi).in(kohteet));
//            }
        }

        { // TekstiHaku
            String match = tekstihakuMatch(pquery.getTeksti());
            Predicate tekstihakuPred = cb.like(
                    from.get(TekstiHakuTulos_.teksti),
                    cb.literal(match));
            preds.add(tekstihakuPred);
        }

        return RepositoryUtil.and(cb, preds);
    }

    @Override
    public List<TekstiHakuTulos> tekstihaku(VapaaTekstiQueryDto pquery) {
        TypedQuery<TekstiHakuTulos> query = getResult(pquery)
            .setFirstResult(pquery.getSivu() * pquery.getSivukoko())
            .setMaxResults(pquery.getSivukoko());

        String querystr = query.unwrap(Query.class).getQueryString();
        return query.getResultList();
    }

    @Override
    public void rakennaTekstihaku() {
        em.createStoredProcedureQuery("rakenna_haku")
                .execute();
    }
}
