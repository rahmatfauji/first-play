package helpers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

public class JsonResponse {
	public static final String MSG_JSON_REQUIRED = "JSON content required";
	public static final String MSG_REG_OK = "Registration success. Please check your email to activate the account.";
	public static final String MSG_REG_CONFIRM_OK = "Registration confirmation success";
	public static final String MSG_LOGIN_OK = "Login success";
	public static final String MSG_LOGIN_FAILED = "Invalid email or password";
	public static final String MSG_DATA_NOT_FOUND = "Data not found";
	
	public static ObjectNode success() {
		ObjectNode result = Json.newObject();
		result.put("status", true);
		result.put("message", "Success");
		return result;
	}
	
	public static ObjectNode success(String message) {
		ObjectNode result = Json.newObject();
		result.put("status", true);
		result.put("message", message);
		return result;
	}
	
	public static ObjectNode success(String message, int code) {
		ObjectNode result = Json.newObject();
	    result.put("status", true);
	    result.put("message", message);
	    result.put("code", code);
	    return result;
	}
	
	public static ObjectNode success(String message, Object data) {
		ObjectNode result = Json.newObject();
	    result.put("status", true);
	    result.put("message", message);
	    result.putPOJO("data", data);
	    return result;
	}
	
	public static ObjectNode success(String message, int code, Object data) {
		ObjectNode result = Json.newObject();
	    result.put("status", true);
	    result.put("message", message);
	    result.put("code", code);
	    result.putPOJO("data", data);
	    return result;
	}
	
	public static ObjectNode success(int code, Object data) {
		ObjectNode result = Json.newObject();
	    result.put("status", true);
	    result.put("message", "Success");
	    result.put("code", code);
	    result.putPOJO("data", data);
	    return result;
	}
	
	public static ObjectNode success(Object data) {
		ObjectNode result = Json.newObject();
	    result.put("status", true);
	    result.put("message", "Success");
	    result.putPOJO("data", data);
	    return result;
	}
	
	public static ObjectNode error(String message) {
		ObjectNode result = Json.newObject();
	    result.put("status", false);
	    result.put("message", message);
	    return result;
	}
	
	public static ObjectNode error(String message, int code) {
		ObjectNode result = Json.newObject();
	    result.put("status", false);
	    result.put("message", message);
	    result.put("code", code);
	    return result;
	}
}
