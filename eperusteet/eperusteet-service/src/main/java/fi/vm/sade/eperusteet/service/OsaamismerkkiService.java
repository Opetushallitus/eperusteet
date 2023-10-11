package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiKategoriaDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiQuery;
import org.apache.tika.mime.MimeTypeException;
import org.springframework.data.domain.Page;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.util.List;

public interface OsaamismerkkiService {
    // TODO oikeudet
    Page<OsaamismerkkiDto> findBy(OsaamismerkkiQuery query);

    List<OsaamismerkkiKategoriaDto> getKategoriat();

    OsaamismerkkiKategoriaDto updateKategoria(OsaamismerkkiKategoriaDto kategoriaDto) throws HttpMediaTypeNotSupportedException, MimeTypeException;
}
