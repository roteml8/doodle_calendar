package ajbc.doodle.calendar.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.entities.User;

@SuppressWarnings("unchecked")
@Component
public class HtUserDao implements UserDao {


	@Autowired
	private HibernateTemplate template;
	
	
	@Override
	public void addUser(User user) throws DaoException {
		template.persist(user);
	}

	

	@Override
	public void updateUser(User user) throws DaoException {
		template.merge(user);
	}



	@Override
	public User getUser(Integer userId) throws DaoException {
		User user = template.get(User.class, userId);
		if (user ==null)
			throw new DaoException("No Such User in DB");
		return user;
	}


	@Override
	public List<User> getAllUsers() throws DaoException {
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
		
		return (List<User>)template.findByCriteria(criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));
	}



	@Override
	public List<User> getUsersByEventId(Integer eventId) throws DaoException {
		// TODO Auto-generated method stub
		return UserDao.super.getUsersByEventId(eventId);
	}



	@Override
	public User getUserByEmail(String email) throws DaoException {
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
		criteria.add(Restrictions.eq("email", email));
		List<User> result = (List<User>) template.findByCriteria(criteria);
		if (result.isEmpty())
			throw new DaoException("No user with the email: "+email);
		return result.get(0);
	}

	
	
	
}
