package org.gatein.wsrp.portlet;

import java.io.IOException;

import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * EventConsumer Portlet Class
 */
public class EventConsumerPortlet extends GenericPortlet {

	@Override
	public void doView(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {
		response.setContentType("text/html");
		String param = request.getParameter("parameter-event");
		request.setAttribute("parameter", param);
		PortletRequestDispatcher dispatcher = getPortletContext()
				.getRequestDispatcher("/view_consumer.jsp");
		dispatcher.include(request, response);
	}

	@Override
	public void processEvent(EventRequest request, EventResponse response)
			throws PortletException, IOException {
		String param = (String) request.getEvent().getValue();
		response.setRenderParameter("parameter-event", param);
	}

}
