package it.musichub.server.rest.impl;
import com.google.gson.Gson;

import spark.ResponseTransformer;
 
public class JsonTransformer implements ResponseTransformer {
 
    @Override
    public String render(Object model) {
        return getGson().toJson(model);
    }

	private static Gson getGson(){
    	Gson gson = new Gson();
    	return gson;
    }
	
}