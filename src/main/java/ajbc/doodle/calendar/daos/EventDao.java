package ajbc.doodle.calendar.daos;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.User;

@Transactional(rollbackFor = {DaoException.class}, readOnly = true)
public interface EventDao {
	
	@Transactional(readOnly = false)
	public default void addEvent(Event event) throws DaoException {
		throw new DaoException("Method not implemented");
	}
	
	public default Event getEvent(Integer eventId) throws DaoException {
		throw new DaoException("Method not implemented");
	}
	
	@Transactional(readOnly = false)
	public default void updateEvent(Event event) throws DaoException {
		throw new DaoException("Method not implemented");
	}
	
	public default List<Event> getAllEvents() throws DaoException {
		throw new DaoException("Method not implemented");
	}

	
	
	public default List<Event> getEventsByUser(Integer userId) throws DaoException {
		throw new DaoException("Method not implemented");
	}
}
