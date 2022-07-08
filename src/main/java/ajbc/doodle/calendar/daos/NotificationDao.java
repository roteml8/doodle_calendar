package ajbc.doodle.calendar.daos;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.User;

@Transactional(rollbackFor = {DaoException.class}, readOnly = true)
public interface NotificationDao {
	
	@Transactional(readOnly = false)
	public default void addNotification(Notification notification) throws DaoException {
		throw new DaoException("Method not implemented");
	}

	public default List<Notification> getNotificationsByEvent(Integer eventId) throws DaoException {
		throw new DaoException("Method not implemented");
	}
	

	public default List<Notification> getNotificationsByEventAndUser(Integer eventId, Integer userId) throws DaoException {
		throw new DaoException("Method not implemented");
	}
	
	public default Notification getNotificationById(Integer notificationId) throws DaoException {
		throw new DaoException("Method not implemented");
	}
	
	@Transactional(readOnly = false)
	public default void updateNotification(Notification notification) throws DaoException {
		throw new DaoException("Method not implemented");
	}
	
}
