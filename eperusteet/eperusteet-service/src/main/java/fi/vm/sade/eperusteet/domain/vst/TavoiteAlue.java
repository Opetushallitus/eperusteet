package fi.vm.sade.eperusteet.domain.vst;

import fi.vm.sade.eperusteet.domain.AbstractAuditedEntity;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidKoodisto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "tavoitealue")
@Audited
@Getter
@Setter
@NoArgsConstructor
public class TavoiteAlue extends AbstractAuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TavoiteAlueTyyppi tavoiteAlueTyyppi;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidKoodisto(koodisto = KoodistoUriArvo.TAVOITEALUEET)
    private Koodi otsikko;

    @OrderColumn
    @ValidKoodisto(koodisto = KoodistoUriArvo.TAVOITTEETLUKUTAIDOT)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "tavoitealue_tavoitteet",
            joinColumns = @JoinColumn(name = "tavoitealue_id"),
            inverseJoinColumns = @JoinColumn(name = "tavoite_koodi_id"))
    private List<Koodi> tavoitteet = new ArrayList<>();

    @OrderColumn
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(name = "tavoitealue_keskeiset_sisaltoalueet",
            joinColumns = @JoinColumn(name = "tavoitealue_id"),
            inverseJoinColumns = @JoinColumn(name = "keskeinen_sisaltoalue_id"))
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private List<TekstiPalanen> keskeisetSisaltoalueet = new ArrayList<>();

    public TavoiteAlue(TavoiteAlue other) {
        this.tavoiteAlueTyyppi = other.getTavoiteAlueTyyppi();
        if (other.getOtsikko() != null) {
            this.otsikko = new Koodi(other.getOtsikko().getUri());
        }
        this.tavoitteet = other.getTavoitteet().stream().map(tavoite -> new Koodi(tavoite.getUri())).collect(Collectors.toList());
        this.keskeisetSisaltoalueet = other.getKeskeisetSisaltoalueet().stream().map(k -> TekstiPalanen.of(k)).collect(Collectors.toList());
    }

    public void setTavoitteet(List<Koodi> tavoitteet) {
        this.tavoitteet.clear();
        if (tavoitteet != null) {
            this.tavoitteet.addAll(tavoitteet);
        }
    }
}
