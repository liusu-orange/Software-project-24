package model;
/**
 * Data model representing a user with authentication and profile details.
 *
 * @author Boliang Chen
 * @version 1.0.0
 * @since v1.0.0
 */
public class UserModel {
    private String username;
    private String password;
    private boolean gender;
    private int age;
    /**
     * Creates a user model with full details.
     * @param username Unique user identifier
     * @param password Encrypted or plain-text password (depends on context)
     * @param gender User's gender
     * @param age User's age
     */
    public UserModel(String username, String password, boolean gender, int age) {
        this.username = username;
        this.password = password;
        this.gender = gender;
        this.age = age;
    }

    public UserModel() {
    }

    /**
     * Gets the user's unique username.
     * @return Username string
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the user's unique username.
     * @param username New username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the user's password.
     * @return Password string
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.
     * @param password New password string
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the user's gender.
     * @return Gender flag (true/false)
     */
    public boolean isGender() {
        return gender;
    }

    /**
     * Sets the user's gender.
     * @param gender New gender flag (true/false)
     */
    public void setGender(boolean gender) {
        this.gender = gender;
    }

    /**
     * Gets the user's age.
     * @return Age as integer
     */
    public int getAge() {
        return age;
    }

    /**
     * Sets the user's age.
     * @param age New age value
     */
    public void setAge(int age) {
        this.age = age;
    }

    public String toString() {
        return "UserModel{username = " + username + ", password = " + password + ", gender = " + gender + ", age = " + age + "}";
    }
}
