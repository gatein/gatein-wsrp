<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<portlet:defineObjects/>
<br />
<c:choose>
	<c:when test='${empty parameter}'>
		Please fill in your name at Event Generator portlet to see if eventing works.<br />
	</c:when>
	<c:otherwise>
		<h1>Welcome ${parameter}!</h1>
		Thanks for submiting your name, if you can see your name above -> events works properly.<br />		
	</c:otherwise>
</c:choose>
<br />