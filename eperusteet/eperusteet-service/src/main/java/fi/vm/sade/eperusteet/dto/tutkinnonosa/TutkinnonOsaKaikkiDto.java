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
import fi.vm.sade.eperusteet.dto.GeneerinenArviointiasteikkoKaikkiDto;
import fi.vm.sade.eperusteet.dto.KevytTekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.arviointi.ArviointiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty("Yhteisen tutkinnon osan osa-alueet")
    private List<OsaAlueKokonaanDto> osaAlueet;

    @ApiModelProperty("Ilmaisee onko kyseessä normaali vai yhteinen osa (uusi tai vanha)")
    private TutkinnonOsaTyyppi tyyppi;
    private ValmaTelmaSisaltoDto valmaTelmaSisalto;

    @ApiModelProperty("Yleinen perusteen ulkopuolella käytetty arviointiasteikko. Käytetään kaikissa uusissa perusteissa.")
    private GeneerinenArviointiasteikkoKaikkiDto geneerinenArviointiasteikko;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Tutkinnon osan lisätarkennukset")
    private List<KevytTekstiKappaleDto> vapaatTekstit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Uusien reformin mukaisien perusteiden ammattitaitovaatimukset")
    private Ammattitaitovaatimukset2019Dto ammattitaitovaatimukset2019;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LokalisoituTekstiDto ammattitaidonOsoittamistavat;

    @Deprecated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Vanhentunut rakenteeton tavoitteet. Ei käytössä uusissa reformin mukaisissa tutkinnon osissa.")
    private LokalisoituTekstiDto tavoitteet;

    @Deprecated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Vanhentunut tutkinnon osa -kohtainen arviointi")
    private ArviointiDto arviointi;

    @Deprecated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Vanhentunut rakenteeton ammattitaitovaatimukset. Ei käytössä uusissa reformin mukaisissa tutkinnon osissa.")
    private LokalisoituTekstiDto ammattitaitovaatimukset;

    public LokalisoituTekstiDto getAmmattitaitovaatimukset() {
        if (ammattitaitovaatimukset == null && ammattitaitovaatimukset2019 != null) {
            Map<Kieli, String> tekstit = new HashMap<>();
            for (Kieli kieli : Kieli.values()) {
                StringBuilder root = new StringBuilder();
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
                            if (nimi != null) {
                                root.append("<b>").append(nimi).append("</b>");
                            }
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

                String result = root.toString().replaceAll("<dl></dl>", "");

                if (!result.isEmpty()) {
                    tekstit.put(kieli, result);
                }
            }
            return LokalisoituTekstiDto.of(tekstit.isEmpty() ? null : tekstit);
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

    @Override
    public LokalisoituTekstiDto getNimi() {
        if (koodi != null && koodi.getNimi() != null && !org.springframework.util.CollectionUtils.isEmpty(koodi.getNimi().getTekstit())) {
            Map<String, String> kielet = new HashMap<>();
            kielet.computeIfAbsent("fi", val -> koodi.getNimi().getTekstit().getOrDefault(Kieli.FI, super.getNimi().get(Kieli.FI)));
            kielet.computeIfAbsent("sv", val -> koodi.getNimi().getTekstit().getOrDefault(Kieli.SV, super.getNimi().get(Kieli.SV)));
            kielet.computeIfAbsent("en", val -> koodi.getNimi().getTekstit().getOrDefault(Kieli.EN, super.getNimi().get(Kieli.EN)));
            return new LokalisoituTekstiDto(kielet);
        } else {
            return super.getNimi();
        }
    }
}
