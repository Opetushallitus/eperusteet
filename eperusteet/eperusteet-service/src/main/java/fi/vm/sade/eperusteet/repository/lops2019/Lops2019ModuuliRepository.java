package fi.vm.sade.eperusteet.repository.lops2019;

import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.moduuli.Lops2019Moduuli;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface Lops2019ModuuliRepository extends JpaWithVersioningRepository<Lops2019Moduuli, Long> {
    @Query("SELECT moduuli FROM Lops2019Moduuli moduuli WHERE moduuli.oppiaine IN (:oppiaineet)")
    List<Lops2019Moduuli> getModuulitByParents(@Param("oppiaineet") Collection<Lops2019Oppiaine> oppiaineet);
}
