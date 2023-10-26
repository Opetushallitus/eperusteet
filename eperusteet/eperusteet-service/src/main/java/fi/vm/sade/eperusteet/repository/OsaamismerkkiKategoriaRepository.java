package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.osaamismerkki.OsaamismerkkiKategoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OsaamismerkkiKategoriaRepository extends JpaRepository<OsaamismerkkiKategoria, Long> {
}
