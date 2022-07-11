package ajbc.doodle.calendar.utils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.User;

public class JsonUtils {
	
	public static void nullifyFieldsInUserList(List<User> list)
	{

		list.forEach(t->t.getEvents().forEach(u->{u.setUsers(null); u.setOwner(null);
		u.setNotifications(null);}));
	}
	
	public static void nullifyFieldsInUser(User user)
	{
		user.getEvents().forEach(u->{u.setUsers(null); u.setOwner(null);
		Set<Notification> nots = u.getNotifications().stream().filter(t->t.getUser().equals(user)).collect(Collectors.toSet());
		nots.forEach(k->{k.setEvent(null); k.setUser(null);});
		u.setNotifications(nots);
		});
	}
	
	public static void nullifyFieldsInEventList(List<Event> list)
	{
		list.forEach(t-> {
			t.setUsers(null);
			t.getOwner().setEvents(null);
			t.getNotifications().forEach(u->{u.setEvent(null); 
			if (u.getUser()!=null)
				u.getUser().setEvents(null);});
		}
	);
	}
		
	public static void nullifyFieldsInEvent(Event event)
	{
		event.getOwner().setEvents(null);
		event.setUsers(null);
		event.getNotifications().forEach(u->{u.setEvent(null); u.getUser().setEvents(null);});
	
	}
	
	public static void nullifyFieldsInNotification(Notification notification)
	{
		notification.getEvent().setUsers(null);
		notification.getEvent().setOwner(null);
		notification.getEvent().setNotifications(null);
		notification.getUser().setEvents(null);
	}
	
	public static void nullifyFieldsInNotificationList(List<Notification> notifications)
	{
		notifications.forEach(t-> {
			t.getEvent().setUsers(null);
			t.getEvent().setOwner(null);
			t.getEvent().setNotifications(null);
			t.getUser().setEvents(null);
		});
		
	}
	
	public static void nullifyEventsInNotificationList(List<Notification> notifications)
	{
		notifications.forEach(t->{t.setEvent(null);t.getUser().setEvents(null);}); 
	}
	
	public static void nullifyUsersInNotificationList(List<Notification> notifications)
	{
		notifications.forEach(t->{t.setUser(null); t.getEvent().setOwner(null);
		t.getEvent().setUsers(null); t.getEvent().setNotifications(null);});
		
	}
	
}
