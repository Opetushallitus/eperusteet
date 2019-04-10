package fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Audited
@Table(name = "yl_lops2019_laaja_alainen_osaaminen")
public class Lops2019LaajaAlainenOsaaminen extends AbstractAuditedReferenceableEntity {

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen opinnot;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "yl_lops2019_laaja_alainen_osaaminen_tavoite",
            joinColumns = @JoinColumn(name = "laaja_alainen_osaaminen_id"),
            inverseJoinColumns = @JoinColumn(name = "tavoite_id"))
    @OrderBy("jarjestys, id")
    private List<Lops2019Tavoite> tavoitteet = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "yl_lops2019_laaja_alainen_osaaminen_painopiste",
            joinColumns = @JoinColumn(name = "laaja_alainen_osaaminen_id"),
            inverseJoinColumns = @JoinColumn(name = "painopiste_id"))
    @OrderBy("jarjestys, id")
    private List<Lops2019Painopiste> painopisteet = new ArrayList<>();

    private Integer jarjestys;
}
