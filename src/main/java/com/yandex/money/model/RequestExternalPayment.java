package com.yandex.money.model;

import com.google.gson.*;
import com.yandex.money.ParamsP2P;
import com.yandex.money.Utils;
import com.yandex.money.net.IRequest;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 *
 */
public class RequestExternalPayment {

    private String status;
    private String error;
    private String request_id;
    private BigDecimal contract_amount;
    private String title;

    public RequestExternalPayment(String status, String error, String request_id, String contract_amount, String title) {
        this.status = status;
        this.error = error;
        this.request_id = request_id;
        this.contract_amount = new BigDecimal(contract_amount);
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getRequest_id() {
        return request_id;
    }

    public BigDecimal getContract_amount() {
        return contract_amount;
    }

    public String getTitle() {
        return title;
    }

    public static class Request implements IRequest<RequestExternalPayment> {

        private String accessToken;
        private String instanceId;
        private String patternId;
        private Map<String, String> params;

        private Request(String accessToken, String instanceId, String patternId, Map<String, String> params) {
            this.accessToken = accessToken;
            if (Utils.isEmpty(instanceId))
                throw new IllegalArgumentException(Utils.emptyParam("instanceId"));
            this.instanceId = instanceId;
            if (Utils.isEmpty(patternId))
                throw new IllegalArgumentException(Utils.emptyParam("patternId"));
            this.patternId = patternId;

            this.params = params;
        }

        public static Request newInstance(String accessToken, String instanceId, String patternId,
                                          Map<String, String> paramsShop) {
            if (paramsShop == null)
                throw new IllegalArgumentException(Utils.emptyParam("paramsShop"));

            return new Request(accessToken, instanceId, patternId, paramsShop);
        }

        public static Request newInstance(String accessToken, String instanceId, ParamsP2P paramsP2P) {
            if (paramsP2P == null)
                throw new IllegalArgumentException(Utils.emptyParam("paramsP2P"));

            return new Request(accessToken, instanceId, P2P, paramsP2P.makeParams());
        }

        @Override
        public URL requestURL() throws MalformedURLException {
            return new URL(URI_API + "/request-external-payment");
        }

        @Override
        public RequestExternalPayment parseResponse(InputStream inputStream) {
            return new GsonBuilder().registerTypeAdapter(RequestExternalPayment.class, new JsonDeserializer<RequestExternalPayment>() {
                @Override
                public RequestExternalPayment deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    JsonObject o = json.getAsJsonObject();
                    return new RequestExternalPayment(
                            Utils.getString(o, "status"),
                            Utils.getString(o, "error"),
                            Utils.getString(o, "request_id"),
                            Utils.getString(o, "contract_amount"),
                            Utils.getString(o, "title")
                            );
                }
            }).create().fromJson(new InputStreamReader(inputStream), RequestExternalPayment.class);
        }
    }
}