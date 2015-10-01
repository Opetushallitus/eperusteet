package fi.vm.sade.eperusteet.dto.lukiokoulutus;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * Created by jsikio on 28.9.2015.
 */
@Getter
@Setter
public class AihekokonaisuudetDto {

    private Long id;
    private UUID tunniste;
    private LokalisoituTekstiDto otsikko;
    private LokalisoituTekstiDto yleiskuvaus;
    private List<AihekokonaisuusDto> aihekokonaisuudet;

}
