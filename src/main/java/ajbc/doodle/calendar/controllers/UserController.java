package ajbc.doodle.calendar.controllers;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import ajbc.doodle.calendar.Application;
import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.daos.UserDao;
import ajbc.doodle.calendar.entities.ErrorMessage;
import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.SubscriptionData;
import ajbc.doodle.calendar.entities.User;
import ajbc.doodle.calendar.entities.webpush.PushMessage;
import ajbc.doodle.calendar.entities.webpush.Subscription;
import ajbc.doodle.calendar.entities.webpush.SubscriptionEndpoint;
import ajbc.doodle.calendar.services.MessagePushService;
import ajbc.doodle.calendar.services.UserService;
import ajbc.doodle.calendar.utils.JsonUtils;

@RequestMapping("/users")
@RestController
public class UserController {

	
	@Autowired
	UserService service;
	
	@Autowired
	MessagePushService messagePushService;
	
	@RequestMapping(method = RequestMethod.POST, path="/addlist")
	public ResponseEntity<?> addUsers(@RequestBody List<User> users) {
		
		try {
				users = service.addUsers(users);
				JsonUtils.nullifyFieldsInUserList(users);
				return ResponseEntity.status(HttpStatus.CREATED).body(users);
			} catch (DaoException e) {
				ErrorMessage errorMessage = new ErrorMessage();
				errorMessage.setData(e.getMessage());
				errorMessage.setMessage("failed to add users to db");
				return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
			}
		
		} 
	
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> addUser(@RequestBody User user) {
		
		try {
			service.addUser(user);
			user = service.getUser(user.getId());
			JsonUtils.nullifyFieldsInUser(user);
			return ResponseEntity.status(HttpStatus.CREATED).body(user);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to add user to db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, path="/{id}")
	public ResponseEntity<?> getUserById(@PathVariable Integer id) {
		
		User user;
		try {
			user = service.getUser(id);
			JsonUtils.nullifyFieldsInUser(user);
			return ResponseEntity.ok(user);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to get user with id "+id);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
		}
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getUsers(@RequestParam Map<String, String> map) throws DaoException {
		List<User> list = new ArrayList<>();
		Set<String> keys = map.keySet();
		try {
		if (keys.contains("eventId"))	
			list = service.getUsersByEvent(Integer.parseInt(map.get("eventId")));
		else if (keys.contains("email"))
			list.add(service.getUserByEmail(map.get("email")));
		else if (keys.contains("startTime") && keys.contains("endTime"))
			list = service.getUsersWithEventInRange(LocalDateTime.parse(map.get("startTime")),
					LocalDateTime.parse(map.get("endTime")));
		else
			list = service.getAllUsers();
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to get users in db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
		JsonUtils.nullifyFieldsInUserList(list);
		return ResponseEntity.ok(list);
	}
	
	@RequestMapping(method = RequestMethod.PUT, path="/{id}")
	public ResponseEntity<?> updateUser(@RequestBody User user, @PathVariable Integer id) {
		
		try {
			user.setId(id);
			service.updateUser(user);
			user = service.getUser(user.getId());
			JsonUtils.nullifyFieldsInUser(user);
			return ResponseEntity.status(HttpStatus.OK).body(user);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to update user in db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	@RequestMapping(method = RequestMethod.PUT, path="/{id}/deactivate")
	public ResponseEntity<?> deactivateUser(@PathVariable Integer id) {
		
		try {
			service.deactivate(id);
			User user = service.getUser(id);
			JsonUtils.nullifyFieldsInUser(user);
			return ResponseEntity.status(HttpStatus.OK).body(user);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to deactivate user in db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	@RequestMapping(method = RequestMethod.DELETE, path="/{id}")
	public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
		
		try {
			service.deleteUser(id);
			return ResponseEntity.status(HttpStatus.OK).body("User was successfully deleted from db");
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to delete user in db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	
	@PostMapping("/subscribe/{email}")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<?> subscribe(@RequestBody Subscription subscription, @PathVariable(required = false) String email) throws InvalidKeyException, JsonProcessingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

		try {
			User user = service.getUserByEmail(email);
			String publicKey = subscription.getKeys().getP256dh();
			String authKey = subscription.getKeys().getAuth();
			String endPoint = subscription.getEndpoint();

			SubscriptionData subData = new SubscriptionData(publicKey, authKey, endPoint);
			user.setSubscriptionData(subData);
			service.updateUser(user);
			service.login(user);
		//	messagePushService.sendPushMessage(user, messagePushService.encryptMessage(user, new PushMessage("message: ", "hello")));
			return ResponseEntity.ok("Logged in user with email: "+email);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to login user with email "+email);
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}

		
	}
	
	@PostMapping("/unsubscribe/{email}")
	public ResponseEntity<?> unsubscribe(@RequestBody SubscriptionEndpoint subscription, @PathVariable(required = false) String email) {
		//this.subscriptions.remove(subscription.getEndpoint());
		try {
			User user = service.getUserByEmail(email);
			service.logout(user);
			return ResponseEntity.ok("Logged out user with email: "+email);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to logout user with email "+email);
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	
	@PostMapping("/isSubscribed")
	public boolean isSubscribed(@RequestBody SubscriptionEndpoint subscription) throws DaoException {
		List<User> users = service.getAllUsers();
		for (User user : users) {
			if (user.getSubscriptionData() != null) {
				if (user.getSubscriptionData().getEndpoint().equals(subscription.getEndpoint()))
					return true;
			}
		}
		return false;
	}


}
