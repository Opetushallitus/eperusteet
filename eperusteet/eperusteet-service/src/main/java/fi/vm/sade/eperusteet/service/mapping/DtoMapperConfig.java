package fi.vm.sade.eperusteet.service.mapping;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fi.vm.sade.eperusteet.domain.KVLiite;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.Koulutus;
import fi.vm.sade.eperusteet.domain.OsaamistasonKriteeri;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.Tiedote;
import fi.vm.sade.eperusteet.domain.TutkintonimikeKoodi;
import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohde;
import fi.vm.sade.eperusteet.domain.digi.Osaamiskokonaisuus;
import fi.vm.sade.eperusteet.domain.digi.OsaamiskokonaisuusPaaAlue;
import fi.vm.sade.eperusteet.domain.liite.Liite;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.moduuli.Lops2019Moduuli;
import fi.vm.sade.eperusteet.domain.osaamismerkki.OsaamismerkkiKategoria;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimus2019;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaAlue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Osaamistavoite;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.domain.tuva.KoulutuksenOsa;
import fi.vm.sade.eperusteet.domain.tuva.TuvaLaajaAlainenOsaaminen;
import fi.vm.sade.eperusteet.domain.vst.KotoKielitaitotaso;
import fi.vm.sade.eperusteet.domain.vst.KotoLaajaAlainenOsaaminen;
import fi.vm.sade.eperusteet.domain.vst.KotoOpinto;
import fi.vm.sade.eperusteet.domain.vst.Opintokokonaisuus;
import fi.vm.sade.eperusteet.domain.vst.Tavoitesisaltoalue;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.Taiteenala;
import fi.vm.sade.eperusteet.domain.yl.lukio.Aihekokonaisuudet;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukioOpetussuunnitelmaRakenne;
import fi.vm.sade.eperusteet.domain.yl.lukio.Lukiokurssi;
import fi.vm.sade.eperusteet.domain.yl.lukio.OpetuksenYleisetTavoitteet;
import fi.vm.sade.eperusteet.dto.KoulutusDto;
import fi.vm.sade.eperusteet.dto.OsaamistasoDto;
import fi.vm.sade.eperusteet.dto.OsaamistasonKriteeriDto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.TiedoteDto;
import fi.vm.sade.eperusteet.dto.arviointi.ArvioinninKohdeDto;
import fi.vm.sade.eperusteet.dto.arviointi.ArviointiAsteikkoDto;
import fi.vm.sade.eperusteet.dto.digi.OsaamiskokonaisuusDto;
import fi.vm.sade.eperusteet.dto.digi.OsaamiskokonaisuusPaaAlueDto;
import fi.vm.sade.eperusteet.dto.fakes.Referer;
import fi.vm.sade.eperusteet.dto.fakes.RefererDto;
import fi.vm.sade.eperusteet.dto.lops2019.Lops2019OppiaineKaikkiDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.Lops2019OppiaineBaseDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.Lops2019OppiaineDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.moduuli.Lops2019ModuuliBaseDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.moduuli.Lops2019ModuuliDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiKategoriaDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteHakuInternalDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.SuoritustapaDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiInfoDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.Ammattitaitovaatimus2019Dto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueKaikkiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueKokonaanDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueLaajaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.Osaamistavoite2020Dto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.AbstractRakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.OsaamisalaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.tuva.KoulutuksenOsaDto;
import fi.vm.sade.eperusteet.dto.tuva.TuvaLaajaAlainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.vst.KotoKielitaitotasoDto;
import fi.vm.sade.eperusteet.dto.vst.KotoLaajaAlainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.vst.KotoOpintoDto;
import fi.vm.sade.eperusteet.dto.vst.OpintokokonaisuusDto;
import fi.vm.sade.eperusteet.dto.vst.TavoitesisaltoalueDto;
import fi.vm.sade.eperusteet.dto.yl.LukioOppiaineUpdateDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.PerusopetuksenPerusteenSisaltoDto;
import fi.vm.sade.eperusteet.dto.yl.TaiteenalaDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukioKurssiLuontiDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukiokurssiMuokkausDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.osaviitteet.AihekokonaisuudetLaajaDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.osaviitteet.AihekokonaisuudetSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.osaviitteet.LukioOpetussuunnitelmaRakenneLaajaDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.osaviitteet.LukioOpetussuunnitelmaRakenneSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.osaviitteet.OpetuksenYleisetTavoitteetLaajaDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.osaviitteet.OpetuksenYleisetTavoitteetSuppeaDto;
import fi.vm.sade.eperusteet.repository.liite.LiiteRepository;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.util.TemporaryKoodiGenerator;

