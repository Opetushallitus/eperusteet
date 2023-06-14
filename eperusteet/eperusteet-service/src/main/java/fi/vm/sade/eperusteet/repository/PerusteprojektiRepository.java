package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PerusteprojektiRepository extends JpaRepository<Perusteprojekti, Long>, PerusteprojektiRepositoryCustom {

    List<Perusteprojekti> findByDiaarinumero(Diaarinumero diaarinumero);

    Perusteprojekti findOneByDiaarinumeroAndTila(Diaarinumero diaarinumero, ProjektiTila tila);

    Perusteprojekti findOneByPerusteDiaarinumeroAndTila(Diaarinumero diaarinumero, ProjektiTila tila);

    Perusteprojekti findOneByRyhmaOid(String ryhmaOid);

    Perusteprojekti findOneByPeruste(Peruste peruste);

    @Query("SELECT p from Perusteprojekti p" +
            " WHERE p.peruste.tyyppi = 'NORMAALI' AND (p.tila = 'JULKAISTU' OR (SELECT COUNT(julkaisu) FROM JulkaistuPeruste julkaisu WHERE julkaisu.peruste.id = p.peruste.id) > 0)" +
            "   AND p.peruste NOT IN (SELECT peruste FROM ValidointiStatus)")
    Set<Perusteprojekti> findAllValidoimattomatUudet();

    @Query("SELECT p from Perusteprojekti p, ValidointiStatus vs" +
            " WHERE p.peruste.tyyppi = 'NORMAALI' AND (p.tila = 'JULKAISTU' OR (SELECT COUNT(julkaisu) FROM JulkaistuPeruste julkaisu WHERE julkaisu.peruste.id = p.peruste.id) > 0)" +
            " AND vs.peruste = p.peruste AND p.peruste.globalVersion.aikaleima > vs.lastCheck")
    Set<Perusteprojekti> findAllValidoimattomat();

    @Query("SELECT p from Perusteprojekti p" +
            " WHERE (p.tila = 'JULKAISTU' OR (SELECT COUNT(julkaisu) FROM JulkaistuPeruste julkaisu WHERE julkaisu.peruste.id = p.peruste.id) > 0)" +
            "   AND p.peruste NOT IN (SELECT peruste FROM MaarayskirjeStatus)")
    Set<Perusteprojekti> findAllMaarayskirjeetUudet();

    @Query("SELECT p from Perusteprojekti p, MaarayskirjeStatus mks" +
            " WHERE (p.tila = 'JULKAISTU' OR (SELECT COUNT(julkaisu) FROM JulkaistuPeruste julkaisu WHERE julkaisu.peruste.id = p.peruste.id) > 0)" +
            " AND mks.peruste = p.peruste AND p.peruste.globalVersion.aikaleima > mks.lastCheck")
    Set<Perusteprojekti> findAllMaarayskirjeet();

    List<Perusteprojekti> findAllByTilaAndPerusteTyyppi(ProjektiTila tila, PerusteTyyppi tyyppi);

    @Query("SELECT p from Perusteprojekti p WHERE p.tila <> 'POISTETTU' AND p.tila <> 'JULKAISTU' AND (p.luoja = ?1 OR p.ryhmaOid IN (?2)) AND p.peruste.tyyppi != 'Opas'")
    List<Perusteprojekti> findOmatPerusteprojektit(String userOid, Set<String> orgs);

    @Query("SELECT p from Perusteprojekti p " +
            "WHERE p.tila <> 'POISTETTU' AND (p.tila = 'JULKAISTU' OR (SELECT COUNT(julkaisu) FROM JulkaistuPeruste julkaisu WHERE julkaisu.peruste.id = p.peruste.id) > 0) " +
            "AND (p.luoja = ?1 OR p.ryhmaOid IN (?2)) AND p.peruste.tyyppi != 'Opas'")
    List<Perusteprojekti> findOmatJulkaistutPerusteprojektit(String userOid, Set<String> orgs);

    @Query("SELECT new fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto(" +
            " p.id, " +
            " p.nimi, " +
            " p.peruste.diaarinumero, " +
            " p.diaarinumero, " +
            " p.peruste.koulutustyyppi, " +
            " p.peruste.tyyppi, " +
            " p.tila" +
            ") FROM Perusteprojekti p WHERE p.peruste.tyyppi != 'Opas'")
    List<PerusteprojektiKevytDto> findAllKevyt();
}
