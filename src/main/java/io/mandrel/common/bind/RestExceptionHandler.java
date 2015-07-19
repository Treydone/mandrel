/*
 * Licensed to Mandrel under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Mandrel licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.mandrel.common.bind;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

	protected ResponseEntity<Object> handleBindException(BindException e, HttpHeaders headers, HttpStatus status, WebRequest request) {

		List<FieldErrorResource> fieldErrorResources = new ArrayList<>();

		List<FieldError> fieldErrors = e.getFieldErrors();
		for (FieldError fieldError : fieldErrors) {
			FieldErrorResource fieldErrorResource = new FieldErrorResource();
			fieldErrorResource.setResource(fieldError.getObjectName());
			fieldErrorResource.setField(fieldError.getField());
			fieldErrorResource.setCode(fieldError.getCode());
			fieldErrorResource.setMessage(fieldError.getDefaultMessage());
			fieldErrorResources.add(fieldErrorResource);
		}

		ErrorResource error = new ErrorResource("Bind", e.getMessage());
		error.setFieldErrors(fieldErrorResources);

		headers.setContentType(MediaType.APPLICATION_JSON);

		return handleExceptionInternal(e, error, headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
	}

}
