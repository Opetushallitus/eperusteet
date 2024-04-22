package fi.vm.sade.eperusteet.domain;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "kayttajaprofiili_preferenssi")
@Getter
@Setter
public class KayttajaprofiiliPreferenssi implements Serializable {

    public KayttajaprofiiliPreferenssi(Kayttajaprofiili kayttajaprofiili, String avain, String arvo) {
        this.kayttajaprofiili = kayttajaprofiili;
        this.avain = avain;
        this.arvo = arvo;
    }

    public KayttajaprofiiliPreferenssi() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "kayttajaprofiili_id")
    private Kayttajaprofiili kayttajaprofiili;

    private String avain;

    private String arvo;
}
