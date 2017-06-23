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

package com.liferay.two.factor.auth.lifecycle.loginpostaction;

import com.liferay.counter.kernel.service.CounterLocalServiceUtil;
import com.liferay.mail.kernel.model.MailMessage;
import com.liferay.mail.kernel.util.MailServiceUtil;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.LifecycleAction;
import com.liferay.portal.kernel.events.LifecycleEvent;
import com.liferay.portal.kernel.model.PasswordPolicy;
import com.liferay.portal.kernel.model.Ticket;
import com.liferay.portal.kernel.model.TicketConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
import com.liferay.portal.kernel.service.TicketLocalServiceUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.SubscriptionSender;
import com.liferay.portal.util.PrefsPropsUtil;
import com.liferay.two.factor.auth.web.internal.constants.TFAPortletKeys;
import com.liferay.two.factor.auth.web.internal.util.TOTPUtil;

import java.mail.InternetAddress;
import java.util.Date;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;

/**
 * @author Brent Krone-Schmidt
 */
@Component(
	immediate = true, property = {"key=login.events.post"},
	service = LifecycleAction.class
)
public class LoginPostAction implements LifecycleAction {

	@Override
	public void processLifecycleEvent(LifecycleEvent lifecycleEvent)
		throws ActionException {

		HttpServletRequest request = lifecycleEvent.getRequest();

		ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(
			KBWebKeys.THEME_DISPLAY);

		User user = themeDisplay.getUser();

		runInternalTwoFactorAuth(user);

		HttpServletResponse response = lifecycleEvent.getResponse();

		PortletURL portletURL = PortletURLFactoryUtil.create(
			actionRequest, TFAPortletKeys.TWO_FACTOR_AUTH,
			layout.getPlid(), PortletRequest.RENDER_PHASE);

		portletURL.setParameter("mvcPath", "/two-factor-auth/view.jsp");
		portletURL.setWindowState(LiferayWindowState.POP_UP);

		response.sendRedirect(portletURL);
	}

	private Ticket createPasscodeTicket(User user) {
		long ticketId = CounterLocalServiceUtil.increment();

		Ticket ticket = TicketLocalServiceUtil.createTicket(ticketId);

		ticket.setKey(
			TOTPUtil.generatePasscode(TOTPUtil.generatePasscodeSecretKey()));

		long classNameId =
			classNameLocalService.getClassNameId(User.class.getName());

		PasswordPolicy passwordPolicy = user.getPasswordPolicy();

		if ((passwordPolicy != null) &&
			(passwordPolicy.getResetTicketMaxAge() > 0)) {

			expirationDate = new Date(
				System.currentTimeMillis() +
					(passwordPolicy.getResetTicketMaxAge() * 1000));
		}

		ticket.setCompanyId(user.getCompanyId());
		ticket.setCreateDate(new Date());
		ticket.setClassNameId(classNameId);
		ticket.setClassPK(user.getUserId());
		ticket.setType(TicketConstants.TYPE_PASSWORD);
		ticket.setExpirationDate(expirationDate);

		return TicketLocalServiceUtil.updateTicket(ticket);
	}

	private void runInternalTwoFactorAuth(User user) {
		Ticket ticket = createPasscodeTicket(user);

		long companyId = user.getCompanyId();

		String fromName = PrefsPropsUtil.getString(
			companyId, PropsKeys.ADMIN_EMAIL_FROM_NAME);

		String fromEmailAddress = PrefsPropsUtil.getString(
			companyId, PropsKeys.ADMIN_EMAIL_FROM_ADDRESS);

		InternetAddress fromAddress =
			new InternetAddress(fromEmailAddress, fromName);

		String toName = user.getFullName();
		String toEmailAddress = user.getEmailAddress();

		InternetAddress toAddress = new InternetAddress(toEmailAddress, toName);

		MailMessage mailMessage =
			new MailMessage(fromAddress, toAddress, subject, body);

		MailServiceUtil.sendEmail(mailMessage);
	}

}