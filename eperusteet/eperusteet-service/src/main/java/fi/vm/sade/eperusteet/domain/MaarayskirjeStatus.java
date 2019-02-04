package fi.vm.sade.eperusteet.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "maarayskirje_status")
public class MaarayskirjeStatus {

    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "peruste_id", nullable = false, unique = true)
    private Peruste peruste;

    @Getter
    @Setter
    @Column(name = "aikaleima", nullable = false)
    private Date lastCheck;

    @Getter
    @Setter
    @Column(name = "lataaminen_ok")
    private boolean lataaminenOk = false;

}
