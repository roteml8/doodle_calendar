package ajbc.doodle.calendar.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.daos.UserDao;
import ajbc.doodle.calendar.entities.ErrorMessage;
import ajbc.doodle.calendar.entities.User;
import ajbc.doodle.calendar.services.UserService;
import ajbc.doodle.calendar.utils.JsonUtils;

@RequestMapping("/users")
@RestController
public class UserController {

	
	@Autowired
	UserService service;
	
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
	
	@RequestMapping(method = RequestMethod.PUT, path="/login/{email}")
	public ResponseEntity<?> login(@PathVariable String email) throws DaoException {
		try {
			service.login(email);
			return ResponseEntity.ok(email);
		}
		catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to login user with email "+email);
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
		
	}
	
	@RequestMapping(method = RequestMethod.PUT, path="/logout/{email}")
	public ResponseEntity<?> logout(@PathVariable String email) throws DaoException {
		try {
			service.logout(email);
			return ResponseEntity.ok(email);
		}
		catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to logout user with email "+email);
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
		
	}
	
	
}
