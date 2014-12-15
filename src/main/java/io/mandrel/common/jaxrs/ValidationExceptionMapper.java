package io.mandrel.common.jaxrs;

import java.util.Map;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;

import org.apache.cxf.validation.ResponseConstraintViolationException;

import com.google.common.collect.Maps;

@Provider
@Slf4j
public class ValidationExceptionMapper implements
		ExceptionMapper<ValidationException> {

	@Override
	public Response toResponse(ValidationException exception) {
		log.debug("Validation failed due to: {}", exception);
		if (exception instanceof ConstraintViolationException) {

			final ConstraintViolationException constraint = (ConstraintViolationException) exception;
			final boolean isResponseException = constraint instanceof ResponseConstraintViolationException;

			Map<String, String> violations = Maps.newHashMap();

			for (final ConstraintViolation<?> violation : constraint
					.getConstraintViolations()) {
				violations.put(violation.getRootBeanClass().getSimpleName()
						+ "." + violation.getPropertyPath(),
						violation.getMessage());
			}

			if (isResponseException) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.build();
			}

			return Response.status(Response.Status.BAD_REQUEST)
					.entity(violations).build();
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		}

	}
}
