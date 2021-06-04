package com.example.zippy.helper;

import android.util.Patterns;
import android.widget.EditText;

import java.util.regex.Pattern;

public class ValidationChecker {

    public static boolean isValidEmail(String email, EditText editTXTEmail){
        // if the email input field is empty
        if(email.isEmpty()){
            editTXTEmail.setError("Enter an email address");
            editTXTEmail.requestFocus();
            return false;
        }
        // if the email is valid
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTXTEmail.setError("Enter a valid email address");
            editTXTEmail.requestFocus();
            return false;
        }
        editTXTEmail.setError(null);
        return true;
    }

    public static boolean isValidPassword(String password, EditText editTXTPassword){
        // if the password input field is empty
        if(password.isEmpty()){
            editTXTPassword.setError("Enter a password");
            editTXTPassword.requestFocus();
            return false;
        }
        Pattern PASSWORD_PATTERN =
                Pattern.compile("^" +
                        "(?=.*[!@#%&*()_+-=|,./?><`~;':])" +     // at least 1 special character
                        "(?=\\S+$)" +            // no white spaces
                        ".{6,}" +                // at least 4 characters
                        "$");

        // if password does not matches to the pattern
        // it will display an error message "Password is too weak"
        if(!PASSWORD_PATTERN.matcher(password).matches()) {
            editTXTPassword.setError("Password is too weak\nRules:\n1.No white spaces\n"
                    +"2.At least six characters\n3.At least one special character");
            return false;
        }
        editTXTPassword.setError(null);
        return true;
    }
    public static boolean isFieldEmpty(String fieldTXT, EditText editTXTField){
        if(fieldTXT.isEmpty()) {
            editTXTField.setError("Field can not be empty");
            editTXTField.requestFocus();
            return true;
        }
        return false;
    }
}
