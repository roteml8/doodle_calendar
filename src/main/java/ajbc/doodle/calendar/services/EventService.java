package ajbc.doodle.calendar.services;

import java.time.LocalDateTime;
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
	
	public void addEvent(Event event, Integer userId) throws DaoException
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
	
	public void updateEvent(Event event, Integer userId) throws DaoException
	{
		if (!userId.equals(event.getOwner().getId()))
			throw new DaoException("Update event can be performed by event owner only");
		eventDao.updateEvent(event);
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
	
	@Transactional
	public List<Event> getAllEventsInRange(LocalDateTime start, LocalDateTime end) throws DaoException
	{
		List<Event> events = eventDao.getAllEvents();
		return events.stream().filter(t->t.getStartTime().isAfter(start) && t.getEndTime().isBefore(end)).toList();
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

	@Transactional
	public List<Event> getUpcomingEventsOfUser(Integer userId) throws DaoException
	{
		Set<Event> events = getEventsOfUser(userId);
		return events.stream().filter(t->t.getStartTime().isAfter(LocalDateTime.now())).toList();
	}
	
	@Transactional
	public List<Event> getEventsOfUserInRange(Integer userId, LocalDateTime start, LocalDateTime end) throws DaoException
	{
		Set<Event> events = getEventsOfUser(userId);
		return events.stream().filter(t->t.getStartTime().isAfter(start) && t.getEndTime().isBefore(end)).toList();
	}
	
	@Transactional
	public List<Event> getUserEventsInNextHoursMinutes(Integer userId, int numHours, int numMinutes) throws DaoException
	{
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime end = now.plusHours(numHours).plusMinutes(numMinutes);
		return getEventsOfUserInRange(userId, now, end);
	}
}
