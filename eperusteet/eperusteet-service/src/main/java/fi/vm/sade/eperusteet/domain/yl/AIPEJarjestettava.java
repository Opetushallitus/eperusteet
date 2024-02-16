package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.dto.yl.AIPEHasId;

public interface AIPEJarjestettava extends AIPEHasId {
    Integer getJarjestys();
    void setJarjestys(Integer value);
}
