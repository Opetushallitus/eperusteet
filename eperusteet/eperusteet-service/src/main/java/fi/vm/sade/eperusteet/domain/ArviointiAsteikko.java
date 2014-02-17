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

package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.dto.EntityReference;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

/**
 *
 * @author teele1
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@Immutable
@Table(name = "arviointiasteikko")
public class ArviointiAsteikko implements Serializable, CachedEntity {
    private static final long serialVersionUID = 1L;
    
    @Id
    private Long id;
    
    @OneToMany(fetch = FetchType.EAGER)
    @OrderColumn
    @Immutable
    private List<Osaamistaso> osaamistasot;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Osaamistaso> getOsaamistasot() {
        return osaamistasot;
    }

    public void setOsaamistasot(List<Osaamistaso> osaamistasot) {
        this.osaamistasot = osaamistasot;
    }

    @Override
    public EntityReference getReference() {
        return new EntityReference(id);
    }
}
