package fi.vm.sade.eperusteet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OmistajaDto {
    private boolean isOwner;
    private Long ownerPerusteProjektiId;
}
