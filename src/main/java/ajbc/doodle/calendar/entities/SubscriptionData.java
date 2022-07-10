package ajbc.doodle.calendar.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "Subscriptions")
public class SubscriptionData {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer subId;
	private String publicKey;
	private String authKey;
	private String endpoint;

	 @OneToOne(mappedBy = "subscriptionData")
	 @JsonIgnore
	 private User user;
	 
		public SubscriptionData(String publicKey, String authKey, String endPoint) {
			super();
			this.publicKey = publicKey;
			this.authKey = authKey;
			this.endpoint = endPoint;
		}
}
