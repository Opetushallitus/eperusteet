package fi.vm.sade.eperusteet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "kommentti")
public class Kommentti extends AbstractAuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @Column(name="poistettu")
    private Boolean poistettu;

    @Getter
    @Setter
    @Column(name="nimi")
    private String nimi;

    @Getter
    @Setter
    @Column(name="ylin_id")
    private Long ylinId;

    @Getter
    @Setter
    @Column(name="parent_id")
    private Long parentId;

    @Getter
    @Setter
    @Column(name="perusteprojekti_id")
    private Long perusteprojektiId;

    @Getter
    @Setter
    @Column(name="sisalto")
    private String sisalto;

    @Getter
    @Setter
    @Column(name="viite_suoritustapa")
    private String suoritustapa;

    @Getter
    @Setter
    @Column(name="viite_perusteenosa_id")
    private Long perusteenOsaId;
}
