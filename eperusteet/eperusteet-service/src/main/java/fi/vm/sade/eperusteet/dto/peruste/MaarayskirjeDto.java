package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.liite.LiiteBaseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaarayskirjeDto {
    private Long id;
    private Map<Kieli, String> url;
    private Map<Kieli, LiiteBaseDto> liitteet;
}
