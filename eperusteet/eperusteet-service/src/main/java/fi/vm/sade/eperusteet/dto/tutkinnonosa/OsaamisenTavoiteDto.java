package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("osaamistavoite")
public class OsaamisenTavoiteDto {
    private Long id;
    private LokalisoituTekstiDto kohde;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto selite;
    private List<LokalisoituTekstiDto> tavoitteet;
}
