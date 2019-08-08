package fi.vm.sade.eperusteet.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.lops2019.Lops2019Sisalto;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
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
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.resource.config.InitJacksonConverter;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import fi.vm.sade.eperusteet.service.util.PerusteprojektiTestHelper;
import fi.vm.sade.eperusteet.service.yl.Lops2019Service;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@Transactional
@DirtiesContext
public class Lops2019IT extends AbstractPerusteprojektiTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    @Dto
    private DtoMapper mapper;

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

    @Before
    public void setup() {
        InitJacksonConverter.configureObjectMapper(objectMapper);
        objectMapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    @Rollback
    public void readJsonToDto() throws IOException {
        PerusteKaikkiDto perusteDto = readPerusteFile();
        Assert.notNull(perusteDto, "Perusteen lukeminen epäonnistui");
    }

    @Test
    @Rollback
    public void convertDtoToEntity() throws IOException {
        PerusteKaikkiDto perusteDto = readPerusteFile();
        Lops2019SisaltoDto lops2019SisaltoDto = perusteDto.getLops2019Sisalto();
        Assert.notNull(lops2019SisaltoDto, "Perusteen sisältö puuttuu");
        List<Lops2019OppiaineKaikkiDto> oppiaineetDto = lops2019SisaltoDto.getOppiaineet();
        Assert.notNull(oppiaineetDto, "Perusteen oppiaineet puuttuvat");
        Peruste peruste = mapper.map(perusteDto, Peruste.class);
        Lops2019Sisalto lops2019Sisalto = peruste.getLops2019Sisalto();
        Assert.notNull(lops2019Sisalto, "Perusteen sisältö puuttuu");
        List<Lops2019Oppiaine> oppiaineet = lops2019Sisalto.getOppiaineet();
        Assert.notNull(oppiaineet, "Perusteen oppiaineet puuttuvat");

    }

    @Test
    @Rollback
    public void createLops2019Peruste() throws IOException {
        PerusteprojektiDto pp = ppTestUtils.createPerusteprojekti(ppl -> {
            ppl.setKoulutustyyppi(KoulutusTyyppi.LUKIOKOULUTUS.toString());
            ppl.setToteutus(KoulutustyyppiToteutus.LOPS2019);
            ppl.setLaajuusYksikko(LaajuusYksikko.OPINTOPISTE);
        });

        PerusteDto perusteDto = ppTestUtils.initPeruste(pp.getPeruste().getIdLong());

        PerusteKaikkiDto perusteKaikkiDto = perusteService.getKokoSisalto(perusteDto.getId());
        Assert.notNull(perusteKaikkiDto.getLops2019Sisalto(), "Perusteen sisältö puuttuu");

        Peruste peruste = repository.findOne(perusteKaikkiDto.getId());
        Assert.notNull(peruste, "Peruste puuttuu");

        // Haetaan tiedostosta perusteen sisältö
        PerusteKaikkiDto perusteTiedostosta = readPerusteFile();
        updatePeruste(peruste, perusteTiedostosta);

        Assert.notNull(peruste.getLops2019Sisalto().getLaajaAlainenOsaaminen(),
                "Perusteen sisällön laaja-alainen osaaminen puuttuu");
    }

    private Peruste updatePeruste(Peruste peruste, PerusteKaikkiDto uusi) {
        Lops2019Sisalto lops2019Sisalto = mapper.map(uusi.getLops2019Sisalto(), Lops2019Sisalto.class);
        peruste.getLops2019Sisalto().setSisalto(lops2019Sisalto.getSisalto());
        peruste.getLops2019Sisalto().setLaajaAlainenOsaaminen(lops2019Sisalto.getLaajaAlainenOsaaminen());
        peruste.getLops2019Sisalto().setOppiaineet(lops2019Sisalto.getOppiaineet());
        return repository.save(peruste);
    }

    private PerusteKaikkiDto readPerusteFile() throws IOException {
        Resource resource = new ClassPathResource("material/lops.json");
        return objectMapper.readValue(resource.getFile(), PerusteKaikkiDto.class);
    }

    @Test
    @Rollback
    public void testOppiaineet() {
        Lops2019OppiaineDto oa = new Lops2019OppiaineDto();
        { // Oppiaine
            oa.setNimi(LokalisoituTekstiDto.of("oppiaine"));
            oa.setKoodi(KoodiDto.of("oppiaine", "123"));
            oa.setArviointi(Lops2019ArviointiDto.builder()
                    .kuvaus(LokalisoituTekstiDto.of("arviointi"))
                    .build());
        }

        Lops2019OppiaineDto lisatty = lops2019Service.addOppiaine(projektiHelper.getPerusteId(), oa);

        { // Oppimaara
            Lops2019OppiaineDto om = new Lops2019OppiaineDto();
            om.setNimi(LokalisoituTekstiDto.of("oppimaara"));
            om.setKoodi(KoodiDto.of("oppiaine", "1234"));
            om.setArviointi(Lops2019ArviointiDto.builder()
                    .kuvaus(LokalisoituTekstiDto.of("arviointi"))
                    .build());
            lisatty.getOppimaarat().add(om);
            lisatty = lops2019Service.updateOppiaine(projektiHelper.getPerusteId(), lisatty);
        }

        Long moduuli1 = 0L;

        { // Moduuli 1
            Lops2019ModuuliBaseDto moduuli = new Lops2019ModuuliBaseDto();
            moduuli.setNimi(LokalisoituTekstiDto.of("moduuli 1"));
            moduuli.setPakollinen(false);
            Lops2019OppiaineDto om = lisatty.getOppimaarat().get(0);
            om.getModuulit().add(moduuli);
            om = lops2019Service.updateOppiaine(projektiHelper.getPerusteId(), om);

            Lops2019ModuuliBaseDto moduuliBaseDto = om.getModuulit().get(0);
            Lops2019ModuuliDto moduuliDto = lops2019Service.getModuuli(projektiHelper.getPerusteId(), om.getId(), moduuliBaseDto.getId());
            moduuli1 = moduuliDto.getId();
            moduuliDto.setKoodi(KoodiDto.of("moduulit", "123"));
            moduuliDto.setKuvaus(LokalisoituTekstiDto.of("kuvaus"));
            Lops2019ModuuliTavoiteDto tavoite = new Lops2019ModuuliTavoiteDto();
            tavoite.setKohde(LokalisoituTekstiDto.of("kohde"));
            moduuliDto.setTavoitteet(tavoite);
            lops2019Service.updateModuuli(projektiHelper.getPerusteId(), moduuliDto);
        }

        { // Moduuli 2
            Lops2019ModuuliBaseDto moduuli = new Lops2019ModuuliBaseDto();
            moduuli.setNimi(LokalisoituTekstiDto.of("moduuli 2"));
            moduuli.setPakollinen(false);
            Lops2019OppiaineDto om = lisatty.getOppimaarat().get(0);
            om.getModuulit().add(moduuli);
            lops2019Service.updateOppiaine(projektiHelper.getPerusteId(), om);
        }

        lisatty = lops2019Service.getOppiaine(projektiHelper.getPerusteId(), lisatty.getId());
        assertThat(lisatty.getOppimaarat()).hasSize(1);
        Lops2019ModuuliDto moduuliDto = lops2019Service.getModuuli(
                projektiHelper.getPerusteId(),
                lisatty.getOppimaarat().get(0).getId(),
                moduuli1);
        assertThat(moduuliDto.getTavoitteet().getKohde().get(Kieli.FI)).isEqualTo("kohde");
    }

    @Test
    public void testKielletytRakennemuutokset() {
        Long perusteId = createLops2019PerusteAndChangeTilaJulkaistu();

        // Yritetään lisätä laaja-alainen osaaminen
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> lops2019Service.addLaajaAlainenOsaaminen(perusteId));

        // Yritetään lisätä oppiaine
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> {
                    Lops2019OppiaineDto oppiaineDto = new Lops2019OppiaineDto();
                    oppiaineDto.setNimi(LokalisoituTekstiDto.of("oppiaine"));
                    lops2019Service.addOppiaine(perusteId, oppiaineDto);
                });

        // Yritetään lisätä moduuli
        List<Lops2019OppiaineDto> oppiaineet = lops2019Service.getOppiaineet(perusteId);
        assertThat(oppiaineet).isNotEmpty();

        Lops2019OppiaineDto oppiaineDto = oppiaineet.get(0);
        List<Lops2019ModuuliBaseDto> moduulit = oppiaineDto.getModuulit();
        Lops2019ModuuliBaseDto moduuliDto = new Lops2019ModuuliBaseDto();
        moduuliDto.setNimi(LokalisoituTekstiDto.of("moduuli"));
        moduuliDto.setPakollinen(true);

        KoodiDto koodiDto = new KoodiDto();
        koodiDto.setUri("moduulikoodistolops2021_moduuli");
        koodiDto.setKoodisto("moduulikoodistolops2021");
        koodiDto.setVersio(1L);
        moduuliDto.setKoodi(koodiDto);

        moduulit.add(moduuliDto);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> lops2019Service.updateOppiaine(perusteId, oppiaineDto));

    }

    private Long createLops2019PerusteAndChangeTilaJulkaistu() {
        PerusteprojektiDto pp = ppTestUtils.createPerusteprojekti(ppl -> {
            ppl.setKoulutustyyppi(KoulutusTyyppi.LUKIOKOULUTUS.toString());
            ppl.setToteutus(KoulutustyyppiToteutus.LOPS2019);
            ppl.setLaajuusYksikko(LaajuusYksikko.OPINTOPISTE);
        });

        Long perusteId = pp.getPeruste().getIdLong();

        PerusteDto perusteDto = ppTestUtils.initPeruste(perusteId);

        PerusteKaikkiDto perusteKaikkiDto = perusteService.getKokoSisalto(perusteDto.getId());
        Lops2019SisaltoDto sisalto = perusteKaikkiDto.getLops2019Sisalto();
        Assert.notNull(sisalto, "Perusteen sisältö puuttuu");

        Lops2019OppiaineDto oppiaineDto = new Lops2019OppiaineDto();
        oppiaineDto.setNimi(LokalisoituTekstiDto.of("oppiaine"));
        KoodiDto koodiDto = new KoodiDto();
        koodiDto.setUri("oppiaineetjaoppimaaratlops2021_oppiaine");
        koodiDto.setKoodisto("oppiaineetjaoppimaaratlops2021");
        koodiDto.setVersio(1L);
        oppiaineDto.setKoodi(koodiDto);

        lops2019Service.addOppiaine(perusteId, oppiaineDto);

        // Julkaistaan peruste
        ppTestUtils.julkaise(pp.getId());

        return perusteId;
    }
}
