package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.HistoriaTapahtuma;
import fi.vm.sade.eperusteet.domain.MuokkausTapahtuma;
import fi.vm.sade.eperusteet.domain.PerusteenMuokkaustieto;
import fi.vm.sade.eperusteet.dto.MuokkaustietoKayttajallaDto;
import fi.vm.sade.eperusteet.dto.PerusteenMuokkaustietoDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.repository.PerusteenMuokkaustietoRepository;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.PerusteenMuokkaustietoService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import java.util.Date;
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
    public List<MuokkaustietoKayttajallaDto> getPerusteenMuokkausTietos(Long opsId, Date viimeisinLuontiaika, int lukumaara) {

        List<MuokkaustietoKayttajallaDto> muokkaustiedot = mapper
                .mapAsList(muokkausTietoRepository.findTop10ByPerusteIdAndLuotuBeforeOrderByLuotuDesc(opsId, viimeisinLuontiaika, lukumaara), MuokkaustietoKayttajallaDto.class);

        Map<String, KayttajanTietoDto> kayttajatiedot = kayttajanTietoService
                .haeKayttajatiedot(muokkaustiedot.stream().map(MuokkaustietoKayttajallaDto::getMuokkaaja).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(kayttajanTieto -> kayttajanTieto.getOidHenkilo(), kayttajanTieto -> kayttajanTieto));

        muokkaustiedot.forEach(muokkaustieto -> muokkaustieto.setKayttajanTieto(kayttajatiedot.get(muokkaustieto.getMuokkaaja())));

        return muokkaustiedot;
    }

    @Override
    public void addOpsMuokkausTieto(Long opsId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma) {
        addOpsMuokkausTieto(opsId, historiaTapahtuma, muokkausTapahtuma, historiaTapahtuma.getNavigationType(), null);
    }

    @Override
    public void addOpsMuokkausTieto(Long opsId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma, String lisatieto) {
        addOpsMuokkausTieto(opsId, historiaTapahtuma, muokkausTapahtuma, historiaTapahtuma.getNavigationType(), lisatieto);
    }

    @Override
    public void addOpsMuokkausTieto(Long opsId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma, NavigationType navigationType) {
        addOpsMuokkausTieto(opsId, historiaTapahtuma, muokkausTapahtuma, navigationType, null);
    }

    @Override
    public void addOpsMuokkausTieto(Long opsId, HistoriaTapahtuma historiaTapahtuma, MuokkausTapahtuma muokkausTapahtuma, NavigationType navigationType, String lisatieto) {
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
                    .perusteId(opsId)
                    .nimi(historiaTapahtuma.getNimi())
                    .tapahtuma(muokkausTapahtuma)
                    .muokkaaja(SecurityUtil.getAuthenticatedPrincipal().getName())
                    .kohde(navigationType)
                    .kohdeId(historiaTapahtuma.getId())
                    .luotu(historiaTapahtuma.getMuokattu())
                    .lisatieto(lisatieto)
                    .poistettu(Objects.equals(muokkausTapahtuma.getTapahtuma(), MuokkausTapahtuma.POISTO.toString()))
                    .build();

            muokkausTietoRepository.save(muokkaustieto);
        } catch (RuntimeException e) {
            log.error("Historiatiedon lisääminen epäonnistui", e);
        }
    }
}

