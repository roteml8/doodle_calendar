package ajbc.doodle.calendar.services;

import java.util.Set;

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
public class EventService {
	
	@Autowired
	EventDao eventDao;
	@Autowired
	UserDao userDao;
	@Autowired
	NotificationDao notificationDao;
	
	public void addEvent(Event event) throws DaoException
	{
		eventDao.addEvent(event);
		Set<User> users = event.getUsers();
		users.forEach(t-> {
			try {
				notificationDao.addNotification(new Notification(event.getStartTime(), t, event));
			} catch (DaoException e) {
				e.printStackTrace();
			}
		});

	}

}
