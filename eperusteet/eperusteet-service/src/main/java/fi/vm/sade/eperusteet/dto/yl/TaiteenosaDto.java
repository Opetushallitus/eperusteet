package fi.vm.sade.eperusteet.dto.yl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaiteenosaDto {

    private Long id;
    private LokalisoituTekstiDto nimi;
    private BigDecimal laajuus;
    private LokalisoituTekstiDto kuvaus;
    private List<LokalisoituTekstiDto> tavoitteet;

    public NavigationType getNavigationType() {
        return NavigationType.taiteenosa;
    }
}
