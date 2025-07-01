package com.jetam6.ArcheusModel;

public class RegisterRequest {

	private String meno;
    private String priezvisko;
    private String email;
    private String password;

    // Gettery a settery
    public String getMeno() { return meno; }
    public void setMeno(String meno) { this.meno = meno; }

    public String getPriezvisko() { return priezvisko; }
    public void setPriezvisko(String priezvisko) { this.priezvisko = priezvisko; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}