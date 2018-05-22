package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.TiedoteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.SuoritustapaDto;
import fi.vm.sade.eperusteet.dto.peruste.TiedoteQuery;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import fi.vm.sade.eperusteet.repository.TiedoteRepositoryCustom;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import java.util.Date;
import java.util.List;
import javax.transaction.Transactional;

import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.annotation.DirtiesContext;

import static fi.vm.sade.eperusteet.service.test.util.TestUtils.lt;

import org.junit.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author mikkom
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
public class TiedoteServiceIT extends AbstractIntegrationTest {
    @Autowired
    private TiedoteService tiedoteService;

    @Autowired
    private PerusteprojektiTestUtils ppTestUtils;

    @Autowired
    private TiedoteRepositoryCustom tiedoteRepositoryCustom;

    @Before
    public void setUp()
    {
        TiedoteDto tiedoteDto;
        tiedoteDto = new TiedoteDto();
        tiedoteDto.setOtsikko(lt("otsikko"));
        tiedoteDto.setSisalto(lt("sisalto"));
        tiedoteDto.setJulkinen(true);
        tiedoteService.addTiedote(tiedoteDto);

        tiedoteDto = new TiedoteDto();
        tiedoteDto.setOtsikko(lt("otsikko2"));
        tiedoteDto.setSisalto(lt("sisalto2"));
        tiedoteDto.setJulkinen(false);
        tiedoteService.addTiedote(tiedoteDto);

    }

    @Test
    public void testGetAll() {
        // Back to the near future
        Date alkaen = new Date((new Date()).getTime() + 1000);
        List<TiedoteDto> tiedotteet = tiedoteService.getAll(false, alkaen.getTime());
        assertEquals(0, tiedotteet.size());

        tiedotteet = tiedoteService.getAll(true, 0L);
        assertEquals(1, tiedotteet.size());

        tiedotteet = tiedoteService.getAll(false, 0L);
        assertEquals(2, tiedotteet.size());

        TiedoteDto tiedote1 = tiedotteet.get(0);
        TiedoteDto tiedote2 = tiedotteet.get(1);

        // Tarkista että lista on järjestetty muokattu-päivämäärän mukaan, uusin ensin
        Assert.assertTrue(tiedote1.getMuokattu().compareTo(tiedote2.getMuokattu()) >= 0);
    }

    @Test
    public void testGetById() {
        List<TiedoteDto> tiedotteet = tiedoteService.getAll(false, 0L);
        assertEquals(2, tiedotteet.size());

        Long id = tiedotteet.get(0).getId();
        assertNotNull(id);
        TiedoteDto tiedote = tiedoteService.getTiedote(id);
        assertNotNull(tiedote);
        assertEquals(id, tiedote.getId());
    }

    @Test
    public void testUpdate() {
        List<TiedoteDto> tiedotteet = tiedoteService.getAll(false, 0L);
        assertEquals(2, tiedotteet.size());

        Long id = tiedotteet.get(0).getId();
        TiedoteDto tiedote = tiedoteService.getTiedote(id);
        String muokattuSisalto = "muokattu sisalto";
        tiedote.setSisalto(lt(muokattuSisalto));
        tiedoteService.updateTiedote(tiedote);

        tiedote = tiedoteService.getTiedote(id);
        assertEquals(muokattuSisalto, tiedote.getSisalto().get(Kieli.FI));
    }

    @Test
    public void testDelete() {
        List<TiedoteDto> tiedotteet = tiedoteService.getAll(false, 0L);
        assertEquals(2, tiedotteet.size());

        Long id = tiedotteet.get(0).getId();
        tiedoteService.removeTiedote(id);

        assertEquals(1, tiedoteService.getAll(false, 0L).size());
    }

    @Test
    public void testHakuJulkinen() {
        TiedoteQuery tq = new TiedoteQuery();
        tq.setJulkinen(true);
        Page<TiedoteDto> tiedotteet = tiedoteService.findBy(tq);
        assertEquals(tiedotteet.getTotalElements(), 1);
    }

    @Test
    public void testHakuSisainen() {
        TiedoteQuery tq = new TiedoteQuery();
        tq.setJulkinen(false);
        Page<TiedoteDto> tiedotteet = tiedoteService.findBy(tq);
        assertEquals(1, tiedotteet.getTotalElements());
    }

    @Test
    @Ignore
    public void testDtoConversion() {
        List<TiedoteDto> tiedotteet = tiedoteService.getAll(false, 0L);
        assertEquals(2, tiedotteet.size());

        PerusteprojektiDto projekti = ppTestUtils.createPerusteprojekti();
        PerusteDto perusteDto = ppTestUtils.initPeruste(projekti.getPeruste().getIdLong(), dto -> {
            SuoritustapaDto stDto = new SuoritustapaDto();
            stDto.setLaajuusYksikko(LaajuusYksikko.OSAAMISPISTE);
            stDto.setSuoritustapakoodi(Suoritustapakoodi.REFORMI);
            dto.getSuoritustavat().add(stDto);
        });

        assertEquals(2, tiedotteet.size());
        Long id = tiedotteet.iterator().next().getId();

        TiedoteDto tiedoteDto = tiedoteService.getTiedote(id);
        tiedoteDto.setPerusteprojekti(new EntityReference(projekti.getId()));
        tiedoteDto = tiedoteService.updateTiedote(tiedoteDto);

        tiedoteDto.getPeruste().getSuoritustavat().clear();
        tiedoteDto = tiedoteService.updateTiedote(tiedoteDto);

        assertEquals(1, tiedoteDto.getPeruste().getSuoritustavat().size());
    }
}
