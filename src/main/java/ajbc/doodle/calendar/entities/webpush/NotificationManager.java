package ajbc.doodle.calendar.entities.webpush;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.User;
import ajbc.doodle.calendar.services.MessagePushService;
import ajbc.doodle.calendar.services.NotificationService;
import lombok.Getter;

@Component
@Getter
public class NotificationManager implements Runnable {
	
	@Autowired
	NotificationService notificationService;
	@Autowired
	MessagePushService mps;
	
	private final int NUM_THREADS = 10;
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
		List<Notification> allNots = notificationService.getAllNotifications();
		queue.addAll(allNots.stream().filter(t->t.getWasSent()==0 && t.getIsActive()==1).toList());
		schedule();
		
	}
	
	public void deleteNotification(Notification notification)
	{
		Notification first = queue.peek();
		queue.remove(notification);
		if (notification.equals(first))
		{
			schedule();
		}
		
	}
	
	
	public void addNotification(Notification notification)
	{
		queue.removeIf(t-> t.getId().equals(notification.getId()));
		
		Notification first = queue.peek();
		queue.add(notification);
		if (first == null || notification.getTiming().isBefore(first.getTiming()))
		{
			long seconds = getDelay(notification.getTiming());
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
		List<Notification> toSend = new ArrayList<>();
		
		Notification first = queue.peek();
		Notification current;
		if (first != null)
		{
			do
			{
				current = queue.poll();
				User user = current.getUser();
				if (user.getIsLogged()==1 && current.getIsActive()==1)
				{
					toSend.add(current);
				}

				current = queue.peek();

			} while (current!=null && current.getTiming().equals(first.getTiming()));
			
			toSend.forEach(t->pool.execute(new NotificationThread(t, mps, notificationService)));
		}
		
		schedule();
		
		
	}
	
	public long getDelay(LocalDateTime time)
	{
		LocalDateTime now = LocalDateTime.now();
		long seconds = ChronoUnit.SECONDS.between(now, time);
		return seconds; 
	}
	
	private void schedule()
	{
		if (queue.peek()!=null)
		{
			if (queue.peek().getTiming().isBefore(LocalDateTime.now()))
				pool.schedule(this, 0, TimeUnit.SECONDS);
			else
				pool.schedule(this, getDelay(queue.peek().getTiming()),TimeUnit.SECONDS);
		}
			
	}

}
