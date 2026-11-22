package application.alquimia.domain.ports.out;

import application.alquimia.domain.models.DeviceData;
import application.alquimia.domain.models.KnownDevice;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DeviceDataRepositoryPort {

    /**
     * Guarda o reemplaza completamente los datos de un dispositivo (F2 y F4).
     */
    void save(KnownDevice device, DeviceData data);

    /**
     * Obtiene los datos registrados de un dispositivo.
     */
    Optional<DeviceData> findByDevice(KnownDevice device);

    /**
     * Obtiene los datos registrados de todos los dispositivos de vigilancia (F3).
     */
    Map<KnownDevice, DeviceData> findAll();

    /**
     * Actualiza parcialmente la información de un dispositivo (F5).
     * Modifica únicamente los datos proporcionados y conserva el resto.
     */
    DeviceData updatePartial(KnownDevice device, Double distance, List<String> message);

    /**
     * Elimina completamente la información almacenada de un dispositivo (F6).
     * @return true si el dispositivo estaba registrado y fue eliminado, false en caso contrario.
     */
    boolean delete(KnownDevice device);

    /**
     * Limpia todos los registros en memoria.
     */
    void clear();
}
