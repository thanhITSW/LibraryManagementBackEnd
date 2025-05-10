package nmtt.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "invalidated_token")
@Data
public class InvalidatedToken {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    String id;

    @Column(name = "expiry_time", nullable = false)
    Date expiryTime;
}