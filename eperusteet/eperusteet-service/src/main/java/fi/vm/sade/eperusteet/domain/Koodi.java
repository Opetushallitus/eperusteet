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

import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

/**
 *
 * @author nkala
 */
@Entity
@Table(name = "koodi")
@EqualsAndHashCode(of = {"uri","arvo"})
public class Koodi implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    private TekstiPalanen nimi;

    @Column(name = "arvo")
    @Getter
    @Setter
    private String arvo;

    @Column(name = "uri")
    @Getter
    @Setter
    private String uri;

    @RelatesToPeruste
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(name = "peruste_osaamisala",
            inverseJoinColumns = @JoinColumn(name = "peruste_id"),
            joinColumns = @JoinColumn(name = "osaamisala_id"))
    @Getter
    @Setter
    private Peruste peruste;
}
