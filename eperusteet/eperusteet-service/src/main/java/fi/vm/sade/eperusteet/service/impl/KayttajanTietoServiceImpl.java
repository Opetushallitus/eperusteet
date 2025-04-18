package fi.vm.sade.eperusteet.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanProjektitiedotDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttooikeusKyselyDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import fi.vm.sade.eperusteet.utils.client.OphClientHelper;
import fi.vm.sade.eperusteet.utils.client.RestClientFactory;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpEntity;
import fi.vm.sade.javautils.http.OphHttpRequest;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;

import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import static fi.vm.sade.eperusteet.service.mapping.KayttajanTietoParser.parsiKayttaja;
import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Service
@Profile("!test & !docker")
public class KayttajanTietoServiceImpl implements KayttajanTietoService {

    @Value("${cas.service.oppijanumerorekisteri-service:''}")
    private String onrServiceUrl;

    @Value("${cas.service.kayttooikeus-service:''}")
    private String koServiceUrl;

    private static final String HENKILO_API = "/henkilo/";
    private static final String HENKILOT_BY_LIST = HENKILO_API + "henkilotByHenkiloOidList";
    private static final String VIRKAILIJA_HAKU = "/virkailija/haku";
    private final String KAYTTOOIKEUS_EPERUSTEET = "EPERUSTEET";
    private final List<String> ROLES = Arrays.asList("ADMIN","READ","CRUD","READ_UPDATE");

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    RestClientFactory restClientFactory;

    @Autowired
    OphClientHelper ophClientHelper;

