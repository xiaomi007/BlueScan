package jp.tamecco.bluescan;

/**
 * Created by xiaomi on 15/08/24.
 */
public class IBeaconModel {

    String modelName;
    String macAddress;
    int rssi;
    int power;
    String uuid;
    int major;
    int minor;
    String kontaktId;

    public IBeaconModel() {
        this.modelName = "<null>";
        this.macAddress = "00:00:00:00:00:00";
        this.rssi = -100;
        this.power = -100;
        this.uuid = "<null>";
        this.major = -1;
        this.minor = -1;
        this.kontaktId = "0000";
    }

}
