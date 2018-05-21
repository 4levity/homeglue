package net.forlevity.homeglue;

import com.google.inject.Guice;
import lombok.extern.log4j.Log4j2;

/**
 * Created by ivan on 5/18/18.
 */
@Log4j2
public class Main {

    public static void main(String... args) throws Exception {
        log.info("starting");
        Guice.createInjector(new ApplicationModule()).getInstance(Application.class).start();
    }
}
