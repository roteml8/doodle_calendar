package ajbc.doodle.calendar.controllers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.entities.ErrorMessage;
import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.User;
import ajbc.doodle.calendar.services.EventService;
import ajbc.doodle.calendar.utils.JsonUtils;

@RequestMapping("/events")
@RestController
public class EventController {
	
	@Autowired
	EventService service;
	
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> addEvent(@RequestBody Event event) {
		
		try {
			service.addEvent(event);
			event = service.getEventById(event.getId());
			JsonUtils.nullifyFieldsInEvent(event);
			return ResponseEntity.status(HttpStatus.CREATED).body(event);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to add event to db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<List<Event>> getEvents(@RequestParam Map<String, String> map) throws DaoException {
		List<Event> list;
		Set<String> keys = map.keySet();
		if (keys.contains("userId"))
			list = service.getEventsOfUser(Integer.parseInt(map.get("userId"))).stream()
			.collect(Collectors.toList());
		else
			list = service.getAllEvents();
		if (list == null)
			return ResponseEntity.notFound().build();
		JsonUtils.nullifyFieldsInEventList(list);
		//list.forEach(t->t.getNotifications().forEach(u->u.setUser(null)));
		
//		SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.filterOutAllExcept(
//				new HashSet<String>(Arrays
//                .asList(new String[] { "name", "firstName" }))) );
//
//	    FilterProvider filters = new SimpleFilterProvider().addFilter("inEventUsers", filter);
//
//	    MappingJacksonValue mapping = new MappingJacksonValue(list);

	   // mapping.setFilters(filters);

	   // return mapping;

		return ResponseEntity.ok(list);
	}
	
	@RequestMapping(method = RequestMethod.GET, path="/{eventId}")
	public ResponseEntity<?> getEventsById(@PathVariable Integer eventId) {
		
		Event event;
		try {
			event = service.getEventById(eventId);
			JsonUtils.nullifyFieldsInEvent(event);
			return ResponseEntity.ok(event);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to get event with id "+eventId);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
		}
	}

}
