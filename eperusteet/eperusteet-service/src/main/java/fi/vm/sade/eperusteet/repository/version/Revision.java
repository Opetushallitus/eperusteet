package fi.vm.sade.eperusteet.repository.version;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Revision implements Serializable {
    private Integer numero;
    private Date pvm;
    private String muokkaajaOid;
    private String kommentti;

    public Revision(Integer number, Long timestamp, String muokkaajaOid, String kommentti) {
        this.numero = number;
        this.pvm = new Date(timestamp);
        this.muokkaajaOid = muokkaajaOid;
        this.kommentti = kommentti;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pvm == null) ? 0 : pvm.hashCode());
        result = prime * result + ((numero == null) ? 0 : numero.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Revision other = (Revision) obj;
        if (pvm == null) {
            if (other.pvm != null) {
                return false;
            }
        } else if (!pvm.equals(other.pvm)) {
            return false;
        }
        if (numero == null) {
            if (other.numero != null) {
                return false;
            }
        } else if (!numero.equals(other.numero)) {
            return false;
        }
        return true;
    }

    public static final Revision DRAFT = new Revision(0, 0L, null, null);

}
