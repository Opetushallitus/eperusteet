package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiBaseDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiExternalDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiKategoriaDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiQuery;
import org.apache.tika.mime.MimeTypeException;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.util.List;

public interface OsaamismerkkiService {
    @PreAuthorize("permitAll()")
    List<OsaamismerkkiExternalDto> getOsaamismerkit();

    @PreAuthorize("permitAll()")
    OsaamismerkkiDto getOsaamismerkkiByUri(String koodiUri);

    @PreAuthorize("permitAll()")
    List<OsaamismerkkiBaseDto> findJulkisetBy(OsaamismerkkiQuery query);

    @PreAuthorize("permitAll()")
    Page<OsaamismerkkiDto> findBy(OsaamismerkkiQuery query);

    @PreAuthorize("permitAll()")
    OsaamismerkkiBaseDto getJulkinenOsaamismerkkiById(Long id);

    @PreAuthorize("permitAll()")
    OsaamismerkkiBaseDto getJulkinenOsaamismerkkiByKoodi(Long koodi);

    @PreAuthorize("permitAll()")
    List<OsaamismerkkiKategoriaDto> getJulkisetKategoriat(OsaamismerkkiQuery query);

    @PreAuthorize("hasPermission(null, 'osaamismerkit', 'LUONTI')")
    OsaamismerkkiDto updateOsaamismerkki(OsaamismerkkiDto osaamismerkkiDto);

    @PreAuthorize("hasPermission(null, 'osaamismerkit', 'LUONTI')")
    OsaamismerkkiKategoriaDto updateKategoria(OsaamismerkkiKategoriaDto kategoriaDto) throws HttpMediaTypeNotSupportedException, MimeTypeException;

    @PreAuthorize("hasPermission(null, 'osaamismerkit', 'LUONTI')")
    List<OsaamismerkkiKategoriaDto> getKategoriat();

    @PreAuthorize("hasPermission(null, 'osaamismerkit', 'LUONTI')")
    void deleteOsaamismerkki(Long id);

    @PreAuthorize("hasPermission(null, 'osaamismerkit', 'LUONTI')")
    void deleteKategoria(Long id);
}
