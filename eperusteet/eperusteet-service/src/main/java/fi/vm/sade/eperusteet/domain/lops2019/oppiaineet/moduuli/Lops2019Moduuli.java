package fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.moduuli;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Audited
@Table(name = "yl_lops2019_moduuli")
public class Lops2019Moduuli extends AbstractAuditedReferenceableEntity {

    @NotNull
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @NotNull
    private Boolean pakollinen;

    @Column(precision = 10, scale = 2)
    private BigDecimal laajuus;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    private Koodi koodi;

    @JoinColumn(name = "tavoitteet_id")
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Lops2019ModuuliTavoite tavoitteet;

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "yl_lops2019_oppiaine_moduuli_sisalto",
            joinColumns = @JoinColumn(name = "moduuli_id"),
            inverseJoinColumns = @JoinColumn(name = "sisalto_id"))
    @OrderBy("id")
    private List<Lops2019ModuuliSisalto> sisallot = new ArrayList<>();

    private Integer jarjestys;

    @Getter
    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(name = "yl_lops2019_oppiaine_moduuli",
            joinColumns = {@JoinColumn(name = "moduuli_id", insertable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "oppiaine_id", insertable = false, updatable = false)})
    private Lops2019Oppiaine oppiaine; // Moduuli viittaus oppiaineeseen/oppimäärään

}
