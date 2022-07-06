package ajbc.doodle.calendar.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.daos.UserDao;
import ajbc.doodle.calendar.entities.User;

@Component
public class UserService {
	
	@Autowired
	UserDao dao;
	
	public void addUser(User user) throws DaoException
	{
		dao.addUser(user);
	}

	public User getUser(Integer userId) throws DaoException
	{
		return dao.getUser(userId);
	}
	
	public List<User> getAllUsers() throws DaoException
	{
		return dao.getAllUsers();
	}
	
	public void updateUser(User user) throws DaoException
	{
		dao.updateUser(user);
	}
}
