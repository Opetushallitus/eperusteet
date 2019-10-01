package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.SkeduloituAjo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkeduloituajoRepository extends JpaRepository<SkeduloituAjo, Long> {

    SkeduloituAjo findByNimi(String nimi);
}
