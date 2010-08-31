<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects/>
<br />
<h1>Hello!</h1>
This is sample portlet to verify events.<br />
Please type in your name and submit. <br /><br />
<portlet:actionURL name="submit" var="submit" />
<form method="post" action="${submit}">
Your name: <input type="text" name="parameter"/> <input type="submit" value="Submit">
</form>
<br />
