package controllers;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import helpers.JsonResponse;
import play.mvc.*;
import play.libs.Json;
import play.libs.ws.*;
import java.util.concurrent.CompletionStage;
public class TestRestController extends Controller{
	private final WSClient ws;
	
	@Inject
	public TestRestController(WSClient ws) {
		this.ws=ws;
	}
	
	public CompletionStage<Result> index() {		
		WSRequest request = ws.url("http://dummy.restapiexample.com/api/v1/employees");
		CompletionStage<? extends WSResponse> responsePromise = request.get();
		
		return responsePromise.thenApply(rs->{
			JsonNode res = rs.asJson().findPath("data");
			return ok(JsonResponse.success(res));
		});
		
	}
	
	public CompletionStage<Result> getWithId(String id) {
		try{
			WSRequest request = ws.url("https://jsonplaceholder.typicode.com/users/"+id);
			CompletionStage<? extends WSResponse> responsePromise = request.get();

			return responsePromise.thenApply(rs->{
				JsonNode res = rs.asJson();
				System.out.print(res);
				ObjectNode data = Json.newObject();
				data.putPOJO("data", res);
				return ok(JsonResponse.success(res));
			}).toCompletableFuture();
		}catch (Exception e){
			return (CompletionStage<Result>) badRequest(JsonResponse.error(e.getMessage(),400));
		}

		
	}

}
