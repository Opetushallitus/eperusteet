/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Osaamistaso;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.arviointi.ArviointiAsteikko;
import fi.vm.sade.eperusteet.dto.OsaamistasoDto;
import fi.vm.sade.eperusteet.dto.arviointi.ArviointiDto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.repository.ArviointiAsteikkoRepository;
import fi.vm.sade.eperusteet.repository.OsaamistasoRepository;
import fi.vm.sade.eperusteet.resource.config.MappingModule;
import fi.vm.sade.eperusteet.service.internal.ArviointiService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author teele1
 */
@Transactional
@DirtiesContext
public class ArviointiServiceIT extends AbstractIntegrationTest {

    @Autowired
    private ArviointiService arviointiService;

    @Autowired
    private ArviointiAsteikkoRepository arviointiAsteikkoRepository;

    @Autowired
    private OsaamistasoRepository osaamistasoRepository;

    @PersistenceContext
    private EntityManager em;

    private final ObjectMapper objectMapper;

    public ArviointiServiceIT() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setPrettyPrint(true);
        converter.getObjectMapper().setPropertyNamingStrategy(new PropertyNamingStrategy() {

            @Override
            public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method,
            String defaultName)
            {
                return tryToconvertFromMethodName(method, defaultName);
            }

            @Override
            public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method,
            String defaultName)
            {
                return tryToconvertFromMethodName(method, defaultName);
            }

            private String tryToconvertFromMethodName(AnnotatedMethod annotatedMethod, String defaultName) {
                if((annotatedMethod.getParameterCount() == 1 && Reference.class.isAssignableFrom(annotatedMethod.getParameter(0).getRawType()))
                        || Reference.class.isAssignableFrom(annotatedMethod.getRawReturnType())) {
                    defaultName = '_' + defaultName;
                }
                return defaultName;
            }
        });
        converter.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        converter.getObjectMapper().registerModule(new JodaModule());
        converter.getObjectMapper().registerModule(new MappingModule());
        converter.getObjectMapper().registerModule(new Hibernate4Module().enable(Hibernate4Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS));

        objectMapper = converter.getObjectMapper();
    }

    @Before
    public void setUp() {
        TekstiPalanen osaamistasoOtsikko = TekstiPalanen.of(Collections.singletonMap(Kieli.FI, "otsikko"));
        em.persist(osaamistasoOtsikko);

        Osaamistaso osaamistaso = new Osaamistaso();
        osaamistaso.setOtsikko(osaamistasoOtsikko);

        em.persist(osaamistaso);

        ArviointiAsteikko arviointiasteikko = new ArviointiAsteikko();
        arviointiasteikko.setOsaamistasot(Collections.singletonList(osaamistaso));

        em.persist(arviointiasteikko);

        TekstiPalanen osaamistasoOtsikko2 = TekstiPalanen.of(Collections.singletonMap(Kieli.FI, "otsikko 2"));
        em.persist(osaamistasoOtsikko2);
        TekstiPalanen osaamistasoOtsikko3 = TekstiPalanen.of(Collections.singletonMap(Kieli.FI, "otsikko 2"));
        em.persist(osaamistasoOtsikko3);

        osaamistaso = new Osaamistaso();
        osaamistaso.setOtsikko(osaamistasoOtsikko2);

        em.persist(osaamistaso);

        Osaamistaso osaamistaso2 = new Osaamistaso();
        osaamistaso2.setOtsikko(osaamistasoOtsikko3);

        em.persist(osaamistaso2);

        arviointiasteikko = new ArviointiAsteikko();
        arviointiasteikko.setOsaamistasot(Arrays.asList(osaamistaso, osaamistaso2));

        em.persist(arviointiasteikko);
        em.flush();
    }

    @Test
    @Rollback(true)
    public void testSaveArviointiFromJson() throws IOException {
        Resource resource = new ClassPathResource("material/valid_arviointi.json");
        ArviointiDto dto = objectMapper.readValue(resource.getFile(), ArviointiDto.class);
        List<Osaamistaso> osaamistasot = osaamistasoRepository.findAll();
        List<ArviointiAsteikko> arviointiasteikot = arviointiAsteikkoRepository.findAll();

        dto.getArvioinninKohdealueet().forEach(aka -> aka.getArvioinninKohteet().forEach(ak -> ak.setArviointiAsteikko(Reference.of(arviointiasteikot.get(0)))));
        dto.getArvioinninKohdealueet().forEach(aka -> aka.getArvioinninKohteet().forEach(ak -> ak.getOsaamistasonKriteerit().forEach(ok -> ok.setOsaamistaso(Reference.of(osaamistasot.get(0).getId())))));
        arviointiService.add(dto);

        em.flush();

        List<ArviointiDto> dtos = arviointiService.findAll();

        Assert.assertNotNull(dtos);
        Assert.assertEquals(1, dtos.size());
        Assert.assertNotNull(dtos.get(0).getArvioinninKohdealueet().get(0).getArvioinninKohteet().get(0).getArviointiAsteikkoDto());
        Assert.assertNotNull(dtos.get(0).getArvioinninKohdealueet().get(0).getArvioinninKohteet().get(0).getOsaamistasonKriteerit().stream().findAny().get().getOsaamistasoDto());
    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    @Rollback(true)
    public void testSaveInvalidArviointiFromJson() throws IOException {
        Resource resource = new ClassPathResource("material/invalid_arviointi.json");
        ArviointiDto dto = objectMapper.readValue(resource.getFile(), ArviointiDto.class);

        List<Osaamistaso> osaamistasot = osaamistasoRepository.findAll();
        List<ArviointiAsteikko> arviointiasteikot = arviointiAsteikkoRepository.findAll();

        dto.getArvioinninKohdealueet().forEach(aka -> aka.getArvioinninKohteet().forEach(ak -> ak.setArviointiAsteikko(Reference.of(arviointiasteikot.get(0)))));
        dto.getArvioinninKohdealueet().forEach(aka -> aka.getArvioinninKohteet().forEach(ak -> ak.getOsaamistasonKriteerit().forEach(ok -> ok.setOsaamistaso(Reference.of(osaamistasot.get(0).getId())))));
        arviointiService.add(dto);
        em.flush();
    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    @Rollback(true)
    public void testSaveInvalidArviointi2FromJson() throws IOException {
        Resource resource = new ClassPathResource("material/invalid_arviointi2.json");
        ArviointiDto dto = objectMapper.readValue(resource.getFile(), ArviointiDto.class);

        List<Osaamistaso> osaamistasot = osaamistasoRepository.findAll();
        List<ArviointiAsteikko> arviointiasteikot = arviointiAsteikkoRepository.findAll();

        dto.getArvioinninKohdealueet().forEach(aka -> aka.getArvioinninKohteet().forEach(ak -> ak.setArviointiAsteikko(Reference.of(arviointiasteikot.get(0)))));
        dto.getArvioinninKohdealueet().forEach(aka -> aka.getArvioinninKohteet().forEach(ak -> ak.getOsaamistasonKriteerit().forEach(ok -> ok.setOsaamistaso(Reference.of(osaamistasot.get(0).getId())))));
        arviointiService.add(dto);
        em.flush();
    }
}
