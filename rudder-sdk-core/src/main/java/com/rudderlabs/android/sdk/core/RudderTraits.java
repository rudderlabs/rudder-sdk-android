package com.rudderlabs.android.sdk.core;

import com.google.gson.annotations.SerializedName;

public class RudderTraits {
    @SerializedName("rl_anonymous_id")
    private String anonymousId;
    @SerializedName("rl_address")
    private TraitsAddress address;
    @SerializedName("rl_age")
    private String age;
    @SerializedName("rl_birthday")
    private String birthday;
    @SerializedName("rl_company")
    private TraitsCompany company;
    @SerializedName("rl_createdat")
    private String createdAt;
    @SerializedName("rl_description")
    private String description;
    @SerializedName("rl_email")
    private String email;
    @SerializedName("rl_firstname")
    private String firstName;
    @SerializedName("rl_gender")
    private String gender;
    @SerializedName("rl_id")
    private String id;
    @SerializedName("rl_lastname")
    private String lastName;
    @SerializedName("rl_name")
    private String name;
    @SerializedName("rl_phone")
    private String phone;
    @SerializedName("rl_title")
    private String title;
    @SerializedName("rl_username")
    private String userName;

    RudderTraits(String anonymousId) {
        this.anonymousId = anonymousId;
    }

    public RudderTraits(TraitsAddress address, String age, String birthday, TraitsCompany company, String createdAt, String description, String email, String firstName, String gender, String id, String lastName, String name, String phone, String title, String userName) {
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
}
