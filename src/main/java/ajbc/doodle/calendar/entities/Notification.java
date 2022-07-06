package ajbc.doodle.calendar.entities;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString

@Entity
@Table(name = "notifications")
public class Notification {
  
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false, nullable = false)
	private Integer id;
	private LocalDateTime timing;
	
	@ManyToOne(cascade = {CascadeType.MERGE})
	@JoinColumn(name="userId")
	private User user;
	
	@ManyToOne(cascade = {CascadeType.MERGE})
	@JoinColumn(name="eventId")
	private Event event;
 
	private int wasSent; //default=0
	private int isActive; //default=1
	
	public Notification(LocalDateTime timing, User user, Event event) {
		this.timing = timing;
		this.user = user;
		this.event = event;
		this.wasSent = 0;
		this.isActive = 1;
	}
	
	
}
