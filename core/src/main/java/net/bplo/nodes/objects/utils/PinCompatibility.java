package net.bplo.nodes.objects.utils;

public record PinCompatibility(boolean compatible, String message) {

    public static PinCompatibility ok() {
        return new PinCompatibility(true, "");
    }

    public static PinCompatibility reject(String message) {
        return new PinCompatibility(false, message);
    }

    public boolean incompatible() {
        return !compatible;
    }
}
