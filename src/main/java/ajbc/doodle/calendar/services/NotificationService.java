package ajbc.doodle.calendar.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

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
	
	
	public void addNotification(Notification notification, Integer userId, Integer eventId) throws DaoException
	{
		if (!doesUserExist(userId) || !doesEventExist(eventId))
			throw new DaoException("No such user or event");
		if (!isEventOfUser(userId, eventId))
			throw new DaoException("No such event of user");
		notification.setIsActive(1);
		notificationDao.addNotification(notification);
	}
	
	@Transactional
	public List<Notification> addNotifications(List<Notification> notifications, Integer userId, Integer eventId) throws DaoException
	{
		List<Notification> addedNotifications = new ArrayList<>();
		for (Notification n: notifications)
		{
			addNotification(n, userId, eventId);
			n = getNotificationById(n.getId());
			addedNotifications.add(n);
		}
		return addedNotifications;
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
	
	public boolean doesUserExist(Integer userId)
	{
		try {
			User user = userDao.getUser(userId);
		} catch (DaoException e) {
			return false;
		}
		return true;
	}
	
	public boolean doesEventExist(Integer eventId)
	{
		try {
			Event event = eventDao.getEvent(eventId);
		} catch (DaoException e) {
			return false;
		}
		return true;
	}
	
	private boolean isEventOfUser(Integer userId, Integer eventId) throws DaoException
	{
		User user = userDao.getUser(userId);
		for (Event e: user.getEvents())
		{
			if (e.getId().equals(eventId))
				return true;
		}
		return false;
	}
	
	public void deactivateNotification(Integer notificationId) throws DaoException
	{
		Notification notification = getNotificationById(notificationId);
		notification.setIsActive(0);
		notificationDao.updateNotification(notification);
	}
	
	public void deleteNotification(Integer notificationId) throws DaoException
	{
		notificationDao.deleteNotification(notificationId);
	}
}
