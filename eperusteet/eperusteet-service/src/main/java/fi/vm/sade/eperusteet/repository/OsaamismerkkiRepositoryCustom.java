package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.osaamismerkki.Osaamismerkki;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface OsaamismerkkiRepositoryCustom {
    Page<Osaamismerkki> findBy(PageRequest page, OsaamismerkkiQuery query);
}
