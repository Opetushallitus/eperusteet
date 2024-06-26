package fi.vm.sade.eperusteet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.vm.sade.eperusteet.domain.PalauteStatus;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PalauteDto {
    private Integer stars;
    private String feedback;

    @JsonProperty("user-agent")
    private String userAgent;

    @JsonProperty("created-at")
    private Date createdAt;
    private String key;
    private String data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PalauteStatus status;

    public void setUser_agent(String userAgent) {
        this.userAgent = userAgent;
    }
}
