package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.yl.lukio.LukioOpetussuunnitelmaRakenne;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.data.jpa.repository.Query;

public interface LukioOpetussuunnitelmaRakenneRepository
        extends JpaWithVersioningRepository<LukioOpetussuunnitelmaRakenne, Long> {
    @Query("SELECT r FROM LukioOpetussuunnitelmaRakenne r WHERE r.sisalto.peruste.id = ?1")
    LukioOpetussuunnitelmaRakenne findByPerusteId(long perusteId);
}
