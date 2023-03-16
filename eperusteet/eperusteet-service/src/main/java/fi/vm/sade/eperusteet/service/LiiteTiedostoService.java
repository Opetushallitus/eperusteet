package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.liite.LiiteTyyppi;
import fi.vm.sade.eperusteet.service.util.Pair;
import org.apache.tika.mime.MimeTypeException;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public interface LiiteTiedostoService {
    Pair<UUID, String> uploadFile(Long perusteId, String nimi, InputStream is, long koko, LiiteTyyppi tyyppi, Set<String> tyypit, Integer width, Integer height, Part file) throws IOException, MimeTypeException, HttpMediaTypeNotSupportedException;
}
