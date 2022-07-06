package ajbc.doodle.calendar.daos;

import org.springframework.transaction.annotation.Transactional;

import ajbc.doodle.calendar.entities.Notification;

@Transactional(rollbackFor = {DaoException.class}, readOnly = true)
public interface NotificationDao {
	
	@Transactional(readOnly = false)
	public default void addNotification(Notification notification) throws DaoException {
		throw new DaoException("Method not implemented");
	}

}
