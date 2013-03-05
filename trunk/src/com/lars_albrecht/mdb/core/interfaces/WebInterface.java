/**
 * 
 */
package com.lars_albrecht.mdb.core.interfaces;

import java.util.ArrayList;

import com.lars_albrecht.mdb.core.controller.MainController;
import com.lars_albrecht.mdb.core.controller.interfaces.IController;
import com.lars_albrecht.mdb.core.interfaces.abstracts.AInterface;
import com.lars_albrecht.mdb.core.interfaces.web.WebServerInterface;

/**
 * @author lalbrecht
 * 
 */
public class WebInterface extends AInterface {

	final ArrayList<Thread>	threadList	= new ArrayList<Thread>();

	public WebInterface(final MainController mainController, final IController controller) {
		super(mainController, controller);
	}

	@Override
	public void starInterface() {
		this.threadList.add(new Thread(new WebServerInterface(this.mainController)));
		this.threadList.get(this.threadList.size() - 1).start();
	}
}
