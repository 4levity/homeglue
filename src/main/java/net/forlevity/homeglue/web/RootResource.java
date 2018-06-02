/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.web;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import net.forlevity.homeglue.persistence.PersistenceService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import java.util.Map;

@Provider
@Path("/")
public class RootResource {

    private final PersistenceService persistence;

    @Inject
    public RootResource(PersistenceService persistence) {
        this.persistence = persistence;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String,String> getRoot() {
        return ImmutableMap.of("hello","world", "persistence", persistence.state().toString());
    }
}
