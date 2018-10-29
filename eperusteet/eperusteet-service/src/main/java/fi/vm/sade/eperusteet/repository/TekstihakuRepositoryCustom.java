package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.tekstihaku.TekstiHaku;
import fi.vm.sade.eperusteet.domain.tekstihaku.TekstiHakuTulos;
import fi.vm.sade.eperusteet.dto.peruste.VapaaTekstiQueryDto;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface TekstihakuRepositoryCustom {
//    @Query("SELECT haku FROM TekstiHaku haku WHERE haku.teksti LIKE %?1%")
    List<TekstiHakuTulos> tekstihaku(VapaaTekstiQueryDto query);

    void rakennaTekstihaku();
}
