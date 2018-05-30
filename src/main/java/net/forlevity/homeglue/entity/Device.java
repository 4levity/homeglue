/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.entity;

import com.google.common.collect.ImmutableMap;
import lombok.*;
import lombok.experimental.Accessors;
import net.forlevity.homeglue.device.DeviceStatus;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "devices")
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@EqualsAndHashCode(of = {"deviceId"})
public class Device implements DeviceStatus {

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

    @Column(name = "connected", nullable = false)
    @Getter
    @Setter
    private boolean connected;

    @ElementCollection
    @MapKeyColumn(name="name")
    @Column(name="value")
    @CollectionTable(name="device_details", joinColumns=@JoinColumn(name="id"))
    private Map<String, String> deviceDetails = new HashMap<>();

    @Override
    public Map<String, String> getDeviceDetails() {
        return ImmutableMap.copyOf(deviceDetails);
    }

    public void setDeviceDetails(Map<String, String> deviceDetails) {
        this.deviceDetails = new HashMap<>(deviceDetails);
    }

    public static Device from(DeviceStatus deviceStatus) {
        Device device = new Device();
        device.setDeviceId(deviceStatus.getDeviceId());
        device.setConnected(deviceStatus.isConnected());
        device.setDeviceDetails(deviceStatus.getDeviceDetails());
        return device;
    }
}
