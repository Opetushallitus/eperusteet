package fi.vm.sade.eperusteet.domain.kios;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "kaantaja_todistusmalli_taitotasokuvaus")
@Audited
@Getter
@Setter
@NoArgsConstructor
public class KaantajaTodistusmalliTaitotasokuvaus extends AbstractAuditedReferenceableEntity {

    @OrderColumn
    @NotAudited
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "kaantajaTodistusmalliTaitotasokuvaus_id")
    private List<KaantajaTodistusmalliTaitotaso> taitotasot = new ArrayList<>();

    public KaantajaTodistusmalliTaitotasokuvaus(KaantajaTodistusmalliTaitotasokuvaus other) {
        if (other != null) {
            this.taitotasot = other.getTaitotasot().stream()
                    .map(KaantajaTodistusmalliTaitotaso::new)
                    .peek(taitotaso -> taitotaso.setKaantajaTodistusmalliTaitotasokuvaus(this))
                    .collect(Collectors.toList());
        }
    }
}

