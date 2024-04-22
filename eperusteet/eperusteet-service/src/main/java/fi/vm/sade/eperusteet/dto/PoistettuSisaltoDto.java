package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.domain.PoistetunTyyppi;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
