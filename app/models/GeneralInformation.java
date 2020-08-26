package models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.Finder;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name="general_information")
public class GeneralInformation extends BaseModel{

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    public User user;

    @Column(columnDefinition = "VARCHAR(100)")
    private String user_id;

    @Column(columnDefinition = "VARCHAR(100)")
    public String fullname;

    @Column(columnDefinition = "VARCHAR(100)")
    public String gender;

    @Column(columnDefinition = "VARCHAR(100)")
    public String dob;

    @Column(columnDefinition = "VARCHAR(100)")
    public String phone;

    @Column(columnDefinition = "VARCHAR(100)")
    public String address;

    @Column(columnDefinition = "VARCHAR(100)")
    public String city;

    @Column(columnDefinition = "VARCHAR(100)")
    public String province;

    @Column(columnDefinition = "VARCHAR(100)")
    public String country;

    public void setData (JsonNode json) {
        this.user_id=json.findPath("user_id").textValue();
        this.fullname=json.findPath("fullname").textValue();
        this.gender=json.findPath("gender").textValue();
        this.dob=json.findPath("dob").textValue();
        this.phone=json.findPath("phone").textValue();
        this.address=json.findPath("address").textValue();
        this.city=json.findPath("city").textValue();
        this.province=json.findPath("province").textValue();
        this.country=json.findPath("country").textValue();

    }
    public static final Finder<Long, GeneralInformation> find = new Finder<>(GeneralInformation.class);
}
