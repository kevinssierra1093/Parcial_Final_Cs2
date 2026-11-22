package application.alquimia.infrastructure.adapters.out.memory;

import application.alquimia.domain.exceptions.InsufficientDataException;
import application.alquimia.domain.models.DeviceData;
import application.alquimia.domain.models.KnownDevice;
import application.alquimia.domain.ports.out.DeviceDataRepositoryPort;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * Adaptador de infraestructura para persistencia en memoria volátil (ConcurrentHashMap).
 * Mantiene la información temporal sin recurrir a bases de datos ni servicios externos.
 */
@Repository
public class InMemoryDeviceDataRepository implements DeviceDataRepositoryPort {

    private final Map<KnownDevice, DeviceData> storage = new ConcurrentHashMap<>();

    @Override
    public void save(KnownDevice device, DeviceData data) {
        storage.put(device, data);
    }

    @Override
    public Optional<DeviceData> findByDevice(KnownDevice device) {
        return Optional.ofNullable(storage.get(device));
    }

    @Override
    public Map<KnownDevice, DeviceData> findAll() {
        return Collections.unmodifiableMap(new HashMap<>(storage));
    }

    @Override
    public DeviceData updatePartial(KnownDevice device, Double distance, List<String> message) {
        return storage.compute(device, (k, existing) -> {
            if (existing == null) {
                throw new InsufficientDataException("No existe registro previo para el dispositivo " + device.getDeviceName() + ". No se puede aplicar actualización parcial.");
            }

            Double finalDistance = (distance != null) ? distance : existing.getDistance();
            List<String> finalMessage = (message != null) ? new ArrayList<>(message) : existing.getMessage();

            return DeviceData.builder()
                    .name(device.getDeviceName())
                    .distance(finalDistance)
                    .message(finalMessage)
                    .build();
        });
    }

    @Override
    public boolean delete(KnownDevice device) {
        return storage.remove(device) != null;
    }

    @Override
    public void clear() {
        storage.clear();
    }
}
