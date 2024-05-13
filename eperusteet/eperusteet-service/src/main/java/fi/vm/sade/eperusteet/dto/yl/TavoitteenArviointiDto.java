package fi.vm.sade.eperusteet.dto.yl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class TavoitteenArviointiDto implements ReferenceableDto {
    private Long id;
    private Optional<LokalisoituTekstiDto> arvioinninKohde;
    private Optional<LokalisoituTekstiDto> hyvanOsaamisenKuvaus;
    private Optional<LokalisoituTekstiDto> osaamisenKuvaus;
    private Optional<Integer> arvosana;
    private Set<Reference> opetuksenTavoitteet;
}
