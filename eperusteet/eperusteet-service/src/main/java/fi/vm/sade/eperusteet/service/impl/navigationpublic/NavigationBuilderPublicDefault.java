package fi.vm.sade.eperusteet.service.impl.navigationpublic;

import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenSisaltoDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.tuva.KoulutuksenOsaDto;
import fi.vm.sade.eperusteet.dto.vst.KotoKielitaitotasoDto;
import fi.vm.sade.eperusteet.dto.vst.KotoLaajaAlainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.vst.KotoOpintoDto;
import fi.vm.sade.eperusteet.dto.vst.OpintokokonaisuusDto;
import fi.vm.sade.eperusteet.service.NavigationBuilderPublic;
import fi.vm.sade.eperusteet.service.PerusteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class NavigationBuilderPublicDefault implements NavigationBuilderPublic {

    private final PerusteService perusteService;

    @Autowired
    public NavigationBuilderPublicDefault(PerusteService perusteService) {
        this.perusteService = perusteService;
    }

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Collections.emptySet();
    }

    public NavigationNodeDto constructNavigation(PerusteenOsaViiteDto.Laaja sisalto) {
        PerusteenOsaDto.Laaja po = sisalto.getPerusteenOsa();
        NavigationType type = getNavigationType(po, sisalto.getLapset());

        NavigationNodeDto result = NavigationNodeDto
                .of(type, getPerusteenOsaNimi(sisalto.getPerusteenOsa()), sisalto.getId()).meta("koodi", getPerusteenosaMetaKoodi(sisalto.getPerusteenOsa()))
                .addAll(sisalto.getLapset().stream()
                        .map(this::constructNavigation)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
        result.setId(sisalto.getId());
        return result;
    }

    private NavigationType getNavigationType(PerusteenOsaDto.Laaja po, List<PerusteenOsaViiteDto.Laaja> lapset) {
        NavigationType type = NavigationType.viite;
        if (po == null) {
            return type;
        }

        if (isTekstikappaleLiite(po)) {
            return NavigationType.liite;
        }

        if (lapset.stream().anyMatch(this::isLinkkisivuType)) {
            return NavigationType.linkkisivu;
        }

        if (!(po instanceof TekstiKappaleDto)) {
            return po.getNavigationType();
        }

        TekstiKappaleDto tk = (TekstiKappaleDto) po;
        if (PerusteenOsaTunniste.RAKENNE.equals(tk.getTunniste())) {
            return NavigationType.muodostuminen;
        }

        if (PerusteenOsaTunniste.LAAJAALAINENOSAAMINEN.equals(tk.getTunniste())) {
            return NavigationType.aipe_laajaalaisetosaamiset;
        }

        return type;
    }

    private boolean isTekstikappaleLiite(PerusteenOsaDto.Laaja po) {
        return po instanceof TekstiKappaleDto && ((TekstiKappaleDto) po).getLiite() != null && ((TekstiKappaleDto) po).getLiite();
    }

    /**
     * Jos Navigaationoden yksikin lapsi on tiettyä ennalta määritettyä tyyppiä,
     * laitetaan parent noden tyypiksi linkkisivu.
     */
    private boolean isLinkkisivuType(PerusteenOsaViiteDto.Laaja lapsi) {
        if (lapsi.getPerusteenOsa() == null) {
            return false;
        }

        return lapsi.getPerusteenOsa() instanceof KotoKielitaitotasoDto ||
               lapsi.getPerusteenOsa() instanceof KotoOpintoDto ||
               lapsi.getPerusteenOsa() instanceof KotoLaajaAlainenOsaaminenDto ||
               lapsi.getPerusteenOsa() instanceof OpintokokonaisuusDto ||
               lapsi.getPerusteenOsa() instanceof KoulutuksenOsaDto;
    }

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId, String kieli, boolean esikatselu, Integer julkaisuRevisio) {
        PerusteKaikkiDto peruste = perusteService.getJulkaistuSisalto(perusteId, julkaisuRevisio, esikatselu);

        Set<PerusteenSisaltoDto> sisallot = peruste.getSisallot();
        NavigationNodeDto result = NavigationNodeDto.of(NavigationType.root, peruste.getNimi());

        if (sisallot.isEmpty()) {
            return result;
        }

        PerusteenSisaltoDto sisalto = sisallot.iterator().next();
        if (sisalto == null) {
            return result;
        }

        PerusteenOsaViiteDto.Laaja sisaltoViite = sisalto.getSisalto();
        if (sisaltoViite != null) {
            result.addAll(constructNavigation(sisaltoViite));
        }

        return result;
    }
}
