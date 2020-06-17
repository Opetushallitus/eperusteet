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

package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import com.fasterxml.jackson.annotation.*;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaAlue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaAlueTyyppi;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.TaiteenalaDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.osaviitteet.AihekokonaisuudetLaajaDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.osaviitteet.LukioOpetussuunnitelmaRakenneLaajaDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.osaviitteet.OpetuksenYleisetTavoitteetLaajaDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 *
 * @author harrik
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OsaAlueDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto kuvaus;
    private OsaAlueTyyppi tyyppi;
    private KoodiDto koodi;
    private String koodiUri;
    private String koodiArvo;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Kieli kieli;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ValmaTelmaSisaltoDto valmaTelmaSisalto;

    public String getKoodiUri() {
        KoodiDto koodi = this.getKoodi();
        if (koodi != null) {
            return koodi.getUri();
        } else {
            return koodiUri;
        }
    }

    public String getKoodiArvo() {
        KoodiDto koodi = this.getKoodi();
        if (koodi != null) {
            return koodi.getArvo();
        } else {
            return koodiArvo;
        }
    }
}
