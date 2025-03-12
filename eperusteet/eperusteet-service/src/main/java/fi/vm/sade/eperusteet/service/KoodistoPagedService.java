package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoPageDto;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;

public interface KoodistoPagedService {

    @PreAuthorize("permitAll()")
    Page<KoodistoKoodiDto> getAllPaged(String koodisto, String nimiFilter, KoodistoPageDto koodistoPageDto);
}
