package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiQuery;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;

public interface OsaamismerkkiService {
    // TODO oikeudet
    Page<OsaamismerkkiDto> findBy(OsaamismerkkiQuery query);
}
