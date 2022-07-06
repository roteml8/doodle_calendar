package ajbc.doodle.calendar.daos;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.entities.Event;

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
		return (List<Event>)template.findByCriteria(criteria);
	}
	
	
}
