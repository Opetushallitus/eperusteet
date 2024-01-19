package fi.vm.sade.eperusteet.domain.maarays;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
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
