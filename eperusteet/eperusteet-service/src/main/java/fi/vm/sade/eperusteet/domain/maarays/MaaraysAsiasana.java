package fi.vm.sade.eperusteet.domain.maarays;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@Table(name = "maarays_asiasana")
@AllArgsConstructor
@NoArgsConstructor
public class MaaraysAsiasana {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ElementCollection
    @CollectionTable(name="maarays_asiasana_asiasana")
    private List<String> asiasana;

    public MaaraysAsiasana copy() {
        MaaraysAsiasana copy = new MaaraysAsiasana();
        copy.setAsiasana(new ArrayList<>());

        if (asiasana != null) {
            copy.getAsiasana().addAll(asiasana);
        }

        return copy;
    }
}
