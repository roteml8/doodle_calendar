package ajbc.doodle.calendar.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.daos.EventDao;
import ajbc.doodle.calendar.daos.NotificationDao;
import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.Notification;

@Component
public class NotificationService {

	@Autowired
	NotificationDao notificationDao;
	@Autowired
	EventDao eventDao;
	
	
	public void addNotification(Notification notification) throws DaoException
	{
		notification.setIsActive(1);
		notificationDao.addNotification(notification);
	}
	
	public List<Notification> getNotificationsByEvent(Integer eventId) throws DaoException
	{
		Event event = eventDao.getEvent(eventId);
		return event.getNotifications().stream().collect(Collectors.toList());
	}
	
	public Notification getNotificationById(Integer notifciationId) throws DaoException
	{
		return notificationDao.getNotificationById(notifciationId);
	}
	
}
