package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.dto.Arviointi2020Dto;
import fi.vm.sade.eperusteet.dto.GeneerinenArviointiasteikkoDto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeName("osaamistavoite2020")
public class Osaamistavoite2020Dto extends OsaamistavoiteDto {
    private Reference esitieto;
    private LokalisoituTekstiDto tunnustaminen;
    private Arviointi2020Dto arviointi;

    @JsonIgnore
    private GeneerinenArviointiasteikkoDto geneerinenArviointiasteikko;

}
