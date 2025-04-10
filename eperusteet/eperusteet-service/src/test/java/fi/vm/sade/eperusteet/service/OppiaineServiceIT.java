package fi.vm.sade.eperusteet.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.yl.VuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.dto.KevytTekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.PerusteVersionDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.yl.*;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.VuosiluokkaKokonaisuusRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.yl.OppiaineLockContext;
import fi.vm.sade.eperusteet.service.yl.OppiaineOpetuksenSisaltoTyyppi;
import fi.vm.sade.eperusteet.service.yl.OppiaineService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static fi.vm.sade.eperusteet.service.test.util.TestUtils.*;
import static org.junit.Assert.*;

@DirtiesContext
@Transactional
public class OppiaineServiceIT extends AbstractIntegrationTest {

    @Autowired
    private VuosiluokkaKokonaisuusRepository vkrepo;
    @Autowired
    private OppiaineService service;
    @Autowired
    @LockCtx(OppiaineLockContext.class)
    private LockService<OppiaineLockContext> lockService;
    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteRepository perusteRepository;

    private Long perusteId;

    @Before
    public void setup() {
        startNewTransaction();
        Peruste peruste = perusteService.luoPerusteRunko(KoulutusTyyppi.PERUSOPETUS, null, LaajuusYksikko.OPINTOVIIKKO, PerusteTyyppi.NORMAALI);
        perusteId = peruste.getId();
        endTransaction();
    }

