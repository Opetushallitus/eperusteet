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

import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author nkala
 */

@Entity
@Getter
@Setter
@Table(name = "koodi")
@EqualsAndHashCode(of = {"uri", "versio"})
public class Koodi implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    @NotEmpty
    private String uri; // Uniikki koodistosta minkä sisällöstä ei voi päätellä mitään

    @NotNull
    @NotEmpty
    private String koodisto;

    private Long versio; // Oletuksena null milloin käytetään uusinta koodiston versiota

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private TekstiPalanen nimi;

    public Koodi() {
    }

    public Koodi(final String uri) {
        this.uri = uri;
    }

    public Koodi(final String uri, final String koodisto) {
        this.uri = uri;
        this.koodisto = koodisto;
        this.versio = null;
    }

    public boolean isTemporary() {
        return uri != null && uri.startsWith("temporary_");
    }

    public String getKoodisto() {
        if (isTemporary()) {
            return getUri().split("_")[1];
        } else {
            return this.koodisto;
        }
    }

    public static void validateChange(final Koodi a, final Koodi b) {
        if (a != null && !Objects.equals(a, b)) {
            throw new BusinessRuleViolationException("koodia-ei-voi-muuttaa");
        }
    }

    @PrePersist
    public void onPrePersist() {
        final String[] osat = this.getUri().split("_");
        if (osat.length < 2) {
            throw new BusinessRuleViolationException("virheellinen-koodi-uri: " + this.getUri());
        }

        final String uriKoodisto = osat[0];
        if (ObjectUtils.isEmpty(this.koodisto)) {
            this.koodisto = uriKoodisto;
        } else if (!Objects.equals(this.getKoodisto(), uriKoodisto)) {
            throw new BusinessRuleViolationException("uri: " + this.getUri() + " ei vastaa koodistoa: " + this.getKoodisto());
        }
    }


    static public Koodi of(String koodisto, String arvo) {
        Koodi result = new Koodi();
        result.setUri(koodisto + "_" + arvo);
        result.setKoodisto(koodisto);
        return result;
    }

}
