package fi.vm.sade.eperusteet.domain;

import java.io.Serializable;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kayttajaprofiili_id")
    private Kayttajaprofiili kayttajaprofiili;

    private String avain;

    private String arvo;
}
