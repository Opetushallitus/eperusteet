package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.digi.Osaamiskokonaisuus;
import fi.vm.sade.eperusteet.domain.digi.OsaamiskokonaisuusKasitteisto;
import fi.vm.sade.eperusteet.domain.digi.OsaamiskokonaisuusPaaAlue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.ValidointiKategoria;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.util.NavigableLokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.Validator;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.vm.sade.eperusteet.domain.TekstiPalanen.tarkistaTekstipalanen;

@Component
@Transactional
@Slf4j
public class ValidatorDigitaalinenOsaaminen implements Validator {

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Override
    public TilaUpdateStatus validate(Long perusteprojektiId, ProjektiTila targetTila) {
        TilaUpdateStatus updateStatus = new TilaUpdateStatus();

        Perusteprojekti projekti = perusteprojektiRepository.findOne(perusteprojektiId);

        boolean hasNimiKaikillaKielilla = projekti.getPeruste().getKielet().stream()
                .allMatch(kieli -> projekti.getPeruste().getNimi().getTeksti() != null
                        && projekti.getPeruste().getNimi().getTeksti().containsKey(kieli));
        if (!hasNimiKaikillaKielilla) {
            updateStatus.addStatus("perusteen-nimea-ei-ole-kaikilla-kielilla");
            updateStatus.setVaihtoOk(false);
        }
        if (projekti.getPeruste().getVoimassaoloAlkaa() == null) {
            updateStatus.setVaihtoOk(false);
            updateStatus.addStatus("peruste-ei-voimassaolon-alkamisaikaa");
        }
        tarkistaPerusteenSisaltoTekstipalaset(projekti.getPeruste(), updateStatus);
        return updateStatus;
    }

    @Transactional(readOnly = true)
    public void tarkistaPerusteenSisaltoTekstipalaset(Peruste peruste, TilaUpdateStatus updateStatus) {
        Set<Kieli> vaaditutKielet = peruste.getKielet();

        if (peruste.getDigitaalinenOsaaminenSisalto() != null) {
            for (PerusteenOsaViite lapsi : peruste.getDigitaalinenOsaaminenSisalto().getSisalto().getLapset()) {
                tarkistaSisalto(lapsi, vaaditutKielet, updateStatus);
            }
        }
    }

    @Transactional(readOnly = true)
    public void tarkistaSisalto(final PerusteenOsaViite viite, final Set<Kieli> pakolliset, TilaUpdateStatus updateStatus) {
        PerusteenOsa perusteenOsa = viite.getPerusteenOsa();
        if (perusteenOsa instanceof TekstiKappale && (perusteenOsa.getTunniste() == PerusteenOsaTunniste.NORMAALI || perusteenOsa.getTunniste() == null)) {
            TekstiKappale tekstikappale = (TekstiKappale) perusteenOsa;
            Map<String, String> virheellisetKielet = new HashMap<>();
            tarkistaTekstipalanen("peruste-validointi-tekstikappale-nimi", tekstikappale.getNimi(), pakolliset, virheellisetKielet, true);
            tarkistaTekstipalanen("peruste-validointi-tekstikappale-teksti", tekstikappale.getTeksti(), pakolliset, virheellisetKielet, true);

            for (Map.Entry<String, String> entry : virheellisetKielet.entrySet()) {
                updateStatus.addStatus(entry.getKey(), ValidointiKategoria.KIELISISALTO, new NavigableLokalisoituTekstiDto(tekstikappale));
            }
        }

        if (perusteenOsa instanceof Osaamiskokonaisuus) {
            Osaamiskokonaisuus osaamiskokonaisuus = (Osaamiskokonaisuus) perusteenOsa;
            Map<String, String> virheellisetKielet = new HashMap<>();
            tarkistaTekstipalanen("peruste-validointi-osaamiskokonaisuus-nimi", osaamiskokonaisuus.getNimi(), pakolliset, virheellisetKielet, true);
            tarkistaTekstipalanen("peruste-validointi-osaamiskokonaisuus-kuvaus", osaamiskokonaisuus.getKuvaus(), pakolliset, virheellisetKielet, true);

            for (OsaamiskokonaisuusKasitteisto kasitteisto : osaamiskokonaisuus.getKasitteistot()) {
                tarkistaTekstipalanen("peruste-validointi-osaamiskokonaisuus-kasitteisto-kuvaus", kasitteisto.getKuvaus(), pakolliset, virheellisetKielet, true);
                tarkistaTekstipalanen("peruste-validointi-osaamiskokonaisuus-keskeinen-kasitteisto-kuvaus", kasitteisto.getKeskeinenKasitteisto(), pakolliset, virheellisetKielet, true);
            }

            for (Map.Entry<String, String> entry : virheellisetKielet.entrySet()) {
                updateStatus.addStatus(entry.getKey(), ValidointiKategoria.KIELISISALTO, new NavigableLokalisoituTekstiDto(osaamiskokonaisuus));
            }
        }

        if (perusteenOsa instanceof OsaamiskokonaisuusPaaAlue) {
            OsaamiskokonaisuusPaaAlue osaamiskokonaisuusPaaAlue = (OsaamiskokonaisuusPaaAlue) perusteenOsa;
            Map<String, String> virheellisetKielet = new HashMap<>();
            tarkistaTekstipalanen("peruste-validointi-osaamiskokonaisuus-paa-alue-nimi", osaamiskokonaisuusPaaAlue.getNimi(), pakolliset, virheellisetKielet, true);
            tarkistaTekstipalanen("peruste-validointi-osaamiskokonaisuus-paa-alue-kuvaus", osaamiskokonaisuusPaaAlue.getKuvaus(), pakolliset, virheellisetKielet, true);

            osaamiskokonaisuusPaaAlue.getOsaAlueet().forEach(osaAlue -> {
                tarkistaTekstipalanen("peruste-validointi-osaamiskokonaisuus-paa-alue-osa-alue-nimi", osaAlue.getNimi(), pakolliset, virheellisetKielet, true);

                osaAlue.getTasokuvaukset().forEach(tasokuvaus -> {
                    tasokuvaus.getKuvaukset().forEach(kuvaus -> {
                        tarkistaTekstipalanen("peruste-validointi-osaamiskokonaisuus-paa-alue-osa-alue-kuvaus", osaAlue.getNimi(), pakolliset, virheellisetKielet, true);
                    });

                    tasokuvaus.getEdistynytOsaaminenKuvaukset().forEach(kuvaus -> {
                        tarkistaTekstipalanen("peruste-validointi-osaamiskokonaisuuspaa-alue-osa-alue-kuvaus", osaAlue.getNimi(), pakolliset, virheellisetKielet, true);
                    });
                });
            });

            for (Map.Entry<String, String> entry : virheellisetKielet.entrySet()) {
                updateStatus.addStatus(entry.getKey(), ValidointiKategoria.KIELISISALTO, new NavigableLokalisoituTekstiDto(osaamiskokonaisuusPaaAlue));
            }
        }

        for (PerusteenOsaViite lapsi : viite.getLapset()) {
            tarkistaSisalto(lapsi, pakolliset, updateStatus);
        }
    }

    @Override
    public boolean applicableKoulutustyyppi(KoulutusTyyppi tyyppi) {
        return true;
    }

    @Override
    public boolean applicableToteutus(KoulutustyyppiToteutus toteutus) {
        return true;
    }

    @Override
    public boolean applicablePerustetyyppi(PerusteTyyppi tyyppi) {
        return PerusteTyyppi.DIGITAALINEN_OSAAMINEN.equals(tyyppi);
    }
}
