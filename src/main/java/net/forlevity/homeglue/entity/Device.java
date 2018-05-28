/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.entity;

import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

@Entity
@Table(name = "devices")
@NoArgsConstructor
@Accessors(chain = true)
@ToString
public class Device {

    @Id
    @GeneratedValue
    @Column(name = "id")
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @NaturalId(mutable = false)
    @Column(name = "code", nullable = false, unique = true)
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private String deviceId;
}
