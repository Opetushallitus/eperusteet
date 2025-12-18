package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.maarays.Maarays;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysLiiteTyyppi;
import fi.vm.sade.eperusteet.dto.ValidointiKategoria;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.MaaraysService;
import fi.vm.sade.eperusteet.service.Validator;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.Validointi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static fi.vm.sade.eperusteet.domain.TekstiPalanen.tarkistaTekstipalanen;

@Service
@Slf4j
@Transactional
public class ValidatorPerusteTiedot implements Validator {

    @Autowired
    private PerusteprojektiRepository repository;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private MaaraysService maaraysService;

    @Override
    public List<Validointi> validate(Long perusteprojektiId, ProjektiTila targetTila) {
        List<Validointi> validoinnit = new ArrayList<>();
        Validointi perusteValidointi = new Validointi(ValidointiKategoria.PERUSTE);
        validoinnit.add(perusteValidointi);

        Perusteprojekti projekti = repository.findById(perusteprojektiId).orElse(null);
        Peruste peruste = projekti.getPeruste();

        if (projekti.getPeruste().getVoimassaoloAlkaa() == null) {
            perusteValidointi.virhe("peruste-ei-voimassaolon-alkamisaikaa", NavigationNodeDto.of(NavigationType.tiedot));
        }

        Diaarinumero diaarinumero = projekti.getPeruste().getDiaarinumero();

        if (diaarinumero == null || StringUtils.isBlank(diaarinumero.getDiaarinumero())) {
            perusteValidointi.virhe("peruste-ei-diaarinumeroa", NavigationNodeDto.of(NavigationType.tiedot));
        }

        if (!isDiaariValid(peruste.getDiaarinumero())) {
            perusteValidointi.virhe("diaarinumero-ei-validi", NavigationNodeDto.of(NavigationType.tiedot));
        }

        validoinnit.add(tarkistaMaarays(projekti.getPeruste()));
        validoinnit.add(tarkistaMuutosmaaraykset(projekti.getPeruste()));

        return validoinnit;
    }

    private Validointi tarkistaMaarays(Peruste peruste) {
        Validointi validointi = new Validointi(ValidointiKategoria.PERUSTE);
        Maarays maarays = mapper.map(maaraysService.getPerusteenMaarays(peruste.getId()), Maarays.class);

        if (maarays != null) {
            Set<Kieli> vaaditutKielet = peruste.getKielet();
            Map<String, String> virheellisetKielet = new HashMap<>();
            tarkistaTekstipalanen("peruste-validointi-maarays-kuvaus", maarays.getKuvaus(), vaaditutKielet, virheellisetKielet, true);
            for (Map.Entry<String, String> entry : virheellisetKielet.entrySet()) {
                validointi.virhe(entry.getKey(), NavigationNodeDto.of(NavigationType.tiedot));
            }

            vaaditutKielet.forEach(kieli -> {
                if (peruste.getMaarayskirje() == null || !peruste.getMaarayskirje().getLiitteet().containsKey(kieli)) {
                    validointi.virhe("peruste-validointi-maarays-dokumentti", NavigationNodeDto.of(NavigationType.tiedot));
                }
            });
        }

        return validointi;
    }

    private Validointi tarkistaMuutosmaaraykset(Peruste peruste) {
        Validointi validointi = new Validointi(ValidointiKategoria.PERUSTE);

        maaraysService.getPerusteenMuutosmaaraykset(peruste.getId()).forEach(muutosmaarays -> {
            peruste.getKielet().forEach(kieli -> {
                if (muutosmaarays.getLiitteet().get(kieli) == null
                        || muutosmaarays.getLiitteet().get(kieli).getLiitteet().isEmpty()
                        || muutosmaarays.getLiitteet().get(kieli).getLiitteet()
                        .stream().filter(liite -> liite.getTyyppi().equals(MaaraysLiiteTyyppi.MAARAYSDOKUMENTTI)).collect(Collectors.toList()).isEmpty()) {
                    validointi.huomautukset("peruste-validointi-muutosmaarays-dokumentti-kieli-puute", NavigationNodeDto.of(NavigationType.tiedot));
                }
            });
        });

        return validointi;
    }

    @Override
    public boolean isDiaariValid(Diaarinumero diaari) {
        if (diaari == null) {
            return true;
        }
        String diaarinumero = diaari.getDiaarinumero();
        return diaarinumero == null
                || "".equals(diaarinumero)
                || "amosaa/yhteiset".equals(diaarinumero)
                || Pattern.matches("^\\d{1,3}/\\d{3}/\\d{4}$", diaarinumero)
                || Pattern.matches("^OPH-\\d{1,5}-\\d{4}$", diaarinumero);
    }

    @Override
    public boolean applicablePerustetyyppi(PerusteTyyppi tyyppi) {
        return tyyppi.equals(PerusteTyyppi.NORMAALI);
    }

    @Override
    public boolean applicableKoulutustyyppi(KoulutusTyyppi tyyppi) {
        return true;
    }

    @Override
    public boolean applicableToteutus(KoulutustyyppiToteutus toteutus) {
        return true;
    }
}
