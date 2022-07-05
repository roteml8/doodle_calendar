package ajbc.doodle.calendar.utils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.entities.User;
import ajbc.doodle.calendar.services.UserService;

@Component
public class DBSeed {
	
	@Autowired
	UserService service;
	
	
	//@EventListener
	public void seed(ContextRefreshedEvent event) throws DaoException
	{
		seedUsers();
		seedEvents();
		seedNotifications();
	}
	
	private void seedUsers() throws DaoException
	{
		User user1 = new User("Rotem", "Levi", "missroteml@gmail.com", 
				LocalDate.of(1994, 8, 8), LocalDate.now());

		User user2 = new User("Yaron", "Shender", "yaronshender@gmail.com",
				LocalDate.of(1994, 6, 22), LocalDate.now());
		
		User user3 = new User("Matan", "Levi", "matanlevi@gmail.com",
				LocalDate.of(2007, 5, 7), LocalDate.now());
		List<User> users = Arrays.asList(user1, user2, user3);
		users.forEach(t->{
			try {
				service.addUser(t);
			} catch (DaoException e) {
				e.printStackTrace();
			}
		});
		
	}
	
	private void seedEvents()
	{
		
	}
	
	private void seedNotifications()
	{
		
	}

}
