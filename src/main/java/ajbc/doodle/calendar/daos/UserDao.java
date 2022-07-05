package ajbc.doodle.calendar.daos;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ajbc.doodle.calendar.entities.User;

@Transactional(rollbackFor = {DaoException.class}, readOnly = true)
public interface UserDao {

	@Transactional(readOnly = false)
	public default void addUser(User user) throws DaoException {
		throw new DaoException("Method not implemented");
	}
	
	public default User getUser(Integer userId) throws DaoException {
		throw new DaoException("Method not implemented");
	}
	
	public default List<User> getAllUsers() throws DaoException {
		throw new DaoException("Method not implemented");
	}

}
