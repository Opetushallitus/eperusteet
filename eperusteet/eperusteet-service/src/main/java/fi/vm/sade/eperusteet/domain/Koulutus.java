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
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author harrik
 */
@Entity
@Table(name = "koulutus")
public class Koulutus implements Serializable, Mergeable<Koulutus>{

    public Koulutus() {
    }

    public Koulutus(TekstiPalanen nimi, String koulutuskoodiArvo, String koulutuskoodiUri, String koulutusalakoodi, String opintoalakoodi) {
        this.nimi = nimi;
        this.koulutuskoodiArvo = koulutuskoodiArvo;
        this.koulutuskoodiUri = koulutuskoodiUri;
        this.koulutusalakoodi = koulutusalakoodi;
        this.opintoalakoodi = opintoalakoodi;
    }

    @RelatesToPeruste
    @NotAudited
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "peruste_koulutus",
            inverseJoinColumns = @JoinColumn(name = "peruste_id"),
            joinColumns = @JoinColumn(name = "koulutus_id"))
    @Getter
    @Setter
    private Set<Peruste> perusteet;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Deprecated // Haetaan tarvittaessa koodistopalvelusta
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

    @Override
    public void mergeState(Koulutus updated) {
        this.setKoulutuskoodiArvo(updated.getKoulutuskoodiArvo());
        this.setKoulutuskoodiUri(updated.getKoulutuskoodiUri());
        this.setKoulutusalakoodi(updated.getKoulutusalakoodi());
        this.setOpintoalakoodi(updated.getOpintoalakoodi());
    }

}
