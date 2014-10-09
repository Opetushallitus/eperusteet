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

import fi.vm.sade.eperusteet.domain.validation.ValidateDateRange;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
/**
 *
 * @author harrik
 */
@Entity
@Table(name = "perusteprojekti")
@ValidateDateRange(start="toimikausiAlku", end="toimikausiLoppu")
@Audited
public class Perusteprojekti extends AbstractAuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @NotNull(message="Nimi ei voi olla tyhjä")
    private String nimi;

    @OneToOne(fetch = FetchType.LAZY, cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @Getter
    @Setter
    private Peruste peruste;

    @Getter
    @Setter
    @Column(unique = true)
    private String diaarinumero;

    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    @Setter
    private Date paatosPvm;

    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    @Setter
    @Column(name="toimikausi_alku")
    private Date toimikausiAlku;

    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    @Setter
    @Column(name="toimikausi_loppu")
    private Date toimikausiLoppu;

    @Getter
    @Setter
    private String tehtavaluokka;

    @Getter
    @Setter
    @Column(name = "ryhmaoid")
    private String ryhmaOid;

    @Getter
    @Setter
    private String tehtava;

    @Getter
    @Setter
    private String yhteistyotaho;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @NotNull
    private ProjektiTila tila = ProjektiTila.LAADINTA;

    public Perusteprojekti() {
    }

    public Perusteprojekti(String nimi, Peruste peruste, String diaarinumero, Date paatosPvm, Date toimikausiAlku, Date toimikausiLoppu, String tehtavaluokka, String ryhmaOid, String tehtava, String yhteistyotaho) {
        this.nimi = nimi;
        this.peruste = peruste;
        this.diaarinumero = diaarinumero;
        this.paatosPvm = paatosPvm;
        this.toimikausiAlku = toimikausiAlku;
        this.toimikausiLoppu = toimikausiLoppu;
        this.tehtavaluokka = tehtavaluokka;
        this.ryhmaOid = ryhmaOid;
        this.tehtava = tehtava;
        this.yhteistyotaho = yhteistyotaho;
    }
}