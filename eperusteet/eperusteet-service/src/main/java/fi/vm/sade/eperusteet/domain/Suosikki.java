package fi.vm.sade.eperusteet.domain;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "suosikki")
@Getter
@Setter
public class Suosikki implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "kayttajaprofiili_id")
    private Kayttajaprofiili kayttajaprofiili;

    @Column(name = "nimi")
    private String nimi;

    @Column(name = "sisalto")
    private String sisalto;

    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    @Setter
    @Column(name = "lisatty")
    private Date lisatty;
}
