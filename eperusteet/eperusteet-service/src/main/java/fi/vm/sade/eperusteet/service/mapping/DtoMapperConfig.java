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
import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.Taiteenala;
import fi.vm.sade.eperusteet.domain.yl.lukio.Aihekokonaisuudet;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukioOpetussuunnitelmaRakenne;
import fi.vm.sade.eperusteet.domain.yl.lukio.Lukiokurssi;
import fi.vm.sade.eperusteet.domain.yl.lukio.OpetuksenYleisetTavoitteet;
import fi.vm.sade.eperusteet.dto.KoulutusDto;
import fi.vm.sade.eperusteet.dto.TiedoteDto;
import fi.vm.sade.eperusteet.dto.peruste.*;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiInfoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.AbstractRakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.OsaamisalaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.*;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukioKurssiLuontiDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukiokurssiMuokkausDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.osaviitteet.*;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.builtin.PassThroughConverter;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory.Builder;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.unenhance.HibernateUnenhanceStrategy;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.client.RestClientException;

/**
 * @author jhyoty
 */
@Configuration
public class DtoMapperConfig {
    private static final Logger logger = LoggerFactory.getLogger(DtoMapperConfig.class);

    @Autowired
    private KoodistoClient koodistoClient;

    private String rakennaKoodiVirhe(Koodi koodi, String message) {
        StringBuilder builder = new StringBuilder();
        builder.append("(koodisto:");
        builder.append(koodi.getKoodisto());
        builder.append(", uri:");
        builder.append(koodi.getUri());
        builder.append(", versio:");
        builder.append(koodi.getVersio());
        builder.append(") koodia ei voitu ladata:");
        builder.append(message);
        return builder.toString();
    }

