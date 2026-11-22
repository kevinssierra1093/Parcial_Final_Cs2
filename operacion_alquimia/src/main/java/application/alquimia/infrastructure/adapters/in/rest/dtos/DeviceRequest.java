package application.alquimia.infrastructure.adapters.in.rest.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRequest {
    @JsonAlias({"device", "dispositivo", "satellite"})
    private String name;

    @JsonAlias({"distancia"})
    private Double distance;

    @JsonAlias({"mensaje"})
    private List<String> message;
}
