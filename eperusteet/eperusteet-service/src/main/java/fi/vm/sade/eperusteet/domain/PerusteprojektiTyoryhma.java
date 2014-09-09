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

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author nkala
 */
@Entity
@Table(name = "perusteprojekti_tyoryhma")
public class PerusteprojektiTyoryhma implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    private Perusteprojekti perusteprojekti;

    @Getter
    @Setter
    @NotNull(message = "Käyttäjän oid ei voi olla tyhjä")
    @Column(name = "kayttaja_oid")
    private String kayttajaOid;

    @Getter
    @Setter
    @NotNull(message = "Työryhmän oid ei voi olla tyhjä")
    @Column(name = "tyoryhma_oid")
    private String tyoryhmaOid;

    public PerusteprojektiTyoryhma(Perusteprojekti perusteprojekti, String kayttajaOid, String tyoryhmaOid) {
        this.perusteprojekti = perusteprojekti;
        this.kayttajaOid = kayttajaOid;
        this.tyoryhmaOid = tyoryhmaOid;
    }

    public PerusteprojektiTyoryhma() {
    }
}
