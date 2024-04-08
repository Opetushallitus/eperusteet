package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.maarays.Maarays;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
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

    @NotNull
    private String luoja;

    @ElementCollection
    @NotNull
    private Set<Long> dokumentit = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private JulkaistuPerusteData data;

    @NotNull
    private Boolean julkinen;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "muutosmaarays_voimaan")
    private Date muutosmaaraysVoimaan;

    @OneToMany(mappedBy = "julkaistuPeruste", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JulkaisuLiite> liitteet = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "muutosmaarays_id")
    private Maarays muutosmaarays;

    public void setLiitteet(List<JulkaisuLiite> liitteet) {
        this.liitteet.clear();
        if (liitteet != null) {
            this.liitteet.addAll(liitteet);
        }
    }

    @PrePersist
    private void prepersist() {
        if (luotu == null) {
            luotu = new Date();
        }
        if (luoja == null) {
            luoja = SecurityUtil.getAuthenticatedPrincipal().getName();
        }
    }
}
