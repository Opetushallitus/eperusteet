package fi.vm.sade.eperusteet.domain.osaamismerkki;

import fi.vm.sade.eperusteet.domain.AbstractAuditedEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Table(name = "osaamismerkki")
@Audited
@Getter
@Setter
public class Osaamismerkki extends AbstractAuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = NOT_AUDITED)
    private TekstiPalanen nimi;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @NotNull
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name="kategoria_id")
    private OsaamismerkkiKategoria kategoria;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OsaamismerkkiTila tila = OsaamismerkkiTila.LAADINTA;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "voimassaolo_alkaa")
    @NotNull
    private Date voimassaoloAlkaa;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "voimassaolo_loppuu")
    private Date voimassaoloLoppuu;

    @OrderColumn
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "osaamismerkki_osaamistavoitteet",
            joinColumns = @JoinColumn(name = "osaamismerkki_id"),
            inverseJoinColumns = @JoinColumn(name = "osaamismerkki_osaamistavoite_id"))
    @Audited(targetAuditMode = NOT_AUDITED)
    private List<OsaamismerkkiOsaamistavoite> osaamistavoitteet = new ArrayList<>();

    @OrderColumn
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "osaamismerkki_arviointikriteerit",
            joinColumns = @JoinColumn(name = "osaamismerkki_id"),
            inverseJoinColumns = @JoinColumn(name = "osaamismerkki_arviointikriteeri_id"))
    @Audited(targetAuditMode = NOT_AUDITED)
    private List<OsaamismerkkiArviointikriteeri> arviointikriteerit = new ArrayList<>();

    @Column(name = "koodi_uri")
    private String koodiUri;

    public void setOsaamistavoitteet(List<OsaamismerkkiOsaamistavoite> osaamistavoitteet) {
        this.osaamistavoitteet.clear();
        if (osaamistavoitteet != null) {
            this.osaamistavoitteet.addAll(osaamistavoitteet);
        }
    }

    public void setArviointikriteerit(List<OsaamismerkkiArviointikriteeri> arviointikriteerit) {
        this.arviointikriteerit.clear();
        if (arviointikriteerit != null) {
            this.arviointikriteerit.addAll(arviointikriteerit);
        }
    }
}
