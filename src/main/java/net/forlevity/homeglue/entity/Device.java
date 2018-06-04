/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.entity;

import com.google.common.collect.ImmutableMap;
import lombok.*;
import lombok.experimental.Accessors;
import net.forlevity.homeglue.device.DeviceState;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "devices")
@NoArgsConstructor
@Accessors(chain = true)
@ToString(of = {"id", "deviceId", "connected"})
@EqualsAndHashCode(of = {"deviceId"})
public class Device {

    @Id
    @GeneratedValue
    @Column(name = "id")
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @NaturalId(mutable = false)
    @Column(name = "device_id", nullable = false, unique = true)
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private String deviceId;
    public static String _deviceId = "deviceId";

    @Column(name = "connected", nullable = false)
    @Getter
    @Setter
    private boolean connected;

    @OneToOne(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Getter
    private Relay relay;

    @OneToOne(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Getter
    private ApplianceDetector applianceDetector;

    @ElementCollection
    @MapKeyColumn(name="name")
    @Column(name="value")
    @CollectionTable(name="device_details", joinColumns=@JoinColumn(name="id"))
    private Map<String, String> details = new HashMap<>();

    public Map<String, String> getDetails() {
        return ImmutableMap.copyOf(details);
    }

    /**
     * Set the device details.
     *
     * @param details details
     */
    public void setDetails(Map<String, String> details) {
        this.details = new HashMap<>(details);
    }

    /**
     * Set the relay child object for this device.
     *
     * @param relay relay
     */
    public void setRelay(Relay relay) {
        if (relay == null) {
            if (this.relay != null) {
                this.relay.setDevice(null);
            }
        } else {
            relay.setDevice(this);
        }
        this.relay = relay;
    }

    /**
     * Set the applianceDetector child object for this device.
     *
     * @param applianceDetector applianceDetector
     */
    public void setApplianceDetector(ApplianceDetector applianceDetector) {
        if (applianceDetector == null) {
            if (this.applianceDetector != null) {
                this.applianceDetector.setDevice(null);
            }
        } else {
            applianceDetector.setDevice(this);
        }
        this.applianceDetector = applianceDetector;
    }

    /**
     * Returns true if the device has a power meter. Appliance detector is attached when a meter is detected.
     *
     * @return true if device has a power meter
     */
    public boolean hasPowerMeter() {
        return (getApplianceDetector() != null);
    }

    public static Device from(DeviceState deviceState) {
        Device device = new Device();
        device.setDeviceId(deviceState.getDeviceId());
        device.setConnected(deviceState.isConnected());
        device.setDetails(deviceState.getDeviceDetails());
        return device;
    }
}
