package fi.vm.sade.eperusteet.repository.lops2019;

import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface Lops2019OppiaineRepository extends JpaWithVersioningRepository<Lops2019Oppiaine, Long> {
    @Query("SELECT om FROM Lops2019Oppiaine om WHERE om.oppiaine IN (:oppiaineet)")
    List<Lops2019Oppiaine> getOppimaaratByParents(@Param("oppiaineet") Collection<Lops2019Oppiaine> oppiaineet);

}
