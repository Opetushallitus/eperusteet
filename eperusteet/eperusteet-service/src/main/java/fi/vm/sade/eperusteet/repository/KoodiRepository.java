package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Koodi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KoodiRepository extends JpaRepository<Koodi, Long> {
    Koodi findOneByUriAndVersio(String uri, Long versio);

    Koodi findFirstByKoodistoOrderByUriDesc(String koodisto);

    Koodi findFirstByUriOrderByVersioDesc(String uri);
}
