package fi.vm.sade.eperusteet.dto.yl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteRakenneOsa;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class LaajaalainenOsaaminenDto implements ReferenceableDto, AIPEHasId {
    private Long id;
    private UUID tunniste;
    private Optional<LokalisoituTekstiDto> nimi;
    private Optional<LokalisoituTekstiDto> kuvaus;
    private Date muokattu;

    public PerusteRakenneOsa getPerusteenOsa() {
        return new PerusteRakenneOsa("aipe_laajaalainen_osaaminen", getNimi().get());
    }
}
