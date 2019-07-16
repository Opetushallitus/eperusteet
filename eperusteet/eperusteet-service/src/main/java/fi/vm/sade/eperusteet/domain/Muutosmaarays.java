package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.liite.Liite;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

/**
 * @author isaul
 */
@Entity
@Table(name = "muutosmaarays")
@Audited
public class Muutosmaarays {

    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Getter
    @Setter
    @ManyToOne
    private Peruste peruste;

    @Getter
    @Setter
    @ManyToOne(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    @ManyToOne(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen url;

    @Getter
    @Setter
    @Audited(targetAuditMode = NOT_AUDITED)
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "muutosmaarays_liite",
            joinColumns = {
                    @JoinColumn(name = "muutosmaarays_id")},
            inverseJoinColumns = {
                    @JoinColumn(name = "liite_id")})
    private Map<Kieli, Liite> liitteet = new HashMap<>();
}
