package fi.vm.sade.eperusteet.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * Global version for peruste related changes.
 * Not audited nor versioned in purpose. Seperate from Peruste: won't affect it or it's muokattu timestamp
 * nor cause locking issues.
 */
@Entity
@Getter
@Setter
@Table(name = "peruste_version", schema = "public")
public class PerusteVersion {
    @Id
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    @GeneratedValue(generator = "peruste_version_id_seq")
    @SequenceGenerator(name = "peruste_version_id_seq", sequenceName = "peruste_version_id_seq")
    private Long id;

    // NOTE: do not annotate this as @RelatesToPeruste
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "peruste_id", nullable = false)
    private Peruste peruste;

    @Column(name = "aikaleima", nullable = false)
    private Date aikaleima = new Date();

    public PerusteVersion() {
    }

    public PerusteVersion(Peruste peruste) {
        this.peruste = peruste;
    }
}
