package application.alquimia.infrastructure.adapters.in.rest.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteRequest {
    @JsonAlias({"satellites", "dispositivos", "satellitesList"})
    private List<DeviceRequest> devices;
}
