package org.gatein.wsrp.portlet;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.gatein.wsrp.portlet.utils.TestObject;

/**
 * EventObjectConsumer Portlet Class
 */
public class EventObjectConsumerPortlet extends GenericPortlet {

	@Override
	public void doView(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {
		response.setContentType("text/html");
		TestObject object = (TestObject) request.getPortletSession().getAttribute("object");
		request.setAttribute("object", object);
		PortletRequestDispatcher dispatcher = getPortletContext()
				.getRequestDispatcher("/view_consumer.jsp");
		dispatcher.include(request, response);
	}

	@Override
	public void processEvent(EventRequest request, EventResponse response)
			throws PortletException, IOException {
		TestObject object = (TestObject) request.getEvent().getValue();
		request.getPortletSession().setAttribute("object", object);
	}

	@Override
	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		request.getPortletSession().removeAttribute("object");
	}

}
