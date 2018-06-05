/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.forlevity.homeglue.entity.Relay;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@JsonAutoDetect
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RelayDto {

    boolean closed;

    public static RelayDto from(Relay entity) {
        return new RelayDto(entity.isClosed());
    }
}
