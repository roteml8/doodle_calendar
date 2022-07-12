package ajbc.doodle.calendar.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
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
		if (!doesUserExist(userId))
			throw new DaoException("UserId does not belong to a user in the DB");
		event.setIsActive(1);
		eventDao.addEvent(event);
		Set<User> users = event.getUsers();
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
	public List<Event> addEvents(List<Event> events, Integer userId) throws DaoException
	{
		List<Event> addedEvents = new ArrayList<>();
		for (Event e: events)
		{
			addEvent(e, userId);
			e = getEventById(e.getId());
			addedEvents.add(e);
			
		}
		return addedEvents;
	}
	
	public void updateEvent(Event event, Integer userId) throws DaoException
	{
		if (!userId.equals(event.getOwner().getId()))
			throw new DaoException("Update event can be performed by event owner only");
		eventDao.updateEvent(event);
		//TODO: update notifications?
		
	}
	
//	@Transactional
	public List<Event> getAllEvents() throws DaoException
	{
		List<Event> events = eventDao.getAllEvents();

		return events;
	}
	
//	@Transactional
	public List<Event> getAllEventsInRange(LocalDateTime start, LocalDateTime end) throws DaoException
	{
		List<Event> events = eventDao.getAllEvents();
		return events.stream().filter(t->t.getStartTime().isAfter(start) && t.getEndTime().isBefore(end)).toList();
	}
	
	public Event getEventById(Integer eventId) throws DaoException
	{
		Event event = eventDao.getEvent(eventId);

		return event;
	}
	
//	@Transactional
	public Set<Event> getEventsOfUser(Integer userId) throws DaoException
	{
		User user = userDao.getUser(userId);
		Set<Event> userEvents = user.getEvents();

		return userEvents;
	}

//	@Transactional
	public List<Event> getUpcomingEventsOfUser(Integer userId) throws DaoException
	{
		Set<Event> events = getEventsOfUser(userId);
		return events.stream().filter(t->t.getStartTime().isAfter(LocalDateTime.now())).toList();
	}
	
//	@Transactional
	public List<Event> getEventsOfUserInRange(Integer userId, LocalDateTime start, LocalDateTime end) throws DaoException
	{
		Set<Event> events = getEventsOfUser(userId);
		return events.stream().filter(t->t.getStartTime().isAfter(start) && t.getEndTime().isBefore(end)).toList();
	}
	
//	@Transactional
	public List<Event> getUserEventsInNextHoursMinutes(Integer userId, int numHours, int numMinutes) throws DaoException
	{
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime end = now.plusHours(numHours).plusMinutes(numMinutes);
		return getEventsOfUserInRange(userId, now, end);
	}
	
	public void deactivate(Integer eventId) throws DaoException
	{
		Event event = getEventById(eventId);
		event.setIsActive(0);
		//TODO: deactivate event notifications
		eventDao.updateEvent(event);
			
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
}
