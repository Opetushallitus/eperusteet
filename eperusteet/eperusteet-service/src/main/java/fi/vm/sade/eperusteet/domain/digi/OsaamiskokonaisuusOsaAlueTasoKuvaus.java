package fi.vm.sade.eperusteet.domain.digi;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
