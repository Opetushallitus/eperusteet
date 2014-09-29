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
package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 *
 * @author jhyoty
 */
@Entity
@Audited
@Table(name = "yl_perusopetuksen_perusteen_sisalto")
public class PerusopetuksenPerusteenSisalto extends AbstractAuditedReferenceableEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @JoinColumn
    private PerusteenOsaViite sisalto;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable
    @Getter
    @Setter
    private Set<LaajaalainenOsaaminen> laajaAlalaisetOsaamiset;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable
    @Getter
    @Setter
    private Set<Oppiaine> oppiaineet;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable
    @Getter
    @Setter
    private Set<VuosiluokkaKokonaisuus> vuosiluokkakokonaisuudet;

}
