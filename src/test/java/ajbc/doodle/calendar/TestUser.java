package ajbc.doodle.calendar;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.Test;

import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.User;

class TestUser {


	@Test
	void checkConstructor()
	{
		String firstName="Rotem", lastName="Levi", email="missroteml@gmail.com";
		LocalDate birthDate = LocalDate.of(1994, 8, 8), joinDate = LocalDate.now();
		User user = new User(firstName, lastName, email, birthDate, joinDate);
		assertNotNull(user);
		assertNull(user.getId());
		assertNull(user.getSubscriptionData());
		assertNotNull(user.getEvents());
		assertTrue(user.getEvents().isEmpty());
		assertEquals(firstName, user.getFirstName());
		assertEquals(lastName, user.getLastName());
		assertEquals(email, user.getEmail());
		assertEquals(birthDate, user.getBirthDate());
		assertEquals(joinDate, user.getJoinDate());
		assertEquals(1, user.getIsActive());
		assertEquals(0, user.getIsLogged());
	}
	
	@Test
	void checkSetFirstName()
	{
		User user = new User();
		String firstName = "Guy";
		user.setFirstName(firstName);
		assertEquals(firstName, user.getFirstName());
	}
	
	@Test
	void checkSetLastName()
	{
		User user = new User();
		String lastName = "Tordjman";
		user.setLastName(lastName);
		assertEquals(lastName, user.getLastName());
	}
	
	@Test
	void checkSetEmail()
	{
		User user = new User();
		String email= "email@google.com";
		user.setEmail(email);
		assertEquals(email, user.getEmail());
	}
	
	@Test 
	void checkSetBirthDate()
	{
		User user = new User();
		LocalDate birthDate = LocalDate.of(2000, 1, 1);
		user.setBirthDate(birthDate);
		assertEquals(birthDate, user.getBirthDate());
	}
	
	@Test
	void checkSetJoinDate()
	{
		User user = new User();
		LocalDate joinDate = LocalDate.of(2000, 1, 1);
		user.setJoinDate(joinDate);
		assertEquals(joinDate, user.getJoinDate());
	}
	
	@Test
	void checkSetIsActive()
	{
		User user = new User();
		user.setIsActive(1);
		assertEquals(1, user.getIsActive());
	}
	
	@Test 
	void checkSetIsLogged()
	{
		User user = new User();
		user.setIsLogged(1);
		assertEquals(1, user.getIsLogged());
	}
	
	@Test
	void checkSetId()
	{
		User user = new User();
		Integer id = 1;
		user.setId(id);
		assertEquals(id, user.getId());
	}
	
	@Test
	void checkSetEvents()
	{
		User user = new User();
		Event e1 = new Event();
		e1.setId(1);
		e1.setTitle("Party");
		e1.setStartTime(LocalDateTime.now());
		e1.setEndTime(LocalDateTime.now().plusHours(2));
		e1.setDescription("my awesome party");
		e1.setLocation("my house");
		
		Set<Event> events = Set.of(e1);
		user.setEvents(events);
		
		Set<Event> userEvents = user.getEvents();

		assertFalse(userEvents.isEmpty());
		assertTrue(userEvents.size()==1);
		assertTrue(userEvents.contains(e1));
		
		
	}

}
