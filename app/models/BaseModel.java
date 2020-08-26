package models;

import io.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class BaseModel extends Model {
	@Id
	@Column(columnDefinition = "BIGINT(20) UNSIGNED")
	private Long id;
}