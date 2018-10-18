package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.views.TekstiHakuTulos;
import fi.vm.sade.eperusteet.dto.peruste.VapaaTekstiQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TekstihakuRepositoryCustom {
//    @Query("SELECT haku FROM TekstiHakuTulos haku WHERE haku.teksti LIKE %?1%")
    Page<TekstiHakuTulos> tekstihaku(VapaaTekstiQueryDto query);

    void rakennaTekstihaku();
}
