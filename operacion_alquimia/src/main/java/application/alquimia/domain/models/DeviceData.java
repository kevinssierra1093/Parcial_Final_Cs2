package application.alquimia.domain.models;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceData {
    private String name;
    private Double distance;
    private List<String> message;

    public List<String> getMessage() {
        return message != null ? message : new ArrayList<>();
    }
}
