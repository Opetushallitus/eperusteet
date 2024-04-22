package fi.vm.sade.eperusteet.dto.yl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class OppiaineBaseDto implements ReferenceableDto {
    private Long id;
    private UUID tunniste;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    //"äitioppiaine" jos kyseessä on oppiaineen oppimäärä
    private Optional<Reference> oppiaine;
    private Optional<Boolean> koosteinen;
    private Optional<Boolean> abstrakti;
    private Optional<LokalisoituTekstiDto> nimi;
    private Optional<Long> jnro;
    private Date muokattu;

    @JsonIgnore
    public LokalisoituTekstiDto getNimiOrDefault(LokalisoituTekstiDto defaultNimi) {
        if (nimi != null) {
            return nimi.orElse(defaultNimi);
        } else {
            return defaultNimi;
        }
    }

    @JsonIgnore
    public Long getJnroOrDefault(Long defaultJnro) {
        if (jnro != null) {
            return jnro.orElse(defaultJnro);
        } else {
            return defaultJnro;
        }
    }
}
