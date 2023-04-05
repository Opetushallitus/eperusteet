package fi.vm.sade.eperusteet.service.impl;

import com.google.common.base.Throwables;
import fi.vm.sade.eperusteet.domain.HistoriaTapahtuma;
import fi.vm.sade.eperusteet.domain.JulkaistuPeruste;
import fi.vm.sade.eperusteet.domain.MuokkausTapahtuma;
import fi.vm.sade.eperusteet.dto.PerusteenMuokkaustietoDto;
import fi.vm.sade.eperusteet.dto.peruste.MuutostapahtumaDto;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenMuokkaustieto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenMuutostietoDto;
import fi.vm.sade.eperusteet.dto.MuokkaustietoKayttajallaDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenMuokkaustietoRepository;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.PerusteenMuokkaustietoService;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

@Slf4j
@Service
@Transactional
public class PerusteenMuokkaustietoServiceImpl implements PerusteenMuokkaustietoService {

    @Autowired
    private PerusteenMuokkaustietoRepository muokkausTietoRepository;

    @Autowired
    private JulkaisutRepository julkaisutRepository;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private KayttajanTietoService kayttajanTietoService;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Override
    public List<MuokkaustietoKayttajallaDto> getPerusteenMuokkausTietos(Long perusteId, Date viimeisinLuontiaika, int lukumaara) {

        List<MuokkaustietoKayttajallaDto> muokkaustiedot = mapper
                .mapAsList(muokkausTietoRepository.findTop10ByPerusteIdAndLuotuBeforeOrderByLuotuDesc(perusteId, viimeisinLuontiaika, lukumaara), MuokkaustietoKayttajallaDto.class);

        try {
            Map<String, KayttajanTietoDto> kayttajatiedot = kayttajanTietoService
                    .haeKayttajatiedot(muokkaustiedot.stream().map(MuokkaustietoKayttajallaDto::getMuokkaaja).collect(Collectors.toList()))
                    .stream().collect(Collectors.toMap(kayttajanTieto -> kayttajanTieto.getOidHenkilo(), kayttajanTieto -> kayttajanTieto));
            muokkaustiedot.forEach(muokkaustieto -> muokkaustieto.setKayttajanTieto(kayttajatiedot.get(muokkaustieto.getMuokkaaja())));
        } catch (Exception ex) {
            log.error(Throwables.getStackTraceAsString(ex));
        }

        return muokkaustiedot;
    }

    @Override
    @IgnorePerusteUpdateCheck
    public void addMuokkaustieto(Long perusteId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma) {
        addMuokkaustieto(perusteId, historiaTapahtuma, muokkausTapahtuma, historiaTapahtuma.getNavigationType(), null);
    }

    @Override
    public void addMuokkaustieto(Long perusteId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma, String lisatieto) {
        addMuokkaustieto(perusteId, historiaTapahtuma, muokkausTapahtuma, historiaTapahtuma.getNavigationType(), lisatieto);
    }

    @Override
    public void addMuokkaustieto(Long perusteId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma, NavigationType navigationType) {
        addMuokkaustieto(perusteId, historiaTapahtuma, muokkausTapahtuma, navigationType, null);
    }

    @Override
    public void addMuokkaustieto(Long perusteId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma, NavigationType navigationType, String lisatieto) {
        try {
            // Merkataan aiemmat tapahtumat poistetuksi
            if (Objects.equals(muokkausTapahtuma.getTapahtuma(), MuokkausTapahtuma.POISTO.toString())) {
                List<PerusteenMuokkaustieto> aiemminTapahtumat = muokkausTietoRepository
                        .findByKohdeId(historiaTapahtuma.getId()).stream()
                        .peek(tapahtuma -> tapahtuma.setPoistettu(true))
                        .collect(Collectors.toList());
                muokkausTietoRepository.save(aiemminTapahtumat);
            }

            // Lisäään uusi tapahtuma
            PerusteenMuokkaustieto muokkaustieto = PerusteenMuokkaustieto.builder()
                    .perusteId(perusteId)
                    .nimi(historiaTapahtuma.getNimi())
                    .tapahtuma(muokkausTapahtuma)
                    .muokkaaja(SecurityUtil.getAuthenticatedPrincipal().getName())
                    .kohde(navigationType)
                    .kohdeId(historiaTapahtuma.getId())
                    .luotu(new Date())
                    .lisatieto(lisatieto)
                    .poistettu(Objects.equals(muokkausTapahtuma.getTapahtuma(), MuokkausTapahtuma.POISTO.toString()))
                    .build();

            muokkausTietoRepository.save(muokkaustieto);
        } catch (RuntimeException e) {
            log.error("Historiatiedon lisääminen epäonnistui", e);
        }
    }

    @Override
    public List<PerusteenMuutostietoDto> getVersionMuutostiedot(Long perusteId, Integer revision) {
        Peruste peruste = perusteRepository.findOne(perusteId);
        JulkaistuPeruste tarkasteltavaJulkaisu = julkaisutRepository.findFirstByPerusteAndRevisionOrderByIdDesc(peruste, revision);
        JulkaistuPeruste edellinenJulkinenJulkaisu = julkaisutRepository.findFirstByPerusteAndJulkinenAndLuotuBeforeOrderByRevisionDesc(peruste, true, tarkasteltavaJulkaisu.getLuotu());

        if (edellinenJulkinenJulkaisu == null) {
            // ei löydy vanhempaa julkista julkaisua, johon verrata
            return new ArrayList<>();
        }
        return filterTapahtumat(getVersioidenValisetMuutokset(perusteId, edellinenJulkinenJulkaisu.getLuotu(), tarkasteltavaJulkaisu.getLuotu()));
    }

