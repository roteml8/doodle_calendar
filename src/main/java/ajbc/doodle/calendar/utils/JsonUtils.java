package ajbc.doodle.calendar.utils;

import java.util.List;

import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.User;

public class JsonUtils {
	
	public static void nullifyFieldsInUserList(List<User> list)
	{
		list.forEach(t->t.getEvents().forEach(u->{u.setUsers(null); u.setOwner(null);
		u.setNotifications(null);}));
	}
	
	public static void nullifyFieldsInUser(User user)
	{
		user.getEvents().forEach(u->{u.setUsers(null); u.setOwner(null); u.setNotifications(null);});
	}
	
	public static void nullifyFieldsInEventList(List<Event> list)
	{
		list.forEach(t-> {
			t.setUsers(null);
			t.getOwner().setEvents(null);
			t.getNotifications().forEach(u->{u.setEvent(null); u.getUser().setEvents(null);});
		}
	);
	}
		
	public static void nullifyFieldsInEvent(Event event)
	{
		event.getOwner().setEvents(null);
		event.setUsers(null);
		event.getNotifications().forEach(u->{u.setEvent(null); u.getUser().setEvents(null);});
	
	}
	
}
