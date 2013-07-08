<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects/>

<%-- check escapeXml support --%>
<portlet:resourceURL id="default"/>
SEPARATOR
<portlet:resourceURL id="escapeTrue" escapeXml="true"/>
SEPARATOR
<portlet:resourceURL id="escapeFalse" escapeXml="false"/>
