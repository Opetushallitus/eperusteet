package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.dto.peruste.TutkinnonOsaQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.NoRepositoryBean;


@NoRepositoryBean
public interface TutkinnonOsaRepositoryCustom {
    Page<TutkinnonOsa> findBy(PageRequest page, TutkinnonOsaQueryDto pquery);
}
