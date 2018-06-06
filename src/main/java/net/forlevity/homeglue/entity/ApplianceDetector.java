/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "appliance_detector")
@NoArgsConstructor
@Accessors(chain = true)
@ToString(of = {"id", "minWatts", "offDelaySecs", "on"})
@EqualsAndHashCode(of = {"device"})
@JsonAutoDetect
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplianceDetector {

    private static final float DEFAULT_MIN_WATTS = 5.0f;
    private static final int DEFAULT_OFF_DELAY_SECS = 0;

    @Id
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Device device;

    @Column(name = "min_watts", nullable = false)
    @Getter
    @Setter
    private double minWatts; // minimum number of watts registering before we say appliance is on

    @Column(name = "off_delay_secs", nullable = false)
    @Getter
    @Setter
    private int offDelaySecs; // how long we can stay under threshold before we say appliance is off

    @Column(name = "max_on_secs", nullable = false)
    @Getter
    @Setter
    private int maxOnSeconds; // how long we can be in "on" state before on-too-long event

    @Column(name = "is_on", nullable = false)
    @Getter
    private boolean on;

    @Column(name = "last_state_change", nullable = false)
    @Getter
    @Setter
    private Instant lastStateChange = Instant.now();

    public ApplianceDetector withDefaultSettings() {
        this.setMinWatts(DEFAULT_MIN_WATTS);
        this.setOffDelaySecs(DEFAULT_OFF_DELAY_SECS);
        return this;
    }

    public void setOn(boolean isOn) {
        this.on = isOn;
        lastStateChange = Instant.now();
    }
}
