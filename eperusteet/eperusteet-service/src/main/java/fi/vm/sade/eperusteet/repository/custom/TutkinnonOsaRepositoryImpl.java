package fi.vm.sade.eperusteet.repository.custom;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa_;
import fi.vm.sade.eperusteet.dto.peruste.TutkinnonOsaQueryDto;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaRepositoryCustom;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;

@Slf4j
public class TutkinnonOsaRepositoryImpl implements TutkinnonOsaRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<TutkinnonOsa> findBy(PageRequest page, TutkinnonOsaQueryDto queryDto) {
        TypedQuery<Long> countQuery = getCountQuery(queryDto);
        TypedQuery<TutkinnonOsa> query = getQuery(queryDto);
        if (page != null) {
            query.setFirstResult(page.getOffset());
            query.setMaxResults(page.getPageSize());
        }
        return new PageImpl<>(
                query.getResultList(),
                page,
                countQuery.getSingleResult());
    }

    private TypedQuery<TutkinnonOsa> getQuery(TutkinnonOsaQueryDto queryDto) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TutkinnonOsa> query = cb.createQuery(TutkinnonOsa.class);
        Predicate pred = buildPredicate(query.from(TutkinnonOsa.class), cb, queryDto);
        query.where(pred).distinct(true);
        return em.createQuery(query);
    }

    private TypedQuery<Long> getCountQuery(TutkinnonOsaQueryDto queryDto) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<TutkinnonOsa> root = query.from(TutkinnonOsa.class);
        Predicate pred = buildPredicate(root, cb, queryDto);
        query.select(cb.countDistinct(root)).where(pred);
        return em.createQuery(query);
    }

    private Predicate buildPredicate(Root<TutkinnonOsa> root, CriteriaBuilder cb, TutkinnonOsaQueryDto queryDto) {
        Predicate pred = cb.equal(root.get(TutkinnonOsa_.tila), PerusteTila.LUONNOS);

        if (StringUtils.isNotEmpty(queryDto.getNimi())) {
            Expression<String> nimiLit = cb.literal(RepositoryUtil.kuten(queryDto.getNimi()));
            SetJoin<TekstiPalanen, LokalisoituTeksti> nimiJoin = root.join(TutkinnonOsa_.nimi).join(TekstiPalanen_.teksti);
            Predicate nimessa = cb.like(cb.lower(nimiJoin.get(LokalisoituTeksti_.teksti)), nimiLit);
            pred = cb.and(pred, nimessa);
        }

        if (StringUtils.isNotEmpty(queryDto.getKoodiUri())) {
            Path<String> koodiUriPath = root.join(TutkinnonOsa_.koodi).get(Koodi_.uri);
            Predicate koodissa = cb.equal(cb.lower(koodiUriPath), queryDto.getKoodiUri().toLowerCase());
            pred = cb.and(pred, koodissa);
        }

        return pred;
    }
}