import java.sql.Blob;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Base64;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.converter.builtin.PassThroughConverter;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory.Builder;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.unenhance.HibernateUnenhanceStrategy;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;

@Slf4j
@Configuration
public class DtoMapperConfig {
    private static final Logger logger = LoggerFactory.getLogger(DtoMapperConfig.class);

    @Autowired
    private KoodistoClient koodistoClient;

    @Autowired
    private LiiteRepository liiteRepository;

    @Lazy
    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    public DtoMapperConfig(KoodistoClient koodistoClient) {
        this.koodistoClient = koodistoClient;
    }

    static public DefaultMapperFactory createFactory(
            BidirectionalConverter<TekstiPalanen, LokalisoituTekstiDto> tekstiPalanenConverter,
            BidirectionalConverter<ReferenceableEntity, Reference> cachedEntityConverter,
            KoodistokoodiConverter koodistokoodiConverter,
            ArviointiConverter arviointiConverter) {
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

        if (tekstiPalanenConverter != null) {
            factory.getConverterFactory().registerConverter(tekstiPalanenConverter);
        }

        if (cachedEntityConverter != null) {
            factory.getConverterFactory().registerConverter(cachedEntityConverter);
        }

        if (koodistokoodiConverter != null) {
            factory.getConverterFactory().registerConverter("koodistokoodiConverter", koodistokoodiConverter);
        }

        if (arviointiConverter != null) {
            factory.getConverterFactory().registerConverter(arviointiConverter);
        }

        factory.getConverterFactory().registerConverter(new PassThroughConverter(TekstiPalanen.class));
        factory.getConverterFactory().registerConverter(new PassThroughConverter(Instant.class));

        // Lisätään Optional tuki
        OptionalSupport.register(factory);

        factory.registerMapper(new ReferenceableCollectionMergeMapper());
        return factory;
    }

    @Bean
    @UncachedDto
    public DtoMapper uncachedDtoMapper(
            @UncachedDto BidirectionalConverter<TekstiPalanen, LokalisoituTekstiDto> tekstiPalanenConverter,
            @UncachedDto BidirectionalConverter<ReferenceableEntity, Reference> cachedEntityConverter,
            ArviointiConverter arviointiConverter,
            KoodistokoodiConverter koodistokoodiConverter) {
        return dtoMapper(tekstiPalanenConverter, cachedEntityConverter, koodistokoodiConverter, arviointiConverter);
    }

    @Bean
    @Dto
    public DtoMapper normalDtoMapper(
            @Dto BidirectionalConverter<TekstiPalanen, LokalisoituTekstiDto> tekstiPalanenConverter,
            @Dto BidirectionalConverter<ReferenceableEntity, Reference> cachedEntityConverter,
            ArviointiConverter arviointiConverter,
            KoodistokoodiConverter koodistokoodiConverter) {
        return dtoMapper(tekstiPalanenConverter, cachedEntityConverter, koodistokoodiConverter, arviointiConverter);
    }

