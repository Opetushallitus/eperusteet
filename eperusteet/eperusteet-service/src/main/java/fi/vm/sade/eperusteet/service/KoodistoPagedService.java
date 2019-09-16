package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoPageDto;
import org.springframework.data.domain.Page;

public interface KoodistoPagedService {

    Page<KoodistoKoodiDto> getAllPaged(String koodisto, String nimiFilter, KoodistoPageDto koodistoPageDto);
}
