package fi.vm.sade.eperusteet.dto.peruste;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JulkaisuDataDto {
    private Long id;
    private int hash;
    private ObjectNode data;
}
