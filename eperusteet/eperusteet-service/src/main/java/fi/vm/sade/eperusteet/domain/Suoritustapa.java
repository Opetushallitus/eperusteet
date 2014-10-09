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

import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;

/**
 *
 * @author harrik
 */
@Entity
@Table(name = "suoritustapa")
@Audited
public class Suoritustapa implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @NotNull
    private Suoritustapakoodi suoritustapakoodi;

    @OneToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @JoinColumn(name = "sisalto_perusteenosaviite_id")
    private PerusteenOsaViite sisalto;

    @OneToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @JoinColumn(name = "tutkinnon_rakenne_id")
    private RakenneModuuli rakenne;

    @Getter
    @Setter
    @Column(name = "yksikko")
    private LaajuusYksikko laajuusYksikko;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "suoritustapa")
    @Getter
    @OrderBy("jarjestys,id")
    @BatchSize(size = 10)
    private Set<TutkinnonOsaViite> tutkinnonOsat = new HashSet<>();

    public void setTutkinnonOsat(Set<TutkinnonOsaViite> osat) {

        for (TutkinnonOsaViite v : osat) {
            v.setSuoritustapa(this);
        }

        tutkinnonOsat.retainAll(osat);
        tutkinnonOsat.addAll(osat);
    }

    public Suoritustapa() {
    }

    public Suoritustapa(Suoritustapakoodi suoritustapakoodi, PerusteenOsaViite sisalto, RakenneModuuli rakenne, LaajuusYksikko laajuusYksikko) {
        this.suoritustapakoodi = suoritustapakoodi;
        this.sisalto = sisalto;
        this.rakenne = rakenne;
        this.laajuusYksikko = laajuusYksikko;
    }

}
