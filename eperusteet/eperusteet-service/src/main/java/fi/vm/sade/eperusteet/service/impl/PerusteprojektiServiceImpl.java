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
package fi.vm.sade.eperusteet.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.eperusteet.domain.*;
import static fi.vm.sade.eperusteet.domain.ProjektiTila.*;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaAlue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.domain.yl.*;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukioOpetussuunnitelmaRakenne;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.lukio.Lukiokurssi;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanProjektitiedotDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaTyoryhmaDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteprojektiQueryDto;
import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.*;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import static fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto.localized;
import fi.vm.sade.eperusteet.repository.*;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiService;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.DokumenttiException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.KayttajanTietoParser;
import fi.vm.sade.eperusteet.service.util.PerusteenRakenne;
import fi.vm.sade.eperusteet.service.util.PerusteenRakenne.Validointi;
import fi.vm.sade.eperusteet.service.util.RestClientFactory;
import static fi.vm.sade.eperusteet.service.util.Util.*;
import fi.vm.sade.generic.rest.CachingRestClient;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toMap;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author harrik
 */
@Service
public class PerusteprojektiServiceImpl implements PerusteprojektiService {
    private static final Logger LOG = LoggerFactory.getLogger(PerusteprojektiServiceImpl.class);

    private static final String HENKILO_YHTEYSTIEDOT_API = "/s2s/henkilo/yhteystiedot";

    @Value("${cas.service.oppijanumerorekisteri-service:''}")
    private String onrServiceUrl;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteprojektiRepository repository;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private KayttajanTietoService kayttajanTietoService;

    @Autowired
    private RestClientFactory restClientFactory;

    @Autowired
    private PerusteprojektiTyoryhmaRepository perusteprojektiTyoryhmaRepository;

    @Autowired
    private PerusteenOsaTyoryhmaRepository perusteenOsaTyoryhmaRepository;

    @Autowired
    private PerusteenOsaRepository perusteenOsaRepository;

    @Autowired
    private PerusteenOsaViiteRepository perusteenOsaViiteRepository;

    @Autowired
    private TutkinnonOsaViiteRepository tutkinnonOsaViiteRepository;

    @Autowired
    private KoodistoClient koodistoService;

    @Autowired
    private DokumenttiService dokumenttiService;

