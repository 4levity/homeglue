/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.entity;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Entity
@Table(name = "appliance_cfg")
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@EqualsAndHashCode(of = {"device"})
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
    private float minWatts; // minimum number of watts registering before we say appliance is on

    @Column(name = "off_delay_secs", nullable = false)
    @Getter
    @Setter
    private int offDelaySecs; // how long we can stay under threshold before we say appliance is off

    @Column(name = "is_on", nullable = false)
    @Getter
    @Setter
    private boolean on;

    public ApplianceDetector withDefaultSettings() {
        this.setMinWatts(DEFAULT_MIN_WATTS);
        this.setOffDelaySecs(DEFAULT_OFF_DELAY_SECS);
        return this;
    }
}
