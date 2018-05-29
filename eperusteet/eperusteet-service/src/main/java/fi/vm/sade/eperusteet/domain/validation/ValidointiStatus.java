package fi.vm.sade.eperusteet.domain.validation;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "validointi_status")
public class ValidointiStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @JoinColumn(name = "peruste_id", nullable = false, unique = true)
    @OneToOne(fetch = FetchType.LAZY)
    private Peruste peruste;

    @Getter
    @Setter
    @Column(name = "aikaleima", nullable = false)
    private Date lastCheck;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @Getter
    @Setter
    private List<ValidointiStatusInfo> infot = new ArrayList<>();

    @Getter
    @Setter
    private boolean vaihtoOk = false;

    public void setInfot(List<ValidointiStatusInfo> infot) {
        if (infot != null) {
            this.infot.clear();
            this.infot.addAll(infot);
        }
    }
}