    @Override
    @Transactional(readOnly = true)
    public List<PerusteprojektiInfoDto> getBasicInfo() {
        return mapper.mapAsList(repository.findAll(), PerusteprojektiInfoDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerusteprojektiKevytDto> getKevytBasicInfo() {
        return repository.findAll().stream()
                .map(pp -> {
                    Peruste peruste = pp.getPeruste();
                    PerusteprojektiKevytDto ppk = mapper.map(pp, PerusteprojektiKevytDto.class);
                    if (ppk != null && peruste != null) {
                        String pdiaari = peruste.getDiaarinumero() != null ? peruste.getDiaarinumero().toString() : null;
                        ppk.setPerusteendiaarinumero(pdiaari);
                        ppk.setKoulutustyyppi(peruste.getKoulutustyyppi());
                        ppk.setTyyppi(peruste.getTyyppi());
                        ppk.setSuoritustavat(peruste.getSuoritustavat().stream()
                                .map(Suoritustapa::getSuoritustapakoodi)
                                .map(stk -> stk.toString())
                                .collect(Collectors.toSet()));
                    }
                    return ppk;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PerusteprojektiKevytDto> findBy(PageRequest page, PerusteprojektiQueryDto query) {
        Page<PerusteprojektiKevytDto> result = repository.findBy(page, query).map(pp -> {
            PerusteprojektiKevytDto ppk = mapper.map(pp, PerusteprojektiKevytDto.class);
            Peruste peruste = pp.getPeruste();
            if (ppk != null && peruste != null) {
                String pdiaari = peruste.getDiaarinumero() != null ? peruste.getDiaarinumero().toString() : null;
                ppk.setPerusteendiaarinumero(pdiaari);
                ppk.setKoulutustyyppi(peruste.getKoulutustyyppi());
                ppk.setTyyppi(peruste.getTyyppi());
                ppk.setSuoritustavat(peruste.getSuoritustavat().stream()
                        .map(Suoritustapa::getSuoritustapakoodi)
                        .map(stk -> stk.toString())
                        .collect(Collectors.toSet()));
            }
            return ppk;
        });
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<PerusteprojektiListausDto> getOmatProjektit() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Set<String> orgs = authentication.getAuthorities().stream()
                .filter(Objects::nonNull)
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .map(x -> x.split("_"))
                .filter(x -> x.length > 0)
                .map(x -> x[x.length - 1])
                .collect(Collectors.toSet());
        String user = authentication.getName();
        return mapper.mapAsList(repository.findOmatPerusteprojektit(user, orgs), PerusteprojektiListausDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteprojektiDto get(Long id) {
        Perusteprojekti p = repository.findOne(id);
        return mapper.map(p, PerusteprojektiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KayttajanTietoDto> getJasenet(Long id) {
        CachingRestClient crc = restClientFactory.get(onrServiceUrl);
        Perusteprojekti p = repository.findOne(id);
        List<KayttajanTietoDto> kayttajat;
        ObjectMapper omapper = new ObjectMapper();

        if (p == null || p.getRyhmaOid() == null | p.getRyhmaOid().isEmpty()) {
            throw new BusinessRuleViolationException("Perusteprojektilla ei ole oid:a");
        }

        try {
            String url = onrServiceUrl + HENKILO_YHTEYSTIEDOT_API;
            String ryhmaOid = p.getRyhmaOid();
            HttpResponse response = crc.post(url, "application/json", "{ \"organisaatioOids\": [\"" + ryhmaOid + "\"] }");
            InputStream contentStream = response.getEntity().getContent();
            kayttajat = KayttajanTietoParser.parsiKayttajat(omapper.readTree(IOUtils.toString(contentStream)));
        } catch (IOException ex) {
            throw new BusinessRuleViolationException("Käyttäjien tietojen hakeminen epäonnistui");
        }

        return kayttajat;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CombinedDto<KayttajanTietoDto, KayttajanProjektitiedotDto>> getJasenetTiedot(Long id) {
        CachingRestClient crc = restClientFactory.get(onrServiceUrl);
        Perusteprojekti p = repository.findOne(id);

        if (p == null || p.getRyhmaOid() == null || p.getRyhmaOid().isEmpty()) {
            throw new BusinessRuleViolationException("Perusteprojektilla ei ole oid:a");
        }

        List<CombinedDto<KayttajanTietoDto, KayttajanProjektitiedotDto>> kayttajat = new ArrayList<>();

        try {
            String url = onrServiceUrl + HENKILO_YHTEYSTIEDOT_API;
            String ryhmaOid = p.getRyhmaOid();
            HttpResponse response = crc.post(url, "application/json", "{ \"organisaatioOids\": [\"" + ryhmaOid + "\"] }");
            InputStream contentStream = response.getEntity().getContent();
            ObjectMapper omapper = new ObjectMapper();

            JsonNode tree = omapper.readTree(IOUtils.toString(contentStream));
            for (JsonNode node : tree) {
                String oid = node.get("oidHenkilo").asText();
                KayttajanTietoDto kayttaja = kayttajanTietoService.hae(oid);
                KayttajanProjektitiedotDto kayttajanProjektitiedot = kayttajanTietoService.haePerusteprojekti(oid, id);

                if (kayttaja != null && kayttajanProjektitiedot != null) {
                    CombinedDto<KayttajanTietoDto, KayttajanProjektitiedotDto> combined = new CombinedDto<>(
                        kayttaja,
                        kayttajanProjektitiedot
                    );
                    kayttajat.add(combined);
                }
            }
        } catch (IOException ex) {
            throw new BusinessRuleViolationException("Käyttäjien tietojen hakeminen epäonnistui");
        }
        return kayttajat;
    }

    @Override
    @Transactional
    public PerusteprojektiDto save(PerusteprojektiLuontiDto perusteprojektiDto) {
        Perusteprojekti perusteprojekti = mapper.map(perusteprojektiDto, Perusteprojekti.class);

        KoulutusTyyppi koulutustyyppi = KoulutusTyyppi.of(perusteprojektiDto.getKoulutustyyppi());
        LaajuusYksikko yksikko = perusteprojektiDto.getLaajuusYksikko();
        PerusteTyyppi tyyppi = perusteprojektiDto.getTyyppi() == null ? PerusteTyyppi.NORMAALI : perusteprojektiDto.getTyyppi();
        perusteprojekti.setTila(LAADINTA);
        perusteprojekti.setRyhmaOid(perusteprojektiDto.getRyhmaOid());

        if (tyyppi != PerusteTyyppi.POHJA) {
            if (koulutustyyppi == null) {
                throw new BusinessRuleViolationException("Opetussuunnitelmalla täytyy olla koulutustyyppi");
            }

            if (yksikko == null && koulutustyyppi
                    .isOneOf(KoulutusTyyppi.PERUSTUTKINTO,
                            KoulutusTyyppi.AMMATTITUTKINTO,
                            KoulutusTyyppi.ERIKOISAMMATTITUTKINTO,
                            KoulutusTyyppi.TELMA,
                            KoulutusTyyppi.VALMA)) {
                throw new BusinessRuleViolationException("Opetussuunnitelmalla täytyy olla yksikkö");
            }

            if (perusteprojektiDto.getDiaarinumero() == null) {
                throw new BusinessRuleViolationException("Diaarinumeroa ei ole asetettu");
            }

            DiaarinumeroHakuDto diaariHaku = onkoDiaarinumeroKaytossa(new Diaarinumero(perusteprojektiDto.getDiaarinumero()));
            Boolean korvaava = diaariHaku.getTila() == ProjektiTila.JULKAISTU && diaariHaku.getLoytyi();

            if (korvaava) {
                Perusteprojekti jyrattava = repository.findOne(diaariHaku.getId());
                perusteprojekti.setPaatosPvm(jyrattava.getPaatosPvm());
                perusteprojekti.setToimikausiAlku(jyrattava.getToimikausiAlku());
                perusteprojekti.setToimikausiLoppu(jyrattava.getToimikausiLoppu());
            }
        }

        if (perusteprojektiDto.getRyhmaOid() == null) {
            throw new BusinessRuleViolationException("Perustetyöryhmä ei ole asetettu");
        }

        Peruste peruste;
        if (perusteprojektiDto.getPerusteId() == null) {
            peruste = perusteService.luoPerusteRunko(koulutustyyppi, yksikko, tyyppi, perusteprojektiDto.isReforminMukainen());
        } else {
            Peruste pohjaPeruste = perusteRepository.findOne(perusteprojektiDto.getPerusteId());
            perusteprojektiDto.setKoulutustyyppi(pohjaPeruste.getKoulutustyyppi());
            peruste = perusteService.luoPerusteRunkoToisestaPerusteesta(perusteprojektiDto, tyyppi);
        }

        if (KoulutusTyyppi.of(peruste.getKoulutustyyppi()).isAmmatillinen()) {
            KVLiite kvliite = new KVLiite();
            if (perusteprojektiDto.getPerusteId() != null) {
                Peruste pohja = perusteRepository.findOne(perusteprojektiDto.getPerusteId());
                if (pohja != null) {
                    kvliite.setPohja(pohja.getKvliite());
                }
            }
            peruste.setKvliite(kvliite);
        }

        if (tyyppi == PerusteTyyppi.POHJA) {
            TekstiPalanen pnimi = TekstiPalanen.of(Kieli.FI, perusteprojektiDto.getNimi());
            peruste.setNimi(pnimi);
        }

        perusteprojekti.setPeruste(peruste);
        perusteprojekti = repository.saveAndFlush(perusteprojekti);

        return mapper.map(perusteprojekti, PerusteprojektiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public DiaarinumeroHakuDto onkoDiaarinumeroKaytossa(Diaarinumero diaarinumero) {
        DiaarinumeroHakuDto reply = new DiaarinumeroHakuDto();
        reply.setLoytyi(false);
        List<Perusteprojekti> perusteprojektit = repository.findByDiaarinumero(diaarinumero);

        if (perusteprojektit.isEmpty()) {
            return reply;
        }

        Perusteprojekti pp = perusteprojektit.get(0);

        for (Perusteprojekti p : perusteprojektit) {
            if (p.getTila() == ProjektiTila.JULKAISTU) {
                pp = p;
                break;
            }
            if (p.getMuokattu().after(pp.getMuokattu())) {
                pp = p;
            }
        }

        reply.setId(pp.getId());
        reply.setLoytyi(true);
        reply.setTila(pp.getTila());
        reply.setDiaarinumero(pp.getDiaarinumero().getDiaarinumero());
        return reply;
    }

    @Override
    @Transactional
    public PerusteprojektiDto update(Long id, PerusteprojektiDto perusteprojektiDto) {
        Perusteprojekti vanhaProjekti = repository.findOne(id);
        if (vanhaProjekti == null) {
            throw new BusinessRuleViolationException("Projektia ei ole olemassa id:llä: " + id);
        }

        perusteprojektiDto.setId(id);
        perusteprojektiDto.setTila(vanhaProjekti.getTila());
        Perusteprojekti perusteprojekti = mapper.map(perusteprojektiDto, Perusteprojekti.class);
        perusteprojekti.setPeruste(vanhaProjekti.getPeruste());
        perusteprojekti = repository.save(perusteprojekti);

        return mapper.map(perusteprojekti, PerusteprojektiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<ProjektiTila> getTilat(Long id) {
        Perusteprojekti p = repository.findOne(id);
        if (p == null) {
            throw new BusinessRuleViolationException("Projektia ei ole olemassa id:llä: " + id);
        }

        return p.getTila().mahdollisetTilat(p.getPeruste().getTyyppi());
    }

    @SuppressWarnings("ServiceMethodEntity")
    @Transactional(readOnly = true)
    public void tarkistaTekstipalanen(final String nimi, final TekstiPalanen palanen,
                                      final Set<Kieli> pakolliset, Map<String, String> virheellisetKielet) {
        tarkistaTekstipalanen(nimi, palanen, pakolliset, virheellisetKielet, false);
    }

    @SuppressWarnings("ServiceMethodEntity")
    @Transactional(readOnly = true)
    public void tarkistaTekstipalanen(final String nimi, final TekstiPalanen palanen,
                                      final Set<Kieli> pakolliset,
                                      Map<String, String> virheellisetKielet, boolean pakollinen) {
        if (palanen == null || palanen.getTeksti() == null) {
            return;
        }

        // Oispa lambdat
        boolean onJollainVaditullaKielella = false;
        if (!pakollinen) {
            for (Kieli kieli : pakolliset) {
                String osa = palanen.getTeksti().get(kieli);
                    if (osa != null && !osa.isEmpty()) {
                        onJollainVaditullaKielella = true;
                        break;
                    }
            }
        }

        if (pakollinen || onJollainVaditullaKielella) {
            for (Kieli kieli : pakolliset) {
                Map<Kieli, String> teksti = palanen.getTeksti();
                if (!teksti.containsKey(kieli) || teksti.get(kieli) == null || teksti.get(kieli).isEmpty()) {
                    virheellisetKielet.put(nimi, kieli.name());
                }
            }
        }
    }

    @SuppressWarnings("ServiceMethodEntity")
    @Transactional(readOnly = true)
    public void tarkistaSisalto(final PerusteenOsaViite viite, final Set<Kieli> pakolliset,
                                Map<String, String> virheellisetKielet) {
        PerusteenOsa perusteenOsa = viite.getPerusteenOsa();
        if (perusteenOsa instanceof TekstiKappale && (perusteenOsa.getTunniste() == PerusteenOsaTunniste.NORMAALI || perusteenOsa.getTunniste() == null)) {
            TekstiKappale tekstikappale = (TekstiKappale) perusteenOsa;
            tarkistaTekstipalanen("peruste-validointi-tekstikappale-nimi", tekstikappale.getNimi(), pakolliset, virheellisetKielet, true);
            tarkistaTekstipalanen("peruste-validointi-tekstikappale-teksti", tekstikappale.getTeksti(), pakolliset, virheellisetKielet);
        }
        for (PerusteenOsaViite lapsi : viite.getLapset()) {
            tarkistaSisalto(lapsi, pakolliset, virheellisetKielet);
        }
    }

    @SuppressWarnings("ServiceMethodEntity")
    @Transactional(readOnly = true)
    public void tarkistaRakenne(final AbstractRakenneOsa aosa, final Set<Kieli> pakolliset,
                                Map<String, String> virheellisetKielet) {
        tarkistaTekstipalanen("peruste-validointi-rakenneosa-kuvaus", aosa.getKuvaus(), pakolliset, virheellisetKielet);
        if (aosa instanceof RakenneModuuli) {
            RakenneModuuli osa = (RakenneModuuli) aosa;
            for (AbstractRakenneOsa lapsi : osa.getOsat()) {
                tarkistaRakenne(lapsi, pakolliset, virheellisetKielet);
            }
        }
    }

    @Transactional(readOnly = true)
    private void tarkistaPerusopetuksenOppiaine(Oppiaine oa, TilaUpdateStatus status,
                                final Set<Kieli> vaaditutKielet, Map<String, String> virheellisetKielet) {
        tarkistaTekstipalanen("peruste-validointi-oppiaine-nimi", oa.getNimi(), vaaditutKielet, virheellisetKielet);

        if (oa.getTehtava() != null) {
            tarkistaTekstipalanen("peruste-validointi-oppiaine-sisalto", oa.getTehtava().getOtsikko(),
                    vaaditutKielet, virheellisetKielet);
            tarkistaTekstipalanen("peruste-validointi-oppiaine-sisalto", oa.getTehtava().getTeksti(),
                    vaaditutKielet, virheellisetKielet);
        }

        Set<OpetuksenKohdealue> kohdealueet = oa.getKohdealueet();
        for (OpetuksenKohdealue ka : kohdealueet) {
            tarkistaTekstipalanen("peruste-validointi-oppiaine-kohdealue", ka.getNimi(), vaaditutKielet, virheellisetKielet);
        }

        Set<OppiaineenVuosiluokkaKokonaisuus> oavlks = oa.getVuosiluokkakokonaisuudet();
        for (OppiaineenVuosiluokkaKokonaisuus oavlk : oavlks) {
            TekstiOsa arviointi = oavlk.getArviointi();
            if (arviointi != null) {
                tarkistaTekstipalanen("peruste-validointi-oppiaine-vlk-sisalto", arviointi.getOtsikko(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-oppiaine-vlk-sisalto", arviointi.getTeksti(),
                        vaaditutKielet, virheellisetKielet);
            }

            TekstiOsa ohjaus = oavlk.getOhjaus();
            if (ohjaus != null) {
                tarkistaTekstipalanen("peruste-validointi-oppiaine-vlk-sisalto", ohjaus.getOtsikko(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-oppiaine-vlk-sisalto", ohjaus.getTeksti(),
                        vaaditutKielet, virheellisetKielet);
            }

            TekstiOsa tehtava = oavlk.getTehtava();
            if (tehtava != null) {
                tarkistaTekstipalanen("peruste-validointi-oppiaine-vlk-sisalto", tehtava.getOtsikko(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-oppiaine-vlk-sisalto", tehtava.getTeksti(),
                        vaaditutKielet, virheellisetKielet);
            }

            TekstiOsa tyotavat = oavlk.getTyotavat();
            if (tyotavat != null) {
                tarkistaTekstipalanen("peruste-validointi-oppiaine-vlk-sisalto", tyotavat.getOtsikko(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-oppiaine-vlk-sisalto", tyotavat.getTeksti(),
                        vaaditutKielet, virheellisetKielet);
            }

            List<KeskeinenSisaltoalue> sisaltoalueet = oavlk.getSisaltoalueet();
            for (KeskeinenSisaltoalue sa : sisaltoalueet) {
                tarkistaTekstipalanen("peruste-validointi-oppiaine-sisaltoalue", sa.getNimi(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-oppiaine-sisaltoalue", sa.getKuvaus(),
                        vaaditutKielet, virheellisetKielet);
            }

            List<OpetuksenTavoite> tavoitteet = oavlk.getTavoitteet();
            for (OpetuksenTavoite tavoite : tavoitteet) {
                tarkistaTekstipalanen("peruste-validointi-oppiaine-vlk-tavoite-tavoite-teksti",
                        tavoite.getTavoite(), vaaditutKielet, virheellisetKielet);
            }
        }

        if (oa.getOppimaarat() != null) {
            for (Oppiaine oppimaara : oa.getOppimaarat()) {
                tarkistaPerusopetuksenOppiaine(oppimaara, status, vaaditutKielet, virheellisetKielet);
            }
        }
    }

    @Transactional(readOnly = true)
    private void tarkistaPerusopetuksenPeruste(Peruste peruste, TilaUpdateStatus status) {
        if (peruste == null) {
            return;
        }

        PerusopetuksenPerusteenSisalto sisalto = peruste.getPerusopetuksenPerusteenSisalto();
        if (sisalto == null) {
            return;
        }

        Set<LaajaalainenOsaaminen> osaamiset = sisalto.getLaajaalaisetosaamiset();
        Set<VuosiluokkaKokonaisuus> vlks = sisalto.getVuosiluokkakokonaisuudet();
        Set<Oppiaine> oppiaineet = sisalto.getOppiaineetCopy();
        Set<Kieli> vaaditutKielet = peruste.getKielet();
        Map<String, String> virheellisetKielet = new HashMap<>();

        // Laajaalaiset osaamiset
        for (LaajaalainenOsaaminen osaaminen : osaamiset) {
            tarkistaTekstipalanen("peruste-validointi-laajaalainen-osaaminen-nimi", osaaminen.getNimi(),
                    vaaditutKielet, virheellisetKielet);
            tarkistaTekstipalanen("peruste-validointi-laajaalainen-osaaminen-kuvaus", osaaminen.getKuvaus(),
                    vaaditutKielet, virheellisetKielet);
        }

        // Vuosiluokkakokonaisuudet
        for (VuosiluokkaKokonaisuus vlk : vlks) {
            tarkistaTekstipalanen("peruste-validointi-vlk-nimi", vlk.getNimi(), vaaditutKielet, virheellisetKielet);

            if (vlk.getTehtava() != null) {
                tarkistaTekstipalanen("peruste-validointi-vlk-tehtava-otsikko", vlk.getTehtava().getOtsikko(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-vlk-tehtava-teksti", vlk.getTehtava().getTeksti(),
                        vaaditutKielet, virheellisetKielet);
            }

            Set<VuosiluokkaKokonaisuudenLaajaalainenOsaaminen> vlklos = vlk.getLaajaalaisetOsaamiset();
            for (VuosiluokkaKokonaisuudenLaajaalainenOsaaminen vlklo : vlklos) {
                tarkistaTekstipalanen("peruste-validointi-vlk-lo", vlklo.getKuvaus(),
                        vaaditutKielet, virheellisetKielet);
            }

            TekstiOsa paikallisestiPaatettavatAsiat = vlk.getPaikallisestiPaatettavatAsiat();
            if (paikallisestiPaatettavatAsiat != null) {
                tarkistaTekstipalanen("peruste-validointi-vlk-sisalto", paikallisestiPaatettavatAsiat.getOtsikko(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-vlk-sisalto", paikallisestiPaatettavatAsiat.getTeksti(),
                        vaaditutKielet, virheellisetKielet);
            }

            TekstiOsa siirtymaEdellisesta = vlk.getSiirtymaEdellisesta();
            if (siirtymaEdellisesta != null) {
                tarkistaTekstipalanen("peruste-validointi-vlk-sisalto", siirtymaEdellisesta.getOtsikko(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-vlk-sisalto", siirtymaEdellisesta.getTeksti(),
                        vaaditutKielet, virheellisetKielet);
            }

            TekstiOsa siirtymaSeuraavaan = vlk.getSiirtymaSeuraavaan();
            if (siirtymaSeuraavaan != null) {
                tarkistaTekstipalanen("peruste-validointi-vlk-sisalto", siirtymaSeuraavaan.getOtsikko(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-vlk-sisalto", siirtymaSeuraavaan.getTeksti(),
                        vaaditutKielet, virheellisetKielet);
            }

            TekstiOsa laajaalainenOsaaminen = vlk.getLaajaalainenOsaaminen();
            if (laajaalainenOsaaminen != null) {
                tarkistaTekstipalanen("peruste-validointi-vlk-sisalto", laajaalainenOsaaminen.getOtsikko(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-vlk-sisalto", laajaalainenOsaaminen.getTeksti(),
                        vaaditutKielet, virheellisetKielet);
            }
        }

        for (Oppiaine oa : oppiaineet) {
            tarkistaPerusopetuksenOppiaine(oa, status, vaaditutKielet, virheellisetKielet);
        }

        for (Entry<String, String> entry : virheellisetKielet.entrySet()) {
            status.setVaihtoOk(false);
            status.addStatus(entry.getKey());
        }
    }

    @SuppressWarnings("ServiceMethodEntity")
    @Transactional(readOnly = true)
    public Map<String, String> tarkistaPerusteenTekstipalaset(Peruste peruste) {
        if (peruste.getTyyppi() == PerusteTyyppi.POHJA) {
            return new HashMap<>();
        }

        Set<Kieli> vaaditutKielet = peruste.getKielet();
        Map<String, String> virheellisetKielet = new HashMap<>();
        tarkistaTekstipalanen("peruste-validointi-kuvaus", peruste.getKuvaus(), vaaditutKielet, virheellisetKielet);
        tarkistaTekstipalanen("peruste-validointi-nimi", peruste.getNimi(), vaaditutKielet, virheellisetKielet);

        for (Koulutus koulutus : peruste.getKoulutukset()) {
            tarkistaTekstipalanen("peruste-validointi-koulutus-nimi", koulutus.getNimi(), vaaditutKielet, virheellisetKielet);
        }

        // Esiopetus
        if (peruste.getEsiopetuksenPerusteenSisalto() != null) {
            for (PerusteenOsaViite lapsi : peruste.getEsiopetuksenPerusteenSisalto().getSisalto().getLapset()) {
                tarkistaSisalto(lapsi, vaaditutKielet, virheellisetKielet);
            }
        }

        // Perusopetus
        if (peruste.getPerusopetuksenPerusteenSisalto() != null) {
            for (PerusteenOsaViite lapsi : peruste.getPerusopetuksenPerusteenSisalto().getSisalto().getLapset()) {
                tarkistaSisalto(lapsi, vaaditutKielet, virheellisetKielet);
            }
        }

        // Muut
        for (Suoritustapa st : peruste.getSuoritustavat()) {
            PerusteenOsaViite sisalto = st.getSisalto();
            for (PerusteenOsaViite lapsi : sisalto.getLapset()) {
                tarkistaSisalto(lapsi, vaaditutKielet, virheellisetKielet);
            }

            tarkistaRakenne(st.getRakenne(), vaaditutKielet, virheellisetKielet);

            for (TutkinnonOsaViite tov : st.getTutkinnonOsat()) {
                TutkinnonOsa tosa = tov.getTutkinnonOsa();
                tarkistaTekstipalanen("peruste-validointi-tutkinnonosa-ammattitaidon-osoittamistavat",
                        tosa.getAmmattitaidonOsoittamistavat(), vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-tutkinnonosa-ammattitaitovaatimukset",
                        tosa.getAmmattitaitovaatimukset(), vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-tutkinnonosa-kuvaus", tosa.getKuvaus(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-tutkinnonosa-nimi", tosa.getNimi(),
                        vaaditutKielet, virheellisetKielet, true);
                tarkistaTekstipalanen("peruste-validointi-tutkinnonosa-tavoitteet", tosa.getTavoitteet(),
                        vaaditutKielet, virheellisetKielet);
            }
        }

        return virheellisetKielet;
    }

    @Override
    @IgnorePerusteUpdateCheck
    @Transactional
    public TilaUpdateStatus updateTila(Long id, ProjektiTila tila, Date siirtymaPaattyy) {

        TilaUpdateStatus updateStatus = new TilaUpdateStatus();
        updateStatus.setVaihtoOk(true);

        Perusteprojekti projekti = repository.findOne(id);

        if (projekti == null) {
            throw new BusinessRuleViolationException("Projektia ei ole olemassa id:llä: " + id);
        }

        // Tarkistetaan mahdolliset tilat
        updateStatus.setVaihtoOk(projekti.getTila().mahdollisetTilat(projekti.getPeruste().getTyyppi()).contains(tila));
        if (!updateStatus.isVaihtoOk()) {
            String viesti = "Tilasiirtymä tilasta '" + projekti.getTila().toString() + "' tilaan '"
                    + tila.toString() + "' ei mahdollinen";
            updateStatus.addStatus(viesti);
            return updateStatus;
        }

        // Tarkistetaan että perusteelle on asetettu nimi perusteeseen asetetuilla kielillä
        if (tila != ProjektiTila.POISTETTU && tila != LAADINTA && tila != KOMMENTOINTI) {
            TekstiPalanen nimi = projekti.getPeruste().getNimi();
            for (Kieli kieli : projekti.getPeruste().getKielet()) {
                if (nimi == null || !nimi.getTeksti().containsKey(kieli)
                        || nimi.getTeksti().get(kieli).isEmpty()) {
                    updateStatus.addStatus("perusteen-nimea-ei-ole-kaikilla-kielilla");
                    updateStatus.setVaihtoOk(false);
                    break;
                }
            }
        }

        Set<String> tutkinnonOsienKoodit = new HashSet<>();
        Peruste peruste = projekti.getPeruste();
        boolean isValmisPohja = PerusteTyyppi.POHJA.equals(peruste.getTyyppi()) && (VALMIS.equals(projekti.getTila()) || PerusteTila.VALMIS.equals(peruste.getTila()));

        // Perusteen validointi
        if (!isValmisPohja && peruste.getSuoritustavat() != null
                && tila != LAADINTA && tila != KOMMENTOINTI && tila != POISTETTU) {
            if (peruste.getLukiokoulutuksenPerusteenSisalto() == null) {
                Validointi validointi;

                // Rakenteiden validointi
                for (Suoritustapa suoritustapa : peruste.getSuoritustavat()) {
                    // Rakenteiden validointi
                    if (suoritustapa.getRakenne() != null) {
                        validointi = PerusteenRakenne.validoiRyhma(peruste.getOsaamisalat(),
                                suoritustapa.getRakenne(),
                                KoulutusTyyppi.of(peruste.getKoulutustyyppi()).isValmaTelma());
                        if (!validointi.ongelmat.isEmpty()) {
                            updateStatus.addStatus("rakenteen-validointi-virhe",
                                    suoritustapa.getSuoritustapakoodi(),
                                    validointi);
                            updateStatus.setVaihtoOk(false);
                        }
                    }

                    // Vapaiden tutkinnon osien tarkistus
                    List<TutkinnonOsaViite> vapaatOsat = vapaatTutkinnonosat(suoritustapa);
                    if (!vapaatOsat.isEmpty()) {
                        List<LokalisoituTekstiDto> nimet = new ArrayList<>();
                        for (TutkinnonOsaViite viite : vapaatOsat) {
                            if (viite.getTutkinnonOsa().getNimi() != null) {
                                nimet.add(new LokalisoituTekstiDto(viite.getTutkinnonOsa().getNimi().getId(),
                                        viite.getTutkinnonOsa().getNimi().getTeksti()));
                            }
                        }
                        updateStatus.addStatus("liittamattomia-tutkinnon-osia", suoritustapa.getSuoritustapakoodi(), nimet);
                        updateStatus.setVaihtoOk(false);
                    }

                    // Tarkistetaan koodittomat tutkinnon osat
                    List<TutkinnonOsa> koodittomatTutkinnonOsat = koodittomatTutkinnonosat(suoritustapa);
                    if (!koodittomatTutkinnonOsat.isEmpty()) {
                        List<LokalisoituTekstiDto> nimet = new ArrayList<>();
                        for (TutkinnonOsa tutkinnonOsa : koodittomatTutkinnonOsat) {
                            if (tutkinnonOsa.getNimi() != null) {
                                nimet.add(new LokalisoituTekstiDto(tutkinnonOsa.getNimi().getId(),
                                        tutkinnonOsa.getNimi().getTeksti()));
                            }
                        }
                        updateStatus.addStatus("koodittomia-tutkinnon-osia", suoritustapa.getSuoritustapakoodi(), nimet);
                        updateStatus.setVaihtoOk(false);
                    }

                    // Tarkista tutke2-osien osa-alueiden koodit
                    List<LokalisoituTekstiDto> koodittomatOsaalueet = new ArrayList<>();
                    for (TutkinnonOsaViite tov : suoritustapa.getTutkinnonOsat()) {
                        TutkinnonOsa tosa = tov.getTutkinnonOsa();
                        if (tosa.getTyyppi() == TutkinnonOsaTyyppi.TUTKE2) {
                            for (OsaAlue oa : tosa.getOsaAlueet()) {
                                if (oa.getKoodiArvo() == null || oa.getKoodiArvo().isEmpty() ||
                                        oa.getKoodiUri() == null || oa.getKoodiUri().isEmpty()) {
                                    koodittomatOsaalueet.add(new LokalisoituTekstiDto(tosa.getId(),
                                            tosa.getNimi().getTeksti()));
                                    break;
                                }
                            }
                        }
                    }

                    // Tarkistetaan osa-alueiden kooditukset
                    if (!koodittomatOsaalueet.isEmpty()) {
                        updateStatus.addStatus("tutke2-osalta-puuttuu-osa-alue-koodi",
                                suoritustapa.getSuoritustapakoodi(), koodittomatOsaalueet);
                    }

                    // Kerätään tutkinnon osien koodit
                    List<LokalisoituTekstiDto> virheellisetKoodistonimet = new ArrayList<>();
                    List<LokalisoituTekstiDto> uniikitKooditTosat = new ArrayList<>();
                    Set<String> uniikitKoodit = new HashSet<>();
                    for (TutkinnonOsaViite tov : getViitteet(suoritustapa)) {
                        TutkinnonOsa tosa = tov.getTutkinnonOsa();
                        String uri = tosa.getKoodiUri();
                        String arvo = tosa.getKoodiArvo();

                        // Tarkistetaan onko sama koodi useammassa tutkinnon osassa
                        if (uniikitKoodit.contains(uri)) {
                            uniikitKooditTosat.add(new LokalisoituTekstiDto(tosa.getNimi().getId(),
                                    tosa.getNimi().getTeksti()));
                        } else {
                            uniikitKoodit.add(uri);
                        }

                        if (arvo != null && uri != null && uri.isEmpty()) {
                            uri = "tutkinnonosa_" + arvo;
                            tosa.setKoodiUri(uri);
                        }

                        if (tosa.getNimi() != null && (uri != null && arvo != null && !uri.isEmpty()
                                && !arvo.isEmpty())) {
                            KoodistoKoodiDto koodi = null;
                            try {
                                koodi = koodistoService.get("tutkinnonosat", uri);
                            } catch (Exception e) {
                                LOG.error(e.getMessage(), e);
                            }
                            if (koodi != null && koodi.getKoodiUri().equals(uri)) {
                                tutkinnonOsienKoodit.add(tov.getTutkinnonOsa().getKoodiArvo());
                            } else {
                                virheellisetKoodistonimet.add(new LokalisoituTekstiDto(tosa.getNimi().getId(),
                                        tosa.getNimi().getTeksti()));
                            }
                        }
                    }

                    if (!virheellisetKoodistonimet.isEmpty()) {
                        updateStatus.addStatus("tutkinnon-osan-asetettua-koodia-ei-koodistossa",
                                suoritustapa.getSuoritustapakoodi(), virheellisetKoodistonimet);
                        updateStatus.setVaihtoOk(false);
                    }

                    if (!uniikitKooditTosat.isEmpty()) {
                        updateStatus.addStatus("tutkinnon-osien-koodit-kaytossa-muissa-tutkinnon-osissa",
                                suoritustapa.getSuoritustapakoodi(), uniikitKooditTosat);
                        updateStatus.setVaihtoOk(false);
                    }
                }

                // Tarkistetaan perusteen tutkinnon osien koodien ja tutkintonimikkeiden yhteys
                List<TutkintonimikeKoodiDto> tutkintonimikeKoodit = perusteService.getTutkintonimikeKoodit(
                        projekti.getPeruste().getId());
                List<String> koodit = new ArrayList<>();
                for (TutkintonimikeKoodiDto tnk : tutkintonimikeKoodit) {
                    if (tnk.getTutkinnonOsaArvo() != null) {
                        koodit.add(tnk.getTutkinnonOsaArvo());
                    }
                }
                if (!tutkinnonOsienKoodit.containsAll(koodit)) {
                    updateStatus.addStatus("tutkintonimikkeen-vaatimaa-tutkinnonosakoodia-ei-loytynyt-tutkinnon-osilta");
                    updateStatus.setVaihtoOk(false);
                }
            }

            if (tila == ProjektiTila.JULKAISTU || tila == ProjektiTila.VALMIS) {
                tarkistaPerusopetuksenPeruste(peruste, updateStatus);
                // Tarkista että kaikki vaadittu kielisisältö on asetettu
                Map<String, String> lokalisointivirheet = tarkistaPerusteenTekstipalaset(projekti.getPeruste());
                for (Entry<String, String> entry : lokalisointivirheet.entrySet()) {
                    updateStatus.setVaihtoOk(false);
                    updateStatus.addStatus(entry.getKey());
                }
            }

            if (tila == ProjektiTila.JULKAISTU) {
                if (projekti.getPeruste().getDiaarinumero() == null) {
                    updateStatus.addStatus("peruste-ei-diaarinumeroa");
                    updateStatus.setVaihtoOk(false);
                }

                if (projekti.getPeruste().getVoimassaoloAlkaa() == null) {
                    updateStatus.addStatus("peruste-ei-voimassaolon-alkamisaikaa");
                    updateStatus.setVaihtoOk(false);
                }
            }

            if (peruste.getLukiokoulutuksenPerusteenSisalto() != null) {
                validoiLukio(peruste, tila, updateStatus);
            }
        }

        // Perusteen tilan muutos
        if (!updateStatus.isVaihtoOk()) {
            return updateStatus;
        }

        if (tila == ProjektiTila.JULKAISTU && projekti.getTila() == ProjektiTila.VALMIS) {
            setPerusteTila(projekti.getPeruste(), PerusteTila.VALMIS);
            Optional.of(peruste)
                    .ifPresent(p -> p.getSuoritustavat()
                    .forEach(suoritustapa -> p.getKielet()
                            .forEach(kieli -> {
                                try {
                                    DokumenttiDto createDtoFor = dokumenttiService.createDtoFor(
                                            p.getId(),
                                            kieli,
                                            suoritustapa.getSuoritustapakoodi(),
                                            GeneratorVersion.UUSI
                                    );
                                    dokumenttiService.setStarted(createDtoFor);
                                    dokumenttiService.generateWithDto(createDtoFor);
                                } catch (DokumenttiException e) {
                                    LOG.error(e.getLocalizedMessage(), e.getCause());
                                }
            })));
        }

        if (tila == ProjektiTila.POISTETTU) {
            if (PerusteTyyppi.POHJA.equals(projekti.getPeruste().getTyyppi())) {
                projekti.setTila(ProjektiTila.POISTETTU);
                projekti.getPeruste().asetaTila(PerusteTila.POISTETTU);
            }
            else {
                setPerusteTila(projekti.getPeruste(), PerusteTila.POISTETTU);
            }
        }

        if (tila == LAADINTA) {
            if (PerusteTyyppi.POHJA.equals(projekti.getPeruste().getTyyppi())) {
                projekti.setTila(ProjektiTila.LAADINTA);
                projekti.getPeruste().asetaTila(PerusteTila.LUONNOS);
            }
            else {
                setPerusteTila(projekti.getPeruste(), PerusteTila.LUONNOS);
            }
        }

        if (projekti.getPeruste().getTyyppi() == PerusteTyyppi.POHJA
                && tila == ProjektiTila.VALMIS
                && projekti.getTila() == LAADINTA) {
            setPerusteTila(projekti.getPeruste(), PerusteTila.VALMIS);
        }

        projekti.setTila(tila);
        repository.save(projekti);
        return updateStatus;
    }

    private void validoiLukio(Peruste peruste, ProjektiTila tila, TilaUpdateStatus updateStatus) {
        LukiokoulutuksenPerusteenSisalto sisalto = peruste.getLukiokoulutuksenPerusteenSisalto();
        LukioOpetussuunnitelmaRakenne rakenne = sisalto.getOpetussuunnitelma();
        updateStatus.forSuoritustapa(Suoritustapakoodi.LUKIOKOULUTUS).toTila(tila)
            .forTilat(jalkeen(LAADINTA))
                .addErrorGiven("peruste-lukio-ei-oppiaineita", rakenne.getOppiaineet().isEmpty())
                .addErrorGiven("peruste-lukio-ei-aihekokonaisuuksia", KoulutusTyyppi.of(peruste.getKoulutustyyppi()) != KoulutusTyyppi.LUKIOVALMISTAVAKOULUTUS
                        && (sisalto.getAihekokonaisuudet() == null || sisalto.getAihekokonaisuudet().getAihekokonaisuudet().isEmpty()))
                .addErrorGiven("peruste-lukio-ei-opetuksen-yleisia-tavoitteita",
                        sisalto.getOpetuksenYleisetTavoitteet() == null)
            .forTilat(jalkeen(KOMMENTOINTI))
                .addErrorStatusForAll("peruste-lukio-liittamaton-kurssi", () ->
                    rakenne.kurssit()
                        .filter(empty(Lukiokurssi::getOppiaineet))
                        .map(localized(Nimetty::getNimi)))
                .addErrorStatusForAll("peruste-lukio-oppiaineessa-ei-kursseja", () ->
                    rakenne.oppiaineetMaarineen()
                        .filter(not(Oppiaine::isKoosteinen)
                        .and(not(Oppiaine::isAbstraktiBool))
                        .and(empty(Oppiaine::getLukiokurssit)))
                        .map(localized(Nimetty::getNimi)))
                .addErrorStatusForAll("peruste-lukio-oppiaineessa-ei-oppimaaria", () ->
                    rakenne.oppiaineet()
                        .filter(and(Oppiaine::isKoosteinen, empty(Oppiaine::getOppimaarat)))
                        .map(localized(Nimetty::getNimi)))
                .addErrorStatusForAll("peruste-lukio-kooodi-puuttuu", () ->
                    rakenne.koodilliset()
                        .filter(emptyString(Koodillinen::getKoodiArvo).or(emptyString(Koodillinen::getKoodiUri)))
                        .map(localized(Nimetty::getNimi)))
                .addErrorStatusForAll("peruste-lukio-sama-koodi", () -> {
                    List<LokalisoituTekstiDto> duplikaatit = new ArrayList<>();
                    rakenne.koodilliset()
                        .filter(emptyString(Koodillinen::getKoodiArvo).negate())
                        .collect(toMap(Koodillinen::getKoodiArvo, k -> k, (a, b) -> {
                            duplikaatit.add(localized(a.getNimi())
                                .concat(" - ")
                                .concat(localized(b.getNimi()))
                                .concat(" ("+a.getKoodiArvo()+")"));
                            return a;
                        }));
                    return duplikaatit.stream();
                });
    }

    private void setPerusteTila(Peruste peruste, PerusteTila tila) {

        // Asetetaan perusteen osien tilat
        for (Suoritustapa suoritustapa : peruste.getSuoritustavat()) {
            setSisaltoTila(peruste, suoritustapa.getSisalto(), tila);
            for (TutkinnonOsaViite tutkinnonosaViite : suoritustapa.getTutkinnonOsat()) {
                setOsatTila(peruste, tutkinnonosaViite, tila);
            }
        }

        if (peruste.getPerusopetuksenPerusteenSisalto() != null) {
            setSisaltoTila(peruste, peruste.getPerusopetuksenPerusteenSisalto().getSisalto(), tila);
        }

        if (peruste.getEsiopetuksenPerusteenSisalto() != null) {
            setSisaltoTila(peruste, peruste.getEsiopetuksenPerusteenSisalto().getSisalto(), tila);
        }

        if (peruste.getLukiokoulutuksenPerusteenSisalto() != null) {
            setSisaltoTila(peruste, peruste.getLukiokoulutuksenPerusteenSisalto().getSisalto(), tila);
        }

        if (peruste.getAipeOpetuksenPerusteenSisalto() != null) {
            setSisaltoTila(peruste, peruste.getAipeOpetuksenPerusteenSisalto().getSisalto(), tila);
        }

        peruste.asetaTila(tila);
    }

    @Transactional
    private void palautaJulkaistuImpl(Peruste peruste, PerusteenOsa po, Long povId) {
        // Tarkistetaan omistaako palautettava peruste, jos on palautetaan se luonnokseksi
        peruste.getSuoritustavat()
            .forEach(st -> st.getTutkinnonOsat().stream()
                .map(TutkinnonOsaViite::getId)
                .filter(id -> id.equals(povId))
                .findFirst()
                .ifPresent(x -> {
                    po.palautaLuonnokseksi();
                }));
    }

    @Transactional
    private void palautaJulkaistu(Peruste peruste, PerusteenOsa po) {
        // FIXME: Selvitä onko tämä tarkkaan haluttu toimintatapa
        // Jos tutkinnonosaviitteitä tai perusteenosaviitteitä on yhteensä enemmän
        // yksi pitää palauttaminen estää
//        boolean onKaytossa = perusteenOsaViiteRepository.countByPerusteenOsaId(po.getId()) + tutkinnonOsaViiteRepository.countByTutkinnonOsaId(po.getId()) < 2;
//        Long vanhinPoViite = perusteenOsaViiteRepository.findAllByPerusteenOsa(po).stream()
//                .map(pov -> pov.getId())
//                .sorted()
//                .findFirst()
//                .get();

        // Etsitään vanhimman tutkintoviitteen id (vanhin tässä tapauksessa pienimmän id:n omaava)

        // FIXME: Refactor
        if (po instanceof TutkinnonOsa) {
            tutkinnonOsaViiteRepository.findAllByTutkinnonOsa((TutkinnonOsa)po).stream()
                    .map(TutkinnonOsaViite::getId)
                    .sorted()
                    .findFirst()
                    .ifPresent(id -> palautaJulkaistuImpl(peruste, po, id));
        }
        else {
            perusteenOsaViiteRepository.findAllByPerusteenOsa(po).stream()
                    .map(PerusteenOsaViite::getId)
                    .sorted()
                    .findFirst()
                    .ifPresent(id -> palautaJulkaistuImpl(peruste, po, id));
        }
    }

    // FIXME: Miksi nämä iteroidaan kahteen kertaan?
    private PerusteenOsaViite setSisaltoTila(Peruste peruste, PerusteenOsaViite sisaltoRoot, PerusteTila tila) {
        if (sisaltoRoot.getPerusteenOsa() != null) {
//            sisaltoRoot.getPerusteenOsa().asetaTila(tila);
            if (tila == PerusteTila.LUONNOS) {
                // TODO: Tarkista onko muita käyttäjiä
                sisaltoRoot.getPerusteenOsa().palautaLuonnokseksi();
                palautaJulkaistu(peruste, sisaltoRoot.getPerusteenOsa());
            }
            else {
                sisaltoRoot.getPerusteenOsa().asetaTila(tila);
            }
        }
        if (sisaltoRoot.getLapset() != null) {
            for (PerusteenOsaViite lapsi : sisaltoRoot.getLapset()) {
                setSisaltoTila(peruste, lapsi, tila);
            }
        }
        return sisaltoRoot;
    }

    private TutkinnonOsaViite setOsatTila(Peruste peruste, TutkinnonOsaViite osa, PerusteTila tila) {
        if (osa.getTutkinnonOsa() != null) {
            if (tila == PerusteTila.LUONNOS) {
                // TODO: Tarkista onko muita käyttäjiä
                palautaJulkaistu(peruste, osa.getTutkinnonOsa());
            }
            else {
                osa.getTutkinnonOsa().asetaTila(tila);
            }
        }
        return osa;
    }

    private List<TutkinnonOsaViite> vapaatTutkinnonosat(Suoritustapa suoritustapa) {
        List<TutkinnonOsaViite> viiteList = new ArrayList<>();
        RakenneModuuli rakenne = suoritustapa.getRakenne();
        if (rakenne != null) {
            for (TutkinnonOsaViite viite : getViitteet(suoritustapa)) {
                if (!rakenne.isInRakenne(viite, true)) {
                    viiteList.add(viite);
                }
            }
        }
        return viiteList;
    }

    private List<TutkinnonOsa> koodittomatTutkinnonosat(Suoritustapa suoritustapa) {
        List<TutkinnonOsa> koodittomatTutkinnonOsat = new ArrayList<>();

        if (suoritustapa.getTutkinnonOsat() != null) {
            for (TutkinnonOsaViite viite : getViitteet(suoritustapa)) {
                if (viite.getTutkinnonOsa().getKoodiArvo() == null || viite.getTutkinnonOsa()
                        .getKoodiArvo().trim().equals("")) {
                    koodittomatTutkinnonOsat.add(viite.getTutkinnonOsa());
                }
            }
        }
        return koodittomatTutkinnonOsat;
    }

    private Collection<TutkinnonOsaViite> getViitteet(Suoritustapa suoritustapa) {
        return suoritustapa.getTutkinnonOsat();
    }

    @Transactional
    @Override
    public List<TyoryhmaHenkiloDto> saveTyoryhma(Long perusteProjektiId, String tyoryhma, List<String> henkilot) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        removeTyoryhma(perusteProjektiId, tyoryhma);
        perusteprojektiTyoryhmaRepository.flush();
        List<PerusteprojektiTyoryhma> res = new ArrayList<>();

        for (String trh : henkilot) {
            res.add(perusteprojektiTyoryhmaRepository.save(new PerusteprojektiTyoryhma(pp, trh, tyoryhma)));
        }
        return mapper.mapAsList(res, TyoryhmaHenkiloDto.class);
    }

    @Transactional
    @Override
    public TyoryhmaHenkiloDto saveTyoryhma(Long perusteProjektiId, TyoryhmaHenkiloDto tr) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        PerusteprojektiTyoryhma ppt = perusteprojektiTyoryhmaRepository.save(new PerusteprojektiTyoryhma(pp,
                tr.getKayttajaOid(), tr.getNimi()));
        return mapper.map(ppt, TyoryhmaHenkiloDto.class);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TyoryhmaHenkiloDto> getTyoryhmaHenkilot(Long perusteProjektiId) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        List<PerusteprojektiTyoryhma> tr = perusteprojektiTyoryhmaRepository.findAllByPerusteprojekti(pp);
        return mapper.mapAsList(tr, TyoryhmaHenkiloDto.class);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TyoryhmaHenkiloDto> getTyoryhmaHenkilot(Long perusteProjektiId, String nimi) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        List<PerusteprojektiTyoryhma> tr = perusteprojektiTyoryhmaRepository.findAllByPerusteprojektiAndNimi(pp, nimi);
        return mapper.mapAsList(tr, TyoryhmaHenkiloDto.class);
    }

    @Transactional
    @Override
    public void removeTyoryhma(Long perusteProjektiId, String nimi) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        perusteprojektiTyoryhmaRepository.deleteAllByPerusteprojektiAndNimi(pp, nimi);
    }

    @Transactional
    @Override
    public List<String> setPerusteenOsaViiteTyoryhmat(Long perusteProjektiId, Long perusteenOsaId, List<String> nimet) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        PerusteenOsa po = perusteenOsaRepository.findOne(perusteenOsaId);
        Set<String> uniques = new HashSet<>(nimet);
        perusteenOsaTyoryhmaRepository.deleteAllByPerusteenosaAndPerusteprojekti(po, pp);
        perusteenOsaTyoryhmaRepository.flush();
        List<String> res = new ArrayList<>();

        for (String nimi : uniques) {
            PerusteenOsaTyoryhma pot = new PerusteenOsaTyoryhma();
            pot.setNimi(nimi);
            pot.setPerusteprojekti(pp);
            pot.setPerusteenosa(po);
            if (perusteprojektiTyoryhmaRepository.findAllByPerusteprojektiAndNimi(pp, nimi).isEmpty()) {
                throw new BusinessRuleViolationException("Perusteprojekti ryhmää ei ole olemassa: " + nimi);
            }
            res.add(perusteenOsaTyoryhmaRepository.save(pot).getNimi());
        }
        return res;
    }

    @Transactional(readOnly = true)
    @Override
    public List<String> getPerusteenOsaViiteTyoryhmat(Long perusteProjektiId, Long perusteenOsaId) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        PerusteenOsa po = perusteenOsaRepository.findOne(perusteenOsaId);
        List<PerusteenOsaTyoryhma> tyoryhmat = perusteenOsaTyoryhmaRepository.findAllByPerusteenosaAndPerusteprojekti(po, pp);
        List<String> res = new ArrayList<>();
        for (PerusteenOsaTyoryhma s : tyoryhmat) {
            res.add(s.getNimi());
        }
        return res;
    }

    @Transactional(readOnly = true)
    @Override
    public List<PerusteenOsaTyoryhmaDto> getSisallonTyoryhmat(Long perusteProjektiId) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        List<PerusteenOsaTyoryhma> tyoryhmat = perusteenOsaTyoryhmaRepository.findAllByPerusteprojekti(pp);
        return mapper.mapAsList(tyoryhmat, PerusteenOsaTyoryhmaDto.class);
    }

}
