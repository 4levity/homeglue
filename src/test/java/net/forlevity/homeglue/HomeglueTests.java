package net.forlevity.homeglue;

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
