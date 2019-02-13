package de.jan.machinelearning.autonomouscar.control;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by DevTastisch on 24.01.2019
 */
public class VehicleController {

    private final Map<VehicleControlKey, Float> keys = new ConcurrentHashMap<>();

    public VehicleController() {
        Arrays.stream(VehicleControlKey.values()).forEach(key -> this.keys.put(key, 0f));
    }

    public void updateKey(VehicleControlKey key, float value) {
        this.keys.put(key, value);
    }

    public float getValue(VehicleControlKey key) {
        return this.keys.get(key);
    }

}
