package com.jamillabltd.firebaseauthemailpass.globalVar;

public class RegisterFunction {

    public  String fullName, email, dob, gender, mobile, profile_pic_link;

    //Empty construction for firebase
    public RegisterFunction(){};

    //construction
    public RegisterFunction(String fullName, String email, String dob, String gender, String mobile, String profile_pic_link) {
        this.fullName = fullName;
        this.email = email;
        this.dob = dob;
        this.gender = gender;
        this.mobile = mobile;
        this.profile_pic_link = profile_pic_link;
    }
}
