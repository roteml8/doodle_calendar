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

/*
 * Controller that implements the API for Events
 */

@RequestMapping("/events")
@RestController
public class EventController {
	
	@Autowired
	EventService service;
	
	/**
	 * Add a new event to the DB
	 * @param event the event object to add
	 * @param userId the ID of the user that is adding the event
	 * @return the new event object with ID, error if the adding process failed
	 */
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
	
	/**
	 * Add a list of new events to the DB
	 * @param events list of events to add
	 * @param userId the userId of the user that adds the events
	 * @return the list of the events with ID, error if the adding process failed
	 */
	@RequestMapping(method = RequestMethod.POST,  path="/addlist/{userId}")
	public ResponseEntity<?> addEvents(@RequestBody List<Event> events, @PathVariable Integer userId) {
		
		try {
			events = service.addEvents(events, userId);
			JsonUtils.nullifyFieldsInEventList(events);
			return ResponseEntity.status(HttpStatus.CREATED).body(events);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to add events to db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	/**
	 * get events according to the request parameters:
	 * startTime, endTime: all events in the range between startTime and endTime
	 * startTime, endTime, userId: all events in the range between startTime and endTime of user with id userId
	 * userId, numHours, numMinutes: all events in the upcoming numHours hours and numMinutes minutes of user with id userId
	 * userId: all events of user with id userId
	 * no param: all events in the DB
	 * @param map map of the request params
	 * @return the list of the required events, error if the get process failed
	 */
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getEvents(@RequestParam Map<String, String> map)  {
		List<Event> list;
		Set<String> keys = map.keySet();
		try {
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
		JsonUtils.nullifyFieldsInEventList(list);
		return ResponseEntity.ok(list);
		}
		catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to get events");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
		}

	}
	
	/**
	 * update event in DB
	 * @param event the event object to update
	 * @param userId the id of the user (only the owner of an event can update it) 
	 * @return the updated event object, error if update failed
	 */
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
	
	/**
	 * update list of events in DB 
	 * @param events the events to update
	 * @return the updated list of events, error if the update failed
	 */
	@RequestMapping(method = RequestMethod.PUT)
	public ResponseEntity<?> updateEvents(@RequestBody List<Event> events) {
		
		try {
			events = service.updatedEvents(events);
			JsonUtils.nullifyFieldsInEventList(events);
			return ResponseEntity.status(HttpStatus.OK).body(events);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to update events in db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	/**
	 * deactivate an event in DB
	 * deactivates all of the event notifications as well
	 * @param eventId the id of the event to deactivate
	 * @return the event object, error if the process failed
	 */
	@RequestMapping(method = RequestMethod.PUT, path="/{eventId}/deactivate")
	public ResponseEntity<?> deactivateEvent(@PathVariable Integer eventId) {
		
		try {
			service.deactivateEvent(eventId);
			Event event = service.getEventById(eventId);
			JsonUtils.nullifyFieldsInEvent(event);
			return ResponseEntity.status(HttpStatus.OK).body(event);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to deactivate event in db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	/**
	 * deactivate events in DB
	 * deactivates all of the events notifications as well
	 * @param eventIds the ids of the events to deactivate
	 * @return a success message, error if the process failed
	 */
	@RequestMapping(method = RequestMethod.PUT, path="/deactivate")
	public ResponseEntity<?> deactivateEvents(@RequestBody List<Integer> eventIds) {
		
		try {
			service.deactivateEvents(eventIds);
			return ResponseEntity.status(HttpStatus.OK).body("succrssfully deactivated events");
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to deactivate events in db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	/**
	 * deletes event from db
	 * deletes all the event notifications as well
	 * @param eventId the id of the event to delete
	 * @return success message, error if the delete failed
	 */
	@RequestMapping(method = RequestMethod.DELETE, path="/{eventId}")
	public ResponseEntity<?> deleteEvent(@PathVariable Integer eventId) {
		
		try {
			service.deleteEvent(eventId);
			return ResponseEntity.status(HttpStatus.OK).body("Event was successfully deleted from db");
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to delete event in db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	/**
	 * delete events from db
	 * deletes all the events notifications as well
	 * @param eventIds the ids of the events to delete
	 * @return success message, error if the delete failed
	 */
	@RequestMapping(method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteEvents(@RequestBody List<Integer> eventIds) {
		
		try {
			service.deleteEvents(eventIds);
			return ResponseEntity.status(HttpStatus.OK).body("Events were successfully deleted from db");
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to delete events in db");
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMessage);
		}
	}
	
	/**
	 * get an event by its id
	 * @param eventId the id of the event to retrieve
	 * @return the event object, error if the process failed
	 */
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
	
	/**
	 * get upcoming events of user (future events)
	 * @param userId the id of the user 
	 * @return the list of the events, error if the process failed
	 */
	@RequestMapping(method = RequestMethod.GET, path="/upcoming/{userId}")
	public ResponseEntity<?> getUpcomingUserEvents(@PathVariable Integer userId) {
		
		List<Event> list;
		try {
			list = service.getUpcomingEventsOfUser(userId);
			list.forEach(t->filterNotificationsByUser(t, userId));
			JsonUtils.nullifyFieldsInEventList(list);
			return ResponseEntity.ok(list);
		} catch (DaoException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setData(e.getMessage());
			errorMessage.setMessage("failed to get upcoming events of user with id "+userId);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
		}


	}
	
	/**
	 * filters event notifications by user (drops notification of other users)
	 * @param event the event
	 * @param userId the id of the user
	 */
	private void filterNotificationsByUser(Event event, Integer userId)
	{
		Set<Notification> nots = event.getNotifications();
		event.setNotifications(nots.stream().filter(t->t.getUser().getId().equals(userId)).collect(Collectors.toSet()));
		event.getNotifications().forEach(t->t.setUser(null));
		
	}

}
