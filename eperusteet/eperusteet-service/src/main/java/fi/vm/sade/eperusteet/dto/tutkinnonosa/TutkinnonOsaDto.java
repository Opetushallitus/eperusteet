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

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.dto.KevytTekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.ammattitaitovaatimukset.AmmattitaitovaatimusKohdealueetDto;
import fi.vm.sade.eperusteet.dto.arviointi.ArviointiDto;
import fi.vm.sade.eperusteet.dto.peruste.Navigable;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKevytDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import org.springframework.util.CollectionUtils;

/**
 * @author jhyoty
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("tutkinnonosa")
@Builder
public class TutkinnonOsaDto extends PerusteenOsaDto.Laaja {
    private LokalisoituTekstiDto tavoitteet;
    private ArviointiDto arviointi;
    private List<AmmattitaitovaatimusKohdealueetDto> ammattitaitovaatimuksetLista;
    private LokalisoituTekstiDto ammattitaitovaatimukset;
    private LokalisoituTekstiDto ammattitaidonOsoittamistavat;
    private LokalisoituTekstiDto kuvaus;
    private KoodiDto koodi;
    private String koodiUri;
    private String koodiArvo;
    private List<OsaAlueDto> osaAlueet;
    private List<KevytTekstiKappaleDto> vapaatTekstit;
    private TutkinnonOsaTyyppi tyyppi;
    private ValmaTelmaSisaltoDto valmaTelmaSisalto;
    private Ammattitaitovaatimukset2019Dto ammattitaitovaatimukset2019;
    private Reference geneerinenArviointiasteikko;
    private PerusteKevytDto alkuperainenPeruste;

    public TutkinnonOsaDto (LokalisoituTekstiDto nimi, PerusteTila tila, PerusteenOsaTunniste tunniste) {
        super(nimi, tila, tunniste);
    }

    @Override
    public String getOsanTyyppi() {
        return "tutkinnonosa";
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
        if (koodi != null && koodi.getNimi() != null && !CollectionUtils.isEmpty(koodi.getNimi().getTekstit())) {
            Map<String, String> kielet = new HashMap<>();
            kielet.computeIfAbsent("fi", val -> koodi.getNimi().getTekstit().getOrDefault(Kieli.FI, super.getNimi().get(Kieli.FI)));
            kielet.computeIfAbsent("sv", val -> koodi.getNimi().getTekstit().getOrDefault(Kieli.SV, super.getNimi().get(Kieli.SV)));
            kielet.computeIfAbsent("en", val -> koodi.getNimi().getTekstit().getOrDefault(Kieli.EN, super.getNimi().get(Kieli.EN)));
            return new LokalisoituTekstiDto(kielet);
        } else {
            return super.getNimi();
        }
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.tutkinnonosa;
    }
}
