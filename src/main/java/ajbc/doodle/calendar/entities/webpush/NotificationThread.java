package ajbc.doodle.calendar.entities.webpush;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
//import ajbc.doodle.calendar.entities.webpush.PushMessage;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.User;
import ajbc.doodle.calendar.services.EventService;
import ajbc.doodle.calendar.services.MessagePushService;
import ajbc.doodle.calendar.services.NotificationService;
import ajbc.doodle.calendar.services.UserService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Component
public class NotificationThread implements Runnable {
	
	private Notification notification;

	private MessagePushService mps;

	NotificationService notificationService;
	
	public NotificationThread(Notification notification, MessagePushService mps, NotificationService service) {
		
		this.notification = notification;
		this.mps = mps;
		this.notificationService = service;
	}



	@Override
	public void run() {
		try {
			Event event = notification.getEvent();
			User user = notification.getUser();
			String info = String.format("NotificationTime: %s, Event ID: %d: %s, Start: %s End: %s, Location: %s, Description: %s, "
					+ "isActive: %s",notification.getTiming().toString(), event.getId(), event.getTitle(), event.getStartTime().toString(), event.getEndTime().toString(),
					event.getLocation(), event.getDescription(), event.getIsActive()==1?true:false);
			boolean fail = mps.sendPushMessage(user, mps.encryptMessage(user, new PushMessage("notification", info)));
			if (!fail)
			{
				notification.setWasSent(1);
				try {
					notificationService.updateNotification(notification, user.getId());
				} catch (DaoException e) {
					e.printStackTrace();
				}
			}
		} catch (InvalidKeyException | JsonProcessingException | NoSuchAlgorithmException | InvalidKeySpecException
				| InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e) {
			e.printStackTrace();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
}



