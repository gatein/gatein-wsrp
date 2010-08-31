<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<portlet:defineObjects/>
<c:choose>
	<c:when test='${empty object}'>
	<br />
		Please select user at Event Object Generator portlet to see if eventing with object works.<br />
	</c:when>
	<c:otherwise>
		<h1>Thanks for selection!</h1>
		You have selected following user: <br /><br />
		<b>${object.firstName}, ${object.lastName}, ${object.username}, ${object.employeeNumber}, ${object.email}</b>
		<br />
		<br />
		If you can see selected user above, events with objects works properly!<br />
		<portlet:actionURL name="clean" var="clean" />
		<form method="post" action="${clean}">
			<input type="submit" value="Clear selection">
		</form>
	</c:otherwise>
</c:choose>
<br />