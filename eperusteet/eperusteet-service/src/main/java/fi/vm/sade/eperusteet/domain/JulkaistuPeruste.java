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
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
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
    @NotNull
    private TekstiPalanen tiedote;

    @Temporal(TemporalType.TIMESTAMP)
    private Date luotu;

    @Getter
    @NotNull
    private String luoja;

//    @ElementCollection
//    @NotNull
//    @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
//    private Set<Long> dokumentit = new HashSet<>();

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private JulkaistuPerusteData data;

    @PrePersist
    private void prepersist() {
        luotu = new Date();
        luoja = SecurityUtil.getAuthenticatedPrincipal().getName();
    }

}