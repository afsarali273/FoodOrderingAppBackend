package com.upgrad.FoodOrderingApp.service.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "ADDRESS", schema = "public")
@NamedQueries({
        @NamedQuery(name = "allAddress", query = "select a from AddressEntity a"),
        @NamedQuery(name = "getAddressById", query = "select a from AddressEntity a where a.uuid=:uuid")
})
public class AddressEntity implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "UUID")
    @Size(max = 200)
    private String uuid;

    @Column(name = "FLAT_BUIL_NUMBER")
    @Size(max = 255)
    private String flatBuilNumber;

    @Column(name = "LOCALITY")
    @NotNull
    @Size(max = 255)
    private String locality;

    @Column(name = "CITY")
    @NotNull
    @Size(max = 30)
    private String city;

    @Column(name = "PINCODE")
    @Size(max = 30)
    private String pinCode;

    @ManyToOne
    @JoinColumn(name = "STATE_ID")
    private StateEntity stateEntity;

    @Column(name = "ACTIVE")
    private Boolean active;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFlatBuilNumber() {
        return flatBuilNumber;
    }

    public void setFlatBuilNumber(String flatBuilNumber) {
        this.flatBuilNumber = flatBuilNumber;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public StateEntity getStateEntity() {
        return stateEntity;
    }

    public void setStateEntity(StateEntity stateEntity) {
        this.stateEntity = stateEntity;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public AddressEntity() {}

    public AddressEntity(
            @Size(max = 200) @NotNull String uuid,
            @Size(max = 255) String flatBuilNo,
            @Size(max = 255) String locality,
            @Size(max = 30) String city,
            @Size(max = 30) String pincode,
            StateEntity state) {
        this.uuid = uuid;
        this.flatBuilNumber = flatBuilNo;
        this.locality = locality;
        this.city = city;
        this.pinCode = pincode;
        this.stateEntity = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressEntity that = (AddressEntity) o;
        return id == that.id && Objects.equals(uuid, that.uuid) && Objects.equals(flatBuilNumber, that.flatBuilNumber) && Objects.equals(locality, that.locality) && Objects.equals(city, that.city) && Objects.equals(pinCode, that.pinCode) && Objects.equals(stateEntity, that.stateEntity) && Objects.equals(active, that.active);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uuid, flatBuilNumber, locality, city, pinCode, stateEntity, active);
    }


}
