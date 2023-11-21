package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.Date;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MuuMaaraysDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private Map<Kieli, String> url;
    private Date muokattu;
    private String muokkaaja;
}
