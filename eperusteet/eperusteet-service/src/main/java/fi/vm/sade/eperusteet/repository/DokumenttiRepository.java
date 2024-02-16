package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Dokumentti;
import fi.vm.sade.eperusteet.domain.DokumenttiTila;
import fi.vm.sade.eperusteet.domain.GeneratorVersion;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DokumenttiRepository extends JpaRepository<Dokumentti, Long> {
    Dokumentti findById(Long id);
    List<Dokumentti> findByPerusteIdAndKieliAndTilaAndSuoritustapakoodiAndGeneratorVersion(
            Long perusteId,
            Kieli kieli,
            DokumenttiTila tila,
            Suoritustapakoodi suoritustapakoodi,
            GeneratorVersion version,
            Sort sort
    );
    List<Dokumentti> findByPerusteIdAndKieliAndTilaAndGeneratorVersion(
            Long perusteId,
            Kieli kieli,
            DokumenttiTila tila,
            GeneratorVersion version,
            Sort sort
    );

    Dokumentti findFirstByPerusteIdAndKieliAndSuoritustapakoodiAndGeneratorVersionOrderByAloitusaikaDesc(
            Long perusteId,
            Kieli kieli,
            Suoritustapakoodi suoritustapakoodi,
            GeneratorVersion version
    );
    Dokumentti findFirstByPerusteIdAndKieliAndGeneratorVersionOrderByAloitusaikaDesc(
            Long perusteId,
            Kieli kieli,
            GeneratorVersion version
    );

    Dokumentti findByIdInAndKieli(Set<Long> id, Kieli kieli);
}
