package it.musichub.server.rest.impl;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.port;

import java.util.function.Consumer;

import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import it.musichub.server.ex.ServiceInitException;
import spark.Spark;

@SwaggerDefinition(/*host = "localhost:4567",*/ //
info = @Info(description = "MusicHub API", //
version = "V1.0", //
title = "API for MusicHub REST Service", //
contact = @Contact(name = "Sigi", url = "http://www.sigitm.com") ) , //
schemes = { SwaggerDefinition.Scheme.HTTP/*, SwaggerDefinition.Scheme.HTTPS*/ }, //
host = "localhost:8080",
/*consumes = { "application/json" }, //*/
produces = { "application/json" }, //
tags = {
		@Tag(name = "health", description = "Health check"),
		@Tag(name = "songs", description = "Search for songs"), 
		@Tag(name = "devices", description = "Handle playback devices"),
		@Tag(name = "control", description = "Control selected device")
		})
public class RestApp {

	private Integer port;
	
	public RestApp(Integer port) {
		super();
		this.port = port;
	}

	public static final String APP_PACKAGE = "it.musichub.server.rest.impl";

	// exception handler during initialization phase
    private Consumer<Exception> initExceptionHandler = (e) -> {
        throw new RuntimeException("Error initializing Spark", e);
    };
    
	public void init() throws ServiceInitException {
		//specify what should happen if initialization fails:
		Spark.initExceptionHandler(initExceptionHandler);
		
		//init port
		port(port);
		
//		//basic auth
//		before(new BasicAuthenticationFilter("/path/*", new AuthenticationDetails("sigi", "sigitm")));
		
		//Spark uses filters to intercept any route, lets add a filter for "before"
		//we need to register a Filter that sets the JSON Content-Type.
        before((request, response) -> response.type("application/json"));
        
        //GZIP everything
        after((request, response) -> response.header("Content-Encoding", "gzip"));
	}
	
	public void start() {
		try {
			// Scan classes with @Api annotation and add as routes
			RouteBuilder.setupRoutes(APP_PACKAGE);

			// Build swagger json description
			final String swaggerJson = SwaggerParser.getSwaggerJson(APP_PACKAGE);
			get("/swagger", (req, res) -> {
				return swaggerJson;
			});

		} catch (Exception e) {
			throw new RuntimeException("Error starting Spark", e);
		}
	}
	
	public void stop() {
		Spark.stop();
	}

}