    @Test
    public void testCRUD() throws IOException {

        startNewTransaction();
        VuosiluokkaKokonaisuus vk = new VuosiluokkaKokonaisuus();
        vk = vkrepo.save(vk);

        OppiaineDto oppiaineDto = new OppiaineDto();
        oppiaineDto.setNimi(lt("Oppiaine"));
        oppiaineDto.setTehtava(to("TehtävänOtsikko", "Tehtava"));
        oppiaineDto.setKoosteinen(Optional.of(false));
        oppiaineDto.setVapaatTekstit(List.of(KevytTekstiKappaleDto.of("nimi", "teksti"), KevytTekstiKappaleDto.of("nimi2", "teksti2")));

        OppiaineenVuosiluokkaKokonaisuusDto vkDto = new OppiaineenVuosiluokkaKokonaisuusDto();
        vkDto.setTehtava(Optional.of(to("Tehtävä", "")));
        vkDto.setVuosiluokkaKokonaisuus(Optional.of(vk.getReference()));
        vkDto.setVapaatTekstit(List.of(KevytTekstiKappaleDto.of("vkDtonimi", "teksti"), KevytTekstiKappaleDto.of("vkDtonimi2", "teksti2")));

        oppiaineDto.setVuosiluokkakokonaisuudet(Sets.newHashSet(vkDto));
        KeskeinenSisaltoalueDto ks = new KeskeinenSisaltoalueDto();
        ks.setNimi(Optional.of(lt("Nimi")));
        vkDto.setSisaltoalueet(Lists.newArrayList(ks));
        PerusteVersionDto versionDto = perusteService.getPerusteVersion(perusteId);
        OppiaineDto oa = service.addOppiaine(perusteId, oppiaineDto, OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);

        startNewTransaction();

        OpetuksenKohdealueDto kohdealueDto = new OpetuksenKohdealueDto();
        kohdealueDto.setNimi(olt("Kohdealue"));
        service.addKohdealue(perusteId, oa.getId(), kohdealueDto);

        startNewTransaction();

        oa = service.getOppiaine(perusteId, oa.getId(), OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);

//        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());
        assertEquals(oa.getVapaatTekstit().size(), 2);
        assertEquals(oa.getVapaatTekstit().get(0).getNimi().get(Kieli.FI), "nimi");
        Long vapaaTekstinId = oa.getVapaatTekstit().get(0).getId();
        Long vlkvapaaTekstinId = oa.getVuosiluokkakokonaisuudet().iterator().next().getVapaatTekstit().get(0).getId();

        OppiaineLockContext lc = new OppiaineLockContext();
        lc.setTyyppi(OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
        lc.setPerusteId(perusteId);
        lc.setOppiaineId(oa.getId());
        lockService.lock(lc);

        startNewTransaction();

        assertEquals("Nimi", oa.getVuosiluokkakokonaisuudet().iterator().next().getSisaltoalueet().get(0).getNimi().get().get(Kieli.FI));
        ks.setNimi(olt("Nimi2"));
        oa.getTehtava().setOtsikko(Optional.of(new LokalisoituTekstiDto(null, null)));
        oa.getTehtava().setTeksti(Optional.empty());
        oa.getVuosiluokkakokonaisuudet().iterator().next().getSisaltoalueet().add(0, ks);
        oa.getVuosiluokkakokonaisuudet().iterator().next().getSisaltoalueet().get(1).setNimi(null);
        oa.getVapaatTekstit().get(0).setNimi(LokalisoituTekstiDto.of(Kieli.FI, "paivitetty_nimi"));
        oa.getVuosiluokkakokonaisuudet().iterator().next().getVapaatTekstit().get(0).setNimi(LokalisoituTekstiDto.of(Kieli.FI, "vlkpaivitetty_nimi"));
        oa.setVapaatTekstit(List.of(oa.getVapaatTekstit().get(0)));
        versionDto = perusteService.getPerusteVersion(perusteId);
        oa = service.updateOppiaine(perusteId, new UpdateDto<>(oa), OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
//        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());

        assertEquals(oa.getVapaatTekstit().size(), 1);
        assertEquals(oa.getVapaatTekstit().get(0).getNimi().get(Kieli.FI), "paivitetty_nimi");
        assertEquals(oa.getVapaatTekstit().get(0).getId(), vapaaTekstinId);

        assertEquals(oa.getVuosiluokkakokonaisuudet().iterator().next().getVapaatTekstit().get(0).getNimi().get(Kieli.FI), "vlkpaivitetty_nimi");
        assertEquals(oa.getVuosiluokkakokonaisuudet().iterator().next().getVapaatTekstit().get(0).getId(), vlkvapaaTekstinId);

        startNewTransaction();

        lockService.unlock(lc);

        startNewTransaction();

        assertNull(oa.getTehtava().getOtsikko());
        assertNull(oa.getTehtava().getTeksti());
        assertEquals("Nimi2", oa.getVuosiluokkakokonaisuudet().iterator().next().getSisaltoalueet().get(0).getNimi().get().get(Kieli.FI));
        assertEquals("Nimi", oa.getVuosiluokkakokonaisuudet().iterator().next().getSisaltoalueet().get(1).getNimi().get().get(Kieli.FI));

        vkDto = oa.getVuosiluokkakokonaisuudet().iterator().next();
        OpetuksenTavoiteDto tavoiteDto = new OpetuksenTavoiteDto();
        tavoiteDto.setKohdealueet(Collections.singleton(new Reference(oa.getKohdealueet().iterator().next().getId())));
        tavoiteDto.setSisaltoalueet(Collections.singleton(new Reference(vkDto.getSisaltoalueet().get(0).getId())));
        tavoiteDto.setTavoite(olt("Tässäpä jokin kiva tavoite"));
        tavoiteDto.setVapaaTeksti(olt("vapaa teksti kentta"));
        tavoiteDto.setArvioinninkohteet(Sets.newHashSet(
                arvio("Kohde", "osaamisenkuvaus", "hyvakuvaus", 8),
                arvio(null, "osaamisenkuvaus2", "hyvakuvaus2", 9)));

        tavoiteDto.setArvioinninKuvaus(olt("arvioinnin kohde"));
        vkDto.getTavoitteet().add(tavoiteDto);
        vkDto.getTavoitteet().add(OpetuksenTavoiteDto.builder().tavoite(olt("tavoite2")).build());

        lc = OppiaineLockContext.of(OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS, perusteId, oa.getId(), vkDto.getId());
        startNewTransaction();

        lockService.lock(lc);
        startNewTransaction();

        versionDto = perusteService.getPerusteVersion(perusteId);
        vkDto = service.updateOppiaineenVuosiluokkaKokonaisuus(perusteId, oa.getId(), new UpdateDto<>(vkDto));
        assertEquals(vkDto.getTavoitteet().size(), 2);
        assertEquals(vkDto.getTavoitteet().get(0).getTavoite().get().get(Kieli.FI), "Tässäpä jokin kiva tavoite");

        List<TavoitteenArviointiDto> arvioinnit = new ArrayList<>(vkDto.getTavoitteet().get(0).getArvioinninkohteet()).stream()
                .sorted(Comparator.comparing(arviointi -> arviointi.getArvosana().get()))
                .collect(Collectors.toList());

        assertEquals("Kohde", arvioinnit.get(0).getArvioinninKohde().get().get(Kieli.FI));
        assertEquals("osaamisenkuvaus", arvioinnit.get(0).getOsaamisenKuvaus().get().get(Kieli.FI));
        assertEquals("osaamisenkuvaus", arvioinnit.get(0).getHyvanOsaamisenKuvaus().get().get(Kieli.FI));
        assertEquals(new Integer(8), arvioinnit.get(0).getArvosana().get());

        assertNull(arvioinnit.get(1).getArvioinninKohde());
        assertEquals("osaamisenkuvaus2", arvioinnit.get(1).getOsaamisenKuvaus().get().get(Kieli.FI));
        assertNull(arvioinnit.get(1).getHyvanOsaamisenKuvaus());
        assertEquals(new Integer(9), arvioinnit.get(1).getArvosana().get());

        assertEquals("arvioinnin kohde", vkDto.getTavoitteet().get(0).getArvioinninKuvaus().get().get(Kieli.FI));
        assertEquals("vapaa teksti kentta", vkDto.getTavoitteet().get(0).getVapaaTeksti().get().get(Kieli.FI));

        vkDto.getSisaltoalueet().clear();
        vkDto.setTavoitteet(List.of(vkDto.getTavoitteet().get(0)));

        startNewTransaction();
        vkDto = service.updateOppiaineenVuosiluokkaKokonaisuus(perusteId, oa.getId(), new UpdateDto<>(vkDto));
        assertEquals(vkDto.getTavoitteet().size(), 1);

        startNewTransaction();
        service.deleteOppiaine(perusteId, oa.getId(), OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);

        endTransaction();
        final Long oppiaineId = oa.getId();
        Assertions.assertThatThrownBy(() -> service.getOppiaine(perusteId, oppiaineId, OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS)).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    public void testLisaaJaPoistaVuosiluokkakokonaisuudenTavoite() {
        startNewTransaction();
        VuosiluokkaKokonaisuus vk = new VuosiluokkaKokonaisuus();
        vk = vkrepo.save(vk);

        OppiaineDto oppiaineDto = new OppiaineDto();
        oppiaineDto.setNimi(lt("Oppiaine"));
        oppiaineDto.setTehtava(to("TehtävänOtsikko", "Tehtava"));
        oppiaineDto.setKoosteinen(Optional.of(false));
        oppiaineDto.setVapaatTekstit(List.of(KevytTekstiKappaleDto.of("nimi", "teksti"), KevytTekstiKappaleDto.of("nimi2", "teksti2")));

        OppiaineenVuosiluokkaKokonaisuusDto vkDto = new OppiaineenVuosiluokkaKokonaisuusDto();
        vkDto.setTehtava(Optional.of(to("Tehtävä", "")));
        vkDto.setVuosiluokkaKokonaisuus(Optional.of(vk.getReference()));
        vkDto.setVapaatTekstit(List.of(KevytTekstiKappaleDto.of("vkDtonimi", "teksti"), KevytTekstiKappaleDto.of("vkDtonimi2", "teksti2")));
        vkDto.setTavoitteet(List.of(
                OpetuksenTavoiteDto.builder().tavoite(olt("tavoite1")).build(),
                OpetuksenTavoiteDto.builder().tavoite(olt("tavoite2")).build(),
                OpetuksenTavoiteDto.builder().tavoite(olt("tavoite3")).build()));

        oppiaineDto.setVuosiluokkakokonaisuudet(Sets.newHashSet(vkDto));
        OppiaineDto oa = service.addOppiaine(perusteId, oppiaineDto, OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);

        startNewTransaction();

        oa = service.getOppiaine(perusteId, oa.getId(), OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
        assertEquals(oa.getVuosiluokkakokonaisuudet().iterator().next().getTavoitteet().size(), 3);

        OppiaineLockContext lc = new OppiaineLockContext();
        lc.setTyyppi(OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
        lc.setPerusteId(perusteId);
        lc.setOppiaineId(oa.getId());
        lockService.lock(lc);

        startNewTransaction();

        oa.getVuosiluokkakokonaisuudet().iterator().next().setTavoitteet(List.of(oa.getVuosiluokkakokonaisuudet().iterator().next().getTavoitteet().get(0)));
        oa = service.updateOppiaine(perusteId, new UpdateDto<>(oa), OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
        assertEquals(oa.getVuosiluokkakokonaisuudet().iterator().next().getTavoitteet().size(), 1);

        endTransaction();
    }

    @Test
    public void testAddAndUpdateOppimaara() throws IOException {
        VuosiluokkaKokonaisuus vk = new VuosiluokkaKokonaisuus();
        vk = vkrepo.save(vk);

        OppiaineDto oppiaineDto = new OppiaineDto();
        oppiaineDto.setNimi(lt("Oppiaine"));
        oppiaineDto.setTehtava(to("TehtävänOtsikko", "Tehtava"));
        oppiaineDto.setKoosteinen(Optional.of(true));

        OppiaineenVuosiluokkaKokonaisuusDto vkDto = new OppiaineenVuosiluokkaKokonaisuusDto();
        vkDto.setTehtava(Optional.of(to("Tehtävä", "")));
        vkDto.setVuosiluokkaKokonaisuus(Optional.of(vk.getReference()));

        oppiaineDto.setVuosiluokkakokonaisuudet(Sets.newHashSet(vkDto));
        KeskeinenSisaltoalueDto ks = new KeskeinenSisaltoalueDto();
        ks.setNimi(Optional.of(lt("Nimi")));
        vkDto.setSisaltoalueet(Lists.newArrayList(ks));
        PerusteVersionDto versionDto = perusteService.getPerusteVersion(perusteId);

        OppiaineDto oa1 = service.addOppiaine(perusteId, oppiaineDto, OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
//        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());
        assertEquals(0, oa1.getOppimaarat().size());

        OppiaineDto oppimaara = new OppiaineDto();
        oppimaara.setNimi(lt("OppimaaranNimi"));
        oppimaara.setOppiaine(Optional.of(new Reference(oa1.getId())));
        vkDto = new OppiaineenVuosiluokkaKokonaisuusDto();
        vkDto.setTehtava(Optional.of(to("Tehtävä", "")));
        vkDto.setVuosiluokkaKokonaisuus(Optional.of(vk.getReference()));
        oppimaara.setVuosiluokkakokonaisuudet(Sets.newHashSet(vkDto));
        versionDto = perusteService.getPerusteVersion(perusteId);
        oppimaara = service.addOppiaine(perusteId, oppimaara, OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
//        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());
        oa1 = service.getOppiaine(perusteId, oa1.getId(), OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
        assertEquals(1, oa1.getOppimaarat().size());

        OppiaineLockContext lc = OppiaineLockContext.of(OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS, perusteId, oppimaara.getId(), null);
        lockService.lock(lc);
        oppimaara.setTehtava(to("Tehtävä", "Tehtävä"));
//        versionDto = perusteService.getPerusteVersion(perusteId);
        oppimaara = service.updateOppiaine(perusteId, new UpdateDto<>(oppimaara), OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
//        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());
        lockService.unlock(lc);
        assertEquals("Tehtävä", oppimaara.getTehtava().getTeksti().get().get(Kieli.FI));
    }

    private static final Logger LOG = LoggerFactory.getLogger(OppiaineServiceIT.class);

    private TavoitteenArviointiDto arvio(String kohde, String kuvaus, String hyvakuvaus, Integer arvosana) {
        TavoitteenArviointiDto arvio = new TavoitteenArviointiDto();
        arvio.setArvioinninKohde(olt(kohde));
        arvio.setOsaamisenKuvaus(olt(kuvaus));
        arvio.setHyvanOsaamisenKuvaus(olt(hyvakuvaus));
        arvio.setArvosana(Optional.of(arvosana));

        return arvio;
    }
}
