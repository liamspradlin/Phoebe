package liam.franco.selene.eventbus;

public class AmbientLightSensorChange {
    public float light;

    public AmbientLightSensorChange(float value) {
        this.light = value;
    }

    public float getLight() {
        return light;
    }
}
