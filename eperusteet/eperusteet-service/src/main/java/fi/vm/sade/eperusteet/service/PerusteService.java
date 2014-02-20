/*
 * Here comes the text of your license
 * Each line should be prefixed with  *
 */
package fi.vm.sade.eperusteet.service;

import com.fasterxml.jackson.databind.JsonNode;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.dto.PerusteQuery;
import fi.vm.sade.eperusteet.dto.PerusteDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 *
 * @author harrik
 */
public interface PerusteService {

    PerusteDto get(final Long id);

    Page<PerusteDto> getAll(PageRequest page, String kieli);

    Page<PerusteDto> findBy(PageRequest page, PerusteQuery pquery);

    PerusteenOsaViite addViite(final Long parentId, final Long seuraavaViite, PerusteenOsaViite viite);

    public String lammitys();
}
