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

/**
 * controller that implements the API for Notifications
 * @author Rotem
 *
 */
@RequestMapping("/notifications")
@RestController
public class NotificationController {
	
	@Autowired
	NotificationService service;
	@Autowired
	NotificationManager notificationManager;
	@Autowired
	MessagePushService messagePushService;
	
	
	/**
	 * add a new notification to the DB
	 * adds the notification to the notification manager
	 * @param notification the notification 
	 * @param userId the id of the notification's user
	 * @param eventId the id of the notification's event (event of the user)
	 * @return the notification object with id, error if the process failed
	 */
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
	
	/**
	 * adds a list of notifications to the db
	 * adds the list of the notifications to the notification manager
	 * @param notifications the notifications
	 * @param userId the notifications user id
	 * @param eventId the notifications event id (event of the user)
	 * @return the list of the notifications with id, error if the process failed
	 */
	@RequestMapping(method = RequestMethod.POST, path="addlist/{userId}/{eventId}")
	public ResponseEntity<?> addNotifications(@RequestBody List<Notification> notifications,
			@PathVariable Integer userId, @PathVariable Integer eventId) {
		try {
			notifications = service.addNotifications(notifications, userId, eventId);
			notifications.forEach(t->notificationManager.addNotification(t));
			JsonUtils.nullifyFieldsInNotificationList(notifications);
			return ResponseEntity.status(HttpStatus.CREATED).body(notifications);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to add notifications to db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	/**
	 * get notification by id
	 * @param notificationId the id of the notification
	 * @return the notification object, error if the process failed
	 */
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

	/**
	 * get notifications by the request params
	 * eventId: get all notifications of event with id eventId
	 * email: get all notifications of user with email
	 * no param: get all notifications in db
	 * @param map the map of the requst params
	 * @return the list of the required notifications, error if the process failed 
	 */
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
	
	/**
	 * update notification
	 * adds the updated notification to the notification manager
	 * @param notification the notification
	 * @param userId the id of the notification's user (only the user is allowed to update their notification)
	 * @return the updated notification, error if the update failed
	 */
	@RequestMapping(method = RequestMethod.PUT, path="/{userId}")
	public ResponseEntity<?> updateNotification(@RequestBody Notification notification, @PathVariable Integer userId) {
		
		try {
			service.updateNotification(notification, userId);
			notification = service.getNotificationById(notification.getId());
			notificationManager.addNotification(notification);
			JsonUtils.nullifyFieldsInNotification(notification);
			return ResponseEntity.status(HttpStatus.OK).body(notification);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to update notification in db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	/**
	 * update list of notifications
	 * adds the updated notifications to the notification manager
	 * @param notifications the list of notifications
	 * @return the updated list, error if the update failed
	 */
	@RequestMapping(method = RequestMethod.PUT)
	public ResponseEntity<?> updateNotifications(@RequestBody List<Notification> notifications) {
		
		try {
			notifications = service.updatedNotifications(notifications);
			notifications.forEach(t->notificationManager.addNotification(t));
			JsonUtils.nullifyFieldsInNotificationList(notifications);
			return ResponseEntity.status(HttpStatus.OK).body(notifications);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to update notifications in db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	/**
	 * deactivates notification
	 * deletes notification in notification manager
	 * @param notificationId the id of the notification
	 * @return the notification object, error if the process failed
	 */
	@RequestMapping(method = RequestMethod.PUT, path="/{notificationId}/deactivate")
	public ResponseEntity<?> deactivateNotification(@PathVariable Integer notificationId) {
		
		try {
			service.deactivateNotification(notificationId);
			Notification notification = service.getNotificationById(notificationId);
			notificationManager.deleteNotification(notificationId);
			JsonUtils.nullifyFieldsInNotification(notification);
			return ResponseEntity.status(HttpStatus.OK).body(notification);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to deactivate notification in db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	/**
	 * deactivates list of notifications
	 * deletes the notifications from notification manager 
	 * @param notificationIds the ids of the notifications
	 * @return success message, error if failed
	 */
	@RequestMapping(method = RequestMethod.PUT, path="/deactivate")
	public ResponseEntity<?> deactivateNotifications(@RequestBody List<Integer> notificationIds) {
		
		try {
			service.deactivateNotifications(notificationIds);
			notificationIds.forEach(t->notificationManager.deleteNotification(t));
			return ResponseEntity.status(HttpStatus.OK).body("Notifications were successfully deactivated");
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to deactivate notifications in db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	/**
	 * deletes notification from db
	 * deletes the notification from the notification manager 
	 * @param notificationId the notification id
	 * @return success message, error if failed
	 */
	@RequestMapping(method = RequestMethod.DELETE, path="/{notificationId}")
	public ResponseEntity<?> deleteNotification(@PathVariable Integer notificationId) {
		
		try {
			service.deleteNotification(notificationId);
			notificationManager.deleteNotification(notificationId);
			return ResponseEntity.status(HttpStatus.OK).body("Notification was successfully deleted");
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to delete notification in db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	/**
	 * delete list of notification 
	 * delete the notifications from notification manager
	 * @param notificationIds the ids of the notifications
	 * @return success message, error if failed
	 */
	@RequestMapping(method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteNotifications(@RequestBody List<Integer> notificationIds) {
		
		try {
			service.deleteNotifications(notificationIds);
			notificationIds.forEach(t->notificationManager.deleteNotification(t));
			return ResponseEntity.status(HttpStatus.OK).body("Notifications were successfully deleted");
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to delete notifications in db");
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
