package ajbc.doodle.calendar.entities.webpush;

import java.util.PriorityQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.services.NotificationService;

@Component
public class NotificationManager {
	
	@Autowired
	NotificationService notificationService;
	
	private final int NUM_THREADS = 3;
	private PriorityQueue<Notification> queue;
	private ScheduledThreadPoolExecutor pool;
	
	public NotificationManager() 
	{
		this.pool = new ScheduledThreadPoolExecutor(NUM_THREADS);
		this.queue = new PriorityQueue<Notification>((n1,n2) -> n1.getTiming().compareTo(n2.getTiming()));
	}
	
	@PostConstruct
	public void initQueue() throws DaoException
	{
	//	this.queue.addAll(notificationService.getAllNotifications());

	}
	
	public void addNotification(Notification notification)
	{
		Notification first = queue.peek();
		queue.add(notification);
		if (first!= null & notification.getTiming().isBefore(first.getTiming()))
		{
			//TODO:
			// schedule manager to wake up in the new notification timing
		}
	}
	
	public void run()
	{
		
	}

}
