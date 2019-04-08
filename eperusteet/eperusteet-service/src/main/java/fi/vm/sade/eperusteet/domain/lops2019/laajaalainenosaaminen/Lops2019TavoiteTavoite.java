package fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen;


import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;

@Entity
@Audited
@Table(name = "yl_lops2019_oppiaine_tavoite_tavoite")
public class Lops2019TavoiteTavoite {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    private Long id;

    @Getter
    @Setter
    private Integer jarjestys;
}
