package fi.vm.sade.eperusteet.dto.ammattitaitovaatimukset;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmmattitaitovaatimusDto {
    private Long id;
    private LokalisoituTekstiDto selite;
    private String ammattitaitovaatimusKoodi;
    private Integer jarjestys;
}
