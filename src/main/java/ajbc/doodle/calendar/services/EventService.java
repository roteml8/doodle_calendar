package ajbc.doodle.calendar.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.daos.EventDao;
import ajbc.doodle.calendar.daos.NotificationDao;
import ajbc.doodle.calendar.daos.UserDao;
import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.User;

@Component
public class EventService {
	
	@Autowired
	EventDao eventDao;
	@Autowired
	UserDao userDao;
	@Autowired
	NotificationDao notificationDao;
	
	public void addEvent(Event event) throws DaoException
	{
		event.setIsActive(1);
		eventDao.addEvent(event);
		List<User> users = event.getUsers();
		users.forEach(t-> {
			try {
				Notification notification = new Notification(event.getStartTime(), t, event);
				notificationDao.addNotification(notification);
			} catch (DaoException e) {
				e.printStackTrace();
			}
		});

	}
	
	@Transactional
	public List<Event> getAllEvents() throws DaoException
	{
		List<Event> events = eventDao.getAllEvents();
//		events.forEach(t->{
//			try {
//				t.setNotifications(notificationDao.getNotificationsByEvent(t.getId()));
//			} catch (DaoException e) {
//				e.printStackTrace();
//			}
//		});
		return events;
	}
	
	public Event getEventById(Integer eventId) throws DaoException
	{
		Event event = eventDao.getEvent(eventId);
//		List<Notification> eventNotifications = notificationDao.getNotificationsByEvent(eventId);
//		event.setNotifications(eventNotifications);
		return event;
	}
	
	@Transactional
	public Set<Event> getEventsOfUser(Integer userId) throws DaoException
	{
		User user = userDao.getUser(userId);
		Set<Event> userEvents = user.getEvents();
//		userEvents.forEach(t->{
//			try {
//				t.setNotifications(notificationDao.getNotificationsByEventAndUser(t.getId(), userId));
//			} catch (DaoException e) {
//				e.printStackTrace();
//			}
//		});
		return userEvents;
	}

	

}
