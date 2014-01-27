/*
 * Here comes the text of your license
 * Each line should be prefixed with  *
 */

package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.dto.PerusteQuery;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 *
 * @author harrik
 */
public interface PerusteService {

    Peruste get(final Long id);

    Page<Peruste> getAll(PageRequest page, String kieli);

    Page<Peruste> findBy(PageRequest page, PerusteQuery pquery);

    PerusteenOsaViite addViite(final Long parentId, final Long seuraavaViite, PerusteenOsaViite viite);
}
