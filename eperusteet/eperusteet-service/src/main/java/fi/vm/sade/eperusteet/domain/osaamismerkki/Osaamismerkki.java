package fi.vm.sade.eperusteet.domain.osaamismerkki;

import fi.vm.sade.eperusteet.domain.AbstractAuditedEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
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
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = NOT_AUDITED)
    private TekstiPalanen nimi;

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
