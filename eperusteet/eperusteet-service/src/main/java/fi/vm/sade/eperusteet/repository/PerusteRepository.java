package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author jhyoty
 */
@Repository
public interface PerusteRepository extends JpaWithVersioningRepository<Peruste, Long>, PerusteRepositoryCustom {
    @Query("SELECT s.sisalto FROM Suoritustapa s, Peruste p LEFT JOIN p.suoritustavat s WHERE p.id = ?1 AND s.suoritustapakoodi = ?2")
    PerusteenOsaViite findSisaltoByIdAndSuoritustapakoodi(Long id, Suoritustapakoodi suoritustapakoodi);

    @Query("SELECT s FROM Suoritustapa s, Peruste p LEFT JOIN p.suoritustavat s WHERE p.id = ?1 AND s.suoritustapakoodi = ?2")
    Suoritustapa findSuoritustapaByIdAndSuoritustapakoodi(Long id, Suoritustapakoodi suoritustapakoodi);

    @Query("SELECT p FROM Suoritustapa s, Peruste p LEFT JOIN p.suoritustavat s WHERE p.id = ?1 AND s.suoritustapakoodi = ?2")
    Peruste findPerusteByIdAndSuoritustapakoodi(Long id, Suoritustapakoodi suoritustapakoodi);

    List<Peruste> findAllByKoulutustyyppi(String koulutustyyppi);


//        select * from peruste
//        inner join (select peruste_id
//                    from peruste_koulutus
//                    inner join (select *
//                                from koulutus
//                                where koulutus_koodi LIKE '%351%') as foo
//                    on foo.id = peruste_koulutus.koulutus_id) as bar
//        on peruste_id = peruste.id;
//    @Query("SELECT * FROM Peruste p JOIN (SELECT p.id FROM p")
//    List<Peruste> findByKoodiUri(String koodiUri);
}
