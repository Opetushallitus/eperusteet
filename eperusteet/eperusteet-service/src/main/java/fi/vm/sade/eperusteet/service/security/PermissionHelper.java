/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.service.security;

import fi.vm.sade.eperusteet.domain.JulkaistuPeruste;
import fi.vm.sade.eperusteet.domain.JulkaistuPeruste_;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.Peruste_;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.WithPerusteTila;
import fi.vm.sade.eperusteet.domain.permissions.PerusteenosanProjekti;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.repository.authorization.PerusteprojektiPermissionRepository;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * @author jhyoty
 */
@Component
public class PermissionHelper {

    @Autowired
    private EntityManager em;

    @Autowired
    private PerusteprojektiPermissionRepository perusteProjektit;

    @Autowired
    private JulkaisutRepository julkaisutRepository;

    @Cacheable(value = "tila", unless = "#result != T(fi.vm.sade.eperusteet.domain.PerusteTila).VALMIS")
    public PerusteTila findPerusteTilaFor(PermissionManager.Target targetType, Serializable id) {
        PerusteTila tila = null;
        switch (targetType) {
            case PERUSTEENOSA:
                tila = findProjektiPerusteTilaFor(id);
                break;
            case PERUSTE:
                tila = julkaistuPeruste(id) ? PerusteTila.VALMIS : findPerusteTilaFor(Peruste.class, id);
                break;
            default:
                return null;
        }
        if (tila == null) {
            throw new NotExistsException("Tilaa ei asetettu");
        }
        return tila;
    }

    private PerusteTila findProjektiPerusteTilaFor(Serializable id) {
        List<Perusteprojekti> valmiitProjektit = perusteProjektit.findProjektiById((Long) id).stream()
                .filter(projekti -> julkaisutRepository.countByPeruste(projekti.getPeruste()) > 0)
                .collect(Collectors.toList());

        return valmiitProjektit.isEmpty() ? findPerusteTilaFor(PerusteenOsa.class, id) : PerusteTila.VALMIS;
    }

    private PerusteTila findPerusteTilaFor(Class<? extends WithPerusteTila> entity, Serializable id) {
        if (id == null) {
            return null;
        }
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<PerusteTila> query = cb.createQuery(PerusteTila.class);
        Root<? extends WithPerusteTila> root = query.from(entity);
        query.select(root.<PerusteTila>get("tila")).where(cb.equal(root.get("id"), id));
        List<PerusteTila> result = em.createQuery(query).getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    private boolean julkaistuPeruste(Serializable id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<JulkaistuPeruste> query = cb.createQuery(JulkaistuPeruste.class);
        Root<JulkaistuPeruste> root = query.from(JulkaistuPeruste.class);
        Join<JulkaistuPeruste, Peruste> peruste = root.join(JulkaistuPeruste_.peruste);
        Predicate perusteId = cb.equal(peruste.get(Peruste_.id), id);

        query.select(root).where(perusteId);

        return !em.createQuery(query).getResultList().isEmpty();
    }
}
