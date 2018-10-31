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
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import org.hibernate.annotations.Immutable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.ObjectUtils;

/**
 *
 * @author nkala
 */

@Entity
@Immutable
@Table(name = "koodi")
@EqualsAndHashCode(of = {"koodisto", "uri", "versio"})
public class Koodi implements Serializable {

    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Getter
    @Setter
    @NotNull
    private String uri; // Uniikki koodistosta minkä sisällöstä ei voi päätellä mitään

    @Getter
    @Setter
    @NotNull
    private String koodisto;

    @Getter
    @Setter
    private Long versio; // Oletuksena null milloin käytetään uusinta koodiston versiota

    public Koodi() {
    }

    public Koodi(String uri, String koodisto) {
        this.uri = uri;
        this.koodisto = koodisto;
        this.versio = null;
    }

    public static void validateChange(Koodi a, Koodi b) {
        if (a != null && !Objects.equals(a, b)) {
            throw new BusinessRuleViolationException("koodia-ei-voi-muuttaa");
        }
    }

    @PrePersist
    public void onPrePersist() {
        if (!ObjectUtils.isEmpty(getUri())) {
            String[] osat = getUri().split("_");
            if (osat.length > 1) {
                String uriKoodisto = osat[0];
                if (!getKoodisto().equals(uriKoodisto)) {
                    throw new BusinessRuleViolationException("uri: " + getUri() + " ei vastaa koodistoa: " + getKoodisto());
                }
            }
        }
    }

}
