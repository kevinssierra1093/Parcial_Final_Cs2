package application.alquimia.domain.services;

import application.alquimia.domain.exceptions.InsufficientDataException;
import application.alquimia.domain.exceptions.InvalidDeviceException;
import application.alquimia.domain.models.AlchemyResult;
import application.alquimia.domain.models.DeviceData;
import application.alquimia.domain.models.KnownDevice;
import application.alquimia.domain.models.Position;
import application.alquimia.domain.ports.out.DeviceDataRepositoryPort;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SavitarProtocolService {

    private final TrilaterationService trilaterationService;
    private final MessageReconstructionService messageReconstructionService;
    private final DeviceDataRepositoryPort deviceDataRepositoryPort;

    /**
     * F1 - PROCESAMIENTO DE INFORMACIÓN COMPLETA
     * Procesa simultáneamente los datos de los 3 dispositivos sin depender del almacenamiento previo.
     */
    public AlchemyResult processCompleteInformation(List<DeviceData> devices) {
        if (devices == null || devices.size() < 3) {
            throw new InsufficientDataException("Se requiere la información simultánea de los 3 dispositivos de vigilancia.");
        }

        Map<KnownDevice, Double> distances = new HashMap<>();
        List<List<String>> messageFragments = new ArrayList<>();

        for (DeviceData data : devices) {
            KnownDevice device = KnownDevice.fromIdentifier(data.getName())
                    .orElseThrow(() -> new InvalidDeviceException("Dispositivo no reconocido: " + data.getName()));

            if (data.getDistance() == null) {
                throw new InsufficientDataException("Distancia no provista para " + device.getDeviceName());
            }

            distances.put(device, data.getDistance());
            messageFragments.add(data.getMessage());
        }

        Position position = trilaterationService.calculateLocation(distances);
        String message = messageReconstructionService.reconstructMessage(messageFragments);

        return new AlchemyResult(position, message);
    }

    /**
     * F2 - REGISTRO INDIVIDUAL
     * Registra o almacena la información de un dispositivo de vigilancia de forma independiente.
     */
    public DeviceData registerIndividualDevice(String deviceIdentifier, Double distance, List<String> message) {
        KnownDevice device = KnownDevice.fromIdentifier(deviceIdentifier)
                .orElseThrow(() -> new InvalidDeviceException("Dispositivo de vigilancia no reconocido: " + deviceIdentifier));

        DeviceData data = DeviceData.builder()
                .name(device.getDeviceName())
                .distance(distance)
                .message(message != null ? message : new ArrayList<>())
                .build();

        deviceDataRepositoryPort.save(device, data);
        return data;
    }

    /**
     * F3 - CONSULTA Y PROCESAMIENTO ACUMULADO
     * Consulta la información en memoria y ejecuta la localización y reconstrucción si hay datos completos.
     */
    public AlchemyResult processAccumulatedInformation() {
        Map<KnownDevice, DeviceData> stored = deviceDataRepositoryPort.findAll();

        if (stored.size() < 3 ||
            !stored.containsKey(KnownDevice.STAR_LABS) ||
            !stored.containsKey(KnownDevice.CCPD) ||
            !stored.containsKey(KnownDevice.STAR_SATELLITE)) {
            throw new InsufficientDataException("No hay suficiente información registrada de los 3 dispositivos para procesar la ubicación.");
        }

        Map<KnownDevice, Double> distances = new HashMap<>();
        List<List<String>> messageFragments = new ArrayList<>();

        for (KnownDevice d : List.of(KnownDevice.STAR_LABS, KnownDevice.CCPD, KnownDevice.STAR_SATELLITE)) {
            DeviceData data = stored.get(d);
            if (data == null || data.getDistance() == null || data.getMessage() == null || data.getMessage().isEmpty()) {
                throw new InsufficientDataException("Datos incompletos para el dispositivo: " + d.getDeviceName());
            }
            distances.put(d, data.getDistance());
            messageFragments.add(data.getMessage());
        }

        Position position = trilaterationService.calculateLocation(distances);
        String message = messageReconstructionService.reconstructMessage(messageFragments);

        return new AlchemyResult(position, message);
    }

    /**
     * F4 - REEMPLAZO COMPLETO
     * Reemplaza completamente los datos de un dispositivo existente.
     */
    public DeviceData replaceDeviceInformation(String deviceIdentifier, Double distance, List<String> message) {
        KnownDevice device = KnownDevice.fromIdentifier(deviceIdentifier)
                .orElseThrow(() -> new InvalidDeviceException("Dispositivo de vigilancia no reconocido: " + deviceIdentifier));

        if (distance == null || message == null) {
            throw new InsufficientDataException("Para un reemplazo completo se deben proporcionar distancia y fragmento de mensaje.");
        }

        DeviceData newData = DeviceData.builder()
                .name(device.getDeviceName())
                .distance(distance)
                .message(message)
                .build();

        deviceDataRepositoryPort.save(device, newData);
        return newData;
    }

    /**
     * F5 - ACTUALIZACIÓN PARCIAL
     * Modifica únicamente los datos proporcionados y conserva el resto.
     */
    public DeviceData updatePartialDeviceInformation(String deviceIdentifier, Double distance, List<String> message) {
        KnownDevice device = KnownDevice.fromIdentifier(deviceIdentifier)
                .orElseThrow(() -> new InvalidDeviceException("Dispositivo de vigilancia no reconocido: " + deviceIdentifier));

        return deviceDataRepositoryPort.updatePartial(device, distance, message);
    }

    /**
     * F6 - ELIMINACIÓN
     * Elimina completamente la información almacenada de un dispositivo sin afectar a los demás.
     */
    public void deleteDeviceInformation(String deviceIdentifier) {
        KnownDevice device = KnownDevice.fromIdentifier(deviceIdentifier)
                .orElseThrow(() -> new InvalidDeviceException("Dispositivo de vigilancia no reconocido: " + deviceIdentifier));

        boolean removed = deviceDataRepositoryPort.delete(device);
        if (!removed) {
            throw new InsufficientDataException("No existía información previa registrada para el dispositivo: " + device.getDeviceName());
        }
    }
}
