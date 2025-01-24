package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.domain.AIPEOpetuksenSisalto;
import fi.vm.sade.eperusteet.domain.AbstractReferenceableEntity;
import fi.vm.sade.eperusteet.domain.HistoriaTapahtuma;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Audited
@Table(name="yl_laajaalainen_osaaminen")
public class LaajaalainenOsaaminen extends AbstractReferenceableEntity implements AIPEJarjestettava, HistoriaTapahtuma {

    @NotNull
    @Column(updatable = false)
    @Getter
    private UUID tunniste = UUID.randomUUID();

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    private TekstiPalanen nimi;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    private TekstiPalanen kuvaus;

    @Getter
    @Setter
    private Integer jarjestys;

    @Column
    @Getter
    @Setter
    @Temporal(TemporalType.TIMESTAMP)
    private Date muokattu;

    @Getter
    @NotAudited
    @RelatesToPeruste
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "yl_perusop_perusteen_sisalto_yl_laajaalainen_osaaminen",
            joinColumns = @JoinColumn(name = "laajaalaisetosaamiset_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "yl_perusop_perusteen_sisalto_id", nullable = false))
    private Set<PerusopetuksenPerusteenSisalto> perusopetuksenPerusteenSisallot = new HashSet<>();


    @Getter
    @NotAudited
    @RelatesToPeruste
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "yl_aipe_opetuksensisalto_yl_laajaalainen_osaaminen",
            joinColumns = @JoinColumn(name = "laajaalaisetosaamiset_id",nullable = false),
            inverseJoinColumns = @JoinColumn(name = "yl_aipe_opetuksensisalto_id", nullable = false))
    private Set<AIPEOpetuksenSisalto> aipeSisallot = new HashSet<>();

    @Getter
    @NotAudited
    @RelatesToPeruste
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "yl_opetuksen_tavoite_yl_laajaalainen_osaaminen",
            joinColumns = @JoinColumn(name = "laajattavoitteet_id",nullable = false),
            inverseJoinColumns = @JoinColumn(name = "yl_opetuksen_tavoite_id", nullable = false))
    private Set<OpetuksenTavoite> opetuksenTavoitteet = new HashSet<>();

    public LaajaalainenOsaaminen kloonaa() {
        LaajaalainenOsaaminen uusiLaaja = new LaajaalainenOsaaminen();
        uusiLaaja.setKuvaus(kuvaus);
        uusiLaaja.setNimi(nimi);
        return uusiLaaja;
    }

    @PrePersist
    private void prepersist() {
        muokattu = new Date();
    }

    @PreUpdate
    protected void preupdate() {
        muokattu = new Date();
    }

    @Override
    public NavigationType getNavigationType() {
        return perusopetuksenPerusteenSisallot != null ? NavigationType.perusopetuslaajaalainenosaaminen : NavigationType.aipe_laajaalainenosaaminen;
    }
}
