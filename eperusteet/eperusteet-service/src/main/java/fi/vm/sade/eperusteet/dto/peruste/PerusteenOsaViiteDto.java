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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.vm.sade.eperusteet.dto.Reference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 *
 * @author jhyoty
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class PerusteenOsaViiteDto<R extends PerusteenOsaDto> {

    private Long id;
    @JsonProperty("_perusteenOsa")
    private Reference perusteenOsaRef;
    private R perusteenOsa;

    public PerusteenOsaViiteDto(R perusteenOsa) {
        this.perusteenOsa = perusteenOsa;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @AllArgsConstructor
    public static class Matala extends PerusteenOsaViiteDto<PerusteenOsaDto.Laaja> {
        private List<Reference> lapset;

        public Matala() {
        }

        public Matala(PerusteenOsaDto.Laaja perusteenOsa) {
            super(perusteenOsa);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Puu<R extends PerusteenOsaDto, L extends Puu<R, L>> extends PerusteenOsaViiteDto<R> {
        private List<L> lapset;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class Laaja extends Puu<PerusteenOsaDto.Laaja, Laaja> {
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class Suppea extends Puu<PerusteenOsaDto.Suppea, Suppea> {
    }

}
