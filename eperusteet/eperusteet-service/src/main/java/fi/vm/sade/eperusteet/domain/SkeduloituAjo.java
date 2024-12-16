package fi.vm.sade.eperusteet.domain;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@Entity
@Table(name = "skeduloitu_ajo")
@AllArgsConstructor
@NoArgsConstructor
public class SkeduloituAjo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "nimi", nullable = false, unique = true, updatable = false)
    private String nimi;

    @Enumerated(EnumType.STRING)
    @NotNull
    private SkeduloituAjoStatus status = SkeduloituAjoStatus.PYSAYTETTY;

    @Column(name = "viimeisin_ajo_kaynnistys")
    private Date viimeisinAjoKaynnistys;

    @Column(name = "viimeisin_ajo_lopetus")
    private Date viimeisinAjoLopetus;

}
