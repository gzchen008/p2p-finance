package controllers;

import constants.Constants;
import play.mvc.Before;
import play.mvc.Controller;

public class BaseController extends Controller {

	@Before
	protected static void injectionInterceptor() throws Exception {
		String injectionVal = new com.shove.web.security.InjectionInterceptor().run();
		if (injectionVal == null || injectionVal.length() > 0) {
			render(Constants.ERROR_PAGE_PATH_INJECTION, injectionVal);
		}
	}
}
