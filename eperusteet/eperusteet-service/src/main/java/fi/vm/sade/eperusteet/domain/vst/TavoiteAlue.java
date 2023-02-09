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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
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

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
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
