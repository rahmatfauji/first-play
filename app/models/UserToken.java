package models;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.ebean.Finder;
import io.ebean.Model;
import play.data.format.Formats;
import play.mvc.Http;
import play.mvc.Http.Request;

@Entity
@Table(name = "user_tokens")
public class UserToken extends Model {
	@Id
	@Column(columnDefinition = "BIGINT(20)")
	public long id;

	@Column(columnDefinition = "VARCHAR(255) NOT NULL", unique = true)
	public String token;

	@Formats.DateTime(pattern = "yyyy-MM-dd hh:mm:ss")
	@Column(columnDefinition = "DATETIME")
	public Date issued_at;

	@Formats.DateTime(pattern = "yyyy-MM-dd hh:mm:ss")
	@Column(columnDefinition = "DATETIME")
	public Date expired_at;

	@Column(columnDefinition = "VARCHAR(255) UNSIGNED")
	public String user_id;

	@Column(columnDefinition = "BOOLEAN")
	public boolean logout;

	public UserToken(String token, Date issued_at, Date expired_at, String user_id) {
		super();
		this.token = token;
		this.issued_at = issued_at;
		this.expired_at = expired_at;
		this.user_id = user_id;
	}

	public static final Finder<Long, UserToken> find = new Finder<>(UserToken.class);

}
