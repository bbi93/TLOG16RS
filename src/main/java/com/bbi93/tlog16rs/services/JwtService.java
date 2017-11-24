package com.bbi93.tlog16rs.services;

import com.bbi93.tlog16rs.entities.TimeLogger;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Random;
import org.apache.commons.codec.digest.DigestUtils;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;

/**
 *
 * @author bbi93
 */
public class JwtService {

	private static final Random RANDOM = new SecureRandom();
	private final int SALT_SIZE = 32;
	private final String secret = "JstqLDmfEFjtyJ73BVKd8Ht427VrsGZ5";
	private final float expirationTimeInMinutes = 5;

	public String generateSalt() {
		byte[] salt = new byte[SALT_SIZE];
		RANDOM.nextBytes(salt);
		return DigestUtils.sha256Hex(salt);
	}

	public String encodePasswordWithSalt(String password, String salt) {
		return DigestUtils.sha256Hex(new StringBuilder().append(password).append(salt).toString());
	}

	/**
	 * Generates JWT for the timelogger
	 *
	 * @param timelogger
	 * @return with the generated token
	 * @throws UnsupportedEncodingException
	 * @throws JoseException
	 */
	public String generateJwtToken(TimeLogger timelogger) throws UnsupportedEncodingException, JoseException {
		JwtClaims claims = new JwtClaims();
		JsonWebSignature jws = new JsonWebSignature();
		claims.setExpirationTimeMinutesInTheFuture(expirationTimeInMinutes);
		claims.setSubject(timelogger.getName());
		jws.setPayload(claims.toJson());
		jws.setKey(new HmacKey(secret.getBytes("UTF-8")));
		jws.setKeyIdHeaderValue("kid");
		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
		jws.setDoKeyValidation(false);
		return jws.getCompactSerialization();
	}

	/**
	 * Validates the token and gives the name of the timelogger
	 *
	 * @param token
	 * @return with the name of the timelogger
	 * @throws InvalidJwtException
	 */
	public String getNameFromJwtToken(String token) throws InvalidJwtException {
		JwtConsumer jwtConsumer = new JwtConsumerBuilder()
			.setVerificationKey(new HmacKey(secret.getBytes()))
			.setRelaxVerificationKeyValidation()
			.setSkipSignatureVerification()
			.build();
		jwtConsumer.processContext(jwtConsumer.process(token));
		JwtClaims jwtClaims = jwtConsumer.processToClaims(token);
		return jwtClaims.getClaimValue("sub").toString();
	}

}
