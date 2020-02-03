package com.rudderstack.android.sdk.core;

import com.google.gson.Gson;
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
    @SerializedName("id")
    private String oldId;
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

    private static final String ANONYMOUSID_KEY = "anonymousid";
    private static final String ADDRESS_KEY = "address";
    private static final String AGE_KEY = "age";
    private static final String BIRTHDAY_KEY = "birthday";
    private static final String COMPANY_KEY = "company";
    private static final String CREATEDAT_KEY = "createdat";
    private static final String DESCRIPTION_KEY = "description";
    private static final String EMAIL_KEY = "email";
    private static final String FIRSTNAME_KEY = "firstname";
    private static final String GENDER_KEY = "gender";
    private static final String USERID_KEY = "userid";
    private static final String LASTNAME_KEY = "lastname";
    private static final String NAME_KEY = "name";
    private static final String PHONE_KEY = "phone";
    private static final String TITLE_KEY = "title";
    private static final String USERNAME_KEY = "username";

    public static String getAnonymousId(Map<String, Object> traitsMap) {
        if(traitsMap != null & traitsMap.containsKey(ANONYMOUSID_KEY))
            return (String)traitsMap.get(ANONYMOUSID_KEY);
        return null;
    }

    public static String getAddress(Map<String, Object> traitsMap) {
        if(traitsMap != null & traitsMap.containsKey(ADDRESS_KEY))
            return (String)traitsMap.get(ADDRESS_KEY);
        return null;
    }

    public static String getAge(Map<String, Object> traitsMap) {
        if(traitsMap != null & traitsMap.containsKey(AGE_KEY))
            return (String)traitsMap.get(AGE_KEY);
        return null;
    }

    public static String getBirthday(Map<String, Object> traitsMap) {
        if(traitsMap != null & traitsMap.containsKey(BIRTHDAY_KEY))
            return (String)traitsMap.get(BIRTHDAY_KEY);
        return null;
    }

    public static String getCompany(Map<String, Object> traitsMap) {
        if(traitsMap != null & traitsMap.containsKey(COMPANY_KEY))
            return (String)traitsMap.get(COMPANY_KEY);
        return null;
    }

    public static String getCreatedAt(Map<String, Object> traitsMap) {
        if(traitsMap != null & traitsMap.containsKey(CREATEDAT_KEY))
            return (String)traitsMap.get(CREATEDAT_KEY);
        return null;
    }

    public static String getDescription(Map<String, Object> traitsMap) {
        if(traitsMap != null & traitsMap.containsKey(DESCRIPTION_KEY))
            return (String)traitsMap.get(DESCRIPTION_KEY);
        return null;
    }

    public static String getFirstname(Map<String, Object> traitsMap) {
        if(traitsMap != null & traitsMap.containsKey(FIRSTNAME_KEY))
            return (String)traitsMap.get(FIRSTNAME_KEY);
        return null;
    }

    public static String getEmail(Map<String, Object> traitsMap) {
        if(traitsMap != null & traitsMap.containsKey(EMAIL_KEY))
            return (String)traitsMap.get(EMAIL_KEY);
        return null;
    }

    public static String getGender(Map<String, Object> traitsMap) {
        if(traitsMap != null & traitsMap.containsKey(GENDER_KEY))
            return (String)traitsMap.get(GENDER_KEY);
        return null;
    }

    public static String getUserid(Map<String, Object> traitsMap) {
        if(traitsMap != null & traitsMap.containsKey(USERID_KEY))
            return (String)traitsMap.get(USERID_KEY);
        return null;
    }

    public static String getLastname(Map<String, Object> traitsMap) {
        if(traitsMap != null & traitsMap.containsKey(LASTNAME_KEY))
            return (String)traitsMap.get(LASTNAME_KEY);
        return null;
    }

    public static String getName(Map<String, Object> traitsMap) {
        if(traitsMap != null & traitsMap.containsKey(NAME_KEY))
            return (String)traitsMap.get(NAME_KEY);
        return null;
    }

    public static String getPhone(Map<String, Object> traitsMap) {
        if(traitsMap != null & traitsMap.containsKey(PHONE_KEY))
            return (String)traitsMap.get(PHONE_KEY);
        return null;
    }

    public static String getTitle(Map<String, Object> traitsMap) {
        if(traitsMap != null & traitsMap.containsKey(TITLE_KEY))
            return (String)traitsMap.get(TITLE_KEY);
        return null;
    }

    public static String getUsername(Map<String, Object> traitsMap) {
        if(traitsMap != null & traitsMap.containsKey(USERNAME_KEY))
            return (String)traitsMap.get(USERNAME_KEY);
        return null;
    }


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
        this.oldId = id;
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
        this.oldId = id;
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

        public String getCity() {
            return city;
        }

        public String getCountry() {
            return country;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public String getState() {
            return state;
        }

        public String getStreet() {
            return street;
        }

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
        
        public static Address fromString(String address){
            Address add = new Gson().fromJson(address,Address.class);
            return add;
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
