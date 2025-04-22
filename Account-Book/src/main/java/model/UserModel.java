package model;

public class UserModel {
    private String username;
    private String password;
    private boolean gender;
    private int age;

    public UserModel(String username, String password, boolean gender, int age) {
        this.username = username;
        this.password = password;
        this.gender = gender;
        this.age = age;
    }

    public UserModel() {
    }

    /**
     * 获取
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取
     * @return gender
     */
    public boolean isGender() {
        return gender;
    }

    /**
     * 设置
     * @param gender
     */
    public void setGender(boolean gender) {
        this.gender = gender;
    }

    /**
     * 获取
     * @return age
     */
    public int getAge() {
        return age;
    }

    /**
     * 设置
     * @param age
     */
    public void setAge(int age) {
        this.age = age;
    }

    public String toString() {
        return "UserModel{username = " + username + ", password = " + password + ", gender = " + gender + ", age = " + age + "}";
    }
}
