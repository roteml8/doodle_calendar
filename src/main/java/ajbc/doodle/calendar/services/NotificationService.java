package ajbc.doodle.calendar.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.daos.NotificationDao;
import ajbc.doodle.calendar.entities.Notification;

@Component
public class NotificationService {

	@Autowired
	NotificationDao notificationDao;
	
	public void addNotification(Notification notification) throws DaoException
	{
		notification.setIsActive(1);
		notificationDao.addNotification(notification);
	}
}
