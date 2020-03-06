package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import java.util.Date;

public interface HistoriaTapahtuma {

    Date getLuotu();

    Date getMuokattu();

    String getLuoja();

    String getMuokkaaja();

    Long getId();

    TekstiPalanen getNimi();

    NavigationType getNavigationType();
}
