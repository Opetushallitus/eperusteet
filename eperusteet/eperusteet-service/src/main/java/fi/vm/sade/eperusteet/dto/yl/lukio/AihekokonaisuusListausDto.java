package fi.vm.sade.eperusteet.dto.yl.lukio;

import fi.vm.sade.eperusteet.domain.LokalisoituTeksti;
import fi.vm.sade.eperusteet.dto.util.Lokalisoitava;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.stream.Stream;

import static fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto.localizeLaterById;

/**
 * Created by jsikio
 */
@Getter
@Setter
public class AihekokonaisuusListausDto implements Serializable, Lokalisoitava {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto kuvaus;
    private Long jnro;
    private Date muokattu;

    public AihekokonaisuusListausDto() {
    }

    public AihekokonaisuusListausDto(Long id, Long nimiId, Long kuvausId, Long jnro, Date muokattu) {
        this.id = id;
        this.nimi = localizeLaterById(nimiId);
        this.kuvaus = localizeLaterById(kuvausId);
        this.jnro = jnro;
        this.muokattu = muokattu;
    }

    @Override
    public Stream<LokalisoituTekstiDto> lokalisoitavatTekstit() {
        return Stream.of(nimi, kuvaus);
    }
}