    @Bean
    @Dto
    public DtoMapper dtoMapper(
            TekstiPalanenConverter tekstiPalanenConverter,
            ReferenceableEntityConverter cachedEntityConverter,
            KoodistokoodiConverter koodistokoodiConverter) {
        DefaultMapperFactory factory = new Builder() {
            @Override
            public DefaultMapperFactory build() {
                return new DefaultMapperFactory(this) {
                    @Override
                    public <A, B> Mapper<A, B> lookupMapper(MapperKey mapperKey) {
                        return super.lookupMapper(fixKey(mapperKey));
                    }

                    @Override
                    public Mapper<Object, Object> lookupMapper(MapperKey mapperKey, MappingContext context) {
                        return super.lookupMapper(fixKey(mapperKey), context);
                    }

                    private MapperKey fixKey(MapperKey mapperKey) {
                        if (mapperKey.getAType().getSimpleName().contains("$$")) {
                            return fixKey(new MapperKey(
                                    mapperKey.getAType().getSuperType(),
                                    mapperKey.getBType()
                            ));
                        }
                        if (mapperKey.getBType().getSimpleName().contains("$$")) {
                            return fixKey(new MapperKey(
                                    mapperKey.getAType(),
                                    mapperKey.getBType().getSuperType())
                            );
                        }
                        return mapperKey;
                    }
                };
            }
        }.unenhanceStrategy(new HibernateUnenhanceStrategy() {
            @Override
            public <T> Type<T> unenhanceType(T object, Type<T> type) {
                if (object instanceof HibernateProxy) {
                    //noinspection unchecked
                    return TypeFactory.resolveValueOf((Class<T>)
                                    ((HibernateProxy) object).getHibernateLazyInitializer().getPersistentClass(),
                            type);
                }
                return super.unenhanceType(object, type);
            }
        })
                .build();

        factory.getConverterFactory().registerConverter(tekstiPalanenConverter);
        factory.getConverterFactory().registerConverter(cachedEntityConverter);
        // Mikä järki konvertoida dto samaksi? testLukiokurssi vaatii, mutta miksi ei ole mockattu?
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
        factory.classMap(PerusopetuksenPerusteenSisalto.class, PerusopetuksenPerusteenSisaltoDto.class)
                .fieldAToB("oppiaineetCopy", "oppiaineet")
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
        factory.classMap(TaiteenalaDto.class, Taiteenala.class)
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
//                .customize(new CustomMapper<RakenneModuuliDto, RakenneModuuli>() {
//                    @Override
//                    public void mapAtoB(RakenneModuuliDto source, RakenneModuuli target, MappingContext context) {
//                        super.mapAtoB(source, target, context);
//                        target.asetaTunniste(source.getTunniste());
//                    }
//                })
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
        factory.classMap(Tiedote.class, TiedoteDto.class)
                .fieldAToB("perusteprojekti.peruste", "peruste")
                .byDefault()
                .register();
        factory.classMap(Peruste.class, PerusteHakuInternalDto.class)
                .byDefault()
                .favorExtension(true)
                .register();
        factory.classMap(Peruste.class, PerusteBaseDto.class)
                .byDefault()
                .favorExtension(true)
                .customize(new CustomMapper<Peruste, PerusteBaseDto>() {
                    @Override
                    public void mapAtoB(Peruste source, PerusteBaseDto target, MappingContext context) {
                        super.mapAtoB(source, target, context);
                        try {
                            KVLiite kvliite = source.getKvliite();
                            if (kvliite != null) {
                                KVLiite pohja = kvliite.getPohja();
                                TekstiPalanen osaaminen = kvliite.getSuorittaneenOsaaminen();
                                TekstiPalanen tyotehtavat = kvliite.getTyotehtavatJoissaVoiToimia();
                                if (pohja != null) {
                                    osaaminen = osaaminen != null ? osaaminen : pohja.getSuorittaneenOsaaminen();
                                    tyotehtavat = tyotehtavat != null ? tyotehtavat : pohja.getTyotehtavatJoissaVoiToimia();
                                }
                                if (osaaminen != null) {
                                    target.setSuorittaneenOsaaminen(new LokalisoituTekstiDto(osaaminen.getId(), osaaminen.getTeksti()));
                                }
                                if (tyotehtavat != null) {
                                    target.setTyotehtavatJoissaVoiToimia(new LokalisoituTekstiDto(tyotehtavat.getId(), tyotehtavat.getTeksti()));
                                }
                            }
                        } catch (RestClientException | AccessDeniedException ex) {
                        }
                    }
                })
                .register();

        factory.classMap(Koulutus.class, KoulutusDto.class)
                .byDefault()
                .customize(new CustomMapper<Koulutus, KoulutusDto>() {
                    @Override
                    public void mapAtoB(Koulutus source, KoulutusDto target, MappingContext context) {
                        try {
                            KoodiDto koodiDto = new KoodiDto();
                            koodiDto.setUri(target.getKoulutuskoodiUri());
                            koodiDto.setKoodisto("koulutus");
                            koodistoClient.addNimiAndUri(koodiDto);
                            target.setNimi(new LokalisoituTekstiDto(koodiDto.getNimi()));
                        } catch (RestClientException | AccessDeniedException ex) {
                            logger.error(ex.getLocalizedMessage());
                        }
                    }
                })
                .register();

        factory.classMap(TutkintonimikeKoodi.class, TutkintonimikeKoodiDto.class)
                .byDefault()
                .customize(new CustomMapper<TutkintonimikeKoodi, TutkintonimikeKoodiDto>() {
                    @Override
                    public void mapAtoB(TutkintonimikeKoodi source, TutkintonimikeKoodiDto target, MappingContext context) {
                        try {
                            KoodiDto koodiDto = new KoodiDto();
                            koodiDto.setUri(target.getTutkintonimikeUri());
                            koodiDto.setKoodisto("tutkintonimikeKoodit");
                            koodistoClient.addNimiAndUri(koodiDto);
                            target.setNimi(koodiDto.getNimi());
                        } catch (RestClientException | AccessDeniedException ex) {
                            logger.error(ex.getLocalizedMessage());
                        }
                    }
                })
                .register();

        factory.classMap(Koodi.class, KoodiDto.class)
                .byDefault()
                .customize(new CustomMapper<Koodi, KoodiDto>() {
                    @Override
                    public void mapAtoB(Koodi a, KoodiDto b, MappingContext context) {
                        try {
                            koodistoClient.addNimiAndUri(b);
                        } catch (RestClientException | AccessDeniedException ex) {
                            logger.error(rakennaKoodiVirhe(a, ex.getLocalizedMessage()));
                        }
                    }
                })
                .register();

        factory.classMap(Koodi.class, OsaamisalaDto.class)
                .field("uri", "osaamisalakoodiUri")
                .byDefault()
                .customize(new CustomMapper<Koodi, OsaamisalaDto>() {
                    @Override
                    public void mapBtoA(OsaamisalaDto osaamisalaDto, Koodi koodi, MappingContext context) {
                        super.mapBtoA(osaamisalaDto, koodi, context);
                        koodi.setKoodisto("osaamisala");
                        koodi.setUri(osaamisalaDto.getOsaamisalakoodiUri());
                    }

                    @Override
                    public void mapAtoB(Koodi a, OsaamisalaDto b, MappingContext context) {
                        try {
                            super.mapAtoB(a, b, context);
                            KoodiDto koodi = koodistoClient.getKoodi(a.getKoodisto(), a.getUri(), a.getVersio());
                            if (koodi != null) {
                                b.setNimi(koodi.getNimi());
                                b.setOsaamisalakoodiArvo(koodi.getArvo());
                            }
                        } catch (RestClientException | AccessDeniedException ex) {
                            logger.error(rakennaKoodiVirhe(a, ex.getLocalizedMessage()));
                        }
                    }
                })
                .register();

        //YL
        factory.classMap(OppiaineDto.class, Oppiaine.class)
                .mapNulls(false)
                .fieldBToA(Oppiaine_.vuosiluokkakokonaisuudet.getName(), Oppiaine_.vuosiluokkakokonaisuudet.getName())
                .byDefault()
                .register();
        factory.classMap(LukioOppiaineUpdateDto.class, Oppiaine.class)
                .mapNulls(true)
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
