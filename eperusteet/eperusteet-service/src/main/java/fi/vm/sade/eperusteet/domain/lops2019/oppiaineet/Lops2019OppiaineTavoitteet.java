package fi.vm.sade.eperusteet.domain.lops2019.oppiaineet;

import lombok.Getter;
import org.hibernate.envers.Audited;

import javax.persistence.*;

@Entity
@Audited
@Table(name = "yl_lops2019_oppiaine_tavoitteet")
public class Lops2019OppiaineTavoitteet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    private Long id;
}
