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

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.domain.yl.KeskeinenSisaltoalue;
import fi.vm.sade.eperusteet.domain.yl.LaajaalainenOsaaminen;
import fi.vm.sade.eperusteet.domain.yl.OpetuksenTavoite;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine_;
import fi.vm.sade.eperusteet.domain.yl.OppiaineenVuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.domain.yl.TekstiOsa;
import fi.vm.sade.eperusteet.domain.yl.VuosiluokkaKokonaisuudenLaajaalainenOsaaminen;
import fi.vm.sade.eperusteet.domain.yl.VuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.SuoritustapaDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiInfoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonOsa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.AbstractRakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.yl.KeskeinenSisaltoalueDto;
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.yl.OpetuksenTavoiteDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineenVuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.dto.yl.TekstiOsaDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuudenLaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuusDto;
import java.util.Collection;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.builtin.PassThroughConverter;
import ma.glasnost.orika.impl.DefaultMapperFactory;
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
        DefaultMapperFactory factory = new DefaultMapperFactory.Builder()
            .build();
        factory.getConverterFactory().registerConverter(tekstiPalanenConverter);
        factory.getConverterFactory().registerConverter(cachedEntityConverter);
        factory.getConverterFactory().registerConverter("koodistokoodiConverter", koodistokoodiConverter);
        factory.getConverterFactory().registerConverter(new PassThroughConverter(TekstiPalanen.class));
        factory.getConverterFactory().registerConverter(new TypeNameConverter());
        factory.getConverterFactory().registerConverter(new OptionalConverter());
        factory.getConverterFactory().registerConverter(new ToOptionalConverter());

        //erikoiskäsittely määritellyille säiliöille koska halutaan säilyttää "PATCH" -ominaisuus
        //TODO: geneerinen ratkaisu kaikille mapattaville säiliöille (mahdollista, mutta pitää miettiä onko liian "maagista").
        factory.registerMapper(new OpetuksenTavoiteCollectionMapper());
        factory.registerMapper(new KeskeinenSisaltoAlueCollectionMapper());
        factory.registerMapper(new VuosiluokkaKokonaisuudenLaajaalainenOsaaminenCollectionMapper());

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

        //YL
        factory.classMap(OppiaineDto.class, Oppiaine.class)
            .mapNulls(false)
            .fieldBToA(Oppiaine_.vuosiluokkakokonaisuudet.getName(), Oppiaine_.vuosiluokkakokonaisuudet.getName())
            .byDefault()
            .register();
        factory.classMap(OppiaineenVuosiluokkaKokonaisuusDto.class, OppiaineenVuosiluokkaKokonaisuus.class)
            .mapNulls(false)
            .byDefault()
            .register();
        factory.classMap(KeskeinenSisaltoalueDto.class, KeskeinenSisaltoalue.class)
            .mapNulls(false)
            .byDefault()
            .register();
        factory.classMap(OpetuksenTavoiteDto.class, OpetuksenTavoite.class)
            .mapNulls(false)
            .byDefault()
            .register();
        factory.classMap(TekstiOsaDto.class, TekstiOsa.class)
            .mapNulls(false)
            .byDefault()
            .register();
        factory.classMap(VuosiluokkaKokonaisuudenLaajaalainenOsaaminenDto.class, VuosiluokkaKokonaisuudenLaajaalainenOsaaminen.class)
            .mapNulls(false)
            .byDefault()
            .register();
        factory.classMap(VuosiluokkaKokonaisuusDto.class, VuosiluokkaKokonaisuus.class)
            .mapNulls(false)
            .byDefault()
            .register();
        factory.classMap(LaajaalainenOsaaminenDto.class, LaajaalainenOsaaminen.class)
            .mapNulls(false)
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

    public static class KeskeinenSisaltoAlueCollectionMapper extends CustomMapper<Collection<KeskeinenSisaltoalueDto>, Collection<KeskeinenSisaltoalue>> {

        private final CollectionMergeMapper<KeskeinenSisaltoalueDto, KeskeinenSisaltoalue> delegate = new CollectionMergeMapper<>(KeskeinenSisaltoalueDto.class, KeskeinenSisaltoalue.class);

        @Override
        public void setMapperFacade(MapperFacade mapperFacade) {
            delegate.setMapperFacade(mapperFacade);
        }

        @Override
        public void mapBtoA(Collection<KeskeinenSisaltoalue> b, Collection<KeskeinenSisaltoalueDto> a, MappingContext context) {
            delegate.mapBtoA(b, a, context);
        }

        @Override
        public void mapAtoB(Collection<KeskeinenSisaltoalueDto> a, Collection<KeskeinenSisaltoalue> b, MappingContext context) {
            delegate.mapAtoB(a, b, context);
        }
    };

    public static class OpetuksenTavoiteCollectionMapper extends CustomMapper<Collection<OpetuksenTavoiteDto>, Collection<OpetuksenTavoite>> {

        private final CollectionMergeMapper<OpetuksenTavoiteDto, OpetuksenTavoite> delegate = new CollectionMergeMapper<>(OpetuksenTavoiteDto.class, OpetuksenTavoite.class);

        @Override
        public void setMapperFacade(MapperFacade mapperFacade) {
            delegate.setMapperFacade(mapperFacade);
        }

        @Override
        public void mapBtoA(Collection<OpetuksenTavoite> b, Collection<OpetuksenTavoiteDto> a, MappingContext context) {
            delegate.mapBtoA(b, a, context);
        }

        @Override
        public void mapAtoB(Collection<OpetuksenTavoiteDto> a, Collection<OpetuksenTavoite> b, MappingContext context) {
            delegate.mapAtoB(a, b, context);
        }
    };

    public static class VuosiluokkaKokonaisuudenLaajaalainenOsaaminenCollectionMapper
        extends CustomMapper<Collection<VuosiluokkaKokonaisuudenLaajaalainenOsaaminenDto>, Collection<VuosiluokkaKokonaisuudenLaajaalainenOsaaminen>> {

        private final CollectionMergeMapper<VuosiluokkaKokonaisuudenLaajaalainenOsaaminenDto, VuosiluokkaKokonaisuudenLaajaalainenOsaaminen> delegate
            = new CollectionMergeMapper<>(VuosiluokkaKokonaisuudenLaajaalainenOsaaminenDto.class, VuosiluokkaKokonaisuudenLaajaalainenOsaaminen.class);

        @Override
        public void setMapperFacade(MapperFacade mapperFacade) {
            delegate.setMapperFacade(mapperFacade);
        }

        @Override
        public void mapAtoB(Collection<VuosiluokkaKokonaisuudenLaajaalainenOsaaminenDto> a, Collection<VuosiluokkaKokonaisuudenLaajaalainenOsaaminen> b, MappingContext context) {
            delegate.mapAtoB(a, b, context);
        }

        @Override
        public void mapBtoA(Collection<VuosiluokkaKokonaisuudenLaajaalainenOsaaminen> b, Collection<VuosiluokkaKokonaisuudenLaajaalainenOsaaminenDto> a, MappingContext context) {
            delegate.mapBtoA(b, a, context);
        }

    }

}
