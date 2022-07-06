package ajbc.doodle.calendar.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
	private String location;
	
	
	@ManyToOne(cascade = {CascadeType.MERGE})
	@JoinColumn(name="ownerId")
	private User owner;
	
	@ManyToMany()
	@JoinTable(
		    name = "UsersEvents",
		    joinColumns = @JoinColumn(name = "eventId", referencedColumnName = "id"),
		    inverseJoinColumns = @JoinColumn(name = "userId", referencedColumnName = "id")
		)
	private Set<User> users = new HashSet<>();
	
	private int isActive; //default=1

	public Event(String title, LocalDateTime startTime, LocalDateTime endTime, String location, String description, User owner) {
		this.title = title;
		this.startTime = startTime;
		this.endTime = endTime;
		this.location = location;
		this.description = description;
		this.owner = owner;
		this.isActive = 1;
	}

	
	
}
