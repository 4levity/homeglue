package net.forlevity.homeglue;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import lombok.extern.log4j.Log4j2;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.IOException;

@Log4j2
public abstract class HomeglueTests {

    @Rule
    public TestName name = new TestName();

    @Before
    public void setup() {
        log.info("***** STARTING TEST {} *****", name.getMethodName());
    }

    public static String resourceAsString(String resourceName) {
        try {
            return Resources.toString(Resources.getResource(resourceName), Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
