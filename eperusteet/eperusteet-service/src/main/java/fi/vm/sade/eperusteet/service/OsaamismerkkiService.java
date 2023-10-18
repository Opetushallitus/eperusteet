package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiKategoriaDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiQuery;
import org.apache.tika.mime.MimeTypeException;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.util.List;

public interface OsaamismerkkiService {
    @PreAuthorize("permitAll()")
    Page<OsaamismerkkiDto> findJulkisetBy(OsaamismerkkiQuery query);

    @PreAuthorize("permitAll()")
    Page<OsaamismerkkiDto> findBy(OsaamismerkkiQuery query);

    @PreAuthorize("permitAll()")
    OsaamismerkkiDto getJulkinenOsaamismerkki(Long id);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    OsaamismerkkiDto updateOsaamismerkki(OsaamismerkkiDto osaamismerkkiDto);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    void deleteOsaamismerkki(Long id);

    @PreAuthorize("permitAll()")
    List<OsaamismerkkiKategoriaDto> getKategoriat();

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    OsaamismerkkiKategoriaDto updateKategoria(OsaamismerkkiKategoriaDto kategoriaDto) throws HttpMediaTypeNotSupportedException, MimeTypeException;
}
