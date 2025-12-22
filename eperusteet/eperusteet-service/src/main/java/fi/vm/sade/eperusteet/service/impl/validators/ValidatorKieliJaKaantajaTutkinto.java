package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.kios.KaantajaTaito;
import fi.vm.sade.eperusteet.domain.kios.KaantajaTaitotasoasteikko;
import fi.vm.sade.eperusteet.domain.maarays.Maarays;
import fi.vm.sade.eperusteet.dto.ValidointiKategoria;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.util.NavigableLokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.MaaraysService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.Validointi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static fi.vm.sade.eperusteet.domain.TekstiPalanen.tarkistaTekstipalanen;

@Component
@Transactional
@Slf4j
public class ValidatorKieliJaKaantajaTutkinto extends ValidatorPeruste {

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private MaaraysService maaraysService;

    @Override
    public List<Validointi> validate(Long perusteprojektiId, ProjektiTila targetTila) {
        Perusteprojekti projekti = perusteprojektiRepository.findById(perusteprojektiId).orElse(null);

        Validointi perusteValidointi = new Validointi(ValidointiKategoria.PERUSTE);
        Validointi sisaltoValidointi = new Validointi(ValidointiKategoria.KIELISISALTO);

        boolean hasNimiKaikillaKielilla = projekti.getPeruste().getKielet().stream()
                .allMatch(kieli -> projekti.getPeruste().getNimi().getTeksti() != null
                        && projekti.getPeruste().getNimi().getTeksti().containsKey(kieli));
        if (!hasNimiKaikillaKielilla) {
            perusteValidointi.virhe("perusteen-nimea-ei-ole-kaikilla-kielilla", NavigationNodeDto.of(NavigationType.tiedot));
        }
        if (projekti.getPeruste().getVoimassaoloAlkaa() == null) {
            perusteValidointi.virhe("peruste-ei-voimassaolon-alkamisaikaa", NavigationNodeDto.of(NavigationType.tiedot));
        }

        if (projekti.getPeruste().getDiaarinumero() == null || !isDiaariValid(projekti.getPeruste().getDiaarinumero())) {
            perusteValidointi.virhe("diaarinumero-ei-validi", NavigationNodeDto.of(NavigationType.tiedot));
        }

        tarkistaPerusteenSisaltoTekstipalaset(projekti.getPeruste(), sisaltoValidointi);
        return Arrays.asList(
                perusteValidointi,
                sisaltoValidointi,
                tarkistaMaarays(projekti.getPeruste())
        );
    }

    private void tarkistaPerusteenSisaltoTekstipalaset(Peruste peruste, Validointi validointi) {
        Set<Kieli> vaaditutKielet = peruste.getKielet();

        if (peruste.getKieliJaKaantajaTutkintoPerusteenSisalto() != null) {
            for (PerusteenOsaViite lapsi : peruste.getKieliJaKaantajaTutkintoPerusteenSisalto().getSisalto().getLapset()) {
                tarkistaSisalto(lapsi, vaaditutKielet, validointi);
            }
        }
    }

