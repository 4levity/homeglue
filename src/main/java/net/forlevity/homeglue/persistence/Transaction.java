/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.persistence;

import org.hibernate.Session;

import java.util.function.Function;

/**
 * A unit of work to be performed in a managed hibernate Session.
 *
 * @param <RT> return type
 */
public interface Transaction<RT> extends Function<Session, RT> {
}
