/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
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

    Page<Peruste> findBy(PageRequest page, String nimi, List<String> koulutusala, List<String> tyyppi, String kieli, List<String> opintoala);

    PerusteenOsaViite lisääViite(final Long parentId, final Long seuraavaViite, PerusteenOsaViite viite);
}
