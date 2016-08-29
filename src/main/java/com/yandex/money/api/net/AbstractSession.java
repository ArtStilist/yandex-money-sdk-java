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

import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.yandex.money.api.net.clients.ApiClient;
import com.yandex.money.api.utils.HttpHeaders;
import com.yandex.money.api.utils.Language;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Logger;

import static com.yandex.money.api.utils.Common.checkNotNull;

/**
 * Abstract session that provides convenience methods to work with requests.
 *
 * @author Slava Yasevich (vyasevich@yamoney.ru)
 */
public abstract class AbstractSession {

    private static final Logger LOGGER = Logger.getLogger(OAuth2Session.class.getName());

    protected final ApiClient client;

    private final CacheControl cacheControl = new CacheControl.Builder().noCache().build();

    private boolean debugLogging = false;

    /**
     * Constructor.
     *
     * @param client API client used to perform operations
     */
    protected AbstractSession(ApiClient client) {
        this.client = checkNotNull(client, "client");
    }

    /**
     * Set requests and responses logging.
     *
     * @param debugLogging {@code true} if logging is required
     */
    public final void setDebugLogging(boolean debugLogging) {
        this.debugLogging = debugLogging;
    }

    protected final <T> Call prepareCall(ApiRequest<T> request) {
        return prepareCall(prepareRequestBuilder(request));
    }

    protected final Call prepareCall(Request.Builder builder) {
        return client.getHttpClient()
                .newCall(checkNotNull(builder, "builder").build());
    }

    protected final <T> Request.Builder prepareRequestBuilder(ApiRequest<T> request) {
        checkNotNull(request, "request");

        Request.Builder builder = new Request.Builder()
                .cacheControl(cacheControl);

        UserAgent userAgent = client.getUserAgent();
        if (userAgent != null) {
            builder.addHeader(HttpHeaders.USER_AGENT, userAgent.getName());
        }

        Language language = client.getLanguage();
        if (language != null) {
            builder.addHeader(HttpHeaders.ACCEPT_LANGUAGE, language.iso6391Code);
        }

        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            String value = entry.getValue();
            if (value != null) {
                builder.addHeader(entry.getKey(), value);
            }
        }

        builder.url(request.requestUrl(client.getHostsProvider()));
        if (request.getMethod() == ApiRequest.Method.POST) {
            builder.post(RequestBody.create(MediaType.parse(request.getContentType()), request.getBody()));
        }

        return builder;
    }

    /**
     * Gets input stream from response. Logging can be applied if
     * {@link com.yandex.money.api.net.OAuth2Session#setDebugLogging(boolean)} is set to
     * {@code true}.
     *
     * @param response response reference
     * @return input stream
     */
    protected final InputStream getInputStream(Response response) throws IOException {
        InputStream stream = response.body().byteStream();
        return debugLogging ? new ResponseLoggingInputStream(stream) : stream;
    }

    /**
     * Processes connection errors.
     *
     * @param response response reference
     * @return WWW-Authenticate field of connection
     */
    protected final String processError(Response response) throws IOException {
        String field = response.header(HttpHeaders.WWW_AUTHENTICATE);
        LOGGER.warning("Server has responded with a error: " + getError(response) + "\n" +
                HttpHeaders.WWW_AUTHENTICATE + ": " + field);
        LOGGER.warning(response.body().string());
        return field;
    }

    private String getError(Response response) {
        return "HTTP " + response.code() + " " + response.message();
    }
}
