package ajbc.doodle.calendar.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.User;
import ajbc.doodle.calendar.services.EventService;
import ajbc.doodle.calendar.services.NotificationService;
import ajbc.doodle.calendar.services.UserService;

@Component
public class DBSeed {
	
	@Autowired
	UserService userService;
	@Autowired
	EventService eventService;
	@Autowired
	NotificationService notificationService;
	
	
	@EventListener
	public void seed(ContextRefreshedEvent event) throws DaoException
	{
//		seedUsers();
//		seedEvents();
//		seedNotifications();
	}
	
	private void seedUsers() throws DaoException
	{
//		User user1 = new User("Rotem", "Levi", "missroteml@gmail.com", 
//				LocalDate.of(1994, 8, 8), LocalDate.now());
//
//		User user2 = new User("Yaron", "Shender", "yaronshender@gmail.com",
//				LocalDate.of(1994, 6, 22), LocalDate.now());
//		
//		User user3 = new User("Matan", "Levi", "matanlevi@gmail.com",
//				LocalDate.of(2007, 5, 7), LocalDate.now());
//		List<User> users = Arrays.asList(user1, user2, user3);
//		users.forEach(t->{
//			try {
//				userService.addUser(t);
//			} catch (DaoException e) {
//				e.printStackTrace();
//			}
//		});
		
		User user = new User("Dana", "Grosman", "danagros@gmail.com",
				LocalDate.of(1992, 1, 1), LocalDate.now());
		userService.addUser(user);
		
	}
	
	private void seedEvents() throws DaoException
	{
		LocalDateTime start = LocalDateTime.of(2023, 1, 1, 14, 0);
		LocalDateTime end = LocalDateTime.of(2023, 1, 1, 14, 30);
		User owner = userService.getUser(5);
		Event event1 = new Event("Appointment", start, end, "office", "appointment with boss", owner);
		List<User> users = Arrays.asList(owner);
		event1.setUsers(users);
		
		eventService.addEvent(event1);
//		
//		LocalDateTime start2 = LocalDateTime.of(2022, 8, 8, 20,0);
//		LocalDateTime end2 = LocalDateTime.of(2022, 8, 8, 23,0);
//		Event event2 = new Event("Bday Party", start2, end2, "Home", "my birthday party", owner);
//		User user2 = userService.getUser(6);
//		User user3 = userService.getUser(7);
//		List<User> users2 = Arrays.asList(owner, user2, user3);
//		event2.setUsers(users2);
//		
//		eventService.addEvent(event2);
//		
//		LocalDateTime start = LocalDateTime.of(2024, 1, 1, 19, 0);
//		LocalDateTime end = LocalDateTime.of(2024, 1, 1, 23, 59);
//		User owner = userService.getUser(8);
//		User user1 = userService.getUser(7);
//		List<User> users = Arrays.asList(owner, user1);
//		Event event = new Event("New Years Party", start, end, "Club", "new years party at the club", owner);
//		event.setUsers(users);
//		eventService.addEvent(event);

	}
	
	private void seedNotifications() throws DaoException
	{
		User owner = userService.getUser(5);
		Event event = eventService.getEventById(114);
		Notification n = new Notification();
		n.setEvent(event);
		n.setUser(owner);
		n.setTiming(LocalDateTime.of(2023, 1, 1, 13, 30));
		notificationService.addNotification(n);
	}

}
