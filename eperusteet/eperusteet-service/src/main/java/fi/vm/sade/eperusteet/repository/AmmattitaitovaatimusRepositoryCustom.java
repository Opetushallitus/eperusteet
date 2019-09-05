package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.AmmattitaitovaatimusQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface AmmattitaitovaatimusRepositoryCustom {
    Page<Peruste> findPerusteetBy(PageRequest page, AmmattitaitovaatimusQueryDto query);

    Page<TutkinnonOsaViite> findTutkinnonOsatBy(PageRequest page, AmmattitaitovaatimusQueryDto query);
}
