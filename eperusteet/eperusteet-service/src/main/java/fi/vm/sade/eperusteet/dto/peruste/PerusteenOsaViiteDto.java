package fi.vm.sade.eperusteet.dto.peruste;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.vm.sade.eperusteet.dto.Reference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

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
