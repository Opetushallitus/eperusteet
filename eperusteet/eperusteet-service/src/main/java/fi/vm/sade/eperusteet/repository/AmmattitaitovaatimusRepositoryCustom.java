package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.dto.AmmattitaitovaatimusQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface AmmattitaitovaatimusRepositoryCustom {
    Page<Peruste> findBy(PageRequest page, AmmattitaitovaatimusQueryDto query);
}
