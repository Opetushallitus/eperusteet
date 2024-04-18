package fi.vm.sade.eperusteet.domain.digi;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "osaamiskokonaisuus_osa_alue_tasokuvaus")
@Audited
@Getter
@Setter
@NoArgsConstructor
public class OsaamiskokonaisuusOsaAlueTasoKuvaus extends AbstractAuditedReferenceableEntity {

    @Enumerated(EnumType.STRING)
    @NotNull
    private DigitaalinenOsaaminenTaso taso;

    @OrderColumn
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(name = "osaamiskokonaisuus_osa_alue_tasokuvaus_ed_keh_osaa_join",
            joinColumns = @JoinColumn(name = "osaamiskokonaisuus_osa_alue_tasokuvaus_id"),
            inverseJoinColumns = @JoinColumn(name = "edelleenKehittyvatOsaamiset_id"))
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private List<TekstiPalanen> edelleenKehittyvatOsaamiset = new ArrayList<>();

    @OrderColumn
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(name = "osaamiskokonaisuus_osa_alue_tasokuvaus_osaamiset_join",
            joinColumns = @JoinColumn(name = "osaamiskokonaisuus_osa_alue_tasokuvaus_id"),
            inverseJoinColumns = @JoinColumn(name = "osaamiset_id"))
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private List<TekstiPalanen> osaamiset = new ArrayList<>();

    @OrderColumn
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(name = "osaamiskokonaisuus_osa_alue_tasokuvaus_edistynytkuvaus_join",
            joinColumns = @JoinColumn(name = "osaamiskokonaisuus_osa_alue_tasokuvaus_id"),
            inverseJoinColumns = @JoinColumn(name = "edistynytOsaaminenKuvaukset_id"))
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private List<TekstiPalanen> edistynytOsaaminenKuvaukset = new ArrayList<>();

    public OsaamiskokonaisuusOsaAlueTasoKuvaus(OsaamiskokonaisuusOsaAlueTasoKuvaus other) {
        this.taso = other.taso;
        this.edelleenKehittyvatOsaamiset.addAll(other.getEdelleenKehittyvatOsaamiset());
        this.osaamiset.addAll(other.getOsaamiset());
        this.edistynytOsaaminenKuvaukset.addAll(other.getEdistynytOsaaminenKuvaukset());
    }
}
