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

package fi.vm.sade.eperusteet.repository.custom;

import fi.vm.sade.eperusteet.domain.Peruste_;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.Perusteprojekti_;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepositoryCustom;
import java.util.HashMap;
import java.util.Set;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Subgraph;
import org.hibernate.annotations.QueryHints;

/**
 *
 * @author harrik
 */
public class PerusteprojektiRepositoryImpl implements PerusteprojektiRepositoryCustom{
    @PersistenceContext
    private EntityManager em;

    @Override
    public Perusteprojekti findByIdEager(Long id) {
        EntityGraph<Perusteprojekti> eg = em.createEntityGraph(Perusteprojekti.class);
        Subgraph<Set<Suoritustapa>> stGraph = eg.addSubgraph(Perusteprojekti_.peruste).addSubgraph(Peruste_.suoritustavat);
        stGraph.addAttributeNodes("sisalto", "rakenne", "tutkinnonOsat");
        stGraph.addSubgraph("tutkinnonOsat").addAttributeNodes("tutkinnonOsa");

        HashMap<String, Object> props = new HashMap<>();
        props.put(QueryHints.FETCHGRAPH, eg);
        Perusteprojekti p = em.find(Perusteprojekti.class, id, props);
        return p;
    }
}
