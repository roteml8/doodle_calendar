package ajbc.doodle.calendar.entities.webpush;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
public class NotificationManager implements Runnable {
	
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
		for (Notification n: (Notification[])queue.toArray())
		{
			if (n.getId().equals(notification.getId()))
			{
				queue.remove(n);
			}
		}
		Notification first = queue.peek();
		queue.add(notification);
		if (first == null || notification.getTiming().isBefore(first.getTiming()))
		{
			LocalDateTime now = LocalDateTime.now();
			long seconds = ChronoUnit.SECONDS.between(now, notification.getTiming());
			pool.schedule(this, seconds,TimeUnit.SECONDS); 
		}

	}
	
	@Override
	public void run()
	{
		//TODO: assign thread to send the next notification (top queue) if the user is logged in
		// if not- mark the notification as irrelevant, move on to the next notification
		// mark notification as sent if it was sent 
		
		// all notifications that need to be sent now - add to list from queue
		// after collecting all current notifications- open thread pool
		// each thread sends one notification to user 
		// set manager to sleep until the next closest notification timing
		
		
	}

}
