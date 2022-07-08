package ajbc.doodle.calendar.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import ajbc.doodle.calendar.entities.ErrorMessage;
import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.services.NotificationService;
import ajbc.doodle.calendar.utils.JsonUtils;

@RequestMapping("/notifications")
@RestController
public class NotificationController {
	
	@Autowired
	NotificationService service;
	
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> addNotification(@RequestBody Notification notification) {
		
		try {
			service.addNotification(notification);
			notification = service.getNotificationById(notification.getId());
			JsonUtils.nullifyFieldsInNotification(notification);
			return ResponseEntity.status(HttpStatus.CREATED).body(notification);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to add notification to db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, path="/{notificationId}")
	public ResponseEntity<?> getNotificationById(@PathVariable Integer notificationId) {
		
		Notification notification;
		try {
			notification = service.getNotificationById(notificationId);
			JsonUtils.nullifyFieldsInNotification(notification);
			return ResponseEntity.ok(notification);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to get notification with id "+notificationId);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
		}
	}

	
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getNotifications(@RequestParam Map<String, String> map) throws DaoException {
		List<Notification> list = new ArrayList<>();
		Set<String> keys = map.keySet();
		if (keys.contains("eventId"))
			list = service.getNotificationsByEvent(Integer.parseInt(map.get("eventId")));
		if (list == null)
			return ResponseEntity.notFound().build();
		JsonUtils.nullifyFieldsInNotificationList(list);

		return ResponseEntity.ok(list);
	}
	
	@RequestMapping(method = RequestMethod.PUT, path="/{userId}")
	public ResponseEntity<?> updateNotification(@RequestBody Notification notification, @PathVariable Integer userId) {
		
		try {
			service.updateNotification(notification, userId);
			notification = service.getNotificationById(notification.getId());
			JsonUtils.nullifyFieldsInNotification(notification);
			return ResponseEntity.status(HttpStatus.OK).body(notification);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to update notification in db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
}
