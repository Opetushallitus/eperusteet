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
package fi.vm.sade.eperusteet.dto.peruste;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 *
 * FIXME: tutkintonimike, osaamisala ja tutkinnon osa käyttämään KoodiDto:ta
 *
 * @author nkala
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutkintonimikeKoodiDto {
    private Long id;
    private Reference peruste;
    private String tutkinnonOsaUri;
    private String tutkinnonOsaArvo;
    private String osaamisalaUri;
    private String osaamisalaArvo;
    private String tutkintonimikeUri;
    private String tutkintonimikeArvo;
    private LokalisoituTekstiDto nimi;

    @Deprecated
    public TutkintonimikeKoodiDto(Reference peruste, String tutkinnonOsaArvo, String osaamisalaArvo, String tutkintonimikeArvo) {
        this.peruste = peruste;
        this.tutkinnonOsaArvo = tutkinnonOsaArvo;
        this.tutkinnonOsaUri = "tutkinnonosat_" + tutkinnonOsaArvo;
        this.osaamisalaArvo = osaamisalaArvo;
        this.osaamisalaUri = "osaamisala_" + osaamisalaArvo;
        this.tutkintonimikeArvo = tutkintonimikeArvo;
        this.tutkintonimikeUri = "tutkintonimikkeet_" + tutkintonimikeArvo;
    }

    @JsonIgnore
    public boolean isTutkintonimikeTemporary() {
        return tutkintonimikeUri.contains("temporary");
    }

}
