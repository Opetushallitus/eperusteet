package fi.vm.sade.eperusteet.repository.custom;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.*;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite_;
import fi.vm.sade.eperusteet.dto.AmmattitaitovaatimusQueryDto;
import fi.vm.sade.eperusteet.repository.AmmattitaitovaatimusRepositoryCustom;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;


@Slf4j
public class AmmattitaitovaatimusRepositoryImpl implements AmmattitaitovaatimusRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Peruste> findBy(PageRequest page, AmmattitaitovaatimusQueryDto pquery) {
        TypedQuery<Long> countQuery = getCountQuery(pquery);
        TypedQuery<Perusteprojekti> query = getQuery(pquery);
        if (page != null) {
            query.setFirstResult(page.getOffset());
            query.setMaxResults(page.getPageSize());
        }
        Page<Perusteprojekti> result = new PageImpl<>(
                query.getResultList(),
                page,
                countQuery.getSingleResult());
        return result.map(Perusteprojekti::getPeruste);
    }

    private TypedQuery<Perusteprojekti> getQuery(AmmattitaitovaatimusQueryDto queryDto) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Perusteprojekti> query = cb.createQuery(Perusteprojekti.class);
        Predicate pred = buildPredicate(query.from(Perusteprojekti.class), cb, queryDto);
        query.where(pred).distinct(true);
        return em.createQuery(query);
    }

    private TypedQuery<Long> getCountQuery(AmmattitaitovaatimusQueryDto queryDto) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Perusteprojekti> root = query.from(Perusteprojekti.class);
        Predicate pred = buildPredicate(root, cb, queryDto);
        query.select(cb.countDistinct(root)).where(pred);
        return em.createQuery(query);
    }

    private Predicate buildPredicate(Root<Perusteprojekti> root, CriteriaBuilder cb, AmmattitaitovaatimusQueryDto queryDto) {
        if (StringUtils.isEmpty(queryDto.getUri())) {
            throw new BusinessRuleViolationException("uri-puuttuu");
        }

        Predicate pred = cb.equal(root.get(Perusteprojekti_.tila), ProjektiTila.LAADINTA);

        Join<TutkinnonOsaViite, TutkinnonOsa> osa = root.join(Perusteprojekti_.peruste)
                .join(Peruste_.suoritustavat)
                .join(Suoritustapa_.tutkinnonOsat)
                .join(TutkinnonOsaViite_.tutkinnonOsa);
        Join<TutkinnonOsa, Ammattitaitovaatimukset2019> vaatimukset = osa.join(TutkinnonOsa_.ammattitaitovaatimukset2019);

        if (false) {
            Join<Ammattitaitovaatimus2019, Koodi> kohdealueettomat = vaatimukset
                    .join(Ammattitaitovaatimukset2019_.vaatimukset)
                    .join(Ammattitaitovaatimus2019_.koodi);
            Predicate kohdealueettamotPredicate = cb.equal(kohdealueettomat.get(Koodi_.uri), queryDto.getUri());
        }

        Join<Ammattitaitovaatimus2019, Koodi> kohdealueelliset = vaatimukset
                .join(Ammattitaitovaatimukset2019_.kohdealueet)
                .join(Ammattitaitovaatimus2019Kohdealue_.vaatimukset)
                .join(Ammattitaitovaatimus2019_.koodi);

        Predicate kohdealueellisetPredicate = cb.equal(kohdealueelliset.get(Koodi_.uri), queryDto.getUri());
        pred = cb.and(pred, kohdealueellisetPredicate);
        return pred;
    }

}
