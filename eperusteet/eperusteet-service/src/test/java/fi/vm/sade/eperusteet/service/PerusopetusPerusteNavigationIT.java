package fi.vm.sade.eperusteet.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.lops2019.Lops2019Sisalto;
import fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminen;
import fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenKokonaisuus;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.moduuli.Lops2019Moduuli;
import fi.vm.sade.eperusteet.domain.yl.EsiopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.Vuosiluokka;
import fi.vm.sade.eperusteet.domain.yl.VuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteVersionDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.yl.KeskeinenSisaltoalueDto;
import fi.vm.sade.eperusteet.dto.yl.OpetuksenKohdealueDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineBaseDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineenVuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineenVuosiluokkaKokonaisuusSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.VuosiluokkaKokonaisuusRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019ModuuliRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019OppiaineRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019SisaltoRepository;
import fi.vm.sade.eperusteet.service.impl.NavigationBuilderDefault;
import fi.vm.sade.eperusteet.service.impl.NavigationBuilderLops2019;
import fi.vm.sade.eperusteet.service.impl.NavigationBuilderPerusopetus;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.DtoMapperConfig;
import fi.vm.sade.eperusteet.service.mapping.KoodistokoodiConverter;
import fi.vm.sade.eperusteet.service.mapping.ReferenceableEntityConverter;
import fi.vm.sade.eperusteet.service.mapping.TekstiPalanenConverter;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.yl.LaajaalainenOsaaminenContext;
import fi.vm.sade.eperusteet.service.yl.OppiaineLockContext;
import fi.vm.sade.eperusteet.service.yl.OppiaineOpetuksenSisaltoTyyppi;
import fi.vm.sade.eperusteet.service.yl.OppiaineService;
import fi.vm.sade.eperusteet.service.yl.VuosiluokkaKokonaisuusService;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import static fi.vm.sade.eperusteet.service.test.util.TestUtils.lt;
import static fi.vm.sade.eperusteet.service.test.util.TestUtils.olt;
import static fi.vm.sade.eperusteet.service.test.util.TestUtils.to;
import static fi.vm.sade.eperusteet.service.test.util.TestUtils.uniikkiString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

