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
import ajbc.doodle.calendar.entities.webpush.NotificationManager;
import ajbc.doodle.calendar.services.MessagePushService;
import ajbc.doodle.calendar.services.NotificationService;
import ajbc.doodle.calendar.utils.JsonUtils;
import org.springframework.web.bind.annotation.GetMapping;

@RequestMapping("/notifications")
@RestController
public class NotificationController {
	
	@Autowired
	NotificationService service;
	@Autowired
	NotificationManager notificationManager;
	@Autowired
	MessagePushService messagePushService;
	
	@RequestMapping(method = RequestMethod.POST, path="/{userId}/{eventId}")
	public ResponseEntity<?> addNotification(@RequestBody Notification notification,
			@PathVariable Integer userId, @PathVariable Integer eventId) {
		try {
			service.addNotification(notification, userId, eventId);
			notification = service.getNotificationById(notification.getId());
			notificationManager.addNotification(notification);
			JsonUtils.nullifyFieldsInNotification(notification);
			return ResponseEntity.status(HttpStatus.CREATED).body(notification);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to add notification to db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	@RequestMapping(method = RequestMethod.POST, path="addlist/{userId}/{eventId}")
	public ResponseEntity<?> addNotifications(@RequestBody List<Notification> notifications,
			@PathVariable Integer userId, @PathVariable Integer eventId) {
		try {
			notifications = service.addNotifications(notifications, userId, eventId);
			JsonUtils.nullifyFieldsInNotificationList(notifications);
			return ResponseEntity.status(HttpStatus.CREATED).body(notifications);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to add notifications to db");
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
	public ResponseEntity<?> getNotifications(@RequestParam Map<String, String> map) {
		List<Notification> list = new ArrayList<>();
		Set<String> keys = map.keySet();
		try {
		if (keys.contains("eventId"))
		{
			list = service.getNotificationsByEvent(Integer.parseInt(map.get("eventId")));
			JsonUtils.nullifyEventsInNotificationList(list);
		}
		else if (keys.contains("email"))
		{
			list = service.getNotificationsByUserEmail(map.get("email"));
			JsonUtils.nullifyUsersInNotificationList(list);
		}
		else
		{
			list = service.getAllNotifications();
			JsonUtils.nullifyFieldsInNotificationList(list);
		}
		}
		catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to get notifications");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
		}
		return ResponseEntity.ok(list);
	}
	
	@RequestMapping(method = RequestMethod.PUT, path="/{userId}")
	public ResponseEntity<?> updateNotification(@RequestBody Notification notification, @PathVariable Integer userId) {
		
		try {
			service.updateNotification(notification, userId);
			notification = service.getNotificationById(notification.getId());
			//notificationManager.addNotification(notification);
			JsonUtils.nullifyFieldsInNotification(notification);
			return ResponseEntity.status(HttpStatus.OK).body(notification);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to update notification in db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	@RequestMapping(method = RequestMethod.PUT, path="/{notificationId}/deactivate")
	public ResponseEntity<?> deactivateNotification(@PathVariable Integer notificationId) {
		
		try {
			service.deactivateNotification(notificationId);
			Notification notification = service.getNotificationById(notificationId);
			notificationManager.addNotification(notification);
			JsonUtils.nullifyFieldsInNotification(notification);
			return ResponseEntity.status(HttpStatus.OK).body(notification);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to deactivate notification in db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	@RequestMapping(method = RequestMethod.DELETE, path="/{notificationId}")
	public ResponseEntity<?> deleteNotification(@PathVariable Integer notificationId) {
		
		try {
			service.deleteNotification(notificationId);
			return ResponseEntity.status(HttpStatus.OK).body("Notification was successfully deleted");
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to delete notification in db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	@GetMapping(path = "/publicSigningKey", produces = "application/octet-stream")
	public byte[] publicSigningKey() {
		return messagePushService.getServerKeys().getPublicKeyUncompressed();
	}

	@GetMapping(path = "/publicSigningKeyBase64")
	public String publicSigningKeyBase64() {
		return  messagePushService.getServerKeys().getPublicKeyBase64();
	}
	

}
