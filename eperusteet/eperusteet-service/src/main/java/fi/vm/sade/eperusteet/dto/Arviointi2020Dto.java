package fi.vm.sade.eperusteet.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.dto.arviointi.ArviointiAsteikkoDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Yhdistää arviointiasteikon ja geneerisen arvioinnin jaetuksi rakenteeksi.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Arviointi2020Dto {
    private LokalisoituTekstiDto kohde;
    private Reference arviointiAsteikko;
    private List<OsaamistasonKriteerit2020Dto> osaamistasonKriteerit = new ArrayList<>();
}
