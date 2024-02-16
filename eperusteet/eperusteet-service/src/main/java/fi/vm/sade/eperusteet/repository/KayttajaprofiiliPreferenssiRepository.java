package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Kayttajaprofiili;
import fi.vm.sade.eperusteet.domain.KayttajaprofiiliPreferenssi;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KayttajaprofiiliPreferenssiRepository extends JpaRepository<KayttajaprofiiliPreferenssi, Long> {
    KayttajaprofiiliPreferenssi findOneByKayttajaprofiiliAndAvain(Kayttajaprofiili kp, String avain);
}
