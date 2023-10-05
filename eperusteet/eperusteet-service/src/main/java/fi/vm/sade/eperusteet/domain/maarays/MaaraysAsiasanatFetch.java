package fi.vm.sade.eperusteet.domain.maarays;

import fi.vm.sade.eperusteet.domain.Kieli;

import java.util.Map;

public interface MaaraysAsiasanatFetch {
    Map<Kieli, MaaraysAsiasana> getAsiasanat();
}
