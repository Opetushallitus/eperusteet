package fi.vm.sade.eperusteet.domain.validation;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "validointi_status_info_validointi")
public class Validointi {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @Getter
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    public List<RakenneOngelma> ongelmat = new ArrayList<>();

    public void setOngelmat(List<RakenneOngelma> rakenneOngelmat) {
        this.ongelmat.clear();
        this.ongelmat.addAll(rakenneOngelmat);
    }
}
