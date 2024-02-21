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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.dto.lops2019.Lops2019SisaltoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaKaikkiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteSuppeaDto;
import fi.vm.sade.eperusteet.dto.tuva.KoulutuksenOsaDto;
import fi.vm.sade.eperusteet.dto.tuva.TutkintoonvalmentavaSisaltoDto;
import fi.vm.sade.eperusteet.dto.vst.VapaasivistystyoSisaltoDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEOpetuksenSisaltoDto;
import fi.vm.sade.eperusteet.dto.yl.EsiopetuksenPerusteenSisaltoDto;
import fi.vm.sade.eperusteet.dto.yl.PerusopetuksenPerusteenSisaltoDto;
import fi.vm.sade.eperusteet.dto.yl.TPOOpetuksenSisaltoDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LukiokoulutuksenPerusteenSisaltoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author nkala
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PerusteKaikkiDto extends PerusteBaseDto {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    Set<SuoritustapaLaajaDto> suoritustavat;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<TutkinnonOsaKaikkiDto> tutkinnonOsat;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<KoulutuksenOsaDto> koulutuksenOsat;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("perusopetus")
    private PerusopetuksenPerusteenSisaltoDto perusopetuksenPerusteenSisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("lukiokoulutus")
    private LukiokoulutuksenPerusteenSisaltoDto lukiokoulutuksenPerusteenSisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("lops2019")
    private Lops2019SisaltoDto lops2019Sisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("esiopetus")
    private EsiopetuksenPerusteenSisaltoDto esiopetuksenPerusteenSisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("aipe")
    private AIPEOpetuksenSisaltoDto aipeOpetuksenPerusteenSisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("tpo")
    private TPOOpetuksenSisaltoDto tpoOpetuksenSisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("vapaasivistystyo")
    private VapaasivistystyoSisaltoDto vstSisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("tutkintoonvalmentava")
    private TutkintoonvalmentavaSisaltoDto tuvasisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("opas")
    private OpasSisaltoDto oppaanSisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("digitaalinenosaaminen")
    private DigitaalisenOsaamisenSisaltoDto digitaalinenOsaaminenSisalto;

    @JsonIgnore
    public Set<PerusteenSisaltoDto> getSisallot() {
        if (PerusteTyyppi.OPAS.equals(this.getTyyppi())) {
            return Collections.singleton(this.getOppaanSisalto());
        } else {
            if (KoulutustyyppiToteutus.AMMATILLINEN.equals(this.getToteutus())) {
                return new HashSet<>(this.getSuoritustavat());
            } else if (this.getPerusopetuksenPerusteenSisalto() != null) {
                return Collections.singleton(this.getPerusopetuksenPerusteenSisalto());
            } else if (this.getLops2019Sisalto() != null) {
                return Collections.singleton(this.getLops2019Sisalto());
            } else if (this.getEsiopetuksenPerusteenSisalto() != null) {
                return Collections.singleton(this.getEsiopetuksenPerusteenSisalto());
            } else if (this.getLukiokoulutuksenPerusteenSisalto() != null) {
                return Collections.singleton(this.getLukiokoulutuksenPerusteenSisalto());
            } else if (this.getAipeOpetuksenPerusteenSisalto() != null) {
                return Collections.singleton(this.getAipeOpetuksenPerusteenSisalto());
            } else if (this.getTpoOpetuksenSisalto() != null) {
                return Collections.singleton(this.getTpoOpetuksenSisalto());
            } else if (this.getVstSisalto() != null) {
                return Collections.singleton(this.getVstSisalto());
            } else if (this.getTuvasisalto() != null) {
                return Collections.singleton(this.getTuvasisalto());
            } else if (this.getDigitaalinenOsaaminenSisalto() != null) {
                return Collections.singleton(this.getDigitaalinenOsaaminenSisalto());
            }
        }
        return new HashSet<>();
    }

    public List<TutkinnonOsaKaikkiDto> getTutkinnonOsat() {
        if (tutkinnonOsat == null) {
            return null;
        }

        return tutkinnonOsat.stream().peek(tutkinnonosa -> {
            Map<Long, TutkinnonOsaViiteSuppeaDto> viitteetLaajuusMap = suoritustavat.stream()
                    .map(SuoritustapaLaajaDto::getTutkinnonOsat)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toMap((viite -> viite.getTutkinnonOsa().getIdLong()), viite -> viite, (first, second) -> first));
            tutkinnonosa.setLaajuus(Optional.ofNullable(viitteetLaajuusMap.get(tutkinnonosa.getId())).map(TutkinnonOsaViiteSuppeaDto::getLaajuus).orElse(null));
            tutkinnonosa.setLaajuusMaksimi(Optional.ofNullable(viitteetLaajuusMap.get(tutkinnonosa.getId())).map(TutkinnonOsaViiteSuppeaDto::getLaajuusMaksimi).orElse(null));
        }).collect(Collectors.toList());
    }
}
