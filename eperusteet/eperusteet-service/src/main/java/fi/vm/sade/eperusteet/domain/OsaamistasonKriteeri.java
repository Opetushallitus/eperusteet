package fi.vm.sade.eperusteet.domain;

import java.io.Serializable;
import java.util.*;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

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
        joinColumns = @JoinColumn(name = "osaamistasonkriteerit_id", nullable = false),
        inverseJoinColumns = @JoinColumn(name = "arvioinninkohde_id", nullable = false))
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
