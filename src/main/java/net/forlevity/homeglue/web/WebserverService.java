/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.web;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.forlevity.homeglue.persistence.PersistenceService;
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

@Singleton
public class WebserverService extends AbstractIdleService {

    private final GuiceResteasyBootstrapServletContextListener guiceContextListener;
    private final PersistenceService persistence;

    private Server server;

    @Inject
    public WebserverService(GuiceResteasyBootstrapServletContextListener guiceContextListener,
                            PersistenceService persistence) {
        this.guiceContextListener = guiceContextListener;
        this.persistence = persistence;
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
        //servletHandler.addFilter(new FilterHolder(injector.getInstance(HelloFilter.class)), "/*", null);
        //servletHandler.addFilter(LogFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        resteasyHandler.setContextPath("/api");
        resteasyHandler.addServlet(resteasyServlet, "/*");

        // bundle handlers
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{ resteasyHandler, htmlHandler });

        // start server
        server = new Server(8080);
        server.setHandler(handlers);
        waitForOtherServices();
        server.start();
    }

    private void waitForOtherServices() {
        // wait for dependencies needed by any resources!
        persistence.awaitRunning();
    }

    @Override
    protected void shutDown() throws Exception {
        server.stop();
    }
}
