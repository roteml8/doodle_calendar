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
import ajbc.doodle.calendar.entities.webpush.NotificationManager;
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
	@Autowired
	NotificationManager notificationManager;
	
	@EventListener
	public void seed(ContextRefreshedEvent event) throws DaoException
	{
//		seedUsers();
//		seedEvents();
//		seedNotifications();
	}
	
	private void seedUsers() throws DaoException
	{
		User user1 = new User("Rotem", "Levi", "missroteml@gmail.com", 
				LocalDate.of(1994, 8, 8), LocalDate.now());

		User user2 = new User("Yaron", "Shender", "yaronshender@gmail.com",
				LocalDate.of(1994, 6, 22), LocalDate.now());
		
		User user3 = new User("Matan", "Levi", "matanlevi@gmail.com",
				LocalDate.of(2007, 5, 7), LocalDate.now());
		User user = new User("Dana", "Grosman", "danagros@gmail.com",
				LocalDate.of(1992, 1, 1), LocalDate.now());
		List<User> users = Arrays.asList(user1, user2, user3, user);
		users.forEach(t->{
			try {
				userService.addUser(t);
			} catch (DaoException e) {
				e.printStackTrace();
			}
		});
		

		
	}
	
	private void seedEvents() throws DaoException
	{
//		LocalDateTime start = LocalDateTime.of(2023, 1, 1, 14, 0);
//		LocalDateTime end = LocalDateTime.of(2023, 1, 1, 14, 30);
//		User owner = userService.getUserByEmail("missroteml@gmail.com");
//		Event event1 = new Event("Appointment", start, end, "office", "appointment with boss", owner);
//		List<User> users = Arrays.asList(owner);
//		event1.setUsers(users);
//		
//		eventService.addEvent(event1);
//		
//		LocalDateTime start2 = LocalDateTime.of(2022, 8, 8, 20,0);
//		LocalDateTime end2 = LocalDateTime.of(2022, 8, 8, 23,0);
//		Event event2 = new Event("Bday Party", start2, end2, "Home", "my birthday party", owner);
//		User user2 = userService.getUserByEmail("yaronshender@gmail.com");
//		User user3 = userService.getUserByEmail("matanlevi@gmail.com");
//		List<User> users2 = Arrays.asList(owner, user2, user3);
//		event2.setUsers(users2);
//		
//		eventService.addEvent(event2);
		
		LocalDateTime start3 = LocalDateTime.of(2024, 1, 1, 19, 0);
		LocalDateTime end3 = LocalDateTime.of(2024, 1, 1, 23, 59);
		User owner3 = userService.getUserByEmail("yaronshender@gmail.com");
		User user1 = userService.getUserByEmail("danagros@gmail.com");
		List<User> users3 = Arrays.asList(owner3, user1);
		Event event = new Event("New Years Party", start3, end3, "Club", "new years party at the club", owner3);
		//event.setUsers(users3);
		eventService.addEvent(event, owner3.getId());

	}
	
	private void seedNotifications() throws DaoException
	{
		
		//LocalDateTime time = LocalDateTime.of(2022, 7, 12, 23, 20);
		User owner = userService.getUserByEmail("missroteml@gmail.com");
		Event event = eventService.getEventById(125);
		Notification n = new Notification();
		n.setEvent(event);
		n.setUser(owner);
		LocalDateTime now = LocalDateTime.now();
		n.setTiming(now.plusSeconds(20));
		notificationService.addNotification(n, owner.getId(), event.getId());
		n = notificationService.getNotificationById(n.getId());
		
		notificationManager.addNotification(n);
		
		n.setTiming(now.plusSeconds(15));
		notificationManager.addNotification(n);
		
		Notification n2 = new Notification();
		Event event2 = eventService.getEventById(124);
		n2.setUser(owner);
		n2.setEvent(event2);
		n2.setTiming(now.plusSeconds(100));;
		notificationService.addNotification(n2, owner.getId(), event2.getId());
		
		notificationManager.addNotification(n2);
		notificationService.deleteNotification(n.getId());
		notificationManager.deleteNotification(n.getId());
//		User user2 = userService.getUserByEmail("yaronshender@gmail.com");
//		Notification n2 = new Notification();
//		Event event2 = eventService.getEventById(130);
//		n2.setUser(user2);
//		n2.setEvent(event2);
//		n2.setTiming(now.plusSeconds(10));
//		notificationService.addNotification(n2, user2.getId(), event2.getId());
//		
//		notificationManager.addNotification(n2);

	}

}
