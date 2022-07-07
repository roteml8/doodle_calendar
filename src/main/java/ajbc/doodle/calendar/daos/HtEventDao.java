package ajbc.doodle.calendar.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.User;

@SuppressWarnings("unchecked")
@Component
public class HtEventDao implements EventDao{

	@Autowired
	private HibernateTemplate template;

	@Override
	public void addEvent(Event event) throws DaoException {
		
		template.persist(event);
	}

	@Override
	public Event getEvent(Integer eventId) throws DaoException {
		Event event = template.get(Event.class, eventId);
		if (event==null)
			throw new DaoException("No Such Event in DB");
		return event;
	}

	@Override
	public List<Event> getAllEvents() throws DaoException {
		DetachedCriteria criteria = DetachedCriteria.forClass(Event.class);
		return (List<Event>)template.findByCriteria(criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));
	}

	@Override
	public List<Event> getEventsByUser(Integer userId) throws DaoException {
		DetachedCriteria criteria = DetachedCriteria.forClass(Event.class);
		criteria.createAlias("users", "user");
		criteria.add(Restrictions.eq("user.id", userId));
		
		return (List<Event>)template.findByCriteria(criteria);
		
	}

	@Override
	public void updateEvent(Event event) throws DaoException {
		template.merge(event);
	}
	
	
	
}
