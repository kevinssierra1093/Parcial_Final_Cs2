package application.alquimia.domain.models;

import java.util.Arrays;
import java.util.Optional;

public enum KnownDevice {
    STAR_LABS("STAR Labs", -500.0, -200.0),
    CCPD("CCPD", 100.0, -100.0),
    STAR_SATELLITE("S.T.A.R. Satellite", 500.0, 100.0);

    private final String deviceName;
    private final double x;
    private final double y;

    KnownDevice(String deviceName, double x, double y) {
        this.deviceName = deviceName;
        this.x = x;
        this.y = y;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Position getPosition() {
        return new Position(x, y);
    }

    public static Optional<KnownDevice> fromIdentifier(String identifier) {
        if (identifier == null) return Optional.empty();
        String clean = identifier.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        return Arrays.stream(values())
                .filter(d -> {
                    String dClean = d.deviceName.toLowerCase().replaceAll("[^a-z0-9]", "");
                    String enumClean = d.name().toLowerCase().replaceAll("[^a-z0-9]", "");
                    return dClean.equals(clean) || enumClean.equals(clean);
                })
                .findFirst();
    }
}
