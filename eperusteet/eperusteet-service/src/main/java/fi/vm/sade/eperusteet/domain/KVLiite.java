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

import fi.vm.sade.eperusteet.domain.arviointi.ArviointiAsteikko;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.tekstihaku.TekstihakuCollection;
import fi.vm.sade.eperusteet.domain.tekstihaku.TekstihakuCtx;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * @author nkala
 */
@Entity
@Table(name = "kvliite")
@Audited
public class KVLiite extends AbstractAuditedEntity implements Serializable, ReferenceableEntity, Tekstihaettava {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @Override
    public EntityReference getReference() {
        return new EntityReference(id);
    }

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "peruste_id")
    @Getter
    @Setter
    private Peruste peruste;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pohja_id")
    @Getter
    @Setter
    private KVLiite pohja;

//     Perusteesta:
//     Tutkinnon nimi (fi/sv/en)
//        - kirjoitetaanko vai tuleeko perusteesta?
//        - voimaantulopäivä
//        - diaarinumero
//    Tutkinnossa osoitettu ammatillinen osaaminen
//        - Tutkinnon muodostuminen (sanallinen  kuvaus)p

    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen suorittaneenOsaaminen;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen tyotehtavatJoissaVoiToimia;

    @Getter
    @Setter
    @Deprecated
    @Column(name = "tutkintotodistuksenAntaja")
    private String tutkintotodistuksenAntajaVanha;


    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen tutkintotodistuksenAntaja;

    @Getter
    @Setter
    @Deprecated
    @Column(name = "tutkinnostaPaattavaViranomainen")
    private String tutkinnostaPaattavaViranomainenVanha;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen tutkinnostaPaattavaViranomainen;

    @ManyToOne
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private ArviointiAsteikko arvosanaAsteikko;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen jatkoopintoKelpoisuus;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kansainvalisetSopimukset;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen saadosPerusta;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen tutkintotodistuksenSaaminen;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen pohjakoulutusvaatimukset;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen lisatietoja;

    @Override
    public void getTekstihaku(TekstihakuCollection haku) {
        haku.add("tekstihaku-suorittaneenosaaminen", getSuorittaneenOsaaminen());
        haku.add("tekstihaku-tyotehtavatjoissavoitoimia", getTyotehtavatJoissaVoiToimia());
    }

    @Override
    public TekstihakuCtx partialContext() {
        return TekstihakuCtx.builder()
                .build();
    }

    public TekstiPalanen getTutkintotodistuksenAntaja() {
        if (tutkintotodistuksenAntaja == null) {
            Map<Kieli, String> tekstit = new HashMap<>();
            for (Kieli kieli : Kieli.values()) {
                tekstit.put(kieli, getTutkintotodistuksenAntajaVanha());
            }
            return TekstiPalanen.of(tekstit);
        } else {
            return tutkintotodistuksenAntaja;
        }
    }

    public TekstiPalanen getTutkinnostaPaattavaViranomainen() {
        if (tutkinnostaPaattavaViranomainen == null) {
            Map<Kieli, String> tekstit = new HashMap<>();
            for (Kieli kieli : Kieli.values()) {
                tekstit.put(kieli, getTutkinnostaPaattavaViranomainenVanha());
            }
            return TekstiPalanen.of(tekstit);
        } else {
            return tutkinnostaPaattavaViranomainen;
        }
    }
}
