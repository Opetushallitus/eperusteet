package fi.vm.sade.eperusteet.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.lops2019.Lops2019Sisalto;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.moduuli.Lops2019Moduuli;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import fi.vm.sade.eperusteet.dto.lops2019.Lops2019OppiaineKaikkiDto;
import fi.vm.sade.eperusteet.dto.lops2019.Lops2019SisaltoDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.Lops2019ArviointiDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.Lops2019OppiaineDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.moduuli.Lops2019ModuuliBaseDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.moduuli.Lops2019ModuuliDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.moduuli.Lops2019ModuuliTavoiteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiImportDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.resource.config.InitJacksonConverter;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import fi.vm.sade.eperusteet.service.util.Pair;
import fi.vm.sade.eperusteet.service.util.PerusteprojektiTestHelper;
import fi.vm.sade.eperusteet.service.yl.Lops2019Service;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@Transactional
@DirtiesContext
public class Lops2019ServiceIT extends AbstractPerusteprojektiTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteDispatcher dispatcher;

    @Autowired
    private PerusteRepository repository;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteprojektiTestUtils ppTestUtils;

    @Autowired
    private Lops2019Service lops2019Service;

    @Autowired
    private PerusteprojektiTestHelper projektiHelper;

    @Before
    public void beforeEach() {
        projektiHelper.setup(KoulutusTyyppi.LUKIOKOULUTUS, KoulutustyyppiToteutus.LOPS2019);
    }

    @Override
    @Before
    public void setup() {
        InitJacksonConverter.configureObjectMapper(objectMapper);
        objectMapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    @Rollback
    public void readJsonToDto() throws IOException {
        final PerusteKaikkiDto perusteDto = this.readPerusteFile();
        Assert.notNull(perusteDto, "Perusteen lukeminen epäonnistui");
    }

    @Test
    @Rollback
    public void convertDtoToEntity() throws IOException {
        final PerusteKaikkiDto perusteDto = this.readPerusteFile();
        final Lops2019SisaltoDto lops2019SisaltoDto = perusteDto.getLops2019Sisalto();
        Assert.notNull(lops2019SisaltoDto, "Perusteen sisältö puuttuu");
        final List<Lops2019OppiaineKaikkiDto> oppiaineetDto = lops2019SisaltoDto.getOppiaineet();
        Assert.notNull(oppiaineetDto, "Perusteen oppiaineet puuttuvat");
        final Peruste peruste = mapper.map(perusteDto, Peruste.class);
        final Lops2019Sisalto lops2019Sisalto = peruste.getLops2019Sisalto();
        Assert.notNull(lops2019Sisalto, "Perusteen sisältö puuttuu");
        final List<Lops2019Oppiaine> oppiaineet = lops2019Sisalto.getOppiaineet();
        Assert.notNull(oppiaineet, "Perusteen oppiaineet puuttuvat");

    }

    @Test
    @Rollback
    public void createLops2019Peruste() throws IOException {
        final PerusteprojektiDto pp = ppTestUtils.createPerusteprojekti(ppl -> {
            ppl.setKoulutustyyppi(KoulutusTyyppi.LUKIOKOULUTUS.toString());
            ppl.setToteutus(KoulutustyyppiToteutus.LOPS2019);
            ppl.setLaajuusYksikko(LaajuusYksikko.OPINTOPISTE);
        });

        final PerusteDto perusteDto = ppTestUtils.initPeruste(pp.getPeruste().getIdLong());

        final PerusteKaikkiDto perusteKaikkiDto = perusteService.getJulkaistuSisalto(perusteDto.getId());
        Assert.notNull(perusteKaikkiDto.getLops2019Sisalto(), "Perusteen sisältö puuttuu");

        final Peruste peruste = repository.findOne(perusteKaikkiDto.getId());
        Assert.notNull(peruste, "Peruste puuttuu");

        // Haetaan tiedostosta perusteen sisältö
        final PerusteKaikkiDto perusteTiedostosta = this.readPerusteFile();
        this.updatePeruste(peruste, perusteTiedostosta);

        Assert.notNull(peruste.getLops2019Sisalto().getLaajaAlainenOsaaminen(),
                "Perusteen sisällön laaja-alainen osaaminen puuttuu");
        assertThat(peruste.getLops2019Sisalto().getLaajaAlainenOsaaminen().getLaajaAlaisetOsaamiset()).hasSize(6);
    }

    private Peruste updatePeruste(final Peruste peruste, final PerusteKaikkiDto uusi) {
        final Lops2019Sisalto lops2019Sisalto = mapper.map(uusi.getLops2019Sisalto(), Lops2019Sisalto.class);
        peruste.getLops2019Sisalto().setSisalto(lops2019Sisalto.getSisalto());
        peruste.getLops2019Sisalto().setLaajaAlainenOsaaminen(lops2019Sisalto.getLaajaAlainenOsaaminen());
        peruste.getLops2019Sisalto().setOppiaineet(lops2019Sisalto.getOppiaineet());
        return repository.save(peruste);
    }

    private PerusteKaikkiDto readPerusteFile() throws IOException {
        final Resource resource = new ClassPathResource("material/lops.json");
        return objectMapper.readValue(resource.getFile(), PerusteKaikkiDto.class);
    }

    @Test
    @Rollback
    public void testOppiaineet() {
        final Pair<Lops2019OppiaineDto, Long> pair = this.createOppiaine();
        Lops2019OppiaineDto lisatty = pair.getFirst();
        final Long moduuli1 = pair.getSecond();

        lisatty = lops2019Service.getOppiaine(projektiHelper.getPerusteId(), lisatty.getId());
        assertThat(lisatty.getOppimaarat()).hasSize(1);
        final Lops2019ModuuliDto moduuliDto = lops2019Service.getModuuli(
                projektiHelper.getPerusteId(),
                lisatty.getOppimaarat().get(0).getId(),
                moduuli1);
        assertThat(moduuliDto.getTavoitteet().getKohde().get(Kieli.FI)).isEqualTo("kohde");
    }

    @Test
    public void testKielletytRakennemuutokset() {
        final Long perusteId = this.createLops2019PerusteAndChangeTilaJulkaistu();

        // Yritetään lisätä laaja-alainen osaaminen
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> lops2019Service.addLaajaAlainenOsaaminen(perusteId));

        // Yritetään lisätä oppiaine
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> {
                    final Lops2019OppiaineDto oppiaineDto = new Lops2019OppiaineDto();
                    oppiaineDto.setNimi(LokalisoituTekstiDto.of("oppiaine"));
                    lops2019Service.addOppiaine(perusteId, oppiaineDto);
                });

        // Yritetään lisätä moduuli
        final List<Lops2019OppiaineDto> oppiaineet = lops2019Service.getOppiaineet(perusteId);
        assertThat(oppiaineet).isNotEmpty();
        final Lops2019OppiaineDto oppiaineDto = oppiaineet.get(0);

        // Vaihdetaan oppimäärien järjestystä
        Lops2019OppiaineDto oppiaine = lops2019Service.getOppiaine(perusteId, oppiaineDto.getId());
        final List<Lops2019OppiaineDto> oppimaarat = oppiaine.getOppimaarat();
        assertThat(oppimaarat).hasSize(2);
        List<Lops2019OppiaineDto> uudetOppimaarat = new ArrayList<>();
        uudetOppimaarat.add(oppimaarat.get(1));
        uudetOppimaarat.add(oppimaarat.get(0));
        oppiaineDto.setOppimaarat(uudetOppimaarat);
        lops2019Service.updateOppiaine(perusteId, oppiaineDto);

        // Yritetään poistaa oppimäärä
        uudetOppimaarat.remove(0);
        oppiaineDto.setOppimaarat(uudetOppimaarat);
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> lops2019Service.updateOppiaine(perusteId, oppiaineDto));

        final Lops2019ModuuliBaseDto moduuliDto = new Lops2019ModuuliBaseDto();
        moduuliDto.setNimi(LokalisoituTekstiDto.of("moduuli"));
        moduuliDto.setPakollinen(true);

        final KoodiDto koodiDto = new KoodiDto();
        koodiDto.setUri("moduulikoodistolops2021_moduuli");
        koodiDto.setKoodisto("moduulikoodistolops2021");
        koodiDto.setVersio(1L);
        moduuliDto.setKoodi(koodiDto);

        oppiaineDto.getModuulit().add(moduuliDto);

        // Yritetään tehdä oppiaineen rakennemuutos
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> lops2019Service.updateOppiaine(perusteId, oppiaineDto));
    }

    @Test
    @Rollback
    public void testOppiaineSort() {
        final Pair<Lops2019OppiaineDto, Long> pair = this.createOppiaine();
        final Lops2019OppiaineDto luotuOppiaine = pair.getFirst();
        this.createPlainOppiaine();

        this.checkLaajaOppiaine(luotuOppiaine.getId());

        final List<Lops2019OppiaineDto> oppiaineet = lops2019Service.getOppiaineet(projektiHelper.getPerusteId());
        Collections.reverse(oppiaineet);
        lops2019Service.sortOppiaineet(projektiHelper.getPerusteId(), oppiaineet);

        this.checkLaajaOppiaine(luotuOppiaine.getId());
    }


    @Test
    @Ignore("oppiaineRepository.getRevisions(oaId) ei palauta revisioita testeissä")
    @Rollback
    public void testOppiaineidenPalautus() {

        final Pair<Lops2019OppiaineDto, Long> pair = this.createOppiaine();
        final Lops2019OppiaineDto luotuOppiaine = pair.getFirst();
        this.createPlainOppiaine();

        // Tarkistetaan lähtörakenne
        this.checkLaajaOppiaine(luotuOppiaine.getId());

        // Poistetaan oppimäärä
        List<Lops2019OppiaineDto> oppiaineet = lops2019Service.getOppiaineet(projektiHelper.getPerusteId());
        final Optional<Lops2019OppiaineDto> oppiaineOptional = oppiaineet.stream()
                .filter(oa -> Objects.equals(oa.getId(), pair.getFirst().getId()))
                .findAny();
        assertThat(oppiaineOptional).isPresent();
        final Lops2019OppiaineDto oppiaine = oppiaineOptional.get();
        oppiaine.getOppimaarat().clear();
        lops2019Service.updateOppiaine(projektiHelper.getPerusteId(), oppiaine);


        // Yritetään palauttaa oppimäärä
        lops2019Service.palautaSisaltoOppiaineet(projektiHelper.getPerusteId());

        // Tarkistetaan loppurakenne
        oppiaineet = lops2019Service.getOppiaineet(projektiHelper.getPerusteId());
        this.checkLaajaOppiaine(oppiaineet.get(0).getId());
    }

    private Lops2019Oppiaine mapOppiaine(Lops2019OppiaineKaikkiDto oa) {
        Lops2019Oppiaine result = mapper.map(oa, Lops2019Oppiaine.class);
        result.setOppimaarat(oa.getOppimaarat().stream()
                .map(this::mapOppiaine)
                .collect(Collectors.toList()));
        result.setModuulit(oa.getModuulit().stream()
                .map(moduuli -> mapper.map(moduuli, Lops2019Moduuli.class))
                .collect(Collectors.toList()));
        return result;
    }

    @Test
    public void testProjektinTuonti() throws IOException {
        final PerusteKaikkiDto perusteData = this.readPerusteFile();
        final Peruste alkuperainen = mapper.map(perusteData, Peruste.class);
        final PerusteprojektiImportDto idto = new PerusteprojektiImportDto(PerusteprojektiLuontiDto.builder()
                .koulutustyyppi(KoulutusTyyppi.LUKIOKOULUTUS.toString())
                .toteutus(KoulutustyyppiToteutus.LOPS2019)
                .build(), perusteData, new ArrayList<>(), new HashMap<>(), new ArrayList<>());
        alkuperainen.getLops2019Sisalto().setOppiaineet(perusteData.getLops2019Sisalto().getOppiaineet().stream()
                .map(this::mapOppiaine)
                .collect(Collectors.toList()));
        idto.getProjekti().setNimi("projekti");
        idto.getProjekti().setDiaarinumero("1234");
        final PerusteprojektiDto lisatty = dispatcher.get(idto.getPeruste(), PerusteImport.class)
                .tuoPerusteprojekti(idto);
        assertThat(lisatty)
                .extracting("nimi", "diaarinumero")
                .contains("projekti", "1234");
        em.flush();
        final Peruste peruste = perusteRepository.getOne(lisatty.getPeruste().getIdLong());
        assertThat(peruste)
                .extracting("koulutustyyppi", "toteutus")
                .contains(KoulutusTyyppi.LUKIOKOULUTUS.toString(), KoulutustyyppiToteutus.LOPS2019);
        assertThat(peruste.getLops2019Sisalto().structureEquals(alkuperainen.getLops2019Sisalto()))
                .isTrue();
    }

    @Test
    public void testModuuliKoodiMapping() {
        Lops2019ModuuliDto dto = new Lops2019ModuuliDto();
        dto.setId(1L);
        KoodiDto koodiDto = new KoodiDto();
        koodiDto.setVersio(1L);
        koodiDto.setKoodisto(KoodistoUriArvo.MODUULIKOODISTOLOPS2021);
        koodiDto.setUri(KoodistoUriArvo.MODUULIKOODISTOLOPS2021 + "_abc1");
        dto.setKoodi(koodiDto);

        Lops2019Moduuli moduuli = new Lops2019Moduuli();

        mapper.map(dto, moduuli);

        assertThat(moduuli).isNotNull();
        assertThat(moduuli.getId()).isEqualTo(dto.getId());
        assertThat(moduuli.getKoodi()).isNotNull();
        assertThat(moduuli.getKoodi().getKoodisto()).isEqualTo(koodiDto.getKoodisto());
        assertThat(moduuli.getKoodi().getUri()).isEqualTo(koodiDto.getUri());
        assertThat(moduuli.getKoodi().getVersio()).isEqualTo(koodiDto.getVersio());
    }

    private void checkLaajaOppiaine(final Long oppiaineId) {
        final List<Lops2019OppiaineDto> oppiaineet = lops2019Service.getOppiaineet(projektiHelper.getPerusteId());
        final Optional<Lops2019OppiaineDto> oppiaine = oppiaineet.stream()
                .filter(oa -> Objects.equals(oa.getId(), oppiaineId))
                .findAny();

        assertThat(oppiaine).isPresent();

        {
            final Lops2019OppiaineDto oppiaineLaaja = lops2019Service.getOppiaine(projektiHelper.getPerusteId(), oppiaine.get().getId());
            final List<Lops2019OppiaineDto> oppimaarat = oppiaineLaaja.getOppimaarat();
            assertThat(oppimaarat).hasSize(1);
            final Lops2019OppiaineDto oppimaaraLaaja = lops2019Service.getOppiaine(projektiHelper.getPerusteId(), oppimaarat.get(0).getId());
            final List<Lops2019ModuuliBaseDto> moduulit = oppimaaraLaaja.getModuulit();
            assertThat(moduulit).hasSize(2);
        }
    }

    private Lops2019OppiaineDto createPlainOppiaine() {
        final Lops2019OppiaineDto oa = new Lops2019OppiaineDto();
        { // Oppiaine
            oa.setNimi(LokalisoituTekstiDto.of("plainoppiaine"));
            oa.setKoodi(KoodiDto.of("oppiaineetjaoppimaaratlops2021", "111"));
            oa.setArviointi(Lops2019ArviointiDto.builder()
                    .kuvaus(LokalisoituTekstiDto.of("arviointi"))
                    .build());
        }

        return lops2019Service.addOppiaine(projektiHelper.getPerusteId(), oa);
    }

    private Pair<Lops2019OppiaineDto, Long> createOppiaine() {
        final Lops2019OppiaineDto oa = new Lops2019OppiaineDto();
        { // Oppiaine
            oa.setNimi(LokalisoituTekstiDto.of("oppiaine"));
            oa.setKoodi(KoodiDto.of("oppiaineetjaoppimaaratlops2021", "123"));
            oa.setArviointi(Lops2019ArviointiDto.builder()
                    .kuvaus(LokalisoituTekstiDto.of("arviointi"))
                    .build());
        }

        Lops2019OppiaineDto lisatty = lops2019Service.addOppiaine(projektiHelper.getPerusteId(), oa);

        { // Oppimaara
            final Lops2019OppiaineDto om = new Lops2019OppiaineDto();
            om.setNimi(LokalisoituTekstiDto.of("oppimaara"));
            om.setKoodi(KoodiDto.of("oppiaineetjaoppimaaratlops2021", "1234"));
            om.setArviointi(Lops2019ArviointiDto.builder()
                    .kuvaus(LokalisoituTekstiDto.of("arviointi"))
                    .build());
            lisatty.getOppimaarat().add(om);
            lisatty = lops2019Service.updateOppiaine(projektiHelper.getPerusteId(), lisatty);
        }

        Long moduuli1 = 0L;
        Lops2019OppiaineDto om = lisatty.getOppimaarat().get(0);

        { // Moduuli 1
            final Lops2019ModuuliBaseDto moduuli = new Lops2019ModuuliBaseDto();
            moduuli.setNimi(LokalisoituTekstiDto.of("moduuli 1"));
            moduuli.setPakollinen(false);
            om.getModuulit().add(moduuli);
            om = lops2019Service.updateOppiaine(projektiHelper.getPerusteId(), om);

            final Lops2019ModuuliBaseDto moduuliBaseDto = om.getModuulit().get(0);
            final Lops2019ModuuliDto moduuliDto = lops2019Service.getModuuli(projektiHelper.getPerusteId(), om.getId(), moduuliBaseDto.getId());
            moduuli1 = moduuliDto.getId();
            moduuliDto.setKoodi(KoodiDto.of("moduulikoodistolops2021", "123"));
            moduuliDto.setKuvaus(LokalisoituTekstiDto.of("kuvaus"));
            final Lops2019ModuuliTavoiteDto tavoite = new Lops2019ModuuliTavoiteDto();
            tavoite.setKohde(LokalisoituTekstiDto.of("kohde"));
            moduuliDto.setTavoitteet(tavoite);
            lops2019Service.updateModuuli(projektiHelper.getPerusteId(), moduuliDto);
        }

        { // Moduuli 2
            final Lops2019ModuuliBaseDto moduuli = new Lops2019ModuuliBaseDto();
            moduuli.setNimi(LokalisoituTekstiDto.of("moduuli 2"));
            moduuli.setPakollinen(false);
            om.getModuulit().add(moduuli);
            lops2019Service.updateOppiaine(projektiHelper.getPerusteId(), om);
        }

        return new Pair<>(lisatty, moduuli1);
    }

    private Long createLops2019PerusteAndChangeTilaJulkaistu() {
        final PerusteprojektiDto pp = ppTestUtils.createPerusteprojekti(ppl -> {
            ppl.setKoulutustyyppi(KoulutusTyyppi.LUKIOKOULUTUS.toString());
            ppl.setToteutus(KoulutustyyppiToteutus.LOPS2019);
            ppl.setLaajuusYksikko(LaajuusYksikko.OPINTOPISTE);
        });

        final Long perusteId = pp.getPeruste().getIdLong();

        final PerusteDto perusteDto = ppTestUtils.initPeruste(perusteId);

        final PerusteKaikkiDto perusteKaikkiDto = perusteService.getJulkaistuSisalto(perusteDto.getId());
        final Lops2019SisaltoDto sisalto = perusteKaikkiDto.getLops2019Sisalto();
        Assert.notNull(sisalto, "Perusteen sisältö puuttuu");

        final Lops2019OppiaineDto oppiaineDto = new Lops2019OppiaineDto();
        oppiaineDto.setNimi(LokalisoituTekstiDto.of("oppiaine"));
        final KoodiDto koodiDto = new KoodiDto();
        koodiDto.setUri("oppiaineetjaoppimaaratlops2021_oppiaine");
        koodiDto.setKoodisto("oppiaineetjaoppimaaratlops2021");
        koodiDto.setVersio(1L);
        oppiaineDto.setKoodi(koodiDto);

        Lops2019OppiaineDto dto = lops2019Service.addOppiaine(perusteId, oppiaineDto);


        Lops2019OppiaineDto oppimaara1Dto = new Lops2019OppiaineDto();
        oppimaara1Dto.setNimi(LokalisoituTekstiDto.of("om1"));
        oppimaara1Dto.setOppiaine(Reference.of(dto.getId()));
        final KoodiDto koodi1Dto = new KoodiDto();
        koodi1Dto.setUri("oppiaineetjaoppimaaratlops2021_om1");
        koodi1Dto.setKoodisto("oppiaineetjaoppimaaratlops2021");
        koodi1Dto.setVersio(1L);
        oppimaara1Dto.setKoodi(koodi1Dto);
        dto.getOppimaarat().add(oppimaara1Dto);

        Lops2019OppiaineDto oppimaara2Dto = new Lops2019OppiaineDto();
        oppimaara2Dto.setNimi(LokalisoituTekstiDto.of("om2"));
        oppimaara2Dto.setOppiaine(Reference.of(dto.getId()));
        final KoodiDto koodi2Dto = new KoodiDto();
        koodi2Dto.setUri("oppiaineetjaoppimaaratlops2021_om2");
        koodi2Dto.setKoodisto("oppiaineetjaoppimaaratlops2021");
        koodi2Dto.setVersio(1L);
        oppimaara2Dto.setKoodi(koodi2Dto);
        dto.getOppimaarat().add(oppimaara2Dto);

        lops2019Service.updateOppiaine(perusteId, dto);


        // Julkaistaan peruste
        ppTestUtils.julkaise(pp.getId());

        return perusteId;
    }


}
