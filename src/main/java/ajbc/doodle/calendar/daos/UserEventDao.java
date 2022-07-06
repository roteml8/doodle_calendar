package ajbc.doodle.calendar.daos;

import org.springframework.transaction.annotation.Transactional;

import ajbc.doodle.calendar.entities.User;

@Transactional(rollbackFor = {DaoException.class}, readOnly = true)

public interface UserEventDao {
	
	@Transactional(readOnly = false)
	public default void addUserToEvent(Integer userId, Integer eventId) throws DaoException {
		throw new DaoException("Method not implemented");
	}
	
	public default User getUsersByEvent(Integer eventId) throws DaoException {
		throw new DaoException("Method not implemented");
	}
	
	public default User getEventsByUser(Integer userId) throws DaoException {
		throw new DaoException("Method not implemented");
	}
	
	@Transactional(readOnly = false)
	public default void deleteUserFromEvent(Integer userId, Integer eventId) throws DaoException {
		throw new DaoException("Method not implemented");
	}

}
