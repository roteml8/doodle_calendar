package ajbc.doodle.calendar.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.daos.EventDao;
import ajbc.doodle.calendar.daos.NotificationDao;
import ajbc.doodle.calendar.daos.UserDao;
import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.User;

@Component
public class NotificationService {

	@Autowired
	NotificationDao notificationDao;
	@Autowired
	EventDao eventDao;
	@Autowired 
	UserDao userDao;
	
	
	public void addNotification(Notification notification) throws DaoException
	{
		notification.setIsActive(1);
		notificationDao.addNotification(notification);
	}
	
	public List<Notification> getAllNotifications() throws DaoException
	{
		return notificationDao.getAllNotifications();
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

	public void updateNotification(Notification notification, Integer userId) throws DaoException {
		
		if (!userId.equals(notification.getUser().getId()))
				throw new DaoException("Update notification can be performed by owner only");
		notificationDao.updateNotification(notification);		
	}
	
	public void deactivate(Integer notificationId) throws DaoException
	{
		Notification notification = getNotificationById(notificationId);
		notification.setIsActive(0);
		notificationDao.updateNotification(notification);		
		
	}
	
	public List<Notification> getNotificationsByUserEmail(String email) throws DaoException
	{
		User user = userDao.getUserByEmail(email);
		Set<Event> userEvents = user.getEvents();
		List<Notification> userNots = new ArrayList<>();
		for (Event e: userEvents)
		{
			List<Notification> eventNots = e.getNotifications().stream()
					.filter(t->t.getUser().getEmail().equals(email)).collect(Collectors.toList());
			userNots.addAll(eventNots);
		}
		return userNots;
	}
	
}
