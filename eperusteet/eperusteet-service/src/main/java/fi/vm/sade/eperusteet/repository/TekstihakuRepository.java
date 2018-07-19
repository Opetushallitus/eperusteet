package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.views.TekstiHakuView;
import net.sf.ehcache.hibernate.HibernateUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;



@Repository
public interface TekstihakuRepository extends JpaRepository<TekstiHakuView, Long> {
    @Query("SELECT haku FROM TekstiHakuView haku WHERE textsearch(haku.teksti, ?) = true")
    Page<TekstiHakuView> tekstihaku(String query, Pageable pageable);
}
