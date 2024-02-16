package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.Date;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerusteInfoDto {
    private Long id;
    private PerusteVersionDto globalVersion;
    private LokalisoituTekstiDto nimi;
    private String diaarinumero;
    private Date voimassaoloLoppuu;
    private Date voimassaoloAlkaa;
    private PerusteTila tila;
    private Set<SuoritustapaDto> suoritustavat;
}
