package fi.vm.sade.eperusteet.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.lops2019.Lops2019Sisalto;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.dto.lops2019.Lops2019SisaltoDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.Lops2019OppiaineDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.resource.config.InitJacksonConverter;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.List;

@Slf4j
@Transactional
@DirtiesContext
public class Lops2019IT extends AbstractIntegrationTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteRepository repository;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    public PerusteprojektiTestUtils ppTestUtils;

    @Before
    public void setup() {
        InitJacksonConverter.configureObjectMapper(objectMapper);
        objectMapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void readJsonToDto() throws IOException {
        PerusteKaikkiDto perusteDto = readPerusteFile();
        Assert.notNull(perusteDto, "Perusteen lukeminen epäonnistui");
    }

    @Test
    public void convertDtoToEntity() throws IOException {
        PerusteKaikkiDto perusteDto = readPerusteFile();
        Lops2019SisaltoDto lops2019SisaltoDto = perusteDto.getLops2019Sisalto();
        Assert.notNull(lops2019SisaltoDto, "Perusteen sisältö puuttuu");
        List<Lops2019OppiaineDto> oppiaineetDto = lops2019SisaltoDto.getOppiaineet();
        Assert.notNull(oppiaineetDto, "Perusteen oppiaineet puuttuvat");
        Peruste peruste = mapper.map(perusteDto, Peruste.class);
        Lops2019Sisalto lops2019Sisalto = peruste.getLops2019Sisalto();
        Assert.notNull(lops2019Sisalto, "Perusteen sisältö puuttuu");
        List<Lops2019Oppiaine> oppiaineet = lops2019Sisalto.getOppiaineet();
        Assert.notNull(oppiaineet, "Perusteen oppiaineet puuttuvat");

    }

    @Test
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
}
