package controllers;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import helpers.JsonResponse;
import helpers.Jwt;
import io.ebean.PagedList;
import jwt.JwtValidator;
import jwt.VerifiedJwt;
import models.User;
import play.libs.F;
import play.mvc.*;
import play.libs.Json;
import play.libs.ws.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
public class TestRestController extends Controller{
	private final WSClient ws;
	
	@Inject
	public TestRestController(WSClient ws) {
		this.ws=ws;
	}
	
	public CompletionStage<Result> index(Http.RequestHeader requestHeader) {
		try {
			F.Either<JwtValidator.Error, VerifiedJwt> res = Jwt.verifyJWT(requestHeader);
			if (res.left.isPresent()) {
				return CompletableFuture.supplyAsync(() -> {
					return forbidden(JsonResponse.error(res.left.get().toString()));
				});
			}

			VerifiedJwt jwt = res.right.get();
			WSRequest request = ws.url("https://jsonplaceholder.typicode.com/users/");
			CompletionStage<? extends WSResponse> responsePromise = request.get();

			return responsePromise.thenApply(rs->{
				JsonNode resp = rs.asJson();
				return ok(JsonResponse.success(resp));
			});

		} catch (IllegalArgumentException e) {
			return CompletableFuture.supplyAsync(() -> {
				return forbidden(JsonResponse.error(e.getMessage()));
			});
		}
		
	}
	
	public CompletionStage<Result> getWithId(String id, Http.RequestHeader requestHeader) {
		try {
			F.Either<JwtValidator.Error, VerifiedJwt> res = Jwt.verifyJWT(requestHeader);
			if (res.left.isPresent()) {
				return CompletableFuture.supplyAsync(() -> {
					return forbidden(JsonResponse.error(res.left.get().toString()));
				});
			}
			System.out.println(id);
			String url ="https://jsonplaceholder.typicode.com/users/";
			if(id != null){
				url = "https://jsonplaceholder.typicode.com/users/"+id;
			}

			WSRequest request = ws.url(url);
			CompletionStage<? extends WSResponse> responsePromise = request.get();

			return responsePromise.thenApply(rs->{
				JsonNode resp = rs.asJson();
				return ok(JsonResponse.success(resp));
			});

		} catch (IllegalArgumentException e) {
			return CompletableFuture.supplyAsync(() -> {
				return forbidden(JsonResponse.error(e.getMessage()));
			});
		}

		
	}

	public Result userShow(int paging, Http.RequestHeader requestHeader){
		try {
			F.Either<JwtValidator.Error, VerifiedJwt> res = Jwt.verifyJWT(requestHeader);
			if (res.left.isPresent()) {
					return forbidden(JsonResponse.error(res.left.get().toString()));
			}
			Integer page = paging;
			page = (page <= 0)?1:page;
			Integer limit = 5;

			int indexPage = (page - 1) * limit;
			PagedList<User> userPagedList = User.find.query().orderBy("CREATED_AT ASC").setFirstRow(indexPage).setMaxRows(limit).findPagedList();
			int totalPage = userPagedList.getTotalPageCount();
			int totalData = userPagedList.getTotalCount();
			int getPage = userPagedList.getPageIndex()+1;

			ObjectNode metaResult= Json.newObject();
			metaResult.put("totalPages", totalPage);
			metaResult.put("totalRows", totalData);
			metaResult.put("getPage", getPage);
			List<User> list = userPagedList.getList();
			ObjectNode data = Json.newObject().putPOJO("meta",metaResult).putPOJO("data", list);
			return ok(JsonResponse.success(data));


		} catch (IllegalArgumentException e) {

				return forbidden(JsonResponse.error(e.getMessage()));

		}


	}
}
