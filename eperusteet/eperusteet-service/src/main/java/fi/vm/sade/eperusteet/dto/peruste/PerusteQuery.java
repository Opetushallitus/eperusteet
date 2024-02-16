package fi.vm.sade.eperusteet.dto.peruste;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerusteQuery {
    private int sivu = 0;
    private int sivukoko = 25;
    private boolean tuleva = true;
    private boolean siirtyma = true;
    private boolean voimassaolo = true;
    private boolean poistunut = true;
    private String nimi;
    private List<String> koulutusala;
    private List<String> koulutustyyppi;
    private Set<String> kieli;
    private List<String> opintoala;
    private String suoritustapa;
    private Set<String> tila;
    private boolean julkaistu;
    private String koulutuskoodi;
    private String perusteTyyppi;
    private String diaarinumero;
    private Long muokattu;
    private String jarjestys;
    private boolean tutkintonimikkeet = false;
    private boolean tutkinnonosat = false;
    private boolean osaamisalat = false;
    private KoulutusVientiEhto koulutusvienti = KoulutusVientiEhto.FALSE;
    private long nykyinenAika = new Date().getTime();
    private Boolean esikatseltavissa;
    private List<String> tutkinnonosaKoodit;
    private List<String> osaamisalaKoodit;

    public void setTyyppi(List<String> tyyppi) {
        this.koulutustyyppi = tyyppi;
    }

    public List<String> getTyyppi() {
        return this.koulutustyyppi;
    }

    public void setTila(String tila) {
        this.tila = new HashSet<>();
        this.tila.add(tila);
    }

    public void setTila(Set<String> tila) {
        this.tila = tila;
    }

    public void setKoulutusvienti(String koulutusvienti) {
        this.koulutusvienti = KoulutusVientiEhto.of(koulutusvienti);
    }

    public boolean isKoulutusvienti() {
        return koulutusvienti == KoulutusVientiEhto.TRUE || koulutusvienti == KoulutusVientiEhto.KAIKKI;
    }
}
