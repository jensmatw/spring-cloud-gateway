/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.gateway.server.mvc.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

public abstract class HttpUtils {

	private static final int BUFFER_SIZE = 16384;

	private HttpUtils() {
		throw new AssertionError("Must not instantiate utility class.");
	}

	public static int copyResponseBody(ClientHttpResponse clientResponse, InputStream inputStream,
			OutputStream outputStream) throws IOException {
		Assert.notNull(clientResponse, "No ClientResponse specified");
		Assert.notNull(inputStream, "No InputStream specified");
		Assert.notNull(outputStream, "No OutputStream specified");

		int transferredBytes;

		if (TEXT_EVENT_STREAM.equals(clientResponse.getHeaders().getContentType())) {
			transferredBytes = copyResponseBodyWithFlushing(inputStream, outputStream);
		}
		else {
			transferredBytes = StreamUtils.copy(inputStream, outputStream);
		}

		return transferredBytes;
	}

	private static int copyResponseBodyWithFlushing(InputStream inputStream, OutputStream outputStream)
			throws IOException {
		int readBytes;
		var totalReadBytes = 0;
		var buffer = new byte[BUFFER_SIZE];

		while ((readBytes = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, readBytes);
			outputStream.flush();
			if (totalReadBytes < Integer.MAX_VALUE) {
				try {
					totalReadBytes = Math.addExact(totalReadBytes, readBytes);
				}
				catch (ArithmeticException e) {
					totalReadBytes = Integer.MAX_VALUE;
				}
			}
		}

		outputStream.flush();

		return totalReadBytes;
	}

}
