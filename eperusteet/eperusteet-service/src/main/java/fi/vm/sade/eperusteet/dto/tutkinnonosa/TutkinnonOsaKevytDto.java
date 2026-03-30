package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import java.sql.Date;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.KoodiOrNimiUtil;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor 
public class TutkinnonOsaKevytDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private KoodiDto koodi;
    private Date muokattu;
 
    public LokalisoituTekstiDto getNimi() {
        return KoodiOrNimiUtil.getNimi(koodi, nimi);
    }
}
