package ajbc.doodle.calendar.controllers;

import java.time.LocalDateTime;
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
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.User;
import ajbc.doodle.calendar.services.EventService;
import ajbc.doodle.calendar.utils.JsonUtils;

@RequestMapping("/events")
@RestController
public class EventController {
	
	@Autowired
	EventService service;
	
	@RequestMapping(method = RequestMethod.POST,  path="/{userId}")
	public ResponseEntity<?> addEvent(@RequestBody Event event, @PathVariable Integer userId) {
		
		try {
			service.addEvent(event, userId);
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
		if(keys.contains("startTime") && keys.contains("endTime"))
		{
			LocalDateTime start = LocalDateTime.parse(map.get("startTime"));
			LocalDateTime end = LocalDateTime.parse(map.get("endTime"));
			if (keys.contains("userId"))
			{	
				list = service.getEventsOfUserInRange(Integer.parseInt(map.get("userId")), start, end);
				list.forEach(t->filterNotificationsByUser(t, Integer.parseInt(map.get("userId"))));
			}
			else
				list = service.getAllEventsInRange(start, end);
		}
		else if (keys.contains("userId"))
		{
			if (keys.contains("numMinutes") && keys.contains("numHours"))
				list = service.getUserEventsInNextHoursMinutes(Integer.parseInt(map.get("userId")),
						Integer.parseInt(map.get("numHours")), Integer.parseInt(map.get("numMinutes")));
			else
				list = service.getEventsOfUser(Integer.parseInt(map.get("userId"))).stream()
				.collect(Collectors.toList());
			list.forEach(t->filterNotificationsByUser(t, Integer.parseInt(map.get("userId"))));
		}	
		else
			list = service.getAllEvents();
		if (list == null)
			return ResponseEntity.notFound().build();
		JsonUtils.nullifyFieldsInEventList(list);

		return ResponseEntity.ok(list);
	}
	
	//TODO
	@RequestMapping(method = RequestMethod.PUT, path="/{userId}")
	public ResponseEntity<?> updateEvent(@RequestBody Event event, @PathVariable Integer userId) {
		
		try {
			service.updateEvent(event, userId);
			event = service.getEventById(event.getId());
			JsonUtils.nullifyFieldsInEvent(event);
			return ResponseEntity.status(HttpStatus.OK).body(event);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to update event in db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
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
	
	@RequestMapping(method = RequestMethod.GET, path="/upcoming/{userId}")
	public ResponseEntity<?> getUpcomingUserEvents(@PathVariable Integer userId) {
		
		List<Event> list;
		try {
			list = service.getUpcomingEventsOfUser(userId);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to get upcoming events of user with id "+userId);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
		}
		if (list == null)
			return ResponseEntity.notFound().build();
		list.forEach(t->filterNotificationsByUser(t, userId));
		JsonUtils.nullifyFieldsInEventList(list);

		return ResponseEntity.ok(list);

	}
	
	private void filterNotificationsByUser(Event event, Integer userId)
	{
		Set<Notification> nots = event.getNotifications();
		event.setNotifications(nots.stream().filter(t->t.getUser().getId().equals(userId)).collect(Collectors.toSet()));
		
	}

}
