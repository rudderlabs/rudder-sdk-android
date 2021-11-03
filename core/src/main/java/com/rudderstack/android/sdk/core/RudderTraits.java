package com.rudderstack.android.sdk.core;

import android.app.Application;

import com.google.gson.Gson;
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
    private Map<String, Object> extras;

    private static final String ANONYMOUSID_KEY = "anonymousId";
    private static final String ADDRESS_KEY = "address";
    private static final String AGE_KEY = "age";
    private static final String BIRTHDAY_KEY = "birthday";
    private static final String COMPANY_KEY = "company";
    private static final String CREATEDAT_KEY = "createdat";
    private static final String DESCRIPTION_KEY = "description";
    private static final String EMAIL_KEY = "email";
    private static final String FIRSTNAME_KEY = "firstname";
    private static final String GENDER_KEY = "gender";
    private static final String USERID_KEY = "userId";
    private static final String LASTNAME_KEY = "lastname";
    private static final String NAME_KEY = "name";
    private static final String PHONE_KEY = "phone";
    private static final String TITLE_KEY = "title";
    private static final String USERNAME_KEY = "username";

    /**
     * Get Anonymous Id from traits
     *
     * @param traitsMap Map
     * @return anonymousId String
     */
    public static String getAnonymousId(Map<String, Object> traitsMap) {
        if (traitsMap != null & traitsMap.containsKey(ANONYMOUSID_KEY))
            return (String) traitsMap.get(ANONYMOUSID_KEY);
        return null;
    }

    /**
     * Get Address from traits
     *
     * @param traitsMap Map
     * @return address String
     */
    public static String getAddress(Map<String, Object> traitsMap) {
        if (traitsMap != null & traitsMap.containsKey(ADDRESS_KEY))
            return new Gson().toJson(traitsMap.get(ADDRESS_KEY));
        return null;
    }

    /**
     * Get Age from traits
     *
     * @param traitsMap Map
     * @return age String
     */
    public static String getAge(Map<String, Object> traitsMap) {
        if (traitsMap != null & traitsMap.containsKey(AGE_KEY))
            return (String) traitsMap.get(AGE_KEY);
        return null;
    }

    /**
     * Get Birthday from traits
     *
     * @param traitsMap Map
     * @return birthday String
     */
    public static String getBirthday(Map<String, Object> traitsMap) {
        if (traitsMap != null & traitsMap.containsKey(BIRTHDAY_KEY))
            return (String) traitsMap.get(BIRTHDAY_KEY);
        return null;
    }

    /**
     * Get Company from traits
     *
     * @param traitsMap Map
     * @return company String
     */
    public static String getCompany(Map<String, Object> traitsMap) {
        if (traitsMap != null & traitsMap.containsKey(COMPANY_KEY))
            return (String) traitsMap.get(COMPANY_KEY);
        return null;
    }

    /**
     * Get createdAt from traits
     *
     * @param traitsMap Map
     * @return created_at String
     */
    public static String getCreatedAt(Map<String, Object> traitsMap) {
        if (traitsMap != null & traitsMap.containsKey(CREATEDAT_KEY))
            return (String) traitsMap.get(CREATEDAT_KEY);
        return null;
    }

    /**
     * Get description from traits
     *
     * @param traitsMap Map
     * @return description String
     */
    public static String getDescription(Map<String, Object> traitsMap) {
        if (traitsMap != null & traitsMap.containsKey(DESCRIPTION_KEY))
            return (String) traitsMap.get(DESCRIPTION_KEY);
        return null;
    }

    /**
     * Get First Name from traits
     *
     * @param traitsMap Map
     * @return firstName String
     */
    public static String getFirstname(Map<String, Object> traitsMap) {
        if (traitsMap != null & traitsMap.containsKey(FIRSTNAME_KEY))
            return (String) traitsMap.get(FIRSTNAME_KEY);
        return null;
    }

    /**
     * Get email from traits
     *
     * @param traitsMap Map
     * @return email String
     */
    public static String getEmail(Map<String, Object> traitsMap) {
        if (traitsMap != null & traitsMap.containsKey(EMAIL_KEY))
            return (String) traitsMap.get(EMAIL_KEY);
        return null;
    }

    /**
     * Get gender from traits
     *
     * @param traitsMap Map
     * @return gender String
     */
    public static String getGender(Map<String, Object> traitsMap) {
        if (traitsMap != null & traitsMap.containsKey(GENDER_KEY))
            return (String) traitsMap.get(GENDER_KEY);
        return null;
    }

    /**
     * Get user id from traits
     *
     * @param traitsMap Map
     * @return userId String
     */
    public static String getUserid(Map<String, Object> traitsMap) {
        if (traitsMap != null & traitsMap.containsKey(USERID_KEY))
            return (String) traitsMap.get(USERID_KEY);
        return null;
    }

    /**
     * Get Last Name from traits
     *
     * @param traitsMap Map
     * @return lastName String
     */
    public static String getLastname(Map<String, Object> traitsMap) {
        if (traitsMap != null & traitsMap.containsKey(LASTNAME_KEY))
            return (String) traitsMap.get(LASTNAME_KEY);
        return null;
    }

    /**
     * Get name from traits
     *
     * @param traitsMap Map
     * @return name String
     */
    public static String getName(Map<String, Object> traitsMap) {
        if (traitsMap != null & traitsMap.containsKey(NAME_KEY))
            return (String) traitsMap.get(NAME_KEY);
        return null;
    }

    /**
     * Get phone from traits
     *
     * @param traitsMap Map
     * @return phone String
     */
    public static String getPhone(Map<String, Object> traitsMap) {
        if (traitsMap != null & traitsMap.containsKey(PHONE_KEY))
            return (String) traitsMap.get(PHONE_KEY);
        return null;
    }

    /**
     * Get title from traits
     *
     * @param traitsMap Map
     * @return title String
     */
    public static String getTitle(Map<String, Object> traitsMap) {
        if (traitsMap != null & traitsMap.containsKey(TITLE_KEY))
            return (String) traitsMap.get(TITLE_KEY);
        return null;
    }

    /**
     * Get user name from traits
     *
     * @param traitsMap Map
     * @return userName String
     */
    public static String getUsername(Map<String, Object> traitsMap) {
        if (traitsMap != null & traitsMap.containsKey(USERNAME_KEY))
            return (String) traitsMap.get(USERNAME_KEY);
        return null;
    }


    /**
     * constructor
     */
    public RudderTraits() {
        Application application = RudderClient.getApplication();
        if (application != null) {
            this.anonymousId = RudderContext.getAnonymousId();
        }
    }


    /**
     * constructor
     *
     * @param anonymousId String
     */
    RudderTraits(String anonymousId) {
        this.anonymousId = anonymousId;
    }

    /**
     * Initialise RudderTraits
     *
     * @param address     Address
     * @param age         String
     * @param birthday    String
     * @param company     Company
     * @param createdAt   String
     * @param description String
     * @param email       String
     * @param firstName   String
     * @param gender      String
     * @param id          String
     * @param lastName    String
     * @param name        String
     * @param phone       String
     * @param title
     * @param userName    String
     */
    public RudderTraits(Address address, String age, String birthday, Company company, String createdAt, String description, String email, String firstName, String gender, String id, String lastName, String name, String phone, String title, String userName) {
        Application application = RudderClient.getApplication();
        if (application != null) {
            this.anonymousId = RudderContext.getAnonymousId();
        }
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

    /**
     * Get Id
     *
     * @return id String
     */
    public String getId() {
        return id;
    }

    /**
     * Get Extras
     *
     * @return map Map
     */
    public Map<String, Object> getExtras() {
        return extras;
    }


    /**
     * Put Address
     *
     * @param address Address
     * @return traits RudderTraits
     */
    public RudderTraits putAddress(Address address) {
        this.address = address;
        return this;
    }

    /**
     * put Age
     *
     * @param age String
     * @return traits RudderTraits
     */
    public RudderTraits putAge(String age) {
        this.age = age;
        return this;
    }

    /**
     * put Birthday
     *
     * @param birthday String
     * @return traits RudderTraits
     */
    public RudderTraits putBirthday(String birthday) {
        this.birthday = birthday;
        return this;
    }

    /**
     * put Birthday as Date
     *
     * @param birthday Date
     * @return traits RudderTraits
     */
    public RudderTraits putBirthday(Date birthday) {
        this.birthday = Utils.toDateString(birthday);
        return this;
    }

    /**
     * put Company
     *
     * @param company Company
     * @return traits RudderTraits
     */
    public RudderTraits putCompany(Company company) {
        this.company = company;
        return this;
    }

    /**
     * put Created At
     *
     * @param createdAt String
     * @return traits RudderTraits
     */
    public RudderTraits putCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * put description
     *
     * @param description String
     * @return traits RudderTraits
     */
    public RudderTraits putDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * put email
     *
     * @param email String
     * @return traits RudderTraits
     */
    public RudderTraits putEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * put First Name
     *
     * @param firstName String
     * @return traits RudderTraits
     */
    public RudderTraits putFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    /**
     * put gender
     *
     * @param gender String
     * @return traits RudderTraits
     */
    public RudderTraits putGender(String gender) {
        this.gender = gender;
        return this;
    }

    /**
     * put id
     *
     * @param id String
     * @return traits RudderTraits
     */
    public RudderTraits putId(String id) {
        this.id = id;
        this.oldId = id;
        return this;
    }

    /**
     * put Last Name
     *
     * @param lastName String
     * @return traits RudderTraits
     */
    public RudderTraits putLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    /**
     * put name
     *
     * @param name String
     * @return traits RudderTraits
     */
    public RudderTraits putName(String name) {
        this.name = name;
        return this;
    }

    /**
     * put phone
     *
     * @param phone String
     * @return traits RudderTraits
     */
    public RudderTraits putPhone(String phone) {
        this.phone = phone;
        return this;
    }

    /**
     * put title
     *
     * @param title String
     * @return traits RudderTraits
     */
    public RudderTraits putTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * put User Name
     *
     * @param userName String
     * @return traits RudderTraits
     */
    public RudderTraits putUserName(String userName) {
        this.userName = userName;
        return this;
    }

    /**
     * put generic key value pairs
     *
     * @param key   String
     * @param value Object
     * @return traits RudderTraits
     */
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

        /**
         * default public constructor
         */
        public Address() {
        }

        /**
         * constructor
         *
         * @param city       String
         * @param country    String
         * @param postalCode String
         * @param state      String
         * @param street     String
         */
        public Address(String city, String country, String postalCode, String state, String street) {
            this.city = city;
            this.country = country;
            this.postalCode = postalCode;
            this.state = state;
            this.street = street;
        }

        /**
         * get city
         *
         * @return city String
         */
        public String getCity() {
            return city;
        }

        /**
         * get country
         *
         * @return country String
         */
        public String getCountry() {
            return country;
        }

        /**
         * get postal code
         *
         * @return postalCode String
         */
        public String getPostalCode() {
            return postalCode;
        }

        /**
         * get state
         *
         * @return state String
         */
        public String getState() {
            return state;
        }

        /**
         * get street
         *
         * @return street String
         */
        public String getStreet() {
            return street;
        }

        /**
         * put city
         *
         * @param city String
         * @return address Address
         */
        public Address putCity(String city) {
            this.city = city;
            return this;
        }

        /**
         * put country
         *
         * @param country String
         * @return address Address
         */
        public Address putCountry(String country) {
            this.country = country;
            return this;
        }

        /**
         * put postal code
         *
         * @param postalCode String
         * @return address Address
         */
        public Address putPostalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        /**
         * put state String
         *
         * @param state String
         * @return address Address
         */
        public Address putState(String state) {
            this.state = state;
            return this;
        }

        /**
         * put street String
         *
         * @param street String
         * @return address Address
         */
        public Address putStreet(String street) {
            this.street = street;
            return this;
        }

        /**
         * make address from String
         *
         * @param address String
         * @return address Address
         */
        public static Address fromString(String address) {
            return new Gson().fromJson(address, Address.class);
        }
    }

    public static class Company {
        @SerializedName("name")
        private String name;
        @SerializedName("id")
        private String id;
        @SerializedName("industry")
        private String industry;

        /**
         * default public constructor
         */
        public Company() {
        }

        /**
         * constructor
         *
         * @param name     String
         * @param id       String
         * @param industry String
         */
        Company(String name, String id, String industry) {
            this.name = name;
            this.id = id;
            this.industry = industry;
        }

        /**
         * put name
         *
         * @param name String
         * @return company Company
         */
        public Company putName(String name) {
            this.name = name;
            return this;
        }

        /**
         * put company Id
         *
         * @param id String
         * @return company Company
         */
        public Company putId(String id) {
            this.id = id;
            return this;
        }

        /**
         * put industry
         *
         * @param industry String
         * @return company Company
         */
        public Company putIndustry(String industry) {
            this.industry = industry;
            return this;
        }
    }
}