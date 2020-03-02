package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.domain.MuokkausTapahtuma;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PerusteenMuokkaustietoDto {

    private Long id;
    private LokalisoituTekstiDto nimi;
    private MuokkausTapahtuma tapahtuma;
    private Long perusteId;
    private Long kohdeId;
    private NavigationType kohde;
    private Date luotu;
    private String muokkaaja;
    private String lisatieto;
    private boolean poistettu;
}
