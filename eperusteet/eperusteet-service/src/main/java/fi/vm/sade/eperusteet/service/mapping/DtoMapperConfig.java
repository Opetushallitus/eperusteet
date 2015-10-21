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
package fi.vm.sade.eperusteet.service.mapping;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine_;
import fi.vm.sade.eperusteet.domain.yl.lukio.Aihekokonaisuudet;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukioOpetussuunnitelmaRakenne;
import fi.vm.sade.eperusteet.domain.yl.lukio.Lukiokurssi;
import fi.vm.sade.eperusteet.domain.yl.lukio.OpetuksenYleisetTavoitteet;
import fi.vm.sade.eperusteet.dto.peruste.*;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiInfoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.AbstractRakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukioKurssiLuontiDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukiokurssiMuokkausDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.osaviitteet.*;
import ma.glasnost.orika.converter.builtin.PassThroughConverter;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.unenhance.HibernateUnenhanceStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author jhyoty
 */
@Configuration
public class DtoMapperConfig {

    @Bean
    @Dto
    public DtoMapper dtoMapper(
        TekstiPalanenConverter tekstiPalanenConverter,
        ReferenceableEntityConverter cachedEntityConverter,
        KoodistokoodiConverter koodistokoodiConverter) {
        DefaultMapperFactory factory
            = new DefaultMapperFactory.Builder()
                .unenhanceStrategy(new HibernateUnenhanceStrategy())
            .build();

        factory.getConverterFactory().registerConverter(tekstiPalanenConverter);
        factory.getConverterFactory().registerConverter(cachedEntityConverter);
        factory.getConverterFactory().registerConverter(new LokalisoituTekstiDtoCopyConverter());
        factory.getConverterFactory().registerConverter("koodistokoodiConverter", koodistokoodiConverter);
        factory.getConverterFactory().registerConverter(new PassThroughConverter(TekstiPalanen.class));
        factory.getConverterFactory().registerConverter(new TypeNameConverter());

        OptionalSupport.register(factory);
        //erikoiskäsittely säiliöille koska halutaan säilyttää "PATCH" -ominaisuus
        factory.registerMapper(new ReferenceableCollectionMergeMapper());

        factory.classMap(PerusteenOsaDto.Suppea.class, PerusteenOsa.class)
            .fieldBToA("class", "osanTyyppi")
            .byDefault()
            .register();
        factory.classMap(PerusteenOsaDto.Laaja.class, PerusteenOsa.class)
            .byDefault()
            .register();
        factory.classMap(TutkinnonOsaDto.class, TutkinnonOsa.class)
            .use(PerusteenOsaDto.Laaja.class, PerusteenOsa.class)
            .byDefault()
            .register();
        factory.classMap(AihekokonaisuudetLaajaDto.class, Aihekokonaisuudet.class)
            .use(PerusteenOsaDto.Laaja.class, PerusteenOsa.class)
            .byDefault()
            .register();
        factory.classMap(AihekokonaisuudetSuppeaDto.class, Aihekokonaisuudet.class)
            .use(PerusteenOsaDto.Suppea.class, PerusteenOsa.class)
            .byDefault()
            .register();
        factory.classMap(OpetuksenYleisetTavoitteetSuppeaDto.class, OpetuksenYleisetTavoitteet.class)
            .use(PerusteenOsaDto.Suppea.class, PerusteenOsa.class)
            .byDefault()
            .register();
        factory.classMap(OpetuksenYleisetTavoitteetLaajaDto.class, OpetuksenYleisetTavoitteet.class)
            .use(PerusteenOsaDto.Laaja.class, PerusteenOsa.class)
            .byDefault()
            .register();
        factory.classMap(LukioOpetussuunnitelmaRakenneLaajaDto.class, LukioOpetussuunnitelmaRakenne.class)
            .use(PerusteenOsaDto.Laaja.class, PerusteenOsa.class)
            .byDefault()
            .register();
        factory.classMap(LukioOpetussuunnitelmaRakenneSuppeaDto.class, LukioOpetussuunnitelmaRakenne.class)
            .use(PerusteenOsaDto.Suppea.class, PerusteenOsa.class)
            .byDefault()
            .register();
        factory.classMap(TekstiKappaleDto.class, TekstiKappale.class)
            .use(PerusteenOsaDto.Laaja.class, PerusteenOsa.class)
            .byDefault()
            .register();
        factory.classMap(PerusteDto.class, Peruste.class)
            .byDefault()
            .register();
        factory.classMap(PerusteprojektiDto.class, Perusteprojekti.class)
            .byDefault()
            .register();
        factory.classMap(PerusteprojektiInfoDto.class, Perusteprojekti.class)
            .fieldBToA("peruste.koulutustyyppi", "koulutustyyppi")
            .byDefault()
            .register();
        factory.classMap(AbstractRakenneOsaDto.class, AbstractRakenneOsa.class)
            .byDefault()
            .register();
        factory.classMap(RakenneModuuliDto.class, RakenneModuuli.class)
            .use(AbstractRakenneOsaDto.class, AbstractRakenneOsa.class)
            .byDefault()
            .register();
        factory.classMap(RakenneOsaDto.class, RakenneOsa.class)
            .use(AbstractRakenneOsaDto.class, AbstractRakenneOsa.class)
            .byDefault()
            .register();
        factory.classMap(TutkinnonOsaViiteDto.class, TutkinnonOsaViite.class)
            .fieldBToA("tutkinnonOsa.nimi", "nimi")
            .fieldBToA("tutkinnonOsa.tyyppi", "tyyppi")
            .byDefault()
            .register();
        factory.classMap(SuoritustapaDto.class, Suoritustapa.class)
            .byDefault()
            .register();
        factory.classMap(LukioKurssiLuontiDto.class, Lukiokurssi.class)
                .exclude("oppiaineet")
                .byDefault()
                .register();
        factory.classMap(LukiokurssiMuokkausDto.class, Lukiokurssi.class)
                .exclude("oppiaineet")
                .byDefault()
                .register();

        //YL
        factory.classMap(OppiaineDto.class, Oppiaine.class)
            .mapNulls(false)
                .fieldBToA(Oppiaine_.vuosiluokkakokonaisuudet.getName(), Oppiaine_.vuosiluokkakokonaisuudet.getName())
            .byDefault()
            .register();
        factory.classMap(OppiaineSuppeaDto.class, Oppiaine.class)
                .fieldBToA(Oppiaine_.muokattu.getName(), Oppiaine_.muokattu.getName())
            .byDefault()
            .register();

        perusteenOsaViiteMapping(factory, PerusteenOsaViiteDto.Matala.class);
        perusteenOsaViiteMapping(factory, PerusteenOsaViiteDto.Suppea.class);
        perusteenOsaViiteMapping(factory, PerusteenOsaViiteDto.Laaja.class);

        return new DtoMapperImpl(factory.getMapperFacade());
    }

    private static void perusteenOsaViiteMapping(DefaultMapperFactory factory, Class<? extends PerusteenOsaViiteDto<?>> dtoClass) {
        //pelkästään yliluokan mappauksen konffaus ei toiminut
        factory.classMap(dtoClass, PerusteenOsaViite.class)
            .mapNulls(false)
            .field("perusteenOsaRef", "perusteenOsa")
            .field("perusteenOsa", "perusteenOsa")
            .mapNulls(true)
            .byDefault()
            .register();
    }

}
