package com.capturecat.core.support.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

public class DateTimeConverter {

	private DateTimeConverter() {
	}

	public static LocalDate convert(String localDate) {
		try {
			return LocalDate.parse(localDate, DateTimeFormatter.ISO_LOCAL_DATE);
		} catch (DateTimeParseException ex) {
			throw new CoreException(ErrorType.INVALID_DATE_FORMAT);
		}
	}
}
