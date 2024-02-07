package fi.vm.sade.eperusteet.repository.liite;

import fi.vm.sade.eperusteet.domain.liite.Liite;
import fi.vm.sade.eperusteet.domain.liite.LiiteTyyppi;

import java.io.InputStream;
import java.util.UUID;

public interface LiiteRepositoryCustom {
    Liite add(LiiteTyyppi tyyppi, String mime, String nimi, long length, InputStream is);
    Liite add(LiiteTyyppi tyyppi, String mime, String nimi, byte[] bytes);
    Liite add(UUID uuid, LiiteTyyppi tyyppi, String mime, String nimi, byte[] bytes);
}
