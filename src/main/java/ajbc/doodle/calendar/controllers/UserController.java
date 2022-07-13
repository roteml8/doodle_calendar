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

/**
 * controller that implements the API for Users
 * @author Rotem
 *
 */
@RequestMapping("/users")
@RestController
public class UserController {

	
	@Autowired
	UserService service;
	
	@Autowired
	MessagePushService messagePushService;
	
	
	/**
	 * add a list of users to db
	 * @param users the list of users
	 * @return the list of users with ids, error if failed
	 */
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
	
	/**
	 * add a user to db
	 * @param user the user
	 * @return the user with id, error if failed
	 */
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
	
	/**
	 * get a user by id
	 * @param id the id
	 * @return the user with the given id, error if failed
	 */
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
	
	/**
	 * get users by request params
	 * eventId: get all users that are part of the event with id eventId
	 * email: get the user with the given email
	 * startTime, endTime: get all users that have en event in the range of startTime to endTime
	 * no param: get all users
	 * @param map the map of request params
	 * @return the list of the required users, error if failed
	 */
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getUsers(@RequestParam Map<String, String> map) {
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
	
	/**
	 * update a user
	 * @param user the updated user object
	 * @param id the id of the user
	 * @return the updated user, error if failed
	 */
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
	
	/**
	 * update list of users
	 * @param users list of users to update
	 * @return the list of updated users, error if failed
	 */
	@RequestMapping(method = RequestMethod.PUT)
	public ResponseEntity<?> updateUsers(@RequestBody List<User> users) {
		
		try {
			users = service.updateUsers(users);
			JsonUtils.nullifyFieldsInUserList(users);
			return ResponseEntity.status(HttpStatus.OK).body(users);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to update users in db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	/**
	 * deactivates a user
	 * deactivates the user notifications
	 * deactivates the events that the user owns
	 * @param id the id of the user to deactivate
	 * @return the updated user object, error if failed
	 */
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
	
	/**
	 * deletes a user from the db
	 * deletes the user notifications
	 * deletes the events that the user owns
	 * @param id the id of the user
	 * @return success message, error if failed
	 */
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
	
	/**
	 * subscribe (login) user to receive push messages
	 * sets the user as logged in
	 * @param subscription subscription data sent from the browser
	 * @param email the user email
	 * @return success message, error if failed
	 * @throws InvalidKeyException
	 * @throws JsonProcessingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws InvalidAlgorithmParameterException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
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
			return ResponseEntity.ok("Logged in user with email: "+email);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to login user with email "+email);
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}

		
	}
	
	/**
	 * unsubscribe (logout) user from push messages
	 * sets the user as logged out
	 * @param subscription end point from browser
	 * @param email the user email
	 * @return success message, error if failed
	 */
	@PostMapping("/unsubscribe/{email}")
	public ResponseEntity<?> unsubscribe(@RequestBody SubscriptionEndpoint subscription, @PathVariable(required = false) String email) {
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
	
	/**
	 * checks if a user is currently subscribed to push messages by checking endpoint
	 * @param subscription end point from browser
	 * @return true if a user is subscribed, false otherwise 
	 * @throws DaoException
	 */
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
