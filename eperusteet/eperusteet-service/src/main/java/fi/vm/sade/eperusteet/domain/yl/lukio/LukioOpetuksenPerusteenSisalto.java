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
package fi.vm.sade.eperusteet.domain.yl.lukio;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.yl.AbstractOppiaineOpetuksenSisalto;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * User: tommiratamaa
 * Date: 9.9.15
 * Time: 13.26
 */
@Entity
@Audited
@Table(name = "yl_lukioopetuksen_perusteen_sisalto", schema = "public")
public class LukioOpetuksenPerusteenSisalto extends AbstractOppiaineOpetuksenSisalto {

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
    private PerusteenOsaViite sisalto = new PerusteenOsaViite();

    @Getter
    @Audited
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "yl_lukioopetuksen_perusteen_sisalto_yl_oppiaine", joinColumns = @JoinColumn(name = "sisalto_id", nullable = false, updatable = false),
        inverseJoinColumns = @JoinColumn(name = "oppiaine_id", nullable = false, updatable = false))
    private Set<Oppiaine> oppiaineet = new HashSet<>(0);

    @Getter
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "perusteenSisalto")
    private Set<Lukiokurssi> kurssit = new HashSet<>(0);

    public LukioOpetuksenPerusteenSisalto kloonaa(Peruste peruste) {
        LukioOpetuksenPerusteenSisalto kopio = new LukioOpetuksenPerusteenSisalto();
        kopio.peruste = peruste;
        kopio.sisalto = this.sisalto.kloonaa();
        return kopio;
    }
}
