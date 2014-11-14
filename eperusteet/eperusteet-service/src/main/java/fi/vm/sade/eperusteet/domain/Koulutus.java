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

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author harrik
 */
@Entity
@Table(name = "koulutus")
public class Koulutus implements Serializable {

    public Koulutus() {
    }

    public Koulutus(TekstiPalanen nimi, String koulutuskoodiArvo, String koulutuskoodiUri, String koulutusalakoodi, String opintoalakoodi) {
        this.nimi = nimi;
        this.koulutuskoodiArvo = koulutuskoodiArvo;
        this.koulutuskoodiUri = koulutuskoodiUri;
        this.koulutusalakoodi = koulutusalakoodi;
        this.opintoalakoodi = opintoalakoodi;
    }

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

    @Column(name = "koulutuskoodi_arvo")
    @Getter
    @Setter
    private String koulutuskoodiArvo;
    
    @Column(name = "koulutuskoodi_uri")
    @Getter
    @Setter
    private String koulutuskoodiUri;

    @Column(name = "koulutusala_koodi")
    @Getter
    @Setter
    private String koulutusalakoodi;

    @Column(name = "opintoala_koodi")
    @Getter
    @Setter
    private String opintoalakoodi;
}
