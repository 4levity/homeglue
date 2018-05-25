/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.testing;

import lombok.extern.log4j.Log4j2;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

@Log4j2
public abstract class HomeglueTests {

    @Rule
    public TestName name = new TestName();

    @Before
    public void setup() {
        log.info("***** STARTING TEST {} *****", name.getMethodName());
    }

}