@DirtiesContext
public class PerusopetusPerusteNavigationIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteDispatcher dispatcher;

    @Autowired
    private OppiaineService service;

    @Autowired
    private VuosiluokkaKokonaisuusRepository vkrepo;

    private Long perusteId;

    @Autowired
    @LockCtx(OppiaineLockContext.class)
    private LockService<OppiaineLockContext> lockService;

    @Autowired
    @Dto
    protected DtoMapper mapper;

    @Autowired
    private VuosiluokkaKokonaisuusService vuosiluokkaKokonaisuusService;

    @Transactional
    @Before
    public void setup() {
        Peruste peruste = perusteService.luoPerusteRunko(KoulutusTyyppi.PERUSOPETUS, null, LaajuusYksikko.OPINTOVIIKKO, PerusteTyyppi.NORMAALI);
        perusteId = peruste.getId();

        VuosiluokkaKokonaisuusDto vk = new VuosiluokkaKokonaisuusDto();
        vk.setVuosiluokat(Sets.newHashSet(Vuosiluokka.VUOSILUOKKA_1, Vuosiluokka.VUOSILUOKKA_2));
        vk = vuosiluokkaKokonaisuusService.addVuosiluokkaKokonaisuus(perusteId, vk);

        addOppiaine(vk, "oppiaine1", 5l, false);
        addOppiaine(vk, "oppiaine2", null, false);
        addOppiaine(vk, "oppiaine3", 3l, false);
        OppiaineDto oppiaine = addOppiaine(vk, "oppiaine4", 2l, true);
        oppiaine.setOppimaarat(Sets.newHashSet(addOppimaara(vk, "nimi2", 8l, oppiaine), addOppimaara(vk, "nimi1", 7l, oppiaine)));

        final OppiaineLockContext ctx = OppiaineLockContext.of(OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS, perusteId, oppiaine.getId(), null);
        lockService.lock(ctx);
        service.updateOppiaine(perusteId, new UpdateDto<>(oppiaine), OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
    }

    @Test
    public void testPerusopetusnavigation() {
        NavigationNodeDto navigationNodeDto  = dispatcher.get(perusteId, NavigationBuilder.class).buildNavigation(perusteId, "fi");
        assertThat(navigationNodeDto).isNotNull();
        assertThat(navigationNodeDto.getChildren()).hasSize(3); // viitteet + vlk + oppiaineet
        assertThat(navigationNodeDto.getChildren()).extracting("type")
                .containsExactly(NavigationType.viite, NavigationType.vuosiluokkakokonaisuus, NavigationType.perusopetusoppiaineet);
        assertThat(navigationNodeDto.getChildren().get(1).getChildren()).hasSize(4); // vlk oppiaineet
        assertThat(navigationNodeDto.getChildren().get(1).getChildren()).extracting("type")
                .containsOnly(NavigationType.perusopetusoppiaine);
        assertThat(navigationNodeDto.getChildren().get(2).getChildren()).hasSize(4); // oppiaineet
        assertThat(navigationNodeDto.getChildren().get(2).getChildren().get(0).getChildren()).hasSize(1); // oppiaineen oppimaarat
        assertThat(navigationNodeDto.getChildren().get(2).getChildren().get(0).getChildren().get(0).getChildren()).hasSize(2); // oppiaineen oppimaarat
    }

    private OppiaineSuppeaDto addOppimaara(VuosiluokkaKokonaisuusDto vk, String nimi, Long jnro, OppiaineDto oppiaine) {
        OppiaineenVuosiluokkaKokonaisuusDto vkDto = new OppiaineenVuosiluokkaKokonaisuusDto();
        vkDto.setVuosiluokkaKokonaisuus(Optional.of(Reference.of(vk.getId())));
        OppiaineDto oppimaara = oppiaine(nimi, jnro, Kieli.FI);
        oppimaara.setOppiaine(Optional.of(Reference.of(oppiaine.getId())));
        oppimaara.setVuosiluokkakokonaisuudet(Sets.newHashSet(vkDto));
        oppimaara = service.addOppiaine(perusteId, oppimaara, OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
        vkDto = service.addOppiaineenVuosiluokkaKokonaisuus(perusteId, oppimaara.getId(), vkDto);

        OppiaineSuppeaDto oppimaaraSuppea = new OppiaineSuppeaDto();
        oppimaaraSuppea.setId(oppimaara.getId());

        return oppimaaraSuppea;
    }

    private OppiaineDto addOppiaine(VuosiluokkaKokonaisuusDto vk, String nimi, Long jnro, boolean koosteinen) {
        OppiaineenVuosiluokkaKokonaisuusDto vkDto = new OppiaineenVuosiluokkaKokonaisuusDto();
        vkDto.setVuosiluokkaKokonaisuus(Optional.of(Reference.of(vk.getId())));
        OppiaineDto oppiaineDto = oppiaine(nimi, jnro, Kieli.FI);
        oppiaineDto.setVuosiluokkakokonaisuudet(Sets.newHashSet(vkDto));
        oppiaineDto.setKoosteinen(Optional.of(koosteinen));
        oppiaineDto = service.addOppiaine(perusteId, oppiaineDto, OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
        service.addOppiaineenVuosiluokkaKokonaisuus(perusteId, oppiaineDto.getId(), vkDto);

        return oppiaineDto;
    }

    private OppiaineDto oppiaine(String nimi, Long jnro, Kieli kieli) {
        OppiaineDto oppiaineDto = new OppiaineDto();
        return oppiaineBase(oppiaineDto, nimi, jnro, kieli);
    }

    private OppiaineSuppeaDto oppiainesuppera(String nimi, Long jnro, Kieli kieli, OppiaineDto parentOppiaine) {
        OppiaineSuppeaDto oppiaineDto = new OppiaineSuppeaDto();
        oppiaineDto = oppiaineBase(oppiaineDto, nimi, jnro, kieli);
        oppiaineDto.setOppiaine(Optional.of(Reference.of(parentOppiaine.getId())));
        return oppiaineBase(oppiaineDto, nimi, jnro, kieli);
    }

    private <T extends OppiaineBaseDto> T oppiaineBase(T baseOppiaine, String nimi, Long jnro, Kieli kieli) {
        if (nimi != null) {
            baseOppiaine.setNimi(Optional.of(lt(nimi, kieli)));
        }
        if (jnro != null) {
            baseOppiaine.setJnro(Optional.of(jnro));
        }

        return baseOppiaine;
    }

}
