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

package com.yandex.money.api.typeadapters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializer;

/**
 * Base class for type adapters.
 *
 * @author Slava Yasevich (vyasevich@yamoney.ru)
 */
public abstract class BaseTypeAdapter<T>
        implements TypeAdapter<T>, JsonSerializer<T>, JsonDeserializer<T> {

    private final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(getType(), this)
            .create();

    @Override
    public final T fromJson(String json) {
        return GSON.fromJson(json, getType());
    }

    @Override
    public final T fromJson(JsonElement element) {
        return GSON.fromJson(element, getType());
    }

    @Override
    public final String toJson(T value) {
        return GSON.toJson(value);
    }

    @Override
    public final JsonElement toJsonTree(T value) {
        return GSON.toJsonTree(value);
    }

    protected abstract Class<T> getType();
}