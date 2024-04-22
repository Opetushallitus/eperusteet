package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.PerusteprojektiTyoryhma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerusteprojektiTyoryhmaRepository extends JpaRepository<PerusteprojektiTyoryhma, Long> {
    List<PerusteprojektiTyoryhma> findAllByPerusteprojektiAndNimi(Perusteprojekti perusteprojekti, String nimi);

    List<PerusteprojektiTyoryhma> deleteAllByPerusteprojektiAndNimi(Perusteprojekti perusteprojekti, String nimi);

    List<PerusteprojektiTyoryhma> findAllByPerusteprojekti(Perusteprojekti perusteprojekti);
}
