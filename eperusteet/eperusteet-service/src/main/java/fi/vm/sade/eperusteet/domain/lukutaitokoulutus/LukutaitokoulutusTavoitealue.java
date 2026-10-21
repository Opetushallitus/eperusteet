package fi.vm.sade.eperusteet.domain.lukutaitokoulutus;

import fi.vm.sade.eperusteet.domain.AbstractAuditedEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "lukutaitokoulutus_tavoitealue")
@Audited
@Getter
@Setter
@NoArgsConstructor
public class LukutaitokoulutusTavoitealue extends AbstractAuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;

    @OrderColumn
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(name = "lukutaitokoulutus_tavoitealue_tavoitteet",
            joinColumns = @JoinColumn(name = "tavoitealue_id"),
            inverseJoinColumns = @JoinColumn(name = "tavoite_id"))
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private List<TekstiPalanen> tavoitteet = new ArrayList<>();

    public LukutaitokoulutusTavoitealue(LukutaitokoulutusTavoitealue other) {
        if (other != null) {
            this.nimi = other.getNimi();
            this.tavoitteet = other.getTavoitteet().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }

    public void setTavoitteet(List<TekstiPalanen> tavoitteet) {
        this.tavoitteet.clear();
        if (tavoitteet != null) {
            tavoitteet.stream().filter(Objects::nonNull).forEach(this.tavoitteet::add);
        }
    }
}