    private DtoMapper dtoMapper(
            BidirectionalConverter<TekstiPalanen, LokalisoituTekstiDto> tekstiPalanenConverter,
            BidirectionalConverter<ReferenceableEntity, Reference> cachedEntityConverter,
            KoodistokoodiConverter koodistokoodiConverter,
            ArviointiConverter arviointiConverter
    ) {
        DefaultMapperFactory factory = createFactory(tekstiPalanenConverter, cachedEntityConverter, koodistokoodiConverter, arviointiConverter);

        factory.classMap(Referer.class, RefererDto.class)
                .byDefault()
                .register();

        factory.classMap(PerusteenOsaDto.Suppea.class, PerusteenOsa.class)
                .byDefault()
                .customize(new CustomMapper<PerusteenOsaDto.Suppea, PerusteenOsa>() {
                    @Override
                    public void mapBtoA(PerusteenOsa perusteenOsa, PerusteenOsaDto.Suppea suppea, MappingContext context) {
                        String name = perusteenOsa.getClass().getSimpleName();
                        if (!ObjectUtils.isEmpty(name)) {
                            suppea.setOsanTyyppi(name.toLowerCase());
                        }
                    }
                })
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

        factory.registerObjectFactory((source, mappingContext) -> JsonNodeFactory.instance.objectNode(), ObjectNode.class);

        factory.classMap(ObjectNode.class, ObjectNode.class)
                .byDefault()
                .customize(new CustomMapper<ObjectNode, ObjectNode>() {
                    @Override
                    public void mapAtoB(ObjectNode a, ObjectNode b, MappingContext context) {
                        b.removeAll();
                        b.setAll(a);
                    }

                    @Override
                    public void mapBtoA(ObjectNode b, ObjectNode a, MappingContext context) {
                        mapAtoB(b, a, context);
                    }
                })
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

        factory.classMap(Perusteprojekti.class, PerusteprojektiKevytDto.class)
                .byDefault()
                .favorExtension(true)
                .customize(new CustomMapper<Perusteprojekti, PerusteprojektiKevytDto>() {
                    @Override
                    public void mapAtoB(Perusteprojekti source, PerusteprojektiKevytDto target, MappingContext context) {
                        super.mapAtoB(source, target, context);
                        if (CollectionUtils.isNotEmpty(source.getPeruste().getJulkaisut()) && !ProjektiTila.POISTETTU.equals(source.getTila())) {
                            target.setTila(ProjektiTila.JULKAISTU);
                        }
                    }
                })
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
                .customize(new CustomMapper<TutkinnonOsaViiteDto, TutkinnonOsaViite>() {
                    @Override
                    public void mapBtoA(TutkinnonOsaViite source, TutkinnonOsaViiteDto target, MappingContext context) {
                        super.mapBtoA(source, target, context);
                        if (source.getTutkinnonOsa().getKoodi() != null) {
                            KoodiDto koodiDto = new KoodiDto();
                            koodiDto.setUri(source.getTutkinnonOsa().getKoodi().getUri());
                            koodiDto.setKoodisto(source.getTutkinnonOsa().getKoodi().getKoodisto());
                            koodistoClient.addNimiAndArvo(koodiDto);
                            target.setNimi(koodiDto.getNimi());
                        }
                    }
                })
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

        factory.classMap(Lops2019Oppiaine.class, Lops2019OppiaineBaseDto.class)
                .byDefault()
                .register();

        factory.classMap(Osaamistavoite2020Dto.class, Osaamistavoite.class)
                .byDefault()
                .field("tavoitteet", "tavoitteet2020")
                .register();

        factory.classMap(OsaAlue.class, OsaAlueLaajaDto.class)
                .byDefault()
                .field("geneerinenArviointiasteikko", "arviointi")
                .register();

        factory.classMap(OsaAlue.class, OsaAlueKokonaanDto.class)
                .byDefault()
                .field("geneerinenArviointiasteikko", "arviointi")
                .register();

        factory.classMap(OsaAlue.class, OsaAlueKaikkiDto.class)
                .byDefault()
                .field("geneerinenArviointiasteikko", "arviointi")
                .register();

        factory.classMap(Lops2019Oppiaine.class, Lops2019OppiaineKaikkiDto.class)
                .byDefault()
                .register();

        factory.classMap(Lops2019Moduuli.class, Lops2019ModuuliBaseDto.class)
                .byDefault()
                .register();

        factory.classMap(Lops2019Moduuli.class, Lops2019ModuuliDto.class)
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
                            koodistoClient.addNimiAndArvo(koodiDto);
                            target.setNimi(koodiDto.getNimi());
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
                        if (!source.getTutkintonimikeUri().contains("temporary")) {
                            try {
                                KoodiDto koodiDto = new KoodiDto();
                                koodiDto.setUri(target.getTutkintonimikeUri());
                                koodiDto.setKoodisto("tutkintonimikkeet");
                                koodistoClient.addNimiAndArvo(koodiDto);
                                target.setNimi(koodiDto.getNimi());
                            } catch (RestClientException | AccessDeniedException ex) {
                                logger.error(ex.getLocalizedMessage());
                            }
                        }
                    }
                })
                .register();

        factory.classMap(Koodi.class, KoodiDto.class)
                .byDefault()
                .customize(new CustomMapper<Koodi, KoodiDto>() {
                    @Override
                    public void mapAtoB(Koodi a, KoodiDto b, MappingContext context) {
                        super.mapAtoB(a, b, context);
                        try {
                            if (!a.isTemporary()) {
                                koodistoClient.addNimiAndArvo(b);
                            }
                        } catch (RestClientException | AccessDeniedException ex) {
                            logger.warn(rakennaKoodiVirhe(a, ex.getLocalizedMessage()));
                        }
                    }

                    @Override
                    public void mapBtoA(KoodiDto b, Koodi a, MappingContext context) {
                        super.mapBtoA(b, a, context);
                        if (StringUtils.isEmpty(b.getUri()) && !StringUtils.isEmpty(b.getKoodisto())) {
                            a.setUri(TemporaryKoodiGenerator.generate(b.getKoodisto()));
                        }
                        if (!a.isTemporary()) {
                            a.setNimi(null);
                        }
                    }
                })
                .register();

        factory.classMap(Ammattitaitovaatimus2019.class, Ammattitaitovaatimus2019Dto.class)
                .byDefault()
                .customize(new CustomMapper<Ammattitaitovaatimus2019, Ammattitaitovaatimus2019Dto>() {
                    @Override
                    public void mapAtoB(Ammattitaitovaatimus2019 source, Ammattitaitovaatimus2019Dto target, MappingContext context) {
                        super.mapAtoB(source, target, context);
                        if (target.getKoodi() != null) {
                            target.setVaatimus(target.getKoodi().getNimi());
                        }
                    }

                    @Override
                    public void mapBtoA(Ammattitaitovaatimus2019Dto source, Ammattitaitovaatimus2019 target, MappingContext context) {
                        super.mapBtoA(source, target, context);
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
                        if (osaamisalaDto.getOsaamisalakoodiUri() != null && !osaamisalaDto.getOsaamisalakoodiUri().contains("temporary")) {
                            koodi.setKoodisto("osaamisala");
                            koodi.setUri(osaamisalaDto.getOsaamisalakoodiUri());
                        }
                    }

                    @Override
                    public void mapAtoB(Koodi a, OsaamisalaDto b, MappingContext context) {
                        try {
                            super.mapAtoB(a, b, context);
                            if (!a.isTemporary()) {
                                KoodiDto koodi = koodistoClient.getKoodi(a.getKoodisto(), a.getUri(), a.getVersio());
                                if (koodi != null) {
                                    b.setNimi(koodi.getNimi());
                                    b.setOsaamisalakoodiArvo(koodi.getArvo());
                                }
                            }
                        } catch (RestClientException | AccessDeniedException ex) {
                            logger.warn(rakennaKoodiVirhe(a, ex.getLocalizedMessage()));
                        }
                    }
                })
                .register();

        //YL
        factory.classMap(LukioOppiaineUpdateDto.class, Oppiaine.class)
                .mapNulls(true)
                .byDefault()
                .register();

        factory.classMap(OppiaineDto.class, Oppiaine.class)
                .mapNulls(true)
                .fieldBToA("vuosiluokkakokonaisuudet", "vuosiluokkakokonaisuudet")
                .customize(new CustomMapper<OppiaineDto, Oppiaine>() {
                    @Override
                    public void mapBtoA(Oppiaine oppiaine, OppiaineDto oppiaineDto, MappingContext context) {
                        super.mapBtoA(oppiaine, oppiaineDto, context);
                        if (oppiaine.getKoodiUri() != null) {
                            oppiaineDto.setKoodi(koodistoClient.getKoodi(oppiaine.getKoodiUri().split("_")[0], oppiaine.getKoodiUri()));
                        }
                    }

                    @Override
                    public void mapAtoB(OppiaineDto oppiaineDto, Oppiaine oppiaine, MappingContext context) {
                        super.mapAtoB(oppiaineDto, oppiaine, context);
                        if (oppiaineDto.getKoodi() != null) {
                            oppiaine.setKoodiUri(oppiaineDto.getKoodi().getUri());
                            oppiaine.setKoodiArvo(oppiaineDto.getKoodi().getArvo());
                        }
                    }
                })
                .byDefault()
                .register();

        factory.classMap(OppiaineSuppeaDto.class, Oppiaine.class)
                .fieldBToA("muokattu", "muokattu")
                .byDefault()
                .register();

        factory.classMap(Lops2019OppiaineBaseDto.class, Lops2019Oppiaine.class)
                .byDefault()
                .register();

        factory.classMap(Lops2019OppiaineDto.class, Lops2019Oppiaine.class)
                .byDefault()
                .register();

        factory.classMap(Lops2019OppiaineKaikkiDto.class, Lops2019Oppiaine.class)
                .byDefault()
                .register();

        perusteenOsaViiteMapping(factory, PerusteenOsaViiteDto.Matala.class);
        perusteenOsaViiteMapping(factory, PerusteenOsaViiteDto.Suppea.class);
        perusteenOsaViiteMapping(factory, PerusteenOsaViiteDto.Laaja.class);

        factory.classMap(OpintokokonaisuusDto.class, Opintokokonaisuus.class)
                .use(PerusteenOsaDto.Laaja.class, PerusteenOsa.class)
                .byDefault()
                .register();

        factory.classMap(TavoitesisaltoalueDto.class, Tavoitesisaltoalue.class)
                .use(PerusteenOsaDto.Laaja.class, PerusteenOsa.class)
                .byDefault()
                .register();

        factory.classMap(KoulutuksenOsaDto.class, KoulutuksenOsa.class)
                .use(PerusteenOsaDto.Laaja.class, PerusteenOsa.class)
                .byDefault()
                .register();

        factory.classMap(TuvaLaajaAlainenOsaaminenDto.class, TuvaLaajaAlainenOsaaminen.class)
                .use(PerusteenOsaDto.Laaja.class, PerusteenOsa.class)
                .byDefault()
                .register();

        factory.classMap(KotoKielitaitotasoDto.class, KotoKielitaitotaso.class)
                .use(PerusteenOsaDto.Laaja.class, PerusteenOsa.class)
                .byDefault()
                .register();

        factory.classMap(KotoOpintoDto.class, KotoOpinto.class)
                .use(PerusteenOsaDto.Laaja.class, PerusteenOsa.class)
                .byDefault()
                .register();

        factory.classMap(KotoLaajaAlainenOsaaminenDto.class, KotoLaajaAlainenOsaaminen.class)
                .use(PerusteenOsaDto.Laaja.class, PerusteenOsa.class)
                .byDefault()
                .register();

        factory.classMap(OsaamiskokonaisuusDto.class, Osaamiskokonaisuus.class)
                .use(PerusteenOsaDto.Laaja.class, PerusteenOsa.class)
                .byDefault()
                .register();

        factory.classMap(OsaamiskokonaisuusPaaAlueDto.class, OsaamiskokonaisuusPaaAlue.class)
                .use(PerusteenOsaDto.Laaja.class, PerusteenOsa.class)
                .byDefault()
                .register();

        factory.classMap(Peruste.class, PerusteKaikkiDto.class)
                .exclude("sisallot")
                .byDefault()
                .register();

        factory.classMap(ArvioinninKohde.class, ArvioinninKohdeDto.class)
                .byDefault()
                .customize(new CustomMapper<ArvioinninKohde, ArvioinninKohdeDto>() {
                    @Override
                    public void mapAtoB(ArvioinninKohde source, ArvioinninKohdeDto target, MappingContext context) {
                        super.mapAtoB(source, target, context);
                        target.setArviointiAsteikkoDto(mapper.map(source.getArviointiAsteikko(), ArviointiAsteikkoDto.class));
                    }

                    @Override
                    public void mapBtoA(ArvioinninKohdeDto source, ArvioinninKohde target, MappingContext context) {
                        super.mapBtoA(source, target, context);
                    }
                })
                .register();

        factory.classMap(OsaamistasonKriteeri.class, OsaamistasonKriteeriDto.class)
                .byDefault()
                .customize(new CustomMapper<OsaamistasonKriteeri, OsaamistasonKriteeriDto>() {
                    @Override
                    public void mapAtoB(OsaamistasonKriteeri source, OsaamistasonKriteeriDto target, MappingContext context) {
                        super.mapAtoB(source, target, context);
                        target.setOsaamistasoDto(mapper.map(source.getOsaamistaso(), OsaamistasoDto.class));
                    }

                    @Override
                    public void mapBtoA(OsaamistasonKriteeriDto source, OsaamistasonKriteeri target, MappingContext context) {
                        super.mapBtoA(source, target, context);
                    }
                })
                .register();

        factory.classMap(OsaamismerkkiKategoria.class, OsaamismerkkiKategoriaDto.class)
                .byDefault()
                .customize(new CustomMapper<OsaamismerkkiKategoria, OsaamismerkkiKategoriaDto>() {
                    @Override
                    public void mapAtoB(OsaamismerkkiKategoria source, OsaamismerkkiKategoriaDto target, MappingContext context) {
                        super.mapAtoB(source, target, context);
                        if (source.getLiite() != null) {
                            try {
                                Liite liite = liiteRepository.findById(source.getLiite().getId());
                                Blob blob = liite.getData();
                                byte[] bytes = blob.getBytes(1L, (int)blob.length());
                                target.getLiite().setBinarydata(Base64.getEncoder().encodeToString(bytes));
                            } catch (SQLException ignored) {
                                logger.error("Osaamismerkin kuvaliitteen haku epäonnistui");
                            }
                        }
                    }

                    @Override
                    public void mapBtoA(OsaamismerkkiKategoriaDto source, OsaamismerkkiKategoria target, MappingContext context) {
                        super.mapBtoA(source, target, context);
                    }
                })
                .register();

        return new DtoMapperImpl(factory.getMapperFacade());
    }

    private static void perusteenOsaViiteMapping(DefaultMapperFactory factory, Class<? extends PerusteenOsaViiteDto<?>> dtoClass) {
        // Pelkästään yliluokan mappauksen konffaus ei toiminut
        factory.classMap(dtoClass, PerusteenOsaViite.class)
                .mapNulls(false)
                .field("perusteenOsaRef", "perusteenOsa")
                .field("perusteenOsa", "perusteenOsa")
                .mapNulls(true)
                .byDefault()
                .register();
    }

    private static String rakennaKoodiVirhe(Koodi koodi, String message) {
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
}
