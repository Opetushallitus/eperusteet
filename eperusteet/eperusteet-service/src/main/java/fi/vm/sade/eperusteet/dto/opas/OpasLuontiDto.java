package fi.vm.sade.eperusteet.dto.opas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKevytDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class OpasLuontiDto extends OpasDto {

    private Long pohjaId;
    private Set<PerusteKevytDto> oppaanPerusteet;
    private Set<KoulutusTyyppi> oppaanKoulutustyypit;
    private LokalisoituTekstiDto lokalisoituNimi;

}
