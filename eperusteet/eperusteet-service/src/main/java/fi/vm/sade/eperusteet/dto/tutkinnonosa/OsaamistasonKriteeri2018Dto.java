package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OsaamistasonKriteeri2018Dto {
    private LokalisoituTekstiDto kuvaus;

    public OsaamistasonKriteeri2018Dto() {
    }

    public OsaamistasonKriteeri2018Dto(LokalisoituTekstiDto kuvaus) {
        this.kuvaus = kuvaus;
    }
}
