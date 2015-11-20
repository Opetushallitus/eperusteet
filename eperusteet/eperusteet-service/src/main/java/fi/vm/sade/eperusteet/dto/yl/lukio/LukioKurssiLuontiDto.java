/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.dto.yl.lukio;

import com.google.common.base.Optional;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokurssiTyyppi;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.TekstiOsaDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Builder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: tommiratamaa
 * Date: 29.9.15
 * Time: 14.54
 */
@Getter
@Setter
@Builder
public class LukioKurssiLuontiDto implements Serializable {
    @NotNull
    private LukiokurssiTyyppi tyyppi;
    private List<KurssinOppiaineDto> oppiaineet = new ArrayList<>();
    @NotNull
    private LokalisoituTekstiDto nimi;
    private String koodiArvo;
    private String koodiUri;
    private LokalisoituTekstiDto lokalisoituKoodi;
    private Optional<LokalisoituTekstiDto> kuvaus;
    private Optional<TekstiOsaDto> tavoitteet;
    private Optional<TekstiOsaDto> keskeinenSisalto;
    private Optional<TekstiOsaDto> tavoitteetJaKeskeinenSisalto;

    private String kommentti;

    public LukioKurssiLuontiDto() {
    }

    public LukioKurssiLuontiDto(LukiokurssiTyyppi tyyppi,
                                List<KurssinOppiaineDto> oppiaineet,
                                LokalisoituTekstiDto nimi,
                                String koodiArvo, String koodiUri,
                                LokalisoituTekstiDto lokalisoituKoodi,
                                Optional<LokalisoituTekstiDto> kuvaus,
                                Optional<TekstiOsaDto> tavoitteet,
                                Optional<TekstiOsaDto> keskeinenSisalto,
                                Optional<TekstiOsaDto> tavoitteetJaKeskeinenSisalto,
                                String kommentti) {
        this.tyyppi = tyyppi;
        this.oppiaineet = oppiaineet;
        this.nimi = nimi;
        this.koodiArvo = koodiArvo;
        this.koodiUri = koodiUri;
        this.lokalisoituKoodi = lokalisoituKoodi;
        this.kuvaus = kuvaus;
        this.tavoitteet = tavoitteet;
        this.keskeinenSisalto = keskeinenSisalto;
        this.tavoitteetJaKeskeinenSisalto = tavoitteetJaKeskeinenSisalto;
        this.kommentti = kommentti;
    }
}
