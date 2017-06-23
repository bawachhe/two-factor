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

package com.liferay.two.factor.auth.web.internal.util;

import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.ietf.tools.TOTP;

/**
 * @author Brent Krone-Schmidt
 */
public class TOTPUtil {

	public static String generatePasscode(String secretKey) {
		String normalizedBase32Key = secretKey.replace(" ", "").toUpperCase();

		Base32 base32 = new Base32();

		byte[] bytes = base32.decode(normalizedBase32Key);

		String hexKey = Hex.encodeHexString(bytes);

		long time = (System.currentTimeMillis() / 1000) / 30;

		String hexTime = Long.toHexString(time);

		return TOTP.generateTOTP(hexKey, hexTime, "6");
	}

	public static String generatePasscodeSecretKey() {
		SecureRandom random = new SecureRandom();

		byte[] bytes = new byte[20];

		random.nextBytes(bytes);

		Base32 base32 = new Base32();

		String secretKey = base32.encodeToString(bytes);

		return secretKey.toLowerCase().replaceAll("(.{4})(?=.{4})", "$1 ");
	}

}