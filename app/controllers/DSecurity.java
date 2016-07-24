package controllers;

import business.User;
import net.sf.json.JSONObject;
import constants.Constants;
import controllers.front.account.LoginAndRegisterAction;
import play.mvc.Before;
import utils.ErrorInfo;

public class DSecurity extends BaseController{

	@Before
	static void checkAccess() {
		AddCheck addCheck = getActionAnnotation(AddCheck.class);
		Check check = getActionAnnotation(Check.class);
		
		if(addCheck != null) {
			 String action = request.action;
			 String encryString = utils.Security.encrypt(action);
			 flash.put("encryString", encryString);
		}
		
		if(check != null) {
			
			
	        String[] request = check.value();
	        String action = request[0];
	        
	        if(!(Constants.VERIFY_SAFE_QUESTION.equals(action) && !User.currUser().isSecretSet)) {
		        String isAjax = request.length == 2 ? request[1] : null;
		        String encryString = params.get("encryString");
		        
		        ErrorInfo error = new ErrorInfo();
		        utils.Security.isValidRequest(action, encryString, error);
		        
		        if(error.code < 0) {
		        	 if(isAjax != null && isAjax.equals("1")) {
		        		 JSONObject json = new JSONObject();
		        		 
		        		 json.put("error", error);
		        		 renderJSON(json);
		        	 }
		        	 
		        	 flash.error(error.msg);
		        	 
		        	 LoginAndRegisterAction.login();
		        }
		        
	        }
	    }
	}
	
	public static boolean isAjaxRequest(){   
	    String header = request.headers.get("X-Requested-With").value();  
	    boolean isAjax = "XMLHttpRequest".equals(header) ? true:false;   
	    return isAjax;   
	} 
}