    @Override
    public void tarkistaSisalto(final PerusteenOsaViite viite, final Set<Kieli> pakolliset, Validointi validointi) {
        PerusteenOsa perusteenOsa = viite.getPerusteenOsa();
        if (perusteenOsa instanceof TekstiKappale && (perusteenOsa.getTunniste() == PerusteenOsaTunniste.NORMAALI || perusteenOsa.getTunniste() == null)) {
            TekstiKappale tekstikappale = (TekstiKappale) perusteenOsa;
            Map<String, String> virheellisetKielet = new HashMap<>();
            tarkistaTekstipalanen("peruste-validointi-tekstikappale-nimi", tekstikappale.getNimi(), pakolliset, virheellisetKielet, true);
            tarkistaTekstipalanen("peruste-validointi-tekstikappale-teksti", tekstikappale.getTeksti(), pakolliset, virheellisetKielet, true);

            for (Map.Entry<String, String> entry : virheellisetKielet.entrySet()) {
                validointi.virhe(entry.getKey(), new NavigableLokalisoituTekstiDto(tekstikappale).getNavigationNode());
            }
        }

        if (perusteenOsa instanceof KaantajaTaito kaantajaTaito && (perusteenOsa.getTunniste() == PerusteenOsaTunniste.NORMAALI || perusteenOsa.getTunniste() == null)) {
            Map<String, String> virheellisetKielet = new HashMap<>();
            tarkistaTekstipalanen("peruste-validointi-kaantaja-taito-nimi", kaantajaTaito.getNimi(), pakolliset, virheellisetKielet, true);

            if (kaantajaTaito.getKuvaus() != null) {
                tarkistaTekstipalanen("peruste-validointi-kaantaja-taito-kuvaus", kaantajaTaito.getKuvaus(), pakolliset, virheellisetKielet, true);
            }
            tarkistaTekstipalanen("peruste-validointi-kaantaja-taito-sisalto", kaantajaTaito.getValiotsikko(), pakolliset, virheellisetKielet, true);

            kaantajaTaito.getKohdealueet().forEach(kohdealue -> {
                tarkistaTekstipalanen("peruste-validointi-kaantaja-taito-sisalto", kohdealue.getKohdealueOtsikko(), pakolliset, virheellisetKielet, true);
                kohdealue.getTutkintovaatimukset().forEach(vaatimus -> {
                    tarkistaTekstipalanen("peruste-validointi-kaantaja-taito-sisalto", vaatimus, pakolliset, virheellisetKielet, true);
                });
                kohdealue.getArviointikriteerit().forEach(kriteeri -> {
                    tarkistaTekstipalanen("peruste-validointi-kaantaja-taito-sisalto", kriteeri, pakolliset, virheellisetKielet, true);
                });

            });

            for (Map.Entry<String, String> entry : virheellisetKielet.entrySet()) {
                validointi.virhe(entry.getKey(), new NavigableLokalisoituTekstiDto(kaantajaTaito).getNavigationNode());
            }
        }

        if (perusteenOsa instanceof KaantajaTaitotasoasteikko taitotasoasteikko && (perusteenOsa.getTunniste() == PerusteenOsaTunniste.NORMAALI || perusteenOsa.getTunniste() == null)) {
            Map<String, String> virheellisetKielet = new HashMap<>();
            tarkistaTekstipalanen("peruste-validointi-kaantaja-taitotaso-asteikko-nimi", taitotasoasteikko.getNimi(), pakolliset, virheellisetKielet, true);

            if (taitotasoasteikko.getKuvaus() != null) {
                tarkistaTekstipalanen("peruste-validointi-kaantaja-taitotaso-asteikko-kuvaus", taitotasoasteikko.getKuvaus(), pakolliset, virheellisetKielet, true);
            }

            taitotasoasteikko.getTaitotasoasteikkoKategoriat().forEach(kategoria -> {
                tarkistaTekstipalanen("peruste-validointi-kaantaja-taitotaso-asteikko-sisalto", kategoria.getOtsikko(), pakolliset, virheellisetKielet, true);

                kategoria.getTaitotasoasteikkoKategoriaTaitotasot().forEach(taitotaso -> {
                    tarkistaTekstipalanen("peruste-validointi-kaantaja-taitotaso-asteikko-sisalto", taitotaso.getOtsikko(), pakolliset, virheellisetKielet, true);
                    tarkistaTekstipalanen("peruste-validointi-kaantaja-taitotaso-asteikko-sisalto", taitotaso.getKuvaus(), pakolliset, virheellisetKielet, true);
                });
            });

            for (Map.Entry<String, String> entry : virheellisetKielet.entrySet()) {
                validointi.virhe(entry.getKey(), new NavigableLokalisoituTekstiDto(taitotasoasteikko).getNavigationNode());
            }
        }

        for (PerusteenOsaViite lapsi : viite.getLapset()) {
            tarkistaSisalto(lapsi, pakolliset, validointi);
        }
    }

    private Validointi tarkistaMaarays(Peruste peruste) {
        Validointi validointi = new Validointi(ValidointiKategoria.PERUSTE);
        Maarays maarays = mapper.map(maaraysService.getPerusteenMaarays(peruste.getId()), Maarays.class);

        if (maarays != null) {
            Set<Kieli> vaaditutKielet = peruste.getKielet();

            vaaditutKielet.forEach(kieli -> {
                if (peruste.getMaarayskirje() == null || !peruste.getMaarayskirje().getLiitteet().containsKey(kieli)) {
                    validointi.virhe("peruste-validointi-maarays-dokumentti", NavigationNodeDto.of(NavigationType.tiedot));
                }
            });
        }

        return validointi;
    }

    @Override
    public boolean applicablePerustetyyppi(PerusteTyyppi tyyppi) {
        return PerusteTyyppi.KIELI_KAANTAJA_TUTKINTO.equals(tyyppi);
    }
}

