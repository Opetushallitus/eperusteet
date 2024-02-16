package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiHakuDto;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Set;

@NoRepositoryBean
public interface TekstiPalanenRepositoryCustom {
    List<LokalisoituTekstiHakuDto> findLokalisoitavatTekstit(Set<Long> tekstiPalanenId);
}
