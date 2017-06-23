<%--
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@ include file="/init.jsp" %>

<div class="login-container">

	<%
	String formName = "loginForm";

	if (windowState.equals(LiferayWindowState.EXCLUSIVE)) {
		formName += "Modal";
	}
	%>

	<liferay-portlet:actionURL name="verifyPasscode" var="verifyPasscodeURL" />

	<aui:form action="<%= verifyPasscodeURL %>" autocomplete="off" cssClass="sign-in-form" method="post" name="<%= formName %>" onSubmit="event.preventDefault();">
		<aui:fieldset>
			<aui:input autoFocus="true" cssClass="clearable" name="passcode" showRequiredLabel="<%= false %>" type="text">
				<aui:validator name="required" />
			</aui:input>
		</aui:fieldset>

		<aui:button-row>
			<aui:button cssClass="btn-lg" type="submit" value="sign-in" />
		</aui:button-row>
	</aui:form>
</div>