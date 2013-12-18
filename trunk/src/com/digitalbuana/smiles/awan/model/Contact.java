package com.digitalbuana.smiles.awan.model;

public class Contact implements Comparable<Contact> {

    private String name=null;
    private String phone=null;
    private String email=null;

    public int compareTo(Contact other) {
        return name.compareTo(other.name);
    }
    
    public String getName(){
    	return this.name;
    }
    public String getPhone(){
    	return this.phone;
    }
    public String getMail(){
    	return this.email;
    }
    
    public void setName(String name){
    	this.name = name;
    }
    public void setPhone(String phone){
    	this.phone = phone;
    }
    public void setMail(String mail){
    	this.email = mail;
    }

    // Add/generate getters/setters and other boilerplate.
}
