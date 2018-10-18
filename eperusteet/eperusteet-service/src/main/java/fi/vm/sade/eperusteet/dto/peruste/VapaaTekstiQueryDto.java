package fi.vm.sade.eperusteet.dto.peruste;


import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, of = { "teksti" })
public class VapaaTekstiQueryDto extends PageableQueryDto implements Serializable {
    private String teksti = "";
}
