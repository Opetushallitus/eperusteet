package fi.vm.sade.eperusteet.dto.yl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
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
    protected LokalisoituTekstiDto nimi;
    private KoodiDto koodi;
    private Optional<Long> jnro;
    private Date muokattu;

    @JsonIgnore
    public Long getJnroOrDefault(Long defaultJnro) {
        if (jnro != null) {
            return jnro.orElse(defaultJnro);
        } else {
            return defaultJnro;
        }
    }

    public LokalisoituTekstiDto getNimi() {
        return Optional.ofNullable(nimi)
                .orElse(Optional.ofNullable(koodi).map(KoodiDto::getNimi).orElse(LokalisoituTekstiDto.of("")));
    }

    public String getNimiOrDefault(String kieli, String defaultNimi) {
        return getNimi().getOrDefault(Kieli.of(kieli), defaultNimi);
    }

    public String getNimiOrEmpty(String kieli) {
        return getNimiOrDefault(kieli, "");
    }
}
