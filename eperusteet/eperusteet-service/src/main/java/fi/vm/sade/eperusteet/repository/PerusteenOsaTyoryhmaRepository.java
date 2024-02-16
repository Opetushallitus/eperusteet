package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTyoryhma;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerusteenOsaTyoryhmaRepository extends JpaRepository<PerusteenOsaTyoryhma, Long> {
    List<PerusteenOsaTyoryhma> findAllByPerusteenosaAndPerusteprojekti(PerusteenOsa perusteenosa, Perusteprojekti perusteprojekti);
    List<PerusteenOsaTyoryhma> findAllByPerusteprojekti(Perusteprojekti perusteprojekti);

    void deleteAllByPerusteenosaAndPerusteprojekti(PerusteenOsa perusteenosa, Perusteprojekti perusteprojekti);
}
