package fi.vm.sade.eperusteet.dto.lukiokoulutus;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by jsikio
 */
@Getter
@Setter
public class AihekokonaisuusListausDto implements Serializable {

    private Long id;
    private String nimi;
    private String kuvaus;
    private Long jnro;
    private Date muokattu;

    public AihekokonaisuusListausDto() {
    }

    public AihekokonaisuusListausDto(Long id, String nimi, String kuvaus, Long jnro, Date muokattu) {
        this.id = id;
        this.nimi = nimi;
        this. kuvaus = kuvaus;
        this.jnro = jnro;
        this.muokattu = muokattu;
    }

}
