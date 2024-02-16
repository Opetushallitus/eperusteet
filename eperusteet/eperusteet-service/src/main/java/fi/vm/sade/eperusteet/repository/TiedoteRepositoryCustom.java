package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Tiedote;
import fi.vm.sade.eperusteet.dto.peruste.TiedoteQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TiedoteRepositoryCustom {
    Page<Tiedote> findBy(PageRequest page, TiedoteQuery query);
}
