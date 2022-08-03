package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.domain.PoistetunTyyppi;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.Date;
import lombok.Data;

@Data
public class PoistettuSisaltoDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private String luoja;
    private Date luotu;
    private String muokkaaja;
    private Date muokattu;
    private PoistetunTyyppi tyyppi;
    private Long poistettuId;
}
