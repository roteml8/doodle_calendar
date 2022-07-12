package ajbc.doodle.calendar.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ajbc.doodle.calendar.Application;
import ajbc.doodle.calendar.ServerKeys;
import ajbc.doodle.calendar.entities.User;
import lombok.Getter;

@Component
@Getter
public class MessagePushService {

	private final CryptoService cryptoService;
	private final ObjectMapper objectMapper;
	private final ServerKeys serverKeys;
	private final HttpClient httpClient;
	private final Algorithm jwtAlgorithm;

	public MessagePushService(CryptoService cryptoService, ObjectMapper objectMapper, ServerKeys serverKeys) {
		this.cryptoService = cryptoService;
		this.objectMapper = objectMapper;
		this.serverKeys = serverKeys;

		this.httpClient = HttpClient.newHttpClient();
		this.jwtAlgorithm = Algorithm.ECDSA256(this.serverKeys.getPublicKey(), this.serverKeys.getPrivateKey());
	}



	public byte[] encryptMessage(User user, Object message)
			throws InvalidKeyException, JsonProcessingException, NoSuchAlgorithmException, InvalidKeySpecException,
			InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		byte[] result = cryptoService.encrypt(objectMapper.writeValueAsString(message),
				user.getSubscriptionData().getPublicKey(), user.getSubscriptionData().getAuthKey(), 0);
		return result;
	}

	public boolean sendPushMessage(User user, byte[] body) {
		String origin = null;
		try {
			URL url = new URL(user.getSubscriptionData().getEndpoint());
			origin = url.getProtocol() + "://" + url.getHost();
		} catch (MalformedURLException e) {
			Application.logger.error("create origin", e);
			return true;
		}

		Date today = new Date();
		Date expires = new Date(today.getTime() + 12 * 60 * 60 * 1000);

		String token = JWT.create().withAudience(origin).withExpiresAt(expires)
				.withSubject("mailto:example@example.com").sign(this.jwtAlgorithm);

		URI endpointURI = URI.create(user.getSubscriptionData().getEndpoint());

		Builder httpRequestBuilder = HttpRequest.newBuilder();
		if (body != null) {
			httpRequestBuilder.POST(BodyPublishers.ofByteArray(body)).header("Content-Type", "application/octet-stream")
					.header("Content-Encoding", "aes128gcm");
		} else {
			httpRequestBuilder.POST(BodyPublishers.ofString(""));
			// httpRequestBuilder.header("Content-Length", "0");
		}

		HttpRequest request = httpRequestBuilder.uri(endpointURI).header("TTL", "180")
				.header("Authorization", "vapid t=" + token + ", k=" + this.serverKeys.getPublicKeyBase64()).build();
		try {
			HttpResponse<Void> response = this.httpClient.send(request, BodyHandlers.discarding());

			switch (response.statusCode()) {
			case 201:
				Application.logger.info("Push message successfully sent: {}", user.getSubscriptionData().getEndpoint());
				break;
			case 404:
			case 410:
				Application.logger.warn("Subscription not found or gone: {}", user.getSubscriptionData().getEndpoint());
				// remove subscription from our collection of subscriptions
				return true;
			case 429:
				Application.logger.error("Too many requests: {}", request);
				break;
			case 400:
				Application.logger.error("Invalid request: {}", request);
				break;
			case 413:
				Application.logger.error("Payload size too large: {}", request);
				break;
			default:
				Application.logger.error("Unhandled status code: {} / {}", response.statusCode(), request);
			}
		} catch (IOException | InterruptedException e) {
			Application.logger.error("send push message", e);
		}

		return false;
	}
}