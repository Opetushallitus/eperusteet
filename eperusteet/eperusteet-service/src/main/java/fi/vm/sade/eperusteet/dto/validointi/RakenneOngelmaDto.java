package fi.vm.sade.eperusteet.dto.validointi;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RakenneOngelmaDto {
    public String ongelma;
    public LokalisoituTekstiDto ryhma;
}
