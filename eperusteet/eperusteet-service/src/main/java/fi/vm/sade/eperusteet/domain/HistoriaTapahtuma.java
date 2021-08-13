package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import java.util.Date;

public interface HistoriaTapahtuma {

    default Date getLuotu() {
        return null;
    }

    default Date getMuokattu() {
        return null;
    }

    default String getLuoja() {
        return null;
    }

    default String getMuokkaaja() {
        return null;
    }

    Long getId();

    TekstiPalanen getNimi();

    NavigationType getNavigationType();
}
