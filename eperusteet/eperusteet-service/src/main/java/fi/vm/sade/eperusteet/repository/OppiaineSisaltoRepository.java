package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.yl.AbstractOppiaineOpetuksenSisalto;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface OppiaineSisaltoRepository<Type extends AbstractOppiaineOpetuksenSisalto>
        extends JpaWithVersioningRepository<Type, Long> {
    Type findByPerusteId(Long perusteId);
}
