package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.TiedoteJulkaisuPaikka;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKevytDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TiedoteKevytDto {
    private Long id;
    private LokalisoituTekstiDto otsikko;
    private Set<TiedoteJulkaisuPaikka> julkaisupaikat;
    private Set<KoulutusTyyppi> koulutustyypit;
    private Set<PerusteKevytDto> perusteet;
    private Date luotu;
    private Date muokattu;
}
