package nmtt.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "permission")
public class Permission {

    @Id
    @Column(name = "name", nullable = false, updatable = false)
    String name;

    @Column(name = "description")
    String description;
}