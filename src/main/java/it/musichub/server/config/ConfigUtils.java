package it.musichub.server.config;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import it.musichub.server.library.IndexerServiceImpl;

public class ConfigUtils implements Serializable {

	private final static Logger logger = Logger.getLogger(ConfigUtils.class);
	

	public static ValidateResult validate(Configuration config) {
		ValidateResult result = new ValidateResult();
		
		//required fields
		Field[] fields = config.getClass().getDeclaredFields();
		for (Field field : fields) {
		    if (field.isAnnotationPresent(Required.class)) {
		    	Object value = null;
		    	try {
		    		field.setAccessible(true);
					value = field.get(config);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					logger.warn("Error accessing field "+field.getName()+" for Required checking",e);
					continue;
				}
		    	if (value == null){
		    		result.setOk(false);
		    		result.addError("Required field "+field.getName()+" is not set.");
		    	}
		    }
		}
		
		//semantic validations
		//xxxxxxxxXXXXXXXXXXXXX
		
		return result;
	}
	
	public static class ValidateResult {
		private boolean ok = true;
		private List<String> errors = new ArrayList<>();
		
		public ValidateResult() {
			super();
		}

		public boolean isOk() {
			return ok;
		}
		public void setOk(boolean ok) {
			this.ok = ok;
		}
		public List<String> getErrors() {
			return errors;
		}
		public void setErrors(List<String> errors) {
			this.errors = errors;
		}
		public void addError(String error) {
			getErrors().add(error);
		}
	}
}
