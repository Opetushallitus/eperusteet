package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.liite.LiiteBaseDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**login
 * @author isaul
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MuutosmaaraysDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto url;
    private Map<Kieli, LiiteBaseDto> liitteet = new HashMap<>();
}
