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
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.LokalisoituTeksti;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimus2019;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimus2019Kohdealue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.dto.GeneerinenArviointiasteikkoDto;
import fi.vm.sade.eperusteet.dto.arviointi.ArviointiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

/**
 *
 * @author jhyoty
 */
@Getter
@Setter
public class TutkinnonOsaKaikkiDto extends PerusteenOsaDto {
    private final String osanTyyppi = "tutkinnonosa";

    private LokalisoituTekstiDto kuvaus;
    private Long opintoluokitus;
    private KoodiDto koodi;
    private String koodiUri;
    private String koodiArvo;
    private List<OsaAlueKokonaanDto> osaAlueet;
    private TutkinnonOsaTyyppi tyyppi;
    private ValmaTelmaSisaltoDto valmaTelmaSisalto;
    private GeneerinenArviointiasteikkoDto geneerinenArviointiasteikko;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Ammattitaitovaatimukset2019Dto ammattitaitovaatimukset2019;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LokalisoituTekstiDto ammattitaidonOsoittamistavat;

    @Deprecated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LokalisoituTekstiDto tavoitteet;

    @Deprecated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ArviointiDto arviointi;

    @Deprecated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LokalisoituTekstiDto ammattitaitovaatimukset;

    public LokalisoituTekstiDto getAmmattitaitovaatimukset() {
        if (ammattitaitovaatimukset == null && ammattitaitovaatimukset2019 != null) {
            Map<Kieli, String> tekstit = new HashMap<>();
            for (Kieli kieli : Kieli.values()) {
                StringBuilder root = new StringBuilder();
                if (ammattitaitovaatimukset2019.getKohde() == null) {
                    continue;
                }
                String kohde = LokalisoituTekstiDto.getOrDefault(ammattitaitovaatimukset2019.getKohde(), kieli, null);
                root.append("<dl>");
                if (kohde != null) {
                    root.append("<dt><i>").append(kohde).append("</i></dt>");
                }

                { // Kohdealueettomat
                    for (Ammattitaitovaatimus2019Dto va : ammattitaitovaatimukset2019.getVaatimukset()) {
                        String str = LokalisoituTekstiDto.getOrDefault(va.getVaatimus(), kieli, null);
                        if (!StringUtils.isEmpty(str)) {
                            root.append("<dd style=\"display: list-item;\">").append(str).append("</dd>");
                        }
                    }
                    root.append("</dl>");
                }

                { // Kohdealueelliset
                    for (AmmattitaitovaatimustenKohdealue2019Dto ka : ammattitaitovaatimukset2019.getKohdealueet()) {
                        if (ka.getVaatimukset() == null || ka.getVaatimukset().isEmpty()) {
                            continue;
                        }

                        if (ka.getKuvaus() != null) {
                            String nimi = LokalisoituTekstiDto.getOrDefault(ka.getKuvaus(), kieli, null);
                            root.append("<b>").append(nimi).append("</b>");
                        }

                        root.append("<dl>");

                        if (kohde != null) {
                            root.append("<dt><i>").append(kohde).append("</i></dt>");
                        }

                        for (Ammattitaitovaatimus2019Dto va : ka.getVaatimukset()) {
                            String str = LokalisoituTekstiDto.getOrDefault(va.getVaatimus(), kieli, null);
                            if (!StringUtils.isEmpty(str)) {
                                root.append("<dd style=\"display: list-item;\">").append(str).append("</dd>");
                            }
                        }
                        root.append("</dl>");
                    }
                }

                tekstit.put(kieli, root.toString());
            }
            return LokalisoituTekstiDto.of(tekstit);
        }
        return ammattitaitovaatimukset;
    }

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
