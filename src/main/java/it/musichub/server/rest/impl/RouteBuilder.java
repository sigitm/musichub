package it.musichub.server.rest.impl;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import java.lang.reflect.Method;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.reflections.Reflections;

import io.swagger.annotations.Api;
import spark.Route;

public class RouteBuilder {

	public static void setupRoutes(String packageName) throws InstantiationException, IllegalAccessException {

		Reflections reflections = new Reflections(packageName);
		Set<Class<?>> apiRoutes = reflections.getTypesAnnotatedWith(Api.class);

		for (Class<?> clazz : apiRoutes) {
			Route sparkRoute = (Route) clazz.newInstance();
			Path path = clazz.getAnnotation(Path.class);
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				String friendlyRoute = path.value().replaceAll("\\{(.*?)\\}", ":$1");
				
				GET get = method.getAnnotation(GET.class);
				if (get != null) {
					get(friendlyRoute, sparkRoute, new JsonTransformer());
					break;
				}
				
				POST post = method.getAnnotation(POST.class);
				if (post != null) {
					post(friendlyRoute, sparkRoute, new JsonTransformer());
					break;
				}

				PUT put = method.getAnnotation(PUT.class);
				if (put != null) {
					put(friendlyRoute, sparkRoute, new JsonTransformer());
					break;
				}
				
				DELETE delete = method.getAnnotation(DELETE.class);
				if (delete != null) {
					delete(friendlyRoute, sparkRoute, new JsonTransformer());
					break;
				}

			}

		}
	}

}
