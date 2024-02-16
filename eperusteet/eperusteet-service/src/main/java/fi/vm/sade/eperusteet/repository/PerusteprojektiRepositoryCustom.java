package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.dto.peruste.PerusteprojektiQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface PerusteprojektiRepositoryCustom {
    Page<Perusteprojekti> findBy(PageRequest page, PerusteprojektiQueryDto query);
}
