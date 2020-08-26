package controllers.core;

import java.io.UnsupportedEncodingException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import models.UserToken;
import org.mindrot.jbcrypt.BCrypt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;

import exceptions.ValidateInputException;
import helpers.JsonResponse;
import helpers.Jwt;
import jwt.VerifiedJwt;
import jwt.JwtValidator.Error;

import models.User;
import play.libs.F;
import play.libs.Json;
import play.mvc.*;

import javax.persistence.PersistenceException;

public class AuthController extends Controller {

	public Result register(Http.Request request) throws Exception {
		JsonNode json = request.body().asJson();
		String email = json.findPath("email").textValue();
		String phone = json.findPath("phone").textValue();
		if (json.isNull()) {
			return ok(JsonResponse.error(JsonResponse.MSG_JSON_REQUIRED));
		} else {
			int isThere = User.find.query().where().eq("email", email).findCount();
			if (isThere > 0) {
				return badRequest(JsonResponse.error("Your email or phone is already register"));
			} else {
				User user = new User();
				user.setData(json);
				user.save();
				return ok(JsonResponse.success(user));
			}
		}
	}

	public Result account(Http.RequestHeader requestHeader)  {
		try {
			F.Either<Error, VerifiedJwt> res = Jwt.verifyJWT(requestHeader);
			if (res.left.isPresent()) {
				return forbidden(JsonResponse.error(res.left.get().toString()));
			}

			VerifiedJwt jwt = res.right.get();
			User user = User.find.query().where().eq("id",jwt.getUserId()).findOne();

			return ok(JsonResponse.success(user));
		} catch (IllegalArgumentException e) {
			return forbidden(JsonResponse.error(e.getMessage()));
		}
	}

	private User validateLogin(JsonNode json) throws ValidateInputException {
		String email = json.findPath("email").textValue();
		if (email == null || email.isEmpty()) {
			throw new ValidateInputException("Email is required", 400);
		}

		String password = json.findPath("password").textValue();
		if (password == null || password.isEmpty()) {
			throw new ValidateInputException("Email is required", 400);
		}

		User user = User.find.query().where().ieq("email", email).findOne();
		if (user == null) {
			throw new ValidateInputException("Sorry email address or password is incorrect", 422);
		}

		if (BCrypt.checkpw(password, user.password)) {
			return user;
		} else {
			return null;
		}
	}

	public Result login(Http.Request request) throws IllegalArgumentException, UnsupportedEncodingException {
		JsonNode json = request.body().asJson();
		if (json == null) {
			return ok(JsonResponse.error(JsonResponse.MSG_JSON_REQUIRED));
		} else {
			try {
				User user = validateLogin(json);
				if (user != null) {
					String token = Jwt.generateToken(user.id);
					String refresh_token = Jwt.generateRefreshToken(user.id);
					ObjectNode data = Json.newObject();
					data.putPOJO("user", user);
					data.put("token", token);
					data.put("token_refresh", refresh_token);
					System.out.println(ConfigFactory.load().getString("play.http.issue.key"));

					return ok(JsonResponse.success("Login success", data));
				} else {
					return ok(JsonResponse.error("Sorry, email address or password is incorrect", 1522));
				}
			} catch (ValidateInputException e) {
				if (e.getCode() > 0) {
					return ok(JsonResponse.error(e.getMessage(), e.getCode()));
				} else {
					return ok(JsonResponse.error(e.getMessage()));
				}
			}
		}
	}

	public Result logout(Http.RequestHeader requestHeader) {
		F.Either<Error, VerifiedJwt> res = Jwt.verifyJWT(requestHeader);
		if (res.left.isPresent()) {
			return forbidden(JsonResponse.error(res.left.get().toString()));
		}

		VerifiedJwt jwt = res.right.get();

		try {
			String token = jwt.getToken();

			if (token != null) {
				UserToken userToken = UserToken.find.query().where().eq("token", token).findOne();
				System.out.println(userToken.logout);
				if (userToken != null) {
					userToken.logout = true;
					String sql = "update user_tokens set logout=1 where token =?";
					int row = UserToken.db().createSqlUpdate(sql).setParameter(1,token).execute();
					return ok(JsonResponse.success());
				} else {
					return ok(JsonResponse.error("Token not found"));
				}
			} else {
				return ok(JsonResponse.error("Token not found"));
			}
		} catch (PersistenceException e) {
			return ok(JsonResponse.error(e.getMessage()));
		}
	}

}
