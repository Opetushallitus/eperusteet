package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface PerusteRepositoryCustom {
    Page<Peruste> findBy(PageRequest page, PerusteQuery pquery);
    Page<Peruste> findBy(PageRequest page, PerusteQuery pquery, Set<Long> koodistostaHaetut);
}
