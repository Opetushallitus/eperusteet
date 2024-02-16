package fi.vm.sade.eperusteet.dto.util;

import fi.vm.sade.eperusteet.domain.Kieli;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LokalisoituTekstiHakuDto {
    private long id;
    private Kieli kieli;
    private String teksti;

}
