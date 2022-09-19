package fi.vm.sade.eperusteet.service.impl;

import com.google.common.base.Throwables;
import fi.vm.sade.eperusteet.domain.HistoriaTapahtuma;
import fi.vm.sade.eperusteet.domain.MuokkausTapahtuma;
import fi.vm.sade.eperusteet.domain.PerusteenMuokkaustieto;
import fi.vm.sade.eperusteet.dto.MuokkaustietoKayttajallaDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.repository.PerusteenMuokkaustietoRepository;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.PerusteenMuokkaustietoService;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class PerusteenMuokkaustietoServiceImpl implements PerusteenMuokkaustietoService {

    @Autowired
    private PerusteenMuokkaustietoRepository muokkausTietoRepository;

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
}

