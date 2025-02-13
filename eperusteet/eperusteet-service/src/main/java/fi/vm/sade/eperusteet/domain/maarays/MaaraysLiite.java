package fi.vm.sade.eperusteet.domain.maarays;

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Blob;
import java.util.UUID;

@Entity
@Setter
@Getter
@Table(name = "maarays_liite")
@AllArgsConstructor
@NoArgsConstructor
public class MaaraysLiite implements Serializable {

    @Id
    @Column(updatable = false)
    private UUID id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private TekstiPalanen nimi;

    @NotNull
    @Size(max = 1024)
    private String tiedostonimi;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MaaraysLiiteTyyppi tyyppi;

    @Basic(fetch = FetchType.LAZY, optional = false)
    @Column(updatable = false, nullable = false)
    @Lob
    @NotNull
    private byte[] data;

    public MaaraysLiite copy() {
        MaaraysLiite copy = new MaaraysLiite();
        copy.setId(UUID.randomUUID());
        if (nimi != null) {
            copy.setNimi(TekstiPalanen.of(nimi.getTeksti()));
        }
        copy.setTiedostonimi(tiedostonimi);
        copy.setTyyppi(tyyppi);
        copy.setData(data);

        return copy;
    }

}
