/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.web;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.util.ServiceDependencies;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import java.net.URI;

/**
 * Guava web server service with Jetty + RESTeasy + Jackson + Guice. Static html served at "/" from JAR /html/ folder.
 * RESTeasy resource paths are prefixed with /api/. Must install() a WebserverGuice module. Will utilize custom Jackson
 * ObjectMapper if provided by Guice.
 */
@Singleton
@Log4j2
public class WebserverService extends AbstractIdleService {

    private final GuiceResteasyBootstrapServletContextListener guiceContextListener;
    private final ServiceDependencies dependencies;
    private final int port;

    private Server server;

    @Inject
    public WebserverService(GuiceResteasyBootstrapServletContextListener guiceContextListener,
                            ServiceDependencies dependencies,
                            @Named("webserver.port") int port) {
        this.guiceContextListener = guiceContextListener;
        this.dependencies = dependencies;
        this.port = port;
    }

    @Override
    protected void startUp() throws Exception {
        // static html
        URI resourceUri = WebserverService.class.getClassLoader().getResource("html/").toURI();
        ServletContextHandler htmlHandler = new ServletContextHandler();
        htmlHandler.setBaseResource(Resource.newResource(resourceUri));
        ServletHolder htmlHolder = new ServletHolder("default", DefaultServlet.class);
        htmlHandler.addServlet(htmlHolder, "/");

        // JAX-RS API
        ServletHolder resteasyServlet = new ServletHolder(HttpServletDispatcher.class);
        resteasyServlet.setInitOrder(0);
        ServletContextHandler resteasyHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        resteasyHandler.addEventListener(guiceContextListener);
        resteasyHandler.setContextPath("/api");
        resteasyHandler.addServlet(resteasyServlet, "/*");

        // bundle handlers
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{ resteasyHandler, htmlHandler });

        // start server
        server = new Server(port);
        server.setHandler(handlers);
        dependencies.waitForDependencies(this);
        server.start();

        log.info("webserver is running at http://localhost:{}/ (accessible over network!)", port);
    }

    @Override
    protected void shutDown() throws Exception {
        server.stop();
    }
}
