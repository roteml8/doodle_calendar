package ajbc.doodle.calendar;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.PriorityQueue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.User;
import ajbc.doodle.calendar.entities.webpush.NotificationManager;
class TestNotificationManager {
	
	NotificationManager manager;
	
	public TestNotificationManager()
	{
		this.manager = new NotificationManager();
	}

	@Test
	void testConstructor() {
		assertNotNull(manager);
		assertNotNull(manager.getPool());
		assertNotNull(manager.getQueue());
		assertEquals(10, manager.getNUM_THREADS());
	}
	
	@Test
	void testGetDelay()
	{
		LocalDateTime time = LocalDateTime.of(2022, 7, 31, 20, 30);
		LocalDateTime now = LocalDateTime.now();
		long seconds = ChronoUnit.SECONDS.between(now, time);
		assertEquals(seconds, manager.getDelay(time));
	}
	

}
