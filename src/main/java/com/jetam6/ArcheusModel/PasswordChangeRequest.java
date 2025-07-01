package com.jetam6.ArcheusModel;

public class PasswordChangeRequest {
	 	private String oldPassword;
	    private String newPassword;

	    // GETERY
	    public String getOldPassword() {
	        return oldPassword;
	    }

	    public String getNewPassword() {
	        return newPassword;
	    }

	    // SETERY
	    public void setOldPassword(String oldPassword) {
	        this.oldPassword = oldPassword;
	    }

	    public void setNewPassword(String newPassword) {
	        this.newPassword = newPassword;
	    }
	
}
