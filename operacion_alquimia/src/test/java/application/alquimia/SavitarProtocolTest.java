package application.alquimia;

import application.alquimia.domain.exceptions.InsufficientDataException;
import application.alquimia.domain.models.AlchemyResult;
import application.alquimia.domain.models.DeviceData;
import application.alquimia.domain.models.Position;
import application.alquimia.domain.services.MessageReconstructionService;
import application.alquimia.domain.services.SavitarProtocolService;
import application.alquimia.domain.services.TrilaterationService;
import application.alquimia.infrastructure.adapters.out.memory.InMemoryDeviceDataRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SavitarProtocolTest {

    private TrilaterationService trilaterationService;
    private MessageReconstructionService messageReconstructionService;
    private InMemoryDeviceDataRepository repository;
    private SavitarProtocolService savitarProtocolService;

    // Punto objetivo de prueba para el laboratorio de Alchemy: (-100.0, 75.0)
    // STAR Labs [-500, -200] -> d1 = sqrt((-100 - -500)^2 + (75 - -200)^2) = sqrt(160000 + 75625) = 485.41
    // CCPD [100, -100]       -> d2 = sqrt((-100 - 100)^2 + (75 - -100)^2) = sqrt(40000 + 30625) = 265.75
    // Satellite [500, 100]   -> d3 = sqrt((-100 - 500)^2 + (75 - 100)^2) = sqrt(360000 + 625) = 600.52
    private final double D1 = 485.41;
    private final double D2 = 265.75;
    private final double D3 = 600.52;

    @BeforeEach
    void setUp() {
        trilaterationService = new TrilaterationService();
        messageReconstructionService = new MessageReconstructionService();
        repository = new InMemoryDeviceDataRepository();
        savitarProtocolService = new SavitarProtocolService(trilaterationService, messageReconstructionService, repository);
    }

    @Test
    @DisplayName("F1: Debe procesar la información completa simultáneamente")
    void shouldProcessCompleteInformation() {
        List<DeviceData> devices = List.of(
                new DeviceData("STAR Labs", D1, List.of("este", "", "", "mensaje", "")),
                new DeviceData("CCPD", D2, List.of("", "es", "", "", "secreto")),
                new DeviceData("S.T.A.R. Satellite", D3, List.of("este", "", "un", "", ""))
        );

        AlchemyResult result = savitarProtocolService.processCompleteInformation(devices);

        assertNotNull(result);
        assertEquals(-100.0, result.position().x(), 1.0);
        assertEquals(75.0, result.position().y(), 1.0);
        assertEquals("este es un mensaje secreto", result.message());
    }

    @Test
    @DisplayName("F2 & F3: Debe registrar dispositivos individualmente y procesar información acumulada")
    void shouldRegisterIndividuallyAndProcessAccumulated() {
        // Al registrar solo 2 dispositivos, la consulta acumulada debe fallar
        savitarProtocolService.registerIndividualDevice("STAR Labs", D1, List.of("este", "", "", "mensaje", ""));
        savitarProtocolService.registerIndividualDevice("CCPD", D2, List.of("", "es", "", "", "secreto"));

        assertThrows(InsufficientDataException.class, () -> savitarProtocolService.processAccumulatedInformation());

        // Al registrar el tercero, debe resolver con éxito
        savitarProtocolService.registerIndividualDevice("S.T.A.R. Satellite", D3, List.of("este", "", "un", "", ""));

        AlchemyResult result = savitarProtocolService.processAccumulatedInformation();
        assertNotNull(result);
        assertEquals(-100.0, result.position().x(), 1.0);
        assertEquals(75.0, result.position().y(), 1.0);
        assertEquals("este es un mensaje secreto", result.message());
    }

    @Test
    @DisplayName("F4: Debe reemplazar completamente la información de un dispositivo")
    void shouldReplaceDeviceInformationCompletely() {
        savitarProtocolService.registerIndividualDevice("CCPD", 10.0, List.of("ruido", "invalido"));

        // Reemplazo completo con los datos correctos
        savitarProtocolService.replaceDeviceInformation("CCPD", D2, List.of("", "es", "", "", "secreto"));

        savitarProtocolService.registerIndividualDevice("STAR Labs", D1, List.of("este", "", "", "mensaje", ""));
        savitarProtocolService.registerIndividualDevice("S.T.A.R. Satellite", D3, List.of("este", "", "un", "", ""));

        AlchemyResult result = savitarProtocolService.processAccumulatedInformation();
        assertEquals("este es un mensaje secreto", result.message());
        assertEquals(-100.0, result.position().x(), 1.0);
    }

    @Test
    @DisplayName("F5: Debe actualizar parcialmente conservando los campos no modificados")
    void shouldUpdatePartialDeviceInformation() {
        savitarProtocolService.registerIndividualDevice("CCPD", 999.9, List.of("", "es", "", "", "secreto"));

        // Actualizamos solo la distancia, conservando el fragmento de mensaje
        DeviceData updated = savitarProtocolService.updatePartialDeviceInformation("CCPD", D2, null);
        assertEquals(D2, updated.getDistance());
        assertEquals(List.of("", "es", "", "", "secreto"), updated.getMessage());

        savitarProtocolService.registerIndividualDevice("STAR Labs", D1, List.of("este", "", "", "mensaje", ""));
        savitarProtocolService.registerIndividualDevice("S.T.A.R. Satellite", D3, List.of("este", "", "un", "", ""));

        AlchemyResult result = savitarProtocolService.processAccumulatedInformation();
        assertEquals("este es un mensaje secreto", result.message());
    }

    @Test
    @DisplayName("F6: Debe eliminar completamente un dispositivo de vigilancia")
    void shouldDeleteDeviceInformation() {
        savitarProtocolService.registerIndividualDevice("STAR Labs", D1, List.of("este"));
        savitarProtocolService.deleteDeviceInformation("STAR Labs");

        // Al intentar eliminar nuevamente debe lanzar excepción por no existir
        assertThrows(InsufficientDataException.class, () -> savitarProtocolService.deleteDeviceInformation("STAR Labs"));
    }
}
