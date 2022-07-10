package ajbc.doodle.calendar.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.daos.UserDao;
import ajbc.doodle.calendar.entities.ErrorMessage;
import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.SubscriptionData;
import ajbc.doodle.calendar.entities.User;
import ajbc.doodle.calendar.entities.webpush.Subscription;
import ajbc.doodle.calendar.entities.webpush.SubscriptionEndpoint;
import ajbc.doodle.calendar.services.UserService;
import ajbc.doodle.calendar.utils.JsonUtils;

@RequestMapping("/users")
@RestController
public class UserController {

	
	@Autowired
	UserService service;
	
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
		if (keys.contains("eventId"))	
			list = service.getUsersByEvent(Integer.parseInt(map.get("eventId")));
		else if (keys.contains("email"))
			list.add(service.getUserByEmail(map.get("email")));
		else if (keys.contains("startTime") && keys.contains("endTime"))
			list = service.getUsersWithEventInRange(LocalDateTime.parse(map.get("startTime")),
					LocalDateTime.parse(map.get("endTime")));
		else
			list = service.getAllUsers();
		if (list == null)
			return ResponseEntity.notFound().build();
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
	
	
	@PostMapping("/subscribe/{email}")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<?> subscribe(@RequestBody Subscription subscription, @PathVariable(required = false) String email) {
		//if user is registered allow subscription
		//this.subscriptions.put(subscription.getEndpoint(), subscription);
		//for each user do 2 things
		//1. turn logged in flag to true
		//2 save 3 parameters in DB
		try {
			User user = service.getUserByEmail(email);
			String publicKey = subscription.getKeys().getP256dh();
			String authKey = subscription.getKeys().getAuth();
			String endPoint = subscription.getEndpoint();

			SubscriptionData subData = new SubscriptionData(publicKey, authKey, endPoint);
			user.setSubscriptionData(subData);
			service.updateUser(user);
			//user = userService.getUserById(user.getId());
			service.login(user);

			return ResponseEntity.ok("Logged in user with email: "+email);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to login user with email "+email);
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
//		System.out.println(subscription.getKeys().getP256dh());
//		System.out.println(subscription.getKeys().getAuth());
//		System.out.println(subscription.getEndpoint());
		
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
}
