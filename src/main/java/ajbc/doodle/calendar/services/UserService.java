package ajbc.doodle.calendar.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.daos.EventDao;
import ajbc.doodle.calendar.daos.UserDao;
import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.User;

@Component
public class UserService {
	
	@Autowired
	UserDao userDao;
	@Autowired
	EventDao eventDao;
	
	public void addUser(User user) throws DaoException
	{
		user.setIsActive(1);
		userDao.addUser(user);
	}

	public User getUser(Integer userId) throws DaoException
	{
		return userDao.getUser(userId);
	}
	
	public List<User> getAllUsers() throws DaoException
	{
		return userDao.getAllUsers();
	}
	
	public void updateUser(User user) throws DaoException
	{
		userDao.updateUser(user);
	}
	
	public List<User> getUsersByEvent(Integer eventId) throws DaoException
	{
		Event event = eventDao.getEvent(eventId);
		return event.getUsers();
		
	}
	
	public User getUserByEmail(String email) throws DaoException
	{
		return userDao.getUserByEmail(email);
	}

	public void login(String email) throws DaoException {
		User user = userDao.getUserByEmail(email);
		user.setIsLogged(1);
		userDao.updateUser(user);
		
	}
	
	public void logout(String email) throws DaoException {
		User user = userDao.getUserByEmail(email);
		user.setIsLogged(0);
		userDao.updateUser(user);
		
	}
}