    private List<PerusteenMuokkaustietoDto> getVersioidenValisetMuutokset(Long perusteId, Date edellinenLuotu, Date nykyinenLuotu) {
        List<MuokkausTapahtuma> includeTapahtumat = Arrays.asList(MuokkausTapahtuma.LUONTI, MuokkausTapahtuma.PAIVITYS, MuokkausTapahtuma.POISTO);
        List<NavigationType> excludeTypet = List.of(NavigationType.peruste);
        return mapper.mapAsList(
                muokkausTietoRepository.findByPerusteIdAndLuotuIsBetweenAndTapahtumaIsInAndKohdeNotIn(perusteId, edellinenLuotu, nykyinenLuotu, includeTapahtumat, excludeTypet),
                PerusteenMuokkaustietoDto.class);
    }

    private List<PerusteenMuutostietoDto> filterTapahtumat(List<PerusteenMuokkaustietoDto> muokkaustiedot) {
        // filtteröidään julkaisuvälin tapahtumat tapahtumatyypeittäin
        List<PerusteenMuokkaustietoDto> poistot = filterMuokkaustiedotByTapahtuma(muokkaustiedot, MuokkausTapahtuma.POISTO);
        List<PerusteenMuokkaustietoDto> paivitykset = filterMuokkaustiedotByTapahtuma(muokkaustiedot, MuokkausTapahtuma.PAIVITYS);
        List<PerusteenMuokkaustietoDto> luonnit = filterMuokkaustiedotByTapahtuma(muokkaustiedot, MuokkausTapahtuma.LUONTI);

        // vain relevantit poisto-tapahtumat
        List<PerusteenMuokkaustietoDto> finalPoistot = poistot.stream()
                .filter(tieto -> luonnit.stream().noneMatch(luonti -> luonti.getKohdeId().equals(tieto.getKohdeId())))
                .collect(Collectors.toList());

        // vain relevantit luonti-tapahtumat
        List<PerusteenMuokkaustietoDto> finalLuonnit = luonnit.stream()
                .filter(tieto -> poistot.stream().noneMatch(poisto -> poisto.getKohdeId().equals(tieto.getKohdeId())))
                .collect(Collectors.toList());

        // vain relevantit päivitys-tapahtumat
        List<PerusteenMuokkaustietoDto> finalPaivitykset = paivitykset.stream()
                .filter(tieto -> poistot.stream().noneMatch(poisto -> poisto.getKohdeId().equals(tieto.getKohdeId()))
                        && luonnit.stream().noneMatch(luonti -> luonti.getKohdeId().equals(tieto.getKohdeId())))
                .collect(Collectors.toList());

        // listataan kaikkien filtteröityjen tapahtumien kohteet
        List<NavigationType> navTypes = Stream.of(finalPoistot, finalLuonnit, finalPaivitykset)
                .flatMap(Collection::stream)
                .map(PerusteenMuokkaustietoDto::getKohde)
                .distinct()
                .collect(Collectors.toList());

        List<PerusteenMuutostietoDto> tyypinmukaan = new ArrayList<>();

        // ryhmitellään muutostiedot kohteen mukaan
        navTypes.forEach(kohde -> {
            PerusteenMuutostietoDto muutostieto = new PerusteenMuutostietoDto();
            List<MuutostapahtumaDto> tapahtumat = new ArrayList<>();

            addMuutostapahtuma(filterMuokkaustiedotByTyyppi(finalLuonnit, kohde), MuokkausTapahtuma.LUONTI, tapahtumat);
            addMuutostapahtuma(filterMuokkaustiedotByTyyppi(finalPaivitykset, kohde), MuokkausTapahtuma.PAIVITYS, tapahtumat);
            addMuutostapahtuma(filterMuokkaustiedotByTyyppi(finalPoistot, kohde), MuokkausTapahtuma.POISTO, tapahtumat);

            muutostieto.setKohde(kohde);
            muutostieto.setTapahtumat(tapahtumat);
            tyypinmukaan.add(muutostieto);
        });
        return tyypinmukaan;
    }

    private List<PerusteenMuokkaustietoDto> filterMuokkaustiedotByTyyppi(List<PerusteenMuokkaustietoDto> muokkaustiedot, NavigationType tyyppi) {
        return muokkaustiedot.stream()
                .filter(tieto -> tieto.getKohde().equals(tyyppi))
                .collect(Collectors.toList());
    }

    private List<PerusteenMuokkaustietoDto> filterMuokkaustiedotByTapahtuma(List<PerusteenMuokkaustietoDto> muokkaustiedot, MuokkausTapahtuma tapahtuma) {
        return muokkaustiedot.stream()
                .filter(t -> t.getTapahtuma().equals(tapahtuma) && t.getKohdeId() != null)
                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingLong(PerusteenMuokkaustietoDto::getKohdeId))), ArrayList::new));
    }

    private void addMuutostapahtuma(List<PerusteenMuokkaustietoDto> tiedotTyypeittain, MuokkausTapahtuma muokkausTapahtuma, List<MuutostapahtumaDto> tapahtumat) {
        if (!tiedotTyypeittain.isEmpty()) {
            MuutostapahtumaDto tapahtuma = new MuutostapahtumaDto();
            tapahtuma.setTapahtuma(muokkausTapahtuma);
            tapahtuma.setMuokkaustiedot(tiedotTyypeittain);
            tapahtumat.add(tapahtuma);
        }
    }
}

