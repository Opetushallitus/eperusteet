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
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Kuvaa oppimäärän yhteen vuosiluokkakokonaisuuteen osalta.
 *
 * @author jhyoty
 */
@Entity
@Audited
@Table(name = "yl_oppiainn_vuosiluokkakokonaisuus")
public class OppiaineenVuosiluokkaKokonaisuus extends AbstractAuditedReferenceableEntity {

    @Getter
    @Setter
    @ManyToOne(optional = false)
    @NotNull
    private Oppiaine oppiaine;

    @Getter
    @Setter
    @ManyToOne
    @NotNull
    private VuosiluokkaKokonaisuus vuosiluokkaKokonaisuus;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true, fetch = FetchType.LAZY)
    private TekstiOsa tehtava;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true, fetch = FetchType.LAZY)
    private TekstiOsa tyotavat;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true, fetch = FetchType.LAZY)
    private TekstiOsa ohjaus;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true, fetch = FetchType.LAZY)
    private TekstiOsa arviointi;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable
    @OrderColumn
    @Getter
    @Setter
    private List<OpetuksenTavoite> tavoitteet = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable
    @OrderColumn
    @Getter
    @Setter
    private List<KeskeinenSisaltoalue> sisaltoAlueet = new ArrayList<>();
}
