package fi.vm.sade.eperusteet.domain.arviointi;

import fi.vm.sade.eperusteet.domain.OsaamistasonKriteeri;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidArvioinninKohde;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml.WhitelistType;
import static fi.vm.sade.eperusteet.service.util.Util.refXnor;
import java.io.Serializable;
import java.util.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "arvioinninkohde")
@ValidArvioinninKohde
@Audited
public class ArvioinninKohde implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @ValidHtml(whitelist = WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen otsikko;

    @ValidHtml(whitelist = WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen selite;

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private ArviointiAsteikko arviointiAsteikko;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    @Getter
    @BatchSize(size = 10)
    private Set<OsaamistasonKriteeri> osaamistasonKriteerit = new HashSet<>();

    @Getter
    @NotAudited
    @RelatesToPeruste
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "arvioinninkohdealue_arvioinninkohde",
            inverseJoinColumns = @JoinColumn(name = "arvioinninkohdealue_id"),
            joinColumns = @JoinColumn(name = "arvioinninkohde_id"))
    private Set<ArvioinninKohdealue> arvioinninKohdealueet = new HashSet<>();

    public ArvioinninKohde() {
    }

    public ArvioinninKohde(ArvioinninKohde other) {
        this.otsikko = other.getOtsikko();
        this.selite = other.getSelite();
        this.arviointiAsteikko = other.getArviointiAsteikko();
        for (OsaamistasonKriteeri k : other.getOsaamistasonKriteerit()) {
            this.osaamistasonKriteerit.add(new OsaamistasonKriteeri(k));
        }
    }

    public void setOsaamistasonKriteerit(Set<OsaamistasonKriteeri> osaamistasonKriteerit) {
        this.osaamistasonKriteerit.clear();
        if (osaamistasonKriteerit != null) {
            this.osaamistasonKriteerit.addAll(osaamistasonKriteerit);
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.otsikko);
        hash = 41 * hash + Objects.hashCode(this.arviointiAsteikko);
        hash = 41 * hash + Objects.hashCode(this.osaamistasonKriteerit);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ArvioinninKohde) {
            final ArvioinninKohde other = (ArvioinninKohde) obj;
            if (!Objects.equals(this.otsikko, other.otsikko)) {
                return false;
            }
            if (!Objects.equals(this.selite, other.selite)) {
                return false;
            }
            if (!Objects.equals(this.arviointiAsteikko, other.arviointiAsteikko)) {
                return false;
            }
            return Objects.equals(this.osaamistasonKriteerit, other.osaamistasonKriteerit);
        }
        return false;
    }

    public boolean structureEquals(ArvioinninKohde other) {
        if (this == other) {
            return true;
        }
        boolean result = refXnor(getOtsikko(), other.getOtsikko());
        result &= Objects.equals(getArviointiAsteikko(), other.getArviointiAsteikko());
        result &= refXnor(getOsaamistasonKriteerit(), other.getOsaamistasonKriteerit());
        return result;
    }

}
