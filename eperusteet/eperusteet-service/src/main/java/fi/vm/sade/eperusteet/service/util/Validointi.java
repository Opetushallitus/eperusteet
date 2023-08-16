package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.ValidointiKategoria;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.service.exception.ValidointiException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@EqualsAndHashCode
public class Validointi {

    private ValidointiKategoria kategoria;
    private List<Virhe> virheet = new ArrayList<>();
    private List<Virhe> huomautukset = new ArrayList<>();
    private List<Virhe> huomiot = new ArrayList<>();

    public Validointi(ValidointiKategoria kategoria) {
        this.kategoria = kategoria;
    }

    @Getter
    @EqualsAndHashCode
    @AllArgsConstructor
    @Builder
    static public class Virhe {
        private String kuvaus;
        private Map<Kieli, String> nimi;
        private NavigationNodeDto navigationNode;
    }

    public void addVirhe(Virhe virhe) {
        if (!virheet.contains(virhe)) {
            virheet.add(virhe);
        }
    }

    public void addHuomautus(Virhe virhe) {
        if (!huomautukset.contains(virhe)) {
            huomautukset.add(virhe);
        }
    }

    public Validointi virhe(String kuvaus, NavigationNodeDto navigationNode) {
        addVirhe(new Virhe(kuvaus, null, navigationNode));
        return this;
    }

    public Validointi virhe(String kuvaus, NavigationNodeDto navigationNode, Map<Kieli, String> nimi) {
        addVirhe(new Virhe(kuvaus, nimi, navigationNode));
        return this;
    }

    public Validointi huomautukset(String kuvaus, NavigationNodeDto navigationNode) {
        huomautukset.add(new Virhe(kuvaus, null, navigationNode));
        return this;
    }

    public Validointi huomautukset(String kuvaus, NavigationNodeDto navigationNode, Map<Kieli, String> nimi) {
        huomautukset.add(new Virhe(kuvaus, nimi, navigationNode));
        return this;
    }

    public void tuomitse() {
        if (!virheet.isEmpty()) {
            throw new ValidointiException(this);
        }
    }

    public boolean virheellinen() {
        return !virheet.isEmpty();
    }
}
