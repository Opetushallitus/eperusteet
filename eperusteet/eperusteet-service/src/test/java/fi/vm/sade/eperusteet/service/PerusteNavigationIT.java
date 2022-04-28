package fi.vm.sade.eperusteet.service;

import com.google.common.collect.Lists;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.lops2019.Lops2019Sisalto;
import fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminen;
import fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenKokonaisuus;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.moduuli.Lops2019Moduuli;
import fi.vm.sade.eperusteet.domain.yl.EsiopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.vst.KotoKielitaitotasoDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019ModuuliRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019OppiaineRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019SisaltoRepository;
import fi.vm.sade.eperusteet.service.impl.PerusteServiceImpl;
import fi.vm.sade.eperusteet.service.impl.navigation.NavigationBuilderDefault;
import fi.vm.sade.eperusteet.service.impl.navigation.NavigationBuilderLops2019;
import fi.vm.sade.eperusteet.service.impl.navigationpublic.NavigationBuilderPublicLinkit;
import fi.vm.sade.eperusteet.service.mapping.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static fi.vm.sade.eperusteet.service.test.util.TestUtils.lt;
import static fi.vm.sade.eperusteet.service.test.util.TestUtils.uniikkiString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class PerusteNavigationIT {

    @Spy
    private PerusteRepository perusteRepository;

    @Spy
    private Lops2019SisaltoRepository lops2019SisaltoRepository;

    @Spy
    private Lops2019OppiaineRepository lops2019OppiaineRepository;

    @Spy
    private Lops2019ModuuliRepository lops2019ModuuliRepository;

    @Spy
    private PerusteDispatcher perusteDispatcher;

    @Spy
    private KoodistoClient koodistoClient;

    private NavigationBuilderDefault navigationBuilderDefault;
    private NavigationBuilderLops2019 navigationBuilderLops2019;

    @Before
    public void init() {
        doAnswer(i -> {
            Object arg = i.getArgument(0);
            KoodiDto koodi = (KoodiDto) arg;
            if (koodi != null) {
                koodi.setNimi(lt(uniikkiString()));
                if (koodi.getUri() != null) {
                    String[] s = koodi.getUri().split("_");
                    koodi.setArvo(s[s.length - 1]);
                }
            }
            return null;
        }).when(koodistoClient).addNimiAndArvo(any(KoodiDto.class));

        Lops2019Sisalto data = sisaltoData();

        Mockito.when(perusteRepository.findOne(anyLong())).thenReturn(perusteData());
        Mockito.when(lops2019SisaltoRepository.findByPerusteId(anyLong())).thenReturn(data);

        Mockito.when(lops2019ModuuliRepository.getModuulitByParents(anyList()))
                .thenReturn(data.getOppiaineet().get(0).getOppimaarat().get(0).getModuulit());

        Mockito.when(lops2019OppiaineRepository.getOppimaaratByParents(anyList()))
                .thenReturn(data.getOppiaineet().get(0).getOppimaarat());

        DtoMapper dtoMapper = new DtoMapperConfig(koodistoClient).normalDtoMapper(
                new TekstiPalanenConverter(),
                new ReferenceableEntityConverter(),
                new ArviointiConverter(),
                new KoodistokoodiConverter());

        this.navigationBuilderDefault = new NavigationBuilderDefault(dtoMapper, this.perusteRepository);

        doReturn(this.navigationBuilderDefault).when(perusteDispatcher).get(NavigationBuilder.class);

        this.navigationBuilderLops2019 = new NavigationBuilderLops2019(
                dtoMapper,
                lops2019SisaltoRepository,
                lops2019OppiaineRepository,
                lops2019ModuuliRepository,
                perusteDispatcher);
    }

    private Lops2019Sisalto sisaltoData() {
        Lops2019Sisalto sisalto = new Lops2019Sisalto();
        Lops2019Oppiaine oa = new Lops2019Oppiaine();
        oa.setId(101L);
        oa.setKoodi(Koodi.of("oppiaineet", "OA"));
        { // Oppimäärä
            Lops2019Oppiaine om = new Lops2019Oppiaine();
            om.setId(102L);
            om.asetaOppiaine(oa);
            om.setKoodi(Koodi.of("oppiaineet", "OM"));
            { // Moduuli 1
                Lops2019Moduuli m = new Lops2019Moduuli();
                m.setId(12L);
                m.setKoodi(Koodi.of("moduulit", "M2"));
                m.asetaOppiaine(om);
                om.getModuulit().add(m);
            }
            { // Moduuli 2
                Lops2019Moduuli m = new Lops2019Moduuli();
                m.setId(11L);
                m.setKoodi(Koodi.of("moduulit", "M1"));
                m.asetaOppiaine(om);
                om.getModuulit().add(m);
            }
            oa.getOppimaarat().add(om);
        }
        sisalto.setOppiaineet(Lists.newArrayList(oa));

        { // Laaja-alaiset osaamiset
            Lops2019LaajaAlainenOsaaminenKokonaisuus kokonaisuus = new Lops2019LaajaAlainenOsaaminenKokonaisuus();
            Lops2019LaajaAlainenOsaaminen laaja1 = new Lops2019LaajaAlainenOsaaminen();
            laaja1.setNimi(TekstiPalanen.of(Kieli.FI, "laaja 2"));
            laaja1.setId(500L);
            laaja1.setKoodi(Koodi.of("laajaalaiset", "LO2"));
            Lops2019LaajaAlainenOsaaminen laaja2 = new Lops2019LaajaAlainenOsaaminen();
            laaja2.setId(501L);
            laaja2.setNimi(TekstiPalanen.of(Kieli.FI, "laaja 1"));
            laaja2.setKoodi(Koodi.of("laajaalaiset", "LO1"));
            kokonaisuus.setLaajaAlaisetOsaamiset(Lists.newArrayList(laaja1, laaja2));
            sisalto.setLaajaAlainenOsaaminen(kokonaisuus);
        }
        return sisalto;
    }

    private Peruste perusteData() {
        Peruste peruste = new Peruste();
        peruste.setTyyppi(PerusteTyyppi.NORMAALI);
        peruste.setKoulutustyyppi("koulutustyyppi_15");
        peruste.setToteutus(KoulutustyyppiToteutus.YKSINKERTAINEN);

        { // Sisältö
            EsiopetuksenPerusteenSisalto sisalto = new EsiopetuksenPerusteenSisalto();
            PerusteenOsaViite root = new PerusteenOsaViite();
            root.setPerusteenOsa(new TekstiKappale());

            { // TekstiKappale
                PerusteenOsaViite alikappale = new PerusteenOsaViite();
                TekstiKappale tk = new TekstiKappale();
                alikappale.setId(100L);
                tk.setTeksti(TekstiPalanen.of(Kieli.FI, "x"));
                tk.setNimi(TekstiPalanen.of(Kieli.FI, "x"));
                alikappale.setPerusteenOsa(tk);
                root.setLapset(Collections.singletonList(alikappale));
            }
            sisalto.setSisalto(root);
            peruste.setSisalto(sisalto);
        }
        return peruste;
    }

    @Test
    public void testYksinkertainenNavigation() {
        NavigationNodeDto navigationNodeDto = navigationBuilderDefault.buildNavigation(42L, "fi");
        assertThat(navigationNodeDto).isNotNull();
        assertThat(navigationNodeDto.getType()).isEqualTo(NavigationType.root);
        assertThat(navigationNodeDto.getChildren().get(0))
                .extracting(NavigationNodeDto::getId, NavigationNodeDto::getType)
                .containsExactly(100L, NavigationType.viite);
    }

    @Test
    public void testLops2019Navigaatio() {
        NavigationNodeDto navigationNodeDto = navigationBuilderLops2019.buildNavigation(42L, "fi");
        assertThat(navigationNodeDto).isNotNull();
        assertThat(navigationNodeDto.getType()).isEqualTo(NavigationType.root);

        assertThat(navigationNodeDto.getChildren())
                .extracting("type", "id")
                .containsExactly(
                        tuple(NavigationType.viite, 100L),
                        tuple(NavigationType.laajaalaiset, null),
                        tuple(NavigationType.oppiaineet, null));

        assertThat(navigationNodeDto.getChildren().get(1).getChildren())
                .extracting("type", "id", "koodi.arvo")
                .containsExactly(
                        tuple(NavigationType.laajaalainen, 501L, "LO1"),
                        tuple(NavigationType.laajaalainen, 500L, "LO2"));

        NavigationNodeDto oppiaine = navigationNodeDto.getChildren().get(2).getChildren().get(0);
        assertThat(oppiaine.getChildren().get(0).getType()).isEqualTo(NavigationType.oppimaarat);

        assertThat(oppiaine)
                .returns(NavigationType.oppiaine, NavigationNodeDto::getType)
                .returns(101L, NavigationNodeDto::getId)
                .returns("OA", n -> n.getKoodi().getArvo());

        NavigationNodeDto oppimaara = oppiaine.getChildren().get(0).getChildren().get(0);
        assertThat(oppimaara.getChildren().get(0).getType()).isEqualTo(NavigationType.moduulit);

        assertThat(oppimaara)
                .returns(NavigationType.oppiaine, NavigationNodeDto::getType)
                .returns(102L, NavigationNodeDto::getId)
                .returns("OM", n -> n.getKoodi().getArvo());

        List<NavigationNodeDto> moduulit = oppimaara.getChildren().get(0).getChildren();
        assertThat(moduulit)
                .extracting("type", "id", "koodi.arvo")
                .containsExactly(
                        tuple(NavigationType.moduuli, 11L, "M1"),
                        tuple(NavigationType.moduuli, 12L, "M2"));
    }

    /**
     * Testataan navigaation generointia tekstikappaleille. Generoidaan itsenäisiä nodeja sekä
     * node jolla on lapsia.
     */
    @Test
    public void testTekstikappaleNavigationGeneration() {
        NavigationBuilderPublicLinkit navigationBuilder = new NavigationBuilderPublicLinkit(new PerusteServiceImpl());
        NavigationNodeDto result = navigationBuilder.constructNavigation(createPerusteeOsaViiteData());

        assertThat(result.getType()).isEqualTo(NavigationType.viite);
        assertThat(result.getId()).isEqualTo(10L);

        List<NavigationNodeDto> lapset = result.getChildren();

        NavigationNodeDto johdantoNode = lapset.get(0);
        assertThat(johdantoNode.getType()).isEqualTo(NavigationType.viite);
        assertThat(johdantoNode.getId()).isEqualTo(20L);

        NavigationNodeDto lahtokohdatNode = lapset.get(1);
        assertThat(lahtokohdatNode.getType()).isEqualTo(NavigationType.viite);
        assertThat(lahtokohdatNode.getId()).isEqualTo(30L);

        List<NavigationNodeDto> lahtokohdatLapset = lahtokohdatNode.getChildren();

        NavigationNodeDto arvoperustaNode = lahtokohdatLapset.get(0);
        assertThat(arvoperustaNode.getType()).isEqualTo(NavigationType.viite);
        assertThat(arvoperustaNode.getId()).isEqualTo(31L);

        NavigationNodeDto laajuusNode = lahtokohdatLapset.get(1);
        assertThat(laajuusNode.getType()).isEqualTo(NavigationType.muodostuminen);
        assertThat(laajuusNode.getId()).isEqualTo(32L);

        NavigationNodeDto kuvausasteikkoLiiteNode = lapset.get(3);
        assertThat(kuvausasteikkoLiiteNode.getType()).isEqualTo(NavigationType.liite);
        assertThat(kuvausasteikkoLiiteNode.getId()).isEqualTo(50L);
    }

    /**
     * Jos perusteenosa on tiettyä ennaltamääritettyä tyyppiä, tehdään parent nodesta tyyppiä linkkisivu.
     * Tämä siitä syystä että tietyille "hankalille" perusteenosille ei haluttu tehdä custom yhteenvetosivua, joten
     * alisivut päätettiin näyttää yhteenvetosivulla ainoastaan linkkeinä.
     */
    @Test
    public void testNodeTypeIsLinkkisivu() {
        NavigationBuilderPublicLinkit navigationBuilder = new NavigationBuilderPublicLinkit(new PerusteServiceImpl());
        NavigationNodeDto result = navigationBuilder.constructNavigation(createPerusteeOsaViiteData());

        List<NavigationNodeDto> lapset = result.getChildren();

        NavigationNodeDto tavoitteetNode = lapset.get(2);
        assertThat(tavoitteetNode.getId()).isEqualTo(40L);
        assertThat(tavoitteetNode.getType()).isEqualTo(NavigationType.linkkisivu);
    }

    /**
     * Testidatan hierarkia:
     *
     * - juurinode (id 10)
     *      - Johdanto (tekstikappale, id 20)
     *      - Koulutuksen järjestämisen lähtökohdat (tekstikappale, id 30)
     *          - Arvoperusta (tekstikappale, id 31)
     *          - Koulutuksen laajuus ja rakenne (muodostuminen, id 32)
     *      - Kotoutumiskoulutuksen tavoitteet ja keskeiset sisällöt (linkkisivu, id 40)
     *          - Kotoutumiskoulutuksen yleiset tavoitteet (tekstikappale, id 41)
     *          - Suomen kieli ja viestintätaidot (koto_kielitaitotaso, id 41)
     *      - Kielitaidon tasojen kuvausasteikko (liite, id 50)
     */
    private PerusteenOsaViiteDto.Laaja createPerusteeOsaViiteData() {
        PerusteenOsaViiteDto.Laaja johdanto = createTekstiLeafNode("Johdanto", 20L);
        PerusteenOsaViiteDto.Laaja lahtokohdat = createNodeWithChildren(
                "Koulutuksen järjestämisen lähtökohdat",
                30L,
                createTekstiLeafNode("Arvoperusta", 31L),
                createMuodostuminenLeafNode("Koulutuksen laajuus ja rakenne", 32L));

        PerusteenOsaViiteDto.Laaja tavoitteet = createNodeWithChildren(
                "Kotoutumiskoulutuksen tavoitteet ja keskeiset sisällöt",
                40L,
                createTekstiLeafNode("Kotoutumiskoulutuksen yleiset tavoitteet", 41L),
                createKielitaitotasoLeafNode("Suomen kieli ja viestintätaidot", 42L));

        PerusteenOsaViiteDto.Laaja kuvausasteikkoLiite = createTekstiLiiteLeafNode("Kielitaidon tasojen kuvausasteikko", 50L);

        ArrayList<PerusteenOsaViiteDto.Laaja> rootinLapset = new ArrayList<>();
        rootinLapset.add(johdanto);
        rootinLapset.add(lahtokohdat);
        rootinLapset.add(tavoitteet);
        rootinLapset.add(kuvausasteikkoLiite);

        PerusteenOsaViiteDto.Laaja rootNode = new PerusteenOsaViiteDto.Laaja();
        rootNode.setId(10L);
        rootNode.setLapset(rootinLapset);

        return rootNode;
    }

    private PerusteenOsaViiteDto.Laaja createTekstiLeafNode(String nimi, long id) {
        return createTekstiLeafNode(nimi, id, false, PerusteenOsaTunniste.NORMAALI);
    }

    private PerusteenOsaViiteDto.Laaja createTekstiLiiteLeafNode(String nimi, long id) {
        return createTekstiLeafNode(nimi, id, true, PerusteenOsaTunniste.NORMAALI);
    }

    private PerusteenOsaViiteDto.Laaja createMuodostuminenLeafNode(String nimi, long id) {
        return createTekstiLeafNode(nimi, id, false, PerusteenOsaTunniste.RAKENNE);
    }

    private PerusteenOsaViiteDto.Laaja createTekstiLeafNode(
            String nimi,
            long id,
            boolean hasLiite,
            PerusteenOsaTunniste perusteenOsaTunniste) {
        TekstiKappaleDto tekstiKappale = new TekstiKappaleDto();
        tekstiKappale.setNimi(LokalisoituTekstiDto.of(nimi));
        tekstiKappale.setTunniste(perusteenOsaTunniste);
        tekstiKappale.setLiite(hasLiite);

        PerusteenOsaViiteDto.Laaja node = new PerusteenOsaViiteDto.Laaja();
        node.setId(id);
        node.setLapset(new ArrayList<>());
        node.setPerusteenOsa(tekstiKappale);

        return node;
    }

    private PerusteenOsaViiteDto.Laaja createKielitaitotasoLeafNode(String nimi, long id) {
        KotoKielitaitotasoDto kielitaitotaso = new KotoKielitaitotasoDto();
        kielitaitotaso.setNimi(LokalisoituTekstiDto.of(nimi));
        kielitaitotaso.setTunniste(PerusteenOsaTunniste.NORMAALI);

        PerusteenOsaViiteDto.Laaja node = new PerusteenOsaViiteDto.Laaja();
        node.setId(id);
        node.setLapset(new ArrayList<>());
        node.setPerusteenOsa(kielitaitotaso);

        return node;
    }

    private PerusteenOsaViiteDto.Laaja createNodeWithChildren(String nimi, long id, PerusteenOsaViiteDto.Laaja... lapset) {
        PerusteenOsaViiteDto.Laaja node = createTekstiLeafNode(nimi, id);
        node.setLapset(new ArrayList<>(Arrays.asList(lapset)));

        return node;
    }
}
