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
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "devices")
@NoArgsConstructor
@Accessors(chain = true)
@ToString(of = {"id", "detectionId", "connected"})
@EqualsAndHashCode(of = {"detectionId"})
public class Device {

    // standardized metadata fields - connectors MAY use these as keys in the device details
    public static final String DETAIL_USER_SPECIFIED_NAME = "name";
    public static final String DETAIL_SERIAL_NUMBER = "serialNumber";
    public static final String DETAIL_FIRMWARE_VERSON = "firmwareVersion";
    public static final String DETAIL_MODEL = "model";

    @Id
    @GeneratedValue
    @Column(name = "id")
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @NaturalId(mutable = false)
    @Column(name = "detection_id", nullable = false, unique = true)
    @Setter(AccessLevel.PRIVATE)
    private String detectionId;
    public static String _detectionId = "detectionId";
    public String getDetectionId() {
        return detectionId;
    }

    @Column(name = "friendly_name")
    @Getter
    private String friendlyName;

    @Column(name = "connected", nullable = false)
    @Getter
    private boolean connected;
    public static String _connected = "connected";

    @Column(name = "last_state_change", nullable = false)
    @Getter
    @Setter
    private Instant lastStateChange = Instant.EPOCH;

    @OneToOne(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Getter
    private Relay relay;

    @OneToOne(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Getter
    private ApplianceDetector applianceDetector;

    @ElementCollection
    @MapKeyColumn(name="k")
    @Column(name="v")
    @CollectionTable(name="device_details", joinColumns=@JoinColumn(name="id"))
    private Map<String, String> details = new HashMap<>();

    public void setConnected(boolean isConnected) {
        if (connected != isConnected) {
            lastStateChange = Instant.now();
            connected = isConnected;
        }
    }

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

    public void setFriendlyName(String friendlyName) {
        if (!validFriendlyName(friendlyName)) {
            throw new IllegalArgumentException("invalid friendlyName");
        }
        this.friendlyName = friendlyName;
    }

    public static boolean validFriendlyName(String fname) {
        return fname != null && fname.matches("[A-Za-z0-9]{1,64}");
    }

    /**
     * Returns true if the device has a power meter. Appliance detector is attached when a meter is detected.
     *
     * @return true if device has a power meter
     */
    public boolean hasPowerMeter() {
        return (getApplianceDetector() != null);
    }

    public boolean hasRelay() {
        return getRelay() != null;
    }

    public static Device from(DeviceState deviceState) {
        Device device = new Device();
        device.setDetectionId(deviceState.getDetectionId());
        device.setConnected(false);
        if (deviceState.getDetails() != null) {
            device.setDetails(deviceState.getDetails());
        }
        String userSpecifiedName = device.details.get(DETAIL_USER_SPECIFIED_NAME);
        if (validFriendlyName(userSpecifiedName)) {
            device.setFriendlyName(userSpecifiedName);
        }
        return device;
    }
}
