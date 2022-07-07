package ajbc.doodle.calendar.entities;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString

@Entity
@Table(name = "notifications")
@JsonInclude(Include.NON_NULL)

public class Notification {
  
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false, nullable = false)
	private Integer id;
	private LocalDateTime timing;
	
	@ManyToOne()
	@JoinColumn(name="userId")
	private User user;
	
	@ManyToOne()
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
