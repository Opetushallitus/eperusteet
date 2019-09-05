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
import java.util.List;
import java.util.Set;


@Slf4j
public class AmmattitaitovaatimusRepositoryImpl implements AmmattitaitovaatimusRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<TutkinnonOsaViite> findTutkinnonOsatBy(PageRequest page, AmmattitaitovaatimusQueryDto pquery) {
        TypedQuery<TutkinnonOsaViite> query = getTosaQuery(pquery);
        TypedQuery<Long> tosaCountQuery = getTosaCountQuery(pquery);
        if (page != null) {
            query.setFirstResult(page.getOffset());
            query.setMaxResults(page.getPageSize());
        }
        Page<TutkinnonOsaViite> result = new PageImpl<>(
                query.getResultList(),
                page,
                tosaCountQuery.getSingleResult());
        return result;
    }

    private TypedQuery<Long> getTosaCountQuery(AmmattitaitovaatimusQueryDto pquery) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Perusteprojekti> root = query.from(Perusteprojekti.class);
        Predicate pred = buildTosaPredicate(root, query, cb, pquery);
        SetJoin<Suoritustapa, TutkinnonOsaViite> join = root.join(Perusteprojekti_.peruste)
                .join(Peruste_.suoritustavat)
                .join(Suoritustapa_.tutkinnonOsat);
        query.select(cb.countDistinct(join)).where(pred);
        return em.createQuery(query);
    }

    private TypedQuery<TutkinnonOsaViite> getTosaQuery(AmmattitaitovaatimusQueryDto pquery) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TutkinnonOsaViite> query = cb.createQuery(TutkinnonOsaViite.class);
        Root<Perusteprojekti> root = query.from(Perusteprojekti.class);
        SetJoin<Suoritustapa, TutkinnonOsaViite> join = root.join(Perusteprojekti_.peruste)
                .join(Peruste_.suoritustavat)
                .join(Suoritustapa_.tutkinnonOsat);
        Predicate pred = buildTosaPredicate(root, query, cb, pquery);
        query.select(join).where(pred).distinct(true);
        return em.createQuery(query);
    }

    private Predicate buildTosaPredicate(Root<Perusteprojekti> root, CriteriaQuery query, CriteriaBuilder cb, AmmattitaitovaatimusQueryDto queryDto) {
        Predicate pred = cb.equal(root.get(Perusteprojekti_.tila), ProjektiTila.JULKAISTU);
        pred = cb.and(pred, cb.or(
                cb.exists(kohdealueettomat(cb, query, queryDto)),
                cb.exists(kohdealueelliset(cb, query, queryDto))));
        return pred;
    }

    @Override
    public Page<Peruste> findPerusteetBy(PageRequest page, AmmattitaitovaatimusQueryDto pquery) {
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
        Predicate pred = buildPredicate(query.from(Perusteprojekti.class), query, cb, queryDto);
        query.where(pred).distinct(true);
        return em.createQuery(query);
    }

    private TypedQuery<Long> getCountQuery(AmmattitaitovaatimusQueryDto queryDto) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Perusteprojekti> root = query.from(Perusteprojekti.class);
        Predicate pred = buildPredicate(root, query, cb, queryDto);
        query.select(cb.countDistinct(root)).where(pred);
        return em.createQuery(query);
    }

    private Predicate buildPredicate(Root<Perusteprojekti> root, CriteriaQuery query, CriteriaBuilder cb, AmmattitaitovaatimusQueryDto queryDto) {
        if (StringUtils.isEmpty(queryDto.getUri())) {
            throw new BusinessRuleViolationException("uri-puuttuu");
        }

        Predicate pred = cb.equal(root.get(Perusteprojekti_.tila), ProjektiTila.JULKAISTU);

        pred = cb.and(pred, cb.or(
                cb.exists(kohdealueettomat(cb, query, queryDto)),
                cb.exists(kohdealueelliset(cb, query, queryDto))));
        return pred;
    }

    private Join<TutkinnonOsaViite, TutkinnonOsa> joinTutkinonOsat(Root<Perusteprojekti> root) {
        return root.join(Perusteprojekti_.peruste)
                .join(Peruste_.suoritustavat)
                .join(Suoritustapa_.tutkinnonOsat)
                .join(TutkinnonOsaViite_.tutkinnonOsa);
    }

    private Subquery<Perusteprojekti> kohdealueettomat(CriteriaBuilder cb, CriteriaQuery query, AmmattitaitovaatimusQueryDto queryDto) {
        Subquery<Perusteprojekti> subquery = query.subquery(Perusteprojekti.class);
        Root<Perusteprojekti> root = subquery.from(Perusteprojekti.class);
        Join<Ammattitaitovaatimus2019, Koodi> koodi = joinTutkinonOsat(root)
                .join(TutkinnonOsa_.ammattitaitovaatimukset2019)
                .join(Ammattitaitovaatimukset2019_.vaatimukset)
                .join(Ammattitaitovaatimus2019_.koodi);
        return subquery.select(root).where(cb.equal(koodi.get(Koodi_.uri), queryDto.getUri()));
    }

    private Subquery<Perusteprojekti> kohdealueelliset(CriteriaBuilder cb, CriteriaQuery query, AmmattitaitovaatimusQueryDto queryDto) {
        Subquery<Perusteprojekti> subquery = query.subquery(Perusteprojekti.class);
        Root<Perusteprojekti> root = subquery.from(Perusteprojekti.class);
        Join<Ammattitaitovaatimus2019, Koodi> koodi = joinTutkinonOsat(root)
                .join(TutkinnonOsa_.ammattitaitovaatimukset2019)
                .join(Ammattitaitovaatimukset2019_.kohdealueet)
                .join(Ammattitaitovaatimus2019Kohdealue_.vaatimukset)
                .join(Ammattitaitovaatimus2019_.koodi);
        return subquery.select(root).where(cb.equal(koodi.get(Koodi_.uri), queryDto.getUri()));
    }

}
