package fi.vm.sade.eperusteet.domain;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kayttajaprofiili_id", insertable = false, updatable = false)
    private Kayttajaprofiili kayttajaprofiili;

    @Column(name = "nimi")
    private String nimi;

    @Column(name = "sisalto")
    private String sisalto;

    @Getter
    @Setter
    @Column(name = "lisatty")
    private Date lisatty;
}
