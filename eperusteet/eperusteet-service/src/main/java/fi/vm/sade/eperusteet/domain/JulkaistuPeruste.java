package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Immutable
@Getter
@Setter
@Table(name = "julkaistu_peruste")
public class JulkaistuPeruste extends AbstractReferenceableEntity {

    @NotNull
    private int revision;

    @ManyToOne
    @JoinColumn(name = "peruste_id")
    @NotNull
    private Peruste peruste;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private TekstiPalanen tiedote;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "julkinen_tiedote_id")
    private TekstiPalanen julkinenTiedote;

    @Temporal(TemporalType.TIMESTAMP)
    private Date luotu;

    @Getter
    @NotNull
    private String luoja;

    @ElementCollection
    @NotNull
    @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
    private Set<Long> dokumentit = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private JulkaistuPerusteData data;

    @Getter
    @NotNull
    private Boolean julkinen;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "muutosmaarays_voimaan")
    private Date muutosmaaraysVoimaan;

    @Getter
    @OrderColumn
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Muutosmaarays> muutosmaaraykset = new ArrayList<>();

    public void setMuutosmaaraykset(List<Muutosmaarays> muutosmaaraykset) {
        this.muutosmaaraykset.clear();
        if (muutosmaaraykset != null) {
            this.muutosmaaraykset.addAll(muutosmaaraykset);
        }
    }

    @PrePersist
    private void prepersist() {
        luotu = new Date();
        if (luoja == null) {
            luoja = SecurityUtil.getAuthenticatedPrincipal().getName();
        }
    }
}
