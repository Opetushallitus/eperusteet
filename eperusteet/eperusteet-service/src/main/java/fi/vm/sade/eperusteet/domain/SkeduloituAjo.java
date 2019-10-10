package fi.vm.sade.eperusteet.domain;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
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
