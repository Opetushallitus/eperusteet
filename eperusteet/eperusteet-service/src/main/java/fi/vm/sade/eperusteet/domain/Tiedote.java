package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.validation.ValidHtml;

import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import javax.persistence.*;

import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * @author mikkom
 */
@Entity
@Table(name="tiedote")
@Audited
public class Tiedote extends AbstractAuditedEntity {
    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Getter
    @Setter
    private boolean julkinen;

    @Getter
    @Setter
    private boolean yleinen; // Jos tiedoite on tarkoitettu etusivulle

    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen otsikko;

    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen sisalto;

    @Deprecated
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    private Perusteprojekti perusteprojekti;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    @CollectionTable(name = "tiedote_julkaisupaikka")
    @Column(name = "julkaisupaikka")
    private Set<TiedoteJulkaisuPaikka> julkaisupaikat;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    @CollectionTable(name = "tiedote_koulutustyyppi")
    @Column(name = "koulutustyyppi")
    private Set<KoulutusTyyppi> koulutustyypit;

    @Getter
    @Setter
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tiedote_peruste", joinColumns = {@JoinColumn(name = "tiedote_id")}, inverseJoinColumns = {@JoinColumn(name = "peruste_id")})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Set<Peruste> perusteet;

    @PrePersist
    void onPersist() {
        this.yleinen = this.julkinen && this.yleinen;
    }

    @Override
    @PreUpdate
    public void preupdate() {
        super.preupdate();
    }
}
