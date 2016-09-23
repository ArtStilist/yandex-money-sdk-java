/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 NBCO Yandex.Money LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.yandex.money.api.net;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.yandex.money.api.exceptions.InsufficientScopeException;
import com.yandex.money.api.exceptions.InvalidRequestException;
import com.yandex.money.api.exceptions.InvalidTokenException;
import com.yandex.money.api.net.clients.ApiClient;
import com.yandex.money.api.util.HttpHeaders;
import com.yandex.money.api.util.MimeTypes;
import com.yandex.money.api.util.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * OAuth2 session that can be used to perform API requests and retrieve responses.
 *
 * @author Slava Yasevich (vyasevich@yamoney.ru)
 */
public class OAuth2Session extends AbstractSession {

    private String accessToken;

    /**
     * Constructor.
     *
     * @param client API client used to perform operations
     */
    public OAuth2Session(ApiClient client) {
        super(client);
    }

    /**
     * Synchronous execution of a request.
     *
     * @param request the request
     * @param <T> response type
     * @return parsed response
     * @throws IOException if something went wrong during IO operations
     * @throws InvalidRequestException if server responded with 404 code
     * @throws InvalidTokenException if server responded with 401 code
     * @throws InsufficientScopeException if server responded with 403 code
     */
    public <T> T execute(ApiRequest<T> request)
            throws IOException, InvalidRequestException, InvalidTokenException, InsufficientScopeException {
        return parseResponse(request, makeCall(request).execute());
    }

    /**
     * Sets access token to perform authorized operations. Can be set to {@code null}, if no
     * access token is required to execute a request.
     *
     * @param accessToken access token
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Checks if session is authorized.
     *
     * @return {@code true} if authorized
     */
    public boolean isAuthorized() {
        return !Strings.isNullOrEmpty(accessToken);
    }

    /**
     * Convenience method to create {@link com.yandex.money.api.net.OAuth2Authorization} object for
     * user authentication.
     *
     * @return authorization parameters
     */
    public OAuth2Authorization createOAuth2Authorization() {
        return new OAuth2Authorization(client);
    }

    private <T> Call makeCall(ApiRequest<T> request) {
        final Request.Builder builder = prepareRequestBuilder(request);
        if (isAuthorized()) {
            builder.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        }
        return prepareCall(builder);
    }

    private <T> T parseResponse(ApiRequest<T> request, Response response) throws IOException,
            InvalidRequestException, InvalidTokenException, InsufficientScopeException {

        InputStream inputStream = null;
        try {
            switch (response.code()) {
                case HttpURLConnection.HTTP_OK:
                case HttpURLConnection.HTTP_ACCEPTED:
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    inputStream = getInputStream(response);
                    if (isJsonType(response)) {
                        return request.parseResponse(inputStream);
                    } else {
                        throw new InvalidRequestException(processError(response));
                    }
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    throw new InvalidTokenException(processError(response));
                case HttpURLConnection.HTTP_FORBIDDEN:
                    throw new InsufficientScopeException(processError(response));
                default:
                    throw new IOException(processError(response));
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private boolean isJsonType(Response response) {
        String field = response.header(HttpHeaders.CONTENT_TYPE);
        return field != null && (field.startsWith(MimeTypes.Application.JSON) || field.startsWith(MimeTypes.Text.JSON));
    }
}
