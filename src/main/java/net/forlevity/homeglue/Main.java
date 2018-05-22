/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue;

import com.google.inject.Guice;
import lombok.extern.log4j.Log4j2;

/**
 * Created by ivan on 5/18/18.
 */
@Log4j2
public class Main {

    public static void main(String... args) throws Exception {
        Guice.createInjector(new ApplicationModule())
                .getInstance(Application.class)
                .start();
    }
}
