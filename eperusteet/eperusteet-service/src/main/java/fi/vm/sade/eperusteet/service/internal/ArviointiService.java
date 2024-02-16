package fi.vm.sade.eperusteet.service.internal;

import fi.vm.sade.eperusteet.domain.arviointi.Arviointi;
import fi.vm.sade.eperusteet.dto.arviointi.ArviointiDto;
import java.util.List;

public interface ArviointiService {

    List<ArviointiDto> findAll();
    ArviointiDto findById(Long id);
    ArviointiDto add(ArviointiDto arviointiDto);
    Arviointi kopioi(Arviointi arviointi);
}
