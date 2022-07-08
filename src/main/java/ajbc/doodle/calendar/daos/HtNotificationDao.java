package ajbc.doodle.calendar.daos;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.User;
@SuppressWarnings("unchecked")
@Component
public class HtNotificationDao implements NotificationDao {
	
	@Autowired
	private HibernateTemplate template;

	@Override
	public void addNotification(Notification notification) throws DaoException {
		
		template.persist(notification);
	}

	@Override
	public List<Notification> getNotificationsByEvent(Integer eventId)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(Notification.class);
		criteria.add(Restrictions.eq("event.id", eventId));
		return (List<Notification>)template.findByCriteria(criteria);

	}

	@Override
	public List<Notification> getNotificationsByEventAndUser(Integer eventId, Integer userId) throws DaoException {
		
		DetachedCriteria criteria = DetachedCriteria.forClass(Notification.class);
		criteria.add(Restrictions.and(Restrictions.eq("event.id", eventId),
				Restrictions.eq("user.id", userId)));
		return (List<Notification>)template.findByCriteria(criteria);
	}

	@Override
	public Notification getNotificationById(Integer notificationId) throws DaoException {
		Notification notification = template.get(Notification.class, notificationId);
		if (notification ==null)
			throw new DaoException("No Such Notification in DB");
		return notification;
	}

	@Override
	public void updateNotification(Notification notification) throws DaoException {
		
		template.merge(notification);
	}
	
	
	

}
