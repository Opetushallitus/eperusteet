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

package fi.vm.sade.eperusteet.domain.yl.aipe;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 *
 * @author nkala
 */
@Entity
@Audited
@Table(name = "yl_aipe_sisalto", schema = "public")
public class AIPESisalto extends AbstractAuditedReferenceableEntity {

    @RelatesToPeruste
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Getter
    @Setter
    @NotNull
    @JoinColumn(name = "peruste_id", nullable = false, updatable = false, unique = true)
    private Peruste peruste;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @Getter
    @Setter
    @JoinColumn(name="sisalto_id")
    private PerusteenOsaViite sisalto = new PerusteenOsaViite(this);

//    @Getter
//    @Setter
//    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
//    @JoinColumn(name="opetuksen_yleiset_tavoitteet_id")
//    private OpetuksenYleisetTavoitteet opetuksenYleisetTavoitteet;

    public AIPESisalto kloonaa(Peruste peruste) {
        AIPESisalto kopio = new AIPESisalto();
        kopio.peruste = peruste;
        kopio.sisalto = this.sisalto.kloonaa();
        return kopio;
    }
}
