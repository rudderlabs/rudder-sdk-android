package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rudderstack.android.sdk.core.util.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RudderTraits {
    @SerializedName("anonymousId")
    private String anonymousId;
    @SerializedName("address")
    private Address address;
    @SerializedName("age")
    private String age;
    @SerializedName("birthday")
    private String birthday;
    @SerializedName("company")
    private Company company;
    @SerializedName("createdat")
    private String createdAt;
    @SerializedName("description")
    private String description;
    @SerializedName("email")
    private String email;
    @SerializedName("firstname")
    private String firstName;
    @SerializedName("gender")
    private String gender;
    @SerializedName("userId")
    private String id;
    @SerializedName("lastname")
    private String lastName;
    @SerializedName("name")
    private String name;
    @SerializedName("phone")
    private String phone;
    @SerializedName("title")
    private String title;
    @SerializedName("username")
    private String userName;
    @Expose(serialize = false)
    private transient Map<String, Object> extras;

    public RudderTraits() {
        RudderContext rudderContext = RudderElementCache.getCachedContext();
        if (rudderContext != null) this.anonymousId = rudderContext.getDeviceId();
    }

    RudderTraits(String anonymousId) {
        this.anonymousId = anonymousId;
    }

    public RudderTraits(Address address, String age, String birthday, Company company, String createdAt, String description, String email, String firstName, String gender, String id, String lastName, String name, String phone, String title, String userName) {
        this.address = address;
        this.age = age;
        this.birthday = birthday;
        this.company = company;
        this.createdAt = createdAt;
        this.description = description;
        this.email = email;
        this.firstName = firstName;
        this.gender = gender;
        this.id = id;
        this.lastName = lastName;
        this.name = name;
        this.phone = phone;
        this.title = title;
        this.userName = userName;
    }

    public String getId() {
        return id;
    }

    public Map<String, Object> getExtras() {
        return extras;
    }

    public RudderTraits putAddress(Address address) {
        this.address = address;
        return this;
    }

    public RudderTraits putAge(String age) {
        this.age = age;
        return this;
    }

    public RudderTraits putBirthday(String birthday) {
        this.birthday = birthday;
        return this;
    }

    public RudderTraits putBirthday(Date birthday) {
        this.birthday = Utils.toDateString(birthday);
        return this;
    }

    public RudderTraits putCompany(Company company) {
        this.company = company;
        return this;
    }

    public RudderTraits putCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public RudderTraits putDescription(String description) {
        this.description = description;
        return this;
    }

    public RudderTraits putEmail(String email) {
        this.email = email;
        return this;
    }

    public RudderTraits putFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public RudderTraits putGender(String gender) {
        this.gender = gender;
        return this;
    }

    public RudderTraits putId(String id) {
        this.id = id;
        return this;
    }

    public RudderTraits putLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public RudderTraits putName(String name) {
        this.name = name;
        return this;
    }

    public RudderTraits putPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public RudderTraits putTitle(String title) {
        this.title = title;
        return this;
    }

    public RudderTraits putUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public RudderTraits put(String key, Object value) {
        if (this.extras == null) {
            this.extras = new HashMap<>();
        }
        this.extras.put(key, value);
        return this;
    }

    public static class Address {
        @SerializedName("city")
        private String city;
        @SerializedName("country")
        private String country;
        @SerializedName("postalcode")
        private String postalCode;
        @SerializedName("state")
        private String state;
        @SerializedName("street")
        private String street;

        public Address() {
        }

        public Address putCity(String city) {
            this.city = city;
            return this;
        }

        public Address putCountry(String country) {
            this.country = country;
            return this;
        }

        public Address putPostalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public Address putState(String state) {
            this.state = state;
            return this;
        }

        public Address putStreet(String street) {
            this.street = street;
            return this;
        }

        public Address(String city, String country, String postalCode, String state, String street) {
            this.city = city;
            this.country = country;
            this.postalCode = postalCode;
            this.state = state;
            this.street = street;
        }
    }

    public static class Company {
        @SerializedName("name")
        private String name;
        @SerializedName("id")
        private String id;
        @SerializedName("industry")
        private String industry;

        Company(String name, String id, String industry) {
            this.name = name;
            this.id = id;
            this.industry = industry;
        }

        public Company putName(String name) {
            this.name = name;
            return this;
        }

        public Company putId(String id) {
            this.id = id;
            return this;
        }

        public Company putIndustry(String industry) {
            this.industry = industry;
            return this;
        }
    }
}
