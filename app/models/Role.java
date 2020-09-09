package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Model;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="roles")
public class Role extends Model{
    @Id
    @Column(columnDefinition = "BIGINT(20) UNSIGNED")
    public Long id;

    @Column(columnDefinition = "VARCHAR(255)")
    public String name;

    @Column(columnDefinition = "VARCHAR(255)")
    public String description;

    @ManyToMany
    @JsonIgnore
    public List<User> user = new ArrayList<User>();

    @JsonIgnore
    @Column(columnDefinition = "VARCHAR(100)")
    public String createdAt;

    @JsonIgnore
    @Column(columnDefinition = "VARCHAR(100)")
    public String updatedAt;
}
