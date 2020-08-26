package models;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import io.ebean.Model;


import javax.persistence.*;

import org.joda.time.DateTime;
import org.mindrot.jbcrypt.BCrypt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import io.ebean.Finder;

@Entity
@Table(name = "users")

public class User extends Model{

	@Id
	@Column(columnDefinition = "VARCHAR(255)")
	public String id;

	@Column(columnDefinition = "VARCHAR(100)")
	public String username;

	@Column(columnDefinition = "VARCHAR(100)")
	public String email;

	@OneToOne(mappedBy = "user")
	public GeneralInformation general_information;

	@JsonIgnore
	@Column(columnDefinition = "VARCHAR(100)")
	public String password;

	@JsonIgnore
	@Column(columnDefinition = "VARCHAR(100)")
	public String createdAt;

	@JsonIgnore
	@Column(columnDefinition = "VARCHAR(100)")
	public String updatedAt;


	private String setPassword(String plainPassword) {
		String bcrypt;
		bcrypt = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
		return bcrypt;
	}
	
	public void setData (JsonNode json) {
		this.id=UUID.randomUUID().toString().replace("-","");
		this.username=json.findPath("username").textValue();
		this.email=json.findPath("email").textValue();
		this.password=setPassword(json.findPath("password").textValue());

	}
	
	public static final Finder<Long, User> find = new Finder<>(User.class);

	
	
	
}
