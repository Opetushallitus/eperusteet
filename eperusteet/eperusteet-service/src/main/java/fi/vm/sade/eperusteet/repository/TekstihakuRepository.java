package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.views.TekstiHakuTulos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;



@Repository
public interface TekstihakuRepository extends JpaRepository<TekstiHakuTulos, Long> {
    @Query("SELECT haku FROM TekstiHakuTulos haku WHERE haku.teksti LIKE %?1%")
    Page<TekstiHakuTulos> tekstihaku(String query, Pageable pageable);
}
