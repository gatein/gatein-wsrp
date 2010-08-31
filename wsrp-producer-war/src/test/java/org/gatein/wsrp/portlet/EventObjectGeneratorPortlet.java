package org.gatein.wsrp.portlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.xml.namespace.QName;

import org.gatein.wsrp.portlet.utils.TestObject;

/**
 * EventObjectGenerator Portlet Class
 */
public class EventObjectGeneratorPortlet extends GenericPortlet {
	
	private List<TestObject> objects;
	
	@Override
	public void init() throws PortletException {
		super.init();
		objects = new ArrayList<TestObject>();
		objects.add(new TestObject("Prabhat", "Jha", "pjha", 654321, "pjha@redhat.com"));
		objects.add(new TestObject("Michal", "Vanco", "mvanco", 123456, "mvanco@redhat.com"));
		objects.add(new TestObject("Marek", "Posolda", "mposolda", 112233, "mposolda@redhat.com"));
		objects.add(new TestObject("Viliam", "Rockai", "vrockai", 223311, "vrockai@redhat.com"));
	}
	
	public TestObject getTestObjectByUserName(String username) {
		for (TestObject object : objects) {
			if (object.getUsername().equals(username)) {
				return object;
			}
		}
		return null;
	}

	@Override
	public void doView(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {
		response.setContentType("text/html");
		request.setAttribute("objects", objects);
		PortletRequestDispatcher dispatcher = getPortletContext()
				.getRequestDispatcher("/view_generator.jsp");
		dispatcher.include(request, response);
	}

	@Override
	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		String username = request.getParameter("username");
		response.setEvent(new QName("urn:jboss:gatein:samples:event:object", "eventObject"), getTestObjectByUserName(username));
	}
}
