package fi.vm.sade.eperusteet.domain.lops2019.oppiaineet;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.moduuli.Lops2019Moduuli;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Audited
@Table(name = "yl_lops2019_oppiaine")
public class Lops2019Oppiaine extends AbstractAuditedReferenceableEntity {

    @Getter
    @Setter
    @NotNull
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    private Koodi koodi;

    @Getter
    @Setter
    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "yl_lops2019_oppiaine_moduuli",
            joinColumns = @JoinColumn(name = "oppiaine_id"),
            inverseJoinColumns = @JoinColumn(name = "moduuli_id"))
    @OrderBy("jarjestys, id")
    private List<Lops2019Moduuli> moduulit = new ArrayList<>();

    @Getter
    @Setter
    @JoinColumn(name="arviointi_id")
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Lops2019Arviointi arviointi;

    @Getter
    @Setter
    @JoinColumn(name="tehtava_id")
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Lops2019Tehtava tehtava;

    @Getter
    @Setter
    @OrderColumn
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "yl_lops2019_oppiaine_laaja_alaiset_osaamiset",
            joinColumns = @JoinColumn(name = "oppiaine_id"),
            inverseJoinColumns = @JoinColumn(name = "laaja_alainen_osaaminen_id"))
    private List<Lops2019OppiaineLaajaAlainenOsaaminen> laajaAlaisetOsaamiset = new ArrayList<>();

    @Getter
    @Setter
    @JoinColumn(name = "tavoitteet_id")
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Lops2019OppiaineTavoitteet tavoitteet;

    public void setOppimaarat(List<Lops2019Oppiaine> oppimaarat) {
        if (this.oppimaarat == null) {
            this.oppimaarat = new ArrayList<>();
        }
        this.oppimaarat.clear();
        this.oppimaarat.addAll(oppimaarat);
    }

    @Getter
    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "yl_lops2019_oppiaine_oppimaara",
            joinColumns = @JoinColumn(name = "oppiaine_id"),
            inverseJoinColumns = @JoinColumn(name = "oppimaara_id"))
    @OrderBy("jarjestys, id")
    private List<Lops2019Oppiaine> oppimaarat = new ArrayList<>();

    @Getter
    @Setter
    private Integer jarjestys;

    @Getter
    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(name = "yl_lops2019_oppiaine_oppimaara",
            joinColumns = {@JoinColumn(name = "oppimaara_id", insertable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "oppiaine_id", insertable = false, updatable = false)})
    private Lops2019Oppiaine oppiaine; // Oppimäärän viittaus oppiaineeseen

}
