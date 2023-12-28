package fi.vm.sade.eperusteet.domain.maarays;

import fi.vm.sade.eperusteet.domain.AbstractAuditedEntity;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyClass;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
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
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private TekstiPalanen nimi;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
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
    @JoinTable(name = "maarays_korvattavat",  joinColumns = {@JoinColumn(name = "korvattavatmaaraykset_id")}, inverseJoinColumns = {@JoinColumn(name = "maarays_id")})
    private List<Maarays> korvaavatMaaraykset = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "maarays_muutettavat",  joinColumns = {@JoinColumn(name = "maarays_id")}, inverseJoinColumns = {@JoinColumn(name = "muutettavatmaaraykset_id")})
    private List<Maarays> muutettavatMaaraykset = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "maarays_muutettavat",  joinColumns = {@JoinColumn(name = "muutettavatmaaraykset_id")}, inverseJoinColumns = {@JoinColumn(name = "maarays_id")})
    private List<Maarays> muuttavatMaaraykset = new ArrayList<>();

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
}
