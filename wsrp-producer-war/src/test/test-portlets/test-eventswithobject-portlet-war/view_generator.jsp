<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %> 
<portlet:defineObjects/>
<h1>Hello!</h1>
This is sample portlet to verify object passing through events.<br />
Please click on any select button below to pass event with object (user).<br />
<br />
<table align="center">
	<thead>
		<tr>
			<th>First Name</th>
			<th>Lasst Name</th>
			<th>Username</th>
			<th>Employee number</th>
			<th>Email</th>
			<th>Select</th>
		</tr>
	</thead>
	<tbody>
	<c:forEach items="${objects}" var="user">
		<portlet:actionURL name="select" var="select">
			<portlet:param name="username" value="${user.username}" />
		</portlet:actionURL>
		<tr>
			<td align="center">${user.firstName}</td>
			<td align="center">${user.lastName}</td>
			<td align="center">${user.username}</td>
			<td align="center">${user.employeeNumber}</td>
			<td align="center">${user.email}</td>
			<td align="center"><form method="post" action="${select}"><input type="submit" value="Select" /></form></td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<br />