    @PostConstruct
    public void configureMapper() {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public KayttajanTietoDto haeKirjautaunutKayttaja() {
        Principal ap = SecurityUtil.getAuthenticatedPrincipal();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        KayttajanTietoDto kayttaja = hae(ap.getName());
        if (kayttaja == null) { //"fallback" jos integraatio on rikki eikä löydä käyttäjän tietoja
            kayttaja = new KayttajanTietoDto(ap.getName());
        }
        Set<String> oikeudet = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_APP_EPERUSTEET_YLOPS"))
                .filter(auth -> !auth.startsWith("ROLE_APP_EPERUSTEET_AMOSAA"))
                .filter(auth -> auth.startsWith("ROLE_APP_EPERUSTEET"))
                .collect(Collectors.toSet());
        kayttaja.setOikeudet(oikeudet);
        return kayttaja;
    }

    @Override
    @Async
    public Future<KayttajanTietoDto> haeAsync(String oid) {
        return new AsyncResult<>(hae(oid));
    }

    @Override
    @Cacheable("kayttajat")
    public KayttajanTietoDto hae(String oid) {
        if (ObjectUtils.isEmpty(oid)) {
            throw new BusinessRuleViolationException("Haettua käyttäjää ei ole olemassa");
        }

        OphHttpClient client = restClientFactory.get(onrServiceUrl, true);

        String url = onrServiceUrl + HENKILO_API + oid;

        OphHttpRequest request = OphHttpRequest.Builder
                .get(url)
                .build();

        return client.<KayttajanTietoDto>execute(request)
                .handleErrorStatus(SC_UNAUTHORIZED, SC_FORBIDDEN)
                .with(res -> Optional.empty())
                .expectedStatus(SC_OK)
                .mapWith(text -> {
                    try {
                        JsonNode json = mapper.readTree(text);
                        return parsiKayttaja(json);
                    } catch (IOException e) {
                        return null;
                    }
                })
                .orElse(null);
    }

    @Override
    public List<KayttajanTietoDto> haeKayttajatiedot(List<String> oid) {
        return ophClientHelper.postAsList(onrServiceUrl, onrServiceUrl + HENKILOT_BY_LIST, oid, KayttajanTietoDto.class);
    }

    @Override
    public List<KayttajanProjektitiedotDto> haePerusteprojektit(String oid) {
        if (oid == null || oid.isEmpty()) {
            throw new BusinessRuleViolationException("Haettua käyttäjää ei ole olemassa");
        }

        OphHttpClient client = restClientFactory.get(onrServiceUrl, true);
        String url = koServiceUrl + HENKILO_API + oid + "/organisaatiohenkilo";


        List<KayttajanProjektitiedotDto> kpp = new ArrayList<>();

        OphHttpRequest request = OphHttpRequest.Builder
                .get(url)
                .build();

        Optional<List<KayttajanProjektitiedotDto>> unfiltered = client.<List<KayttajanProjektitiedotDto>>execute(request)
                .handleErrorStatus(SC_UNAUTHORIZED)
                .with(res -> Optional.empty())
                .expectedStatus(SC_OK)
                .mapWith(text -> {
                    try {
                        return mapper.readValue(text, new TypeReference<List<KayttajanProjektitiedotDto>>(){});
                    } catch (IOException ex) {
                        return new ArrayList<>();
                    }
                });

        if (unfiltered.isPresent()) {
            for (KayttajanProjektitiedotDto kp : unfiltered.get()) {
                Perusteprojekti pp = perusteprojektiRepository.findOneByRyhmaOid(kp.getOrganisaatioOid());
                if (pp != null) {
                    kp.setPerusteprojekti(pp.getId());
                    kpp.add(kp);
                }
            }
        }
        return kpp;
    }

    @Override
    @Cacheable("kayttajan_projekti")
    public KayttajanProjektitiedotDto haePerusteprojekti(String oid, Long projektiId) {
        if (oid == null || oid.isEmpty()) {
            throw new BusinessRuleViolationException("Haettua käyttäjää ei ole olemassa");
        }

        Perusteprojekti pp = perusteprojektiRepository.findById(projektiId).orElse(null);

        if (pp == null) {
            throw new BusinessRuleViolationException("Käyttäjällä ei ole kyseistä perusteprojektia");
        }

        OphHttpClient client = restClientFactory.get(onrServiceUrl, true);
        String url = koServiceUrl + HENKILO_API + oid + "/organisaatiohenkilo/" + pp.getRyhmaOid();

        OphHttpRequest request = OphHttpRequest.Builder
                .get(url)
                .build();

        Optional<KayttajanProjektitiedotDto> dto = client.<KayttajanProjektitiedotDto>execute(request)
                .handleErrorStatus(SC_UNAUTHORIZED)
                .with(res -> Optional.empty())
                .expectedStatus(SC_OK)
                .mapWith(text -> {
                    try {
                        return mapper.readValue(text, KayttajanProjektitiedotDto.class);
                    } catch (IOException e) {
                        return null;
                    }
                });

        KayttajanProjektitiedotDto kayttajanProjektitiedotDto = null;
        if (dto.isPresent()) {
            kayttajanProjektitiedotDto = dto.get();
            kayttajanProjektitiedotDto.setPerusteprojekti(pp.getId());
        }

        return kayttajanProjektitiedotDto;
    }

    @Override
    public JsonNode getOrganisaatioVirkailijat(String organisaatioOid) {
        OphHttpClient client = restClientFactory.get(koServiceUrl, true);
        String url = koServiceUrl + VIRKAILIJA_HAKU;

        KayttooikeusKyselyDto kayttooikeusKyselyDto = KayttooikeusKyselyDto.builder()
                .kayttooikeudet(ImmutableMap.of(KAYTTOOIKEUS_EPERUSTEET, ROLES))
                .organisaatioOids(Collections.singletonList(organisaatioOid))
                .build();

        try {
            OphHttpEntity entity = new OphHttpEntity.Builder()
                    .content(mapper.writeValueAsString(kayttooikeusKyselyDto))
                    .contentType(ContentType.APPLICATION_JSON)
                    .build();

            OphHttpRequest request = OphHttpRequest.Builder
                    .post(url)
                    .setEntity(entity)
                    .build();

            return client.<JsonNode>execute(request)
                    .expectedStatus(SC_OK)
                    .mapWith(text -> {
                        try {
                            return mapper.readTree(text);
                        } catch (IOException ex) {
                            throw new BusinessRuleViolationException("Organisaation kuuluvien henkilöiden hakeminen epäonnistui", ex);
                        }
                    })
                    .orElse(null);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
