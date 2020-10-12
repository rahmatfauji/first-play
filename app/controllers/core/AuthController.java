package controllers.core;

import java.io.UnsupportedEncodingException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import helpers.KeyCorrector;
import io.ebean.Expr;
import models.GeneralInformation;
import models.UserRole;
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

    public Result register(Http.Request request) {
        try {
            Optional<String> api_key = request.getHeaders().get("secret-api-key");
            if (!api_key.isPresent() || !KeyCorrector.check_api_key(api_key.get())) {
                return forbidden(JsonResponse.error("You are not authorization"));
            }

            JsonNode json = request.body().asJson();
            validateRegister(json);
            String email = json.findPath("email").textValue();
            String username = json.findPath("username").textValue();
            if (json.isNull()) {
                return ok(JsonResponse.error(JsonResponse.MSG_JSON_REQUIRED));
            }
            int isThere = User.find.query().where().or(Expr.eq("email", email), Expr.eq("username", username)).findCount();
            System.out.println(isThere);
            if (isThere > 0) {
                return badRequest(JsonResponse.error("Your email or username is already register"));
            } else {
                User.db().beginTransaction();
                User user = new User();
                user.setData(json);
                user.save();

                GeneralInformation generalInformation = new GeneralInformation();
                generalInformation.setData(user.id, json);
                generalInformation.save();

                UserRole userRole = new UserRole();
                userRole.setData(user.id, json);
                userRole.save();

                User.db().commitTransaction();

                return ok(JsonResponse.success(user));
            }

        } catch (Exception e) {
            return badRequest(JsonResponse.error(e.getMessage()));
        }

    }

    public Result login(Http.Request request) throws IllegalArgumentException, UnsupportedEncodingException {
        Optional<String> api_key = request.getHeaders().get("secret-api-key");
        if (!api_key.isPresent() || !KeyCorrector.check_api_key(api_key.get())) {
            return forbidden(JsonResponse.error("You are not authorization"));
        }
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
        try {
            Optional<String> api_key = requestHeader.getHeaders().get("secret-api-key");
            if (!api_key.isPresent() || !KeyCorrector.check_api_key(api_key.get())) {
                return forbidden(JsonResponse.error("You are not authorization"));
            }

            F.Either<Error, VerifiedJwt> res = Jwt.verifyJWT(requestHeader);
            if (res.left.isPresent()) {
                return forbidden(JsonResponse.error(res.left.get().toString()));
            }

            VerifiedJwt jwt = res.right.get();
            String token = jwt.getToken();

            if (token != null) {
                UserToken userToken = UserToken.find.query().where().eq("token", token).findOne();
                System.out.println(userToken.logout);
                if (userToken != null) {
                    userToken.logout = true;
                    String sql = "update user_tokens set logout=1 where token =?";
                    int row = UserToken.db().createSqlUpdate(sql).setParameter(1, token).execute();
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

    public Result account(Http.RequestHeader requestHeader) {
        try {
            Optional<String> api_key = requestHeader.getHeaders().get("secret-api-key");
            if (!api_key.isPresent() || !KeyCorrector.check_api_key(api_key.get())) {
                return forbidden(JsonResponse.error("You are not authorization"));
            }

            F.Either<Error, VerifiedJwt> res = Jwt.verifyJWT(requestHeader);
            if (res.left.isPresent()) {
                return forbidden(JsonResponse.error(res.left.get().toString()));
            }

            VerifiedJwt jwt = res.right.get();
            User user = User.find.query().where().eq("id", jwt.getUserId()).findOne();

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

    private void validateRegister(JsonNode json) throws ValidateInputException {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        String regex2 = "^\\S([A-Za-z])\\w+";

        String email = json.findPath("email").textValue();
        if (email == null || email.isEmpty()) {
            throw new ValidateInputException("Email is required", 400);
        }

        if (!email.matches(regex)) {
            throw new ValidateInputException("Email format is not correct", 400);
        }

        String password = json.findPath("password").textValue();
        if (password == null || password.isEmpty()) {
            throw new ValidateInputException("Password is required", 400);
        }


        String username = json.findPath("username").textValue();
        if (username == null || username.isEmpty()) {
            throw new ValidateInputException("Username is required", 400);
        }
        if (!username.matches(regex2)) {
            throw new ValidateInputException("Username is cannot use special characters", 400);
        }

        String fullname = json.findPath("fullname").textValue();
        if (fullname == null || fullname.isEmpty()) {
            throw new ValidateInputException("Fullname is required", 400);
        }

        String gender = json.findPath("gender").textValue();
        if (gender == null || gender.isEmpty()) {
            throw new ValidateInputException("Gender is required", 400);
        }

        String dob = json.findPath("dob").textValue();
        if (dob == null || dob.isEmpty()) {
            throw new ValidateInputException("Date of Birth is required", 400);
        }

        String phone = json.findPath("phone").textValue();
        if (phone == null || phone.isEmpty()) {
            throw new ValidateInputException("Phone is required", 400);
        }

        String address = json.findPath("address").textValue();
        if (address == null || address.isEmpty()) {
            throw new ValidateInputException("Address is required", 400);
        }

        String city = json.findPath("city").textValue();
        if (city == null || city.isEmpty()) {
            throw new ValidateInputException("City is required", 400);
        }

        String province = json.findPath("province").textValue();
        if (province == null || province.isEmpty()) {
            throw new ValidateInputException("Province is required", 400);
        }

        String country = json.findPath("country").textValue();
        if (country == null || country.isEmpty()) {
            throw new ValidateInputException("Country is required", 400);
        }

        int role = json.findPath("role").asInt();
        if (role == 0) {
            throw new ValidateInputException("Role is required", 400);
        }


    }


}
