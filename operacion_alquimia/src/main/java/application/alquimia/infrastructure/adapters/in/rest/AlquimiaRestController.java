package application.alquimia.infrastructure.adapters.in.rest;

import application.alquimia.domain.exceptions.InsufficientDataException;
import application.alquimia.domain.models.AlchemyResult;
import application.alquimia.domain.models.DeviceData;
import application.alquimia.domain.services.SavitarProtocolService;
import application.alquimia.infrastructure.adapters.in.rest.dtos.AlchemyResponseDto;
import application.alquimia.infrastructure.adapters.in.rest.dtos.CompleteRequest;
import application.alquimia.infrastructure.adapters.in.rest.dtos.DeviceRequest;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alquimia")
@RequiredArgsConstructor
public class AlquimiaRestController {

    private final SavitarProtocolService savitarProtocolService;

    /**
     * F1 - PROCESAMIENTO DE INFORMACIÓN COMPLETA
     * POST /alquimia
     */
    @PostMapping
    public ResponseEntity<AlchemyResponseDto> processCompleteInformation(@RequestBody(required = false) CompleteRequest request) {
        if (request == null || request.getDevices() == null || request.getDevices().isEmpty()) {
            throw new InsufficientDataException("El cuerpo JSON debe incluir el arreglo 'devices' con los 3 dispositivos de vigilancia.");
        }

        List<DeviceData> domainData = request.getDevices().stream()
                .map(d -> DeviceData.builder()
                        .name(d.getName())
                        .distance(d.getDistance())
                        .message(d.getMessage())
                        .build())
                .collect(Collectors.toList());

        AlchemyResult result = savitarProtocolService.processCompleteInformation(domainData);
        return ResponseEntity.ok(new AlchemyResponseDto(result.position(), result.message()));
    }

    /**
     * F2 - REGISTRO INDIVIDUAL
     * POST /alquimia/{device}
     */
    @PostMapping("/{device}")
    public ResponseEntity<DeviceData> registerIndividualDevice(
            @PathVariable("device") String device,
            @RequestBody(required = false) DeviceRequest request) {
        if (request == null) {
            throw new InsufficientDataException("El cuerpo JSON no puede ser nulo.");
        }
        DeviceData saved = savitarProtocolService.registerIndividualDevice(device, request.getDistance(), request.getMessage());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * F3 - CONSULTA Y PROCESAMIENTO ACUMULADO
     * GET /alquimia
     */
    @GetMapping
    public ResponseEntity<AlchemyResponseDto> processAccumulatedInformation() {
        AlchemyResult result = savitarProtocolService.processAccumulatedInformation();
        return ResponseEntity.ok(new AlchemyResponseDto(result.position(), result.message()));
    }

    /**
     * F4 - REEMPLAZO COMPLETO
     * PUT /alquimia/{device}
     */
    @PutMapping("/{device}")
    public ResponseEntity<DeviceData> replaceDeviceInformation(
            @PathVariable("device") String device,
            @RequestBody(required = false) DeviceRequest request) {
        if (request == null) {
            throw new InsufficientDataException("El cuerpo JSON no puede ser nulo.");
        }
        DeviceData replaced = savitarProtocolService.replaceDeviceInformation(device, request.getDistance(), request.getMessage());
        return ResponseEntity.ok(replaced);
    }

    /**
     * F5 - ACTUALIZACIÓN PARCIAL
     * PATCH /alquimia/{device}
     */
    @PatchMapping("/{device}")
    public ResponseEntity<DeviceData> updatePartialDeviceInformation(
            @PathVariable("device") String device,
            @RequestBody(required = false) DeviceRequest request) {
        if (request == null) {
            throw new InsufficientDataException("El cuerpo JSON no puede ser nulo.");
        }
        DeviceData updated = savitarProtocolService.updatePartialDeviceInformation(device, request.getDistance(), request.getMessage());
        return ResponseEntity.ok(updated);
    }

    /**
     * F6 - ELIMINACIÓN
     * DELETE /alquimia/{device}
     */
    @DeleteMapping("/{device}")
    public ResponseEntity<Void> deleteDeviceInformation(@PathVariable("device") String device) {
        savitarProtocolService.deleteDeviceInformation(device);
        return ResponseEntity.noContent().build();
    }
}
