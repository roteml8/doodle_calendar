package ajbc.doodle.calendar.daos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.entities.Notification;
@SuppressWarnings("unchecked")
@Component
public class HtNotificationDao implements NotificationDao {
	
	@Autowired
	private HibernateTemplate template;

	@Override
	public void addNotification(Notification notification) throws DaoException {
		
		template.persist(notification);
	}


}
