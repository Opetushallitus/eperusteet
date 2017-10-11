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

package fi.vm.sade.eperusteet.domain.arviointi;

import fi.vm.sade.eperusteet.domain.Osaamistaso;
import fi.vm.sade.eperusteet.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import java.io.Serializable;
import java.util.List;
import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 *
 * @author teele1
 */
@Entity(name = "ArviointiAsteikko")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "arviointiasteikko")
public class ArviointiAsteikko implements Serializable, ReferenceableEntity {

    @Id
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @OrderColumn(name = "osaamistasot_order")
    @OneToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "arviointiasteikko_osaamistaso",
            joinColumns = @JoinColumn(name = "arviointiasteikko_id"),
            inverseJoinColumns = @JoinColumn(name = "osaamistasot_id")
    )
    private List<Osaamistaso> osaamistasot;

    @Override
    public EntityReference getReference() {
        return new EntityReference(id);
    }
}
