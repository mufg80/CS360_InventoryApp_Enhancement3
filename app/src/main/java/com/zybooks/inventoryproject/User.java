package com.zybooks.inventoryproject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// Data class to hold login information.
public class User {

    // Properties for database and hashing.
    private final int id;
    private final String user;
    private final String password;

    private String hash;

    // Constructor to fully initialize object.
    public User(int i, String user, String password, String hash){
        this.id = i;
        this.user = user;
        this.password = password;
        // If hash is passed as null, this means its coming from user login, create hash, if
        // not null it has came from the database.
        if(hash == null){
            ComputeHash();
        }else{
            this.hash = hash;
        }


    }
    // Public getters.
    public String getUser(){return this.user;}
    public String getHash(){return this.hash;}

    // Compute hash creates hash code for given password using sha256 algorithm.
    private void ComputeHash(){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            md.update(this.password.getBytes());

            // Get the hash's bytes
            byte[] bytes = md.digest();

            // Convert the byte array to a hexadecimal string
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++){
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            this.hash = sb.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    // Equals override makes two user objects equal if user and hash are the same.
    @Override
    public boolean equals(Object obj){
        if(obj == null) return false;
        if (this == obj) return true;

        if (!(obj instanceof User)) return false;
        User other = (User) obj;
        return this.user.equals(other.user) && this.hash.equals(other.hash);
    }

    // Boilerplate overriding of hashcode as good programming practice.
    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
        int result1 = hash != null ? hash.hashCode() : 0;
        return 31 * result + result1;
    }


}
