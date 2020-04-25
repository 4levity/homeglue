/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.testing;

import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.util.Json;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

@Log4j2
public abstract class HomeglueTests {

    protected Json json = new Json(true);

    @BeforeEach
    public void setup(TestInfo testInfo) {
        log.info("***** STARTING TEST {} *****", testInfo.getDisplayName());
    }

    // for lambdas:
    protected static Object throwThisTestException(Object object) {
        throw new ThisTestException();
    }
}
