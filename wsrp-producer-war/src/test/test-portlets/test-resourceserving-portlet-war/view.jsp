<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<portlet:defineObjects />
<h1>Resource serving...</h1>
This is sample portlet to verify portlet resource serving.<br />
In this sample the image is rendered with resourceURL, not static source.<br />
<br />
<img src="<portlet:resourceURL />" />
<br /><br />
If you can see above image, resource serving works properly. There are parameters to verify image:<br />
<ul>
	<li>hash: <span id="hash">${hash}</span></li>
	<li>image size: <span id="width">${width}</span>x<span id="height">${height}</span></li>
</ul>