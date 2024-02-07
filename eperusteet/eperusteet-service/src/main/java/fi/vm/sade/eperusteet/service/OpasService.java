package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.opas.OpasDto;
import fi.vm.sade.eperusteet.dto.opas.OpasLuontiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteHakuDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import fi.vm.sade.eperusteet.dto.peruste.PerusteprojektiQueryDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;

public interface OpasService {

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'LUKU')")
    OpasDto get(@P("id") final Long id);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    OpasDto save(OpasLuontiDto opasDto);

    @PreAuthorize("permitAll()")
    Page<PerusteHakuDto> findBy(PageRequest page, PerusteQuery pquery);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    Page<PerusteprojektiKevytDto> findProjektiBy(PageRequest p, PerusteprojektiQueryDto pquery);
}
