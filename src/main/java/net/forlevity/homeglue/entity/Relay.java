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

@Entity
@Table(name = "relays")
@NoArgsConstructor
@Accessors(chain = true)
@ToString(of = {"id", "closed"})
@EqualsAndHashCode(of = {"device"})
@JsonAutoDetect
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Relay {

    @Id
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Device device;

    @Column(name = "closed", nullable = false)
    @Getter
    @Setter
    private boolean closed;
}
