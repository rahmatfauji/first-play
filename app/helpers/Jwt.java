package helpers;

import java.io.UnsupportedEncodingException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.typesafe.config.ConfigFactory;

import jwt.VerifiedJwt;
import play.libs.F;
import play.mvc.Http;
import views.html.defaultpages.error;
import jwt.VerifiedJwtImpl;
import jwt.JwtValidator.Error;
import models.UserToken;

public class Jwt {
	public static final String secret = ConfigFactory.load().getString("play.http.secret.key");
	public static final String issueString = ConfigFactory.load().getString("play.http.issue.key");

//	public static final String ADMIN_API_KEY = ConfigFactory.load().hasPath("play.path.admin_key")
//			? ConfigFactory.load().getString("play.server.admin_key")
//			: "";

	public static String generateToken(String userId) throws UnsupportedEncodingException {
		// expires at 6 hours
		ZonedDateTime zonedNow = ZonedDateTime.now(ZoneId.systemDefault());
		Date issuedAt = Date.from(zonedNow.toInstant());
		Date expiresAt = Date.from(zonedNow.plusHours(6).toInstant());

		Algorithm algorithm = Algorithm.HMAC256(secret);
		String token = JWT.create().withIssuer(issueString).withClaim("token_type", "token")
				.withClaim("user_id", userId).withExpiresAt(expiresAt).sign(algorithm);

		// insert generated token to db
		UserToken userToken = new UserToken(token, issuedAt, expiresAt, userId);
		userToken.save();

		return token;
	}

	public static String generateRefreshToken(String userId) throws UnsupportedEncodingException {
		// expires at 7 days
		Date expiresAt = Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusDays(7).toInstant());

		Algorithm algorithm = Algorithm.HMAC256(secret);
		return JWT.create().withIssuer(issueString).withClaim("token_type", "refresh_token")
				.withClaim("user_id", userId).withExpiresAt(expiresAt).sign(algorithm);
	}

	public static F.Either<Error, VerifiedJwt> verifyJWT(Http.RequestHeader RequestHeader) {
		try {
			Optional<String> authHeader = RequestHeader.getHeaders().get("Authorization");
			if (!authHeader.filter(ah -> ah.contains("Bearer ")).isPresent()) {
				return F.Either.Left(Error.INVALID_SIGNATURE_OR_CLAIM);
			}
			String token = authHeader.map(ah -> ah.replace("Bearer ", "")).orElse("");

			Algorithm algorithm = Algorithm.HMAC256(secret);
			JWTVerifier verifier = JWT.require(algorithm).withIssuer(issueString).build();
			DecodedJWT jwt = verifier.verify(token);
			VerifiedJwtImpl verifiedJwt = new VerifiedJwtImpl(jwt);

			UserToken userToken = UserToken.find.query().where().eq("token", token).findOne();
			if (userToken == null) {
				return F.Either.Left(Error.INVALID_SIGNATURE_OR_CLAIM);
			}

			if (userToken.logout) {
				return F.Either.Left(Error.TOKEN_EXPIRED);
			}

			String tokenType = verifiedJwt.getTokenType();
			if (tokenType != null && tokenType.equals("token")) {
				return F.Either.Right(verifiedJwt);
			} else {
				return F.Either.Left(Error.INVALID_TOKEN_TYPE);
			}

		} catch (JWTVerificationException e) {
			System.out.println(e.getMessage());
			if (e.getMessage().substring(0, 21).equals("The Token has expired")) {
				return F.Either.Left(Error.TOKEN_EXPIRED);
			} else {
				return F.Either.Left(Error.INVALID_SIGNATURE_OR_CLAIM);
			}
		} catch (IllegalArgumentException | UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
			return F.Either.Left(Error.INVALID_SIGNATURE_OR_CLAIM);
		}
	}
	
	public static F.Either<Error, VerifiedJwt> verifyJWTRefreshToken(String refresh_token){
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			JWTVerifier verifier = JWT.require(algorithm).withIssuer(issueString).build();
			DecodedJWT jwt = verifier.verify(refresh_token);
			VerifiedJwtImpl verifiedJwt = new VerifiedJwtImpl(jwt);
			
			String tokenType = verifiedJwt.getTokenType();
			if(tokenType != null && tokenType.equals("refresh_token")) {
				return F.Either.Right(verifiedJwt);
			}else {
				return F.Either.Left(Error.INVALID_TOKEN_TYPE);
			}
		}catch(JWTVerificationException e) {
			return F.Either.Left(Error.INVALID_SIGNATURE_OR_CLAIM);
		}catch(IllegalArgumentException | UnsupportedEncodingException e) {
			return F.Either.Left(Error.INVALID_SIGNATURE_OR_CLAIM);
		}
	}
}
