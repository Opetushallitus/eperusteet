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
import fi.vm.sade.eperusteet.domain.ArviointiAsteikko;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.LokalisoituTeksti;
import fi.vm.sade.eperusteet.domain.Osaamistaso;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.dto.ArviointiDto;
import fi.vm.sade.eperusteet.dto.EntityReference;
import fi.vm.sade.eperusteet.resource.config.EPerusteetMappingModule;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author teele1
 */
@Transactional
public class ArviointiServiceIT extends AbstractIntegrationTest {
    
    @Autowired
    private ArviointiService arviointiService;
    
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
                if((annotatedMethod.getParameterCount() == 1 && EntityReference.class.isAssignableFrom(annotatedMethod.getParameter(0).getRawType()))
                        || EntityReference.class.isAssignableFrom(annotatedMethod.getRawReturnType())) {
                    defaultName = '_' + defaultName;
                }
                return defaultName;
            }
        });
        converter.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        converter.getObjectMapper().registerModule(new JodaModule());
        converter.getObjectMapper().registerModule(new EPerusteetMappingModule());
        converter.getObjectMapper().registerModule(new Hibernate4Module().enable(Hibernate4Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS));
        
        objectMapper = converter.getObjectMapper();
    }
    
    @Before
    public void setUp() {
        TekstiPalanen osaamistasoOtsikko = new TekstiPalanen(Collections.singletonMap(Kieli.FI, new LokalisoituTeksti(Kieli.FI, "otsikko")));
        em.persist(osaamistasoOtsikko);

        Osaamistaso osaamistaso = new Osaamistaso();
        osaamistaso.setId(1L);
        osaamistaso.setOtsikko(osaamistasoOtsikko);

        em.persist(osaamistaso);

        ArviointiAsteikko arviointiasteikko = new ArviointiAsteikko();
        arviointiasteikko.setId(1L);
        arviointiasteikko.setOsaamistasot(Collections.singletonList(osaamistaso));

        em.persist(arviointiasteikko);
        
        TekstiPalanen osaamistasoOtsikko2 = new TekstiPalanen(Collections.singletonMap(Kieli.FI, new LokalisoituTeksti(Kieli.FI, "otsikko 2")));
        em.persist(osaamistasoOtsikko2);
        TekstiPalanen osaamistasoOtsikko3 = new TekstiPalanen(Collections.singletonMap(Kieli.FI, new LokalisoituTeksti(Kieli.FI, "otsikko 2")));
        em.persist(osaamistasoOtsikko3);

        osaamistaso = new Osaamistaso();
        osaamistaso.setId(2L);
        osaamistaso.setOtsikko(osaamistasoOtsikko2);
        
        em.persist(osaamistaso);
        
        Osaamistaso osaamistaso2 = new Osaamistaso();
        osaamistaso2.setId(3L);
        osaamistaso2.setOtsikko(osaamistasoOtsikko3);

        em.persist(osaamistaso2);

        arviointiasteikko = new ArviointiAsteikko();
        arviointiasteikko.setId(2L);
        arviointiasteikko.setOsaamistasot(Arrays.asList(osaamistaso, osaamistaso2));

        em.persist(arviointiasteikko);
        em.flush();
    }
    
    @Test
    @Rollback(true)
    public void testSaveArviointiFromJson() throws IOException {
        Resource resource = new ClassPathResource("material/arviointi.json");
        ArviointiDto dto = objectMapper.readValue(resource.getFile(), ArviointiDto.class);
        
        arviointiService.add(dto);
        
        em.flush();
        
        List<ArviointiDto> dtos = arviointiService.findAll();
        
        Assert.assertNotNull(dtos);
        Assert.assertEquals(1, dtos.size());
    }
    
    @Test(expected = ConstraintViolationException.class)
    @Rollback(true)
    public void testSaveInvalidArviointiFromJson() throws IOException {
        Resource resource = new ClassPathResource("material/arviointi2.json");
        ArviointiDto dto = objectMapper.readValue(resource.getFile(), ArviointiDto.class);
        arviointiService.add(dto);
    }
}
