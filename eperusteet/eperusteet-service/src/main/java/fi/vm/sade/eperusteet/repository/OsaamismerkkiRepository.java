package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.osaamismerkki.Osaamismerkki;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OsaamismerkkiRepository extends JpaRepository<Osaamismerkki, Long> {
    Osaamismerkki findById(Long id);
}
