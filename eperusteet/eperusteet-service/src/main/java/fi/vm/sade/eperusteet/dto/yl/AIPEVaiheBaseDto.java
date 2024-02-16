package fi.vm.sade.eperusteet.dto.yl;

import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIPEVaiheBaseDto implements AIPEHasId {
    private Long id;
    private UUID tunniste;

    private Date luotu;
    private Date muokattu;
}
