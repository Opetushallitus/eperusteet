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

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.dto.arviointi.ArviointiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author jhyoty
 */
@Getter
@Setter
public class TutkinnonOsaKaikkiDto extends PerusteenOsaDto {
    private final String osanTyyppi = "tutkinnonosa";
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LokalisoituTekstiDto tavoitteet;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ArviointiDto arviointi;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LokalisoituTekstiDto ammattitaitovaatimukset;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LokalisoituTekstiDto ammattitaidonOsoittamistavat;
    private LokalisoituTekstiDto kuvaus;
    private Long opintoluokitus;
    private KoodiDto koodi;
    private String koodiUri;
    private String koodiArvo;
    private List<OsaAlueKokonaanDto> osaAlueet;
    private TutkinnonOsaTyyppi tyyppi;

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
