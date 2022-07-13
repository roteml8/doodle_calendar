package ajbc.doodle.calendar.entities.webpush;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
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
		//initialize the queue and schedule manager to the closest notification  
		//add to queue only notifications that are active and were not sent
		
		List<Notification> allNots = notificationService.getAllNotifications();
		queue.addAll(allNots.stream().filter(t->t.getWasSent()==0 && t.getIsActive()==1).toList());
		schedule();
		
	}
	
	public void deleteNotification(Integer notificationId)
	{
		// delete notification with the given id from queue
		// check if the delete notification was the head of the queue
		// if so, reschedule the manager to the new head of the queue
		
		Notification first = queue.peek();
		queue.removeIf(t->t.getId().equals(notificationId));
		if (first.getId().equals(notificationId))
		{
			schedule();
		}
		

	}
	
	
	public void addNotification(Notification notification)
	{
		// remove from queue the notification with the same id of the given notification (if exists) to avoid duplicates
		// add the new notification
		// check if the queue was empty or the new notification is closer than the previous head of queue
		// if so, schedule manager to the new notification
		
		queue.removeIf(t-> t.getId().equals(notification.getId()));
		
		Notification first = queue.peek();
		queue.add(notification);
		if (first == null || notification.getTiming().isBefore(first.getTiming()))
		{
			schedule();
		}

	}
	
	@Override
	public void run()
	{
		// collect the notifications that need to be sent now 
		// assign to each notification a thread to send it
		// schedule the manager to the new head of queue
		
		List<Notification> toSend = new ArrayList<>();
		
		Notification first = queue.peek();
		Notification current;
		if (first != null)
		{
			do
			{
				current = queue.poll();
				User user = current.getUser();
				// send only if the user is now logged in and the notification is marked active
				if (user.getIsLogged()==1 && current.getIsActive()==1)
				{
					toSend.add(current);
				}

				current = queue.peek();

				// keep collecting all messages with the same time
			} while (current!=null && current.getTiming().equals(first.getTiming())); 
			
			toSend.forEach(t->pool.execute(new NotificationThread(t, mps, notificationService)));
		}
		
		schedule();
		
	}
	
	public long getDelay(LocalDateTime time)
	{
		// calculate delay in seconds from now to the given time
		
		LocalDateTime now = LocalDateTime.now();
		long seconds = ChronoUnit.SECONDS.between(now, time);
		return seconds; 
	}
	
	
	private void schedule()
	{
		// remove all tasks from the thread pool
		// if the queue is not empty:
		// if the notification time in the head of queue has passed then schedule to send it now
		// else, schedule the manager to the time of the notification in the head of queue
		BlockingQueue<Runnable> tasks = pool.getQueue();
		tasks.clear();
		if (queue.peek()!=null)
		{
			if (queue.peek().getTiming().isBefore(LocalDateTime.now()))
				pool.schedule(this, 0, TimeUnit.SECONDS);
			else
				pool.schedule(this, getDelay(queue.peek().getTiming()),TimeUnit.SECONDS);
		}
			
	}

}
