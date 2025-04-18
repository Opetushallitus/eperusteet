package fi.vm.sade.eperusteet.service.navigation;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.yl.Vuosiluokka;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineBaseDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineenVuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.repository.VuosiluokkaKokonaisuusRepository;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.yl.OppiaineLockContext;
import fi.vm.sade.eperusteet.service.yl.OppiaineOpetuksenSisaltoTyyppi;
import fi.vm.sade.eperusteet.service.yl.OppiaineService;
import fi.vm.sade.eperusteet.service.yl.VuosiluokkaKokonaisuusService;
import org.assertj.core.util.Maps;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static fi.vm.sade.eperusteet.service.test.util.TestUtils.lt;
import static org.assertj.core.api.Assertions.assertThat;

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
        OppiaineDto oppiaine = addOppiaine(null, "oppiaine4", 2l, true);
        oppiaine.setOppimaarat(Sets.newHashSet(addOppimaara(vk, "nimi2", 8l, oppiaine), addOppimaara(vk, "nimi1", 7l, oppiaine)));

        final OppiaineLockContext ctx = OppiaineLockContext.of(OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS, perusteId, oppiaine.getId(), null);
        lockService.lock(ctx);
        service.updateOppiaine(perusteId, new UpdateDto<>(oppiaine), OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
    }

    @Test
    public void testPerusopetusnavigation() {
        NavigationNodeDto navigationNodeDto  = dispatcher.get(perusteId, NavigationBuilder.class).buildNavigation(perusteId, "fi");
        assertThat(navigationNodeDto).isNotNull();
        assertThat(navigationNodeDto.getChildren()).hasSize(3);
        assertThat(navigationNodeDto.getChildren()).extracting("type")
                .containsExactly(NavigationType.vuosiluokkakokonaisuudet, NavigationType.perusopetusoppiaineet, NavigationType.perusopetuslaajaalaisetosaamiset);
        assertThat(navigationNodeDto.getChildren().get(1).getChildren()).hasSize(4);
        assertThat(navigationNodeDto.getChildren().get(1).getChildren()).extracting("type")
                .containsOnly(NavigationType.perusopetusoppiaine);
        assertThat(navigationNodeDto.getChildren().get(1).getChildren()).hasSize(4); // oppiaineet
        assertThat(navigationNodeDto.getChildren().get(1).getChildren().get(0).getChildren()).hasSize(3); // oppiaineen oppimaarat
        assertThat(navigationNodeDto.getChildren().get(1).getChildren().get(0).getChildren().get(0).getType()).isEqualTo(NavigationType.oppimaarat); // oppiaineen oppimaarat
        assertThat(navigationNodeDto.getChildren().get(1).getChildren().get(0).getChildren().get(1).getType()).isEqualTo(NavigationType.perusopetusoppiaine);
        assertThat(navigationNodeDto.getChildren().get(1).getChildren().get(0).getChildren().get(1).getMeta()).isEqualTo(Maps.newHashMap("oppimaara", true));
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
        OppiaineDto oppiaineDto = oppiaine(nimi, jnro, Kieli.FI);
        OppiaineenVuosiluokkaKokonaisuusDto vkDto = new OppiaineenVuosiluokkaKokonaisuusDto();
        if (vk != null) {
            vkDto.setVuosiluokkaKokonaisuus(Optional.of(Reference.of(vk.getId())));
            oppiaineDto.setVuosiluokkakokonaisuudet(Sets.newHashSet(vkDto));
        }
        oppiaineDto.setKoosteinen(Optional.of(koosteinen));
        oppiaineDto = service.addOppiaine(perusteId, oppiaineDto, OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);

        if (vk != null) {
            service.addOppiaineenVuosiluokkaKokonaisuus(perusteId, oppiaineDto.getId(), vkDto);
        }

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
            baseOppiaine.setNimi(lt(nimi, kieli));
        }
        if (jnro != null) {
            baseOppiaine.setJnro(Optional.of(jnro));
        }

        return baseOppiaine;
    }

}
