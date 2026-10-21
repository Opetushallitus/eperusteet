package fi.vm.sade.eperusteet.domain.lukutaitokoulutus;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "lukutaitokoulutus_tjs")
@Audited
@Getter
@Setter
public class LukutaitokoulutusTavoitteetJaSisaltoalueet extends PerusteenOsa implements Serializable {

    @OrderColumn
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "lukutaitokoulutus_tjs_tavoitealueet",
            joinColumns = @JoinColumn(name = "tjs_id"),
            inverseJoinColumns = @JoinColumn(name = "tavoitealue_id"))
    private List<LukutaitokoulutusTavoitealue> tavoitealueet = new ArrayList<>();

    public LukutaitokoulutusTavoitteetJaSisaltoalueet() {
    }

    public LukutaitokoulutusTavoitteetJaSisaltoalueet(LukutaitokoulutusTavoitteetJaSisaltoalueet other) {
        super(other);
        copyState(other);
    }

    @Override
    public LukutaitokoulutusTavoitteetJaSisaltoalueet copy() {
        return new LukutaitokoulutusTavoitteetJaSisaltoalueet(this);
    }

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.lukutaitokoulutus_tavoitteetjasisaltoalueet;
    }

    public void setTavoitealueet(List<LukutaitokoulutusTavoitealue> tavoitealueet) {
        this.tavoitealueet.clear();
        if (tavoitealueet != null) {
            this.tavoitealueet.addAll(tavoitealueet);
        }
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof LukutaitokoulutusTavoitteetJaSisaltoalueet other) {
            setNimi(other.getNimi());
            copyState(other);
        }
    }

    @Override
    public boolean structureEquals(PerusteenOsa updated) {
        boolean result = false;
        if (updated instanceof LukutaitokoulutusTavoitteetJaSisaltoalueet that) {
            result = super.structureEquals(that);
            result &= getTavoitealueet().size() == that.getTavoitealueet().size();
        }
        return result;
    }

    private void copyState(LukutaitokoulutusTavoitteetJaSisaltoalueet other) {
        if (other == null) {
            return;
        }

        setTavoitealueet(other.getTavoitealueet().stream()
                .map(LukutaitokoulutusTavoitealue::new)
                .collect(Collectors.toList()));
    }
}
