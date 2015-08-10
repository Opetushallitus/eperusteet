package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import java.util.List;
import java.util.Set;
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

    Peruste findByDiaarinumero(Diaarinumero diaarinumero);

    Peruste findOneByDiaarinumeroAndTila(Diaarinumero diaarinumero, PerusteTila tila);

    @Query("SELECT DISTINCT p.id FROM Peruste p " +
        "LEFT JOIN p.suoritustavat s " +
        "LEFT JOIN p.perusopetuksenPerusteenSisalto ps " +
        "LEFT JOIN p.esiopetuksenPerusteenSisalto eps " +
        "WHERE p.tila = ?2 AND (s.sisalto.id IN ?1 OR ps.sisalto.id IN ?1 OR eps.sisalto.id IN ?1)")
    Set<Long> findBySisaltoRoots(Iterable<? extends Number> rootIds, PerusteTila tila);

    @Query("SELECT DISTINCT p.id FROM Peruste p JOIN p.suoritustavat s JOIN s.tutkinnonOsat to WHERE p.tila = ?2 AND to.tutkinnonOsa.id = ?1")
    Set<Long> findByTutkinnonosaId(Long id, PerusteTila tila);

    @Query("SELECT p.tila from Peruste p WHERE p.id = ?1")
    PerusteTila getTila(Long id);

}
