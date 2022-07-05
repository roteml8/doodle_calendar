package ajbc.doodle.calendar.entities;

import java.time.LocalDate;
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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "events")
public class Event {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false, nullable = false)
	private Integer id;
	
	private String title;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private String description;
	
	@Column(insertable = false, updatable = false)
	private Integer ownerId;
	
	@ManyToOne(cascade = {CascadeType.MERGE})
	@JoinColumn(name="ownerId")
	private User owner;
	
	private int isActive; //default=1

	public Event(String title, LocalDateTime startTime, LocalDateTime endTime, String description, Integer ownerId) {
		this.title = title;
		this.startTime = startTime;
		this.endTime = endTime;
		this.description = description;
		this.ownerId = ownerId;
		this.isActive = 1;
	}

	
	
}
