package application.alquimia.infrastructure.adapters.in.rest.dtos;

import application.alquimia.domain.models.Position;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlchemyResponseDto {
    private Position position;
    private String message;
}
