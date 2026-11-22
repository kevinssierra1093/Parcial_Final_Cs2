package application.alquimia.domain.services;

import application.alquimia.domain.exceptions.LocationCalculationException;
import application.alquimia.domain.models.KnownDevice;
import application.alquimia.domain.models.Position;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TrilaterationService {

    private static final double EPSILON = 2.0; // Tolerancia permitida en metros/unidades

    /**
     * Calcula las coordenadas (x, y) del laboratorio de Alchemy usando trilateración 2D
     * a partir de las distancias medidas por los 3 dispositivos de vigilancia.
     */
    public Position calculateLocation(Map<KnownDevice, Double> distances) {
        if (distances.size() < 3) {
            throw new LocationCalculationException("Se requieren las distancias de los 3 dispositivos para la trilateración.");
        }

        KnownDevice d1 = KnownDevice.STAR_LABS;
        KnownDevice d2 = KnownDevice.CCPD;
        KnownDevice d3 = KnownDevice.STAR_SATELLITE;

        Double r1 = distances.get(d1);
        Double r2 = distances.get(d2);
        Double r3 = distances.get(d3);

        if (r1 == null || r2 == null || r3 == null || r1 < 0 || r2 < 0 || r3 < 0) {
            throw new LocationCalculationException("Distancias inválidas o ausentes para uno o más dispositivos.");
        }

        double x1 = d1.getX(), y1 = d1.getY();
        double x2 = d2.getX(), y2 = d2.getY();
        double x3 = d3.getX(), y3 = d3.getY();

        // Sistema lineal derivado de la diferencia de círculos:
        // A*x + B*y = C
        // D*x + E*y = F
        double A = 2 * (x2 - x1);
        double B = 2 * (y2 - y1);
        double C = Math.pow(r1, 2) - Math.pow(r2, 2) - Math.pow(x1, 2) - Math.pow(y1, 2) + Math.pow(x2, 2) + Math.pow(y2, 2);

        double D = 2 * (x3 - x2);
        double E = 2 * (y3 - y2);
        double F = Math.pow(r2, 2) - Math.pow(r3, 2) - Math.pow(x2, 2) - Math.pow(y2, 2) + Math.pow(x3, 2) + Math.pow(y3, 2);

        double det = (A * E) - (B * D);

        if (Math.abs(det) < 1e-7) {
            throw new LocationCalculationException("Los dispositivos están alineados (colineales); no es posible determinar la posición unívoca.");
        }

        double x = ((C * E) - (B * F)) / det;
        double y = ((A * F) - (C * D)) / det;

        // Redondeo a 2 decimales para precisión limpia
        double roundedX = Math.round(x * 100.0) / 100.0;
        double roundedY = Math.round(y * 100.0) / 100.0;

        // Verificación de consistencia de distancias
        double distCalc1 = Math.hypot(roundedX - x1, roundedY - y1);
        double distCalc2 = Math.hypot(roundedX - x2, roundedY - y2);
        double distCalc3 = Math.hypot(roundedX - x3, roundedY - y3);

        if (Math.abs(distCalc1 - r1) > EPSILON || Math.abs(distCalc2 - r2) > EPSILON || Math.abs(distCalc3 - r3) > EPSILON) {
            throw new LocationCalculationException(String.format(
                    "Las distancias no convergen en una posición coherente. Desviaciones: [%.2f, %.2f, %.2f]",
                    Math.abs(distCalc1 - r1), Math.abs(distCalc2 - r2), Math.abs(distCalc3 - r3)
            ));
        }

        return new Position(roundedX, roundedY);
    }
}
