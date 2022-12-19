package fi.vm.sade.eperusteet.dto.ammattitaitovaatimukset;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Created by autio on 19.10.2015.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmmattitaitovaatimusDto {
    private Long id;
    private LokalisoituTekstiDto selite;
    private String ammattitaitovaatimusKoodi;
    private Integer jarjestys;
}
