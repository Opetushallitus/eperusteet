package fi.vm.sade.eperusteet.domain;

import java.io.Serializable;
import java.util.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohde;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml.WhitelistType;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "osaamistasonkriteeri")
@Audited
public class OsaamistasonKriteeri implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Osaamistaso osaamistaso;

    @ValidHtml(whitelist = WhitelistType.MINIMAL)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @OrderColumn
    @JoinTable(name = "osaamistasonkriteeri_tekstipalanen",
               joinColumns = @JoinColumn(name = "osaamistasonkriteeri_id"),
               inverseJoinColumns = @JoinColumn(name = "tekstipalanen_id"))
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @BatchSize(size = 25)
    private List<TekstiPalanen> kriteerit = new ArrayList<>();

    @Getter
    @NotAudited
    @RelatesToPeruste
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "arvioinninkohde_osaamistasonkriteeri",
        joinColumns = @JoinColumn(name = "osaamistasonkriteerit_id", updatable = false, nullable = false),
        inverseJoinColumns = @JoinColumn(name = "arvioinninkohde_id", nullable = false, updatable = false))
    private Set<ArvioinninKohde> arvioinninKohteet = new HashSet<>();

    public OsaamistasonKriteeri() {
    }

    public OsaamistasonKriteeri(OsaamistasonKriteeri other) {
        this.osaamistaso = other.osaamistaso;
        this.kriteerit.addAll(other.kriteerit);
    }

    public List<TekstiPalanen> getKriteerit() {
        return new ArrayList<>(kriteerit);
    }

    public void setKriteerit(List<TekstiPalanen> kriteerit) {
        this.kriteerit.clear();
        if (kriteerit != null) {
            for (TekstiPalanen t : kriteerit) {
                if (t != null) {
                    this.kriteerit.add(t);
                }
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.osaamistaso);
        hash = 29 * hash + Objects.hashCode(this.kriteerit);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OsaamistasonKriteeri) {
            final OsaamistasonKriteeri other = (OsaamistasonKriteeri) obj;
            if (!Objects.equals(this.osaamistaso, other.osaamistaso)) {
                return false;
            }
            return Objects.equals(this.kriteerit, other.kriteerit);
        }
        return false;
    }

}
