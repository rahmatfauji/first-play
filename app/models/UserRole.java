package models;

import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.Finder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "roles_users")
public class UserRole extends BaseModel{
    public int roles_id;
    public String users_id;

    public void setData (String id) {
        this.users_id=id;
        this.roles_id=1;

    }
    public static final Finder<Long, UserRole> find = new Finder<>(UserRole.class);
}
