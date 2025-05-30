package fi.vm.sade.eperusteet.domain.maarays;

import fi.vm.sade.eperusteet.domain.AbstractAuditedEntity;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "maarays")
@Setter
@Getter
@NoArgsConstructor
public class Maarays extends AbstractAuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private TekstiPalanen nimi;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private TekstiPalanen kuvaus;

    private String diaarinumero;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "voimassaolo_alkaa")
    private Date voimassaoloAlkaa;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "voimassaolo_loppuu")
    private Date voimassaoloLoppuu;

    @Temporal(TemporalType.TIMESTAMP)
    private Date maarayspvm;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MaaraysTyyppi tyyppi;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MaaraysLiittyyTyyppi liittyyTyyppi = MaaraysLiittyyTyyppi.EI_LIITY;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tila")
    private MaaraysTila tila = MaaraysTila.LUONNOS;

    @ElementCollection
    @CollectionTable(name="maarays_koulutustyypit")
    private List<String> koulutustyypit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "peruste_id")
    private Peruste peruste;

    @ManyToMany
    @JoinTable(name = "maarays_korvattavat",  joinColumns = {@JoinColumn(name = "maarays_id")}, inverseJoinColumns = {@JoinColumn(name = "korvattavatmaaraykset_id")})
    private List<Maarays> korvattavatMaaraykset = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "maarays_muutettavat",  joinColumns = {@JoinColumn(name = "maarays_id")}, inverseJoinColumns = {@JoinColumn(name = "muutettavatmaaraykset_id")})
    private List<Maarays> muutettavatMaaraykset = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "maarays_asiasanat")
    @MapKeyEnumerated(EnumType.STRING)
    private Map<Kieli, MaaraysAsiasana> asiasanat = new HashMap<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "maarays_liitteet")
    @MapKeyEnumerated(EnumType.STRING)
    private Map<Kieli, MaaraysKieliLiitteet> liitteet = new HashMap<>();

    public void setKorvattavatMaaraykset(List<Maarays> korvattavatMaaraykset) {
        this.korvattavatMaaraykset.clear();
        if (korvattavatMaaraykset != null) {
            this.korvattavatMaaraykset.addAll(korvattavatMaaraykset);
        }
    }

    public void setMuutettavatMaaraykset(List<Maarays> muutettavatMaaraykset) {
        this.muutettavatMaaraykset.clear();
        if (muutettavatMaaraykset != null) {
            this.muutettavatMaaraykset.addAll(muutettavatMaaraykset);
        }
    }

    public Maarays copy() {
        Maarays copy = new Maarays();

        if (nimi != null) {
            copy.setNimi(TekstiPalanen.of(nimi.getTeksti()));
        }

        if (kuvaus != null) {
            copy.setKuvaus(TekstiPalanen.of(kuvaus.getTeksti()));
        }

        copy.setDiaarinumero(diaarinumero);
        copy.setVoimassaoloAlkaa(voimassaoloAlkaa);
        copy.setVoimassaoloLoppuu(voimassaoloLoppuu);
        copy.setMaarayspvm(maarayspvm);
        copy.setTyyppi(tyyppi);
        copy.setLiittyyTyyppi(liittyyTyyppi);
        copy.setTila(tila);
        copy.setKoulutustyypit(new ArrayList<>());
        copy.getKoulutustyypit().addAll(koulutustyypit);
        copy.getKorvattavatMaaraykset().addAll(korvattavatMaaraykset);
        copy.getMuutettavatMaaraykset().addAll(muutettavatMaaraykset);

        if (asiasanat != null) {
            asiasanat.keySet().forEach(kieli -> copy.asiasanat.put(kieli, asiasanat.get(kieli).copy()));
        }

        if (liitteet != null) {
            liitteet.keySet().forEach(kieli -> copy.liitteet.put(kieli, liitteet.get(kieli).copy()));
        }

        return copy;
    }
}
