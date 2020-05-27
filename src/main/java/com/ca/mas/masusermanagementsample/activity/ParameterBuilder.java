/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.masusermanagementsample.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.ca.mas.connecta.client.MASConnectOptions;
import com.ca.mas.connecta.client.MASConnectaManager;
import com.ca.mas.core.storage.sharedstorage.MASSharedStorage;
import com.ca.mas.core.util.KeyUtilsAsymmetric;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASAuthenticationListener;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASClaims;
import com.ca.mas.foundation.MASClaimsBuilder;
import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.foundation.MASConnectionListener;
import com.ca.mas.foundation.MASDevice;
import com.ca.mas.foundation.MASGroup;
import com.ca.mas.foundation.MASIdToken;
import com.ca.mas.foundation.MASMultiFactorAuthenticator;
import com.ca.mas.foundation.MASOtpAuthenticationHandler;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASRequestBody;
import com.ca.mas.foundation.MASSecurityConfiguration;
import com.ca.mas.foundation.MASSessionUnlockCallback;
import com.ca.mas.foundation.MASTokenRequest;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;
import com.ca.mas.foundation.auth.MASProximityLoginBLE;
import com.ca.mas.foundation.auth.MASProximityLoginBLEPeripheralListener;
import com.ca.mas.foundation.auth.MASProximityLoginBLEUserConsentHandler;
import com.ca.mas.identity.common.MASFilteredRequest;
import com.ca.mas.identity.common.MASFilteredRequestBuilder;
import com.ca.mas.identity.group.MASMember;
import com.ca.mas.identity.group.MASOwner;
import com.ca.mas.messaging.MASMessage;
import com.ca.mas.messaging.MessagingConsts;
import com.ca.mas.messaging.topic.MASTopic;
import com.ca.mas.messaging.topic.MASTopicBuilder;
import com.ca.mas.storage.MASSecureLocalStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

class ParameterBuilder {
    private static String TAG = ParameterBuilder.class.getSimpleName();
    private static ParameterBuilder parameterBuilder = new ParameterBuilder();
    private static HashMap<Class, Converter> builders;
    private Context context;

    protected static ParameterBuilder getInstance() {
        return parameterBuilder;
    }

    public void init(Context context) {
        this.context = context;
    }

    Converter getBuilder(Class type) {
        return builders.get(type);
    }

    private ParameterBuilder() {
        builders = new HashMap<>();
        // primitives
        builders.put(boolean.class, new BooleanConverter());
        builders.put(int.class, new IntegerConverter());
        builders.put(long.class, new LongConverter());
        builders.put(char[].class, new CharArrayConverter());

        // non-MAS
        builders.put(Context.class, new ContextConverter());
        builders.put(JSONObject.class, new JSONObjectConverter());
        builders.put(URL.class, new URLConverter());
        builders.put(Object.class, new ObjectParamConverter());
        builders.put(PrivateKey.class, new PrivateKeyConverter());
        builders.put(Date.class, new DateConverter());
        builders.put(List.class, new ListConverter());
        builders.put(Uri.class, new UriConverter());

        // MAS
        builders.put(MASClaims.class, new MASClaimsConverter());
        builders.put(MASTopic.class, new MASTopicConverter());
        builders.put(MASRequest.class, new MASRequestParameterConverter());
        builders.put(MASTokenRequest.class, new MASTokenRequestParameterConverter());
        builders.put(MASCallback.class, new MASCallbackConverter());
        builders.put(MASSessionUnlockCallback.class, new MASSessionUnlockCallbackConverter());
        builders.put(MASConnectionListener.class, new MASConnectionListenerConverter());
        builders.put(MASFilteredRequest.class, new FilteredRequestBuilderConverter());
        builders.put(MASMessage.class, new MASMessageConverter());
        builders.put(MASProximityLoginBLEPeripheralListener.class, new MASProximityLoginBLEPeripheralListenerConverter());
        builders.put(MASConnectOptions.class, new MASConnectOptionsConverter());
        builders.put(MASIdToken.class, new MASIdTokenConverter());
        builders.put(MASAuthenticationListener.class, new MASAuthenticationListenerConverter());

        parameterBuilder = this;
    }

    abstract class Converter<T> {
        abstract T convert(String value);
    }

    private class BooleanConverter extends Converter<Boolean> {
        @Override
        Boolean convert(String value) {
            return Boolean.valueOf(value);
        }
    }

    private class IntegerConverter extends Converter<Integer> {
        @Override
        Integer convert(String value) {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    private class CharArrayConverter extends Converter<char[]> {
        @Override
        char[] convert(String value) {
            return value.toCharArray();
        }
    }

    private class LongConverter extends Converter<Long> {
        @Override
        Long convert(String value) {
            try {
                return Long.valueOf(value);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
    }

    private class ContextConverter extends Converter<Context> {
        public Context convert(String value) {
            return context;
        }
    }

    /**
     * Attempts to convert String input into a JSONObject.
     */
    private class JSONObjectConverter extends Converter<JSONObject> {
        @Override
        public JSONObject convert(String value) {
            JSONObject object = null;
            try {
                object = new JSONObject(value);
            } catch (JSONException e) {
                Log.e(TAG, "Unable to create a " + JSONObject.class.getSimpleName() + " from String.");
            }
            return object;
        }
    }

    /**
     * Attempts to convert String input into a URL.
     */
    private class URLConverter extends Converter<URL> {
        @Override
        public URL convert(String value) {
            if (value == null || value.trim().length() == 0) {
                return null;
            }
            try {
                return new URL(value);
            } catch (MalformedURLException e) {
                Log.e(TAG, "Failed to convert to URL object.", e);
                return null;
            }
        }
    }

    /**
     * Attempts to convert String input into a URL.
     */
    private class UriConverter extends Converter<Uri> {
        @Override
        public Uri convert(String value) {
            if (value == null || value.trim().length() == 0) {
                return null;
            }
            return new Uri.Builder().encodedAuthority(value).build();
        }
    }


    private class MASTopicConverter extends Converter<MASTopic> {
        @Override
        public MASTopic convert(String value) {
            try {
                JSONObject jsonObject = new JSONObject(value);
                MASTopicBuilder b = new MASTopicBuilder();
                String user = "";
                if (jsonObject.has("user")) {
                    user = jsonObject.getString("user");
                    b.setUserId(user);
                }

                if (jsonObject.has("topic")) {
                    String topic = jsonObject.getString("topic");
                    b.setCustomTopic(topic);
                } else {
                    b.setCustomTopic(user);
                }

                if (jsonObject.has("enforceTopicStructure")) {
                    boolean enforceTopicStructure = jsonObject.getBoolean("enforceTopicStructure");
                    b.enforceTopicStructure(enforceTopicStructure);
                }

                if (jsonObject.has("qos")) {
                    Integer qos = jsonObject.getInt("qos");
                    b.setQos(qos);
                }

                return b.build();
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private class MASCallbackConverter extends Converter<MASCallback> {
        @Override
        MASCallback convert(String value) {
            // this will be handled locally, this object is irrelevant
            return new MASCallback<Object>() {
                public void onSuccess(Object result) {
                }

                public void onError(Throwable e) {
                }
            };
        }
    }

    private class MASSessionUnlockCallbackConverter extends Converter<MASSessionUnlockCallback> {
        @Override
        MASSessionUnlockCallback convert(String value) {
            // this will be handled locally, this object is irrelevant
            return new MASSessionUnlockCallback<Object>() {
                @Override
                public void onUserAuthenticationRequired() {
                }

                public void onSuccess(Object result) {
                }

                public void onError(Throwable e) {
                }
            };
        }
    }

    public class MASConnectionListenerConverter extends Converter<MASConnectionListener> {
        @Override
        MASConnectionListener convert(String value) {
            return new MASConnectionListener() {
                @Override
                public void onObtained(HttpURLConnection connection) {

                }

                @Override
                public void onConnected(HttpURLConnection connection) {
                    Map<String, List<String>> request = connection.getRequestProperties();
                    StringBuilder sb = new StringBuilder();
                    sb.append("{").append(connection.getURL()).append("}");
                    for (String key : request.keySet()) {
                        List<String> values = request.get(key);
                        if (values != null && !values.isEmpty()) {
                            sb.append("Request method: ").append(connection.getRequestMethod()).append("\n");
                            sb.append("{\"").append(key).append("\":");
                            sb.append("\"").append(values.get(0)).append("\"}");
                        }
                    }
                    Log.d(MASConnectionListener.class.getCanonicalName(), sb.toString());
                }
            };
        }
    }

    class MASRequestParameterConverter extends Converter<MASRequest> {
        @Override
        public MASRequest convert(String value) {
            try {
                JSONObject jsonObject = new JSONObject(value);
                String path = jsonObject.getString("path");
                JSONObject parameters = jsonObject.getJSONObject("parameter");
                JSONObject headers = jsonObject.getJSONObject("header");
                String method = jsonObject.getString("method");
                String content = jsonObject.getString("content");
                String scope = jsonObject.optString("scope");
                String responseType = jsonObject.optString("response_type");
                boolean isTokenRequest = jsonObject.optBoolean("isTokenRequest", false);

                // Path
                Uri.Builder uriBuilder = new Uri.Builder().encodedPath(path);

                // Parameters parsing
                JSONArray parameterNames = parameters.names();
                for (int i = 0; i < parameterNames.length(); i++) {
                    String paramName = parameterNames.getString(i);
                    String paramValue = parameters.getString(paramName);
                    uriBuilder.appendQueryParameter(paramName, paramValue);
                }

                MASRequest.MASRequestBuilder requestBuilder = new MASRequest.MASRequestBuilder(uriBuilder.build());

                // Headers
                Iterator<String> headersIterator = headers.keys();
                while (headersIterator.hasNext()) {
                    String headerKey = headersIterator.next();
                    String headerValue = headers.getString(headerKey);
                    requestBuilder.header(headerKey, headerValue);
                }

                // Method/Content
                switch (method.toLowerCase()) {
                    case "get":
                        requestBuilder.get();
                        break;
                    case "put":
                        if (responseType.contains("json")) {
                            try {
                                JSONObject contentJson = new JSONObject(content);
                                requestBuilder.put(MASRequestBody.jsonBody(contentJson));
                            } catch (JSONException e) {
                                Log.e(TAG, "Failed to parse MASRequest as JSON: " + e.getMessage());
                                requestBuilder.put(MASRequestBody.stringBody(content));
                            }
                        } else if (responseType.contains("byte")) {
                            requestBuilder.put(MASRequestBody.byteArrayBody(content.getBytes()));
                        } else {
                            requestBuilder.put(MASRequestBody.stringBody(content));
                        }
                        break;
                    case "post":
                        if (responseType.contains("json")) {
                            try {
                                JSONObject contentJson = new JSONObject(content);
                                requestBuilder.post(MASRequestBody.jsonBody(contentJson));
                            } catch (JSONException e) {
                                Log.e(TAG, "Failed to parse MASRequest as JSON: " + e.getMessage());
                                requestBuilder.post(MASRequestBody.stringBody(content));
                            }
                        } else if (responseType.contains("byte")) {
                            requestBuilder.post(MASRequestBody.byteArrayBody(content.getBytes()));
                        } else {
                            requestBuilder.post(MASRequestBody.stringBody(content));
                        }
                        break;
                    case "delete":
                        if (responseType.contains("json")) {
                            try {
                                JSONObject contentJson = new JSONObject(content);
                                requestBuilder.delete(MASRequestBody.jsonBody(contentJson));
                            } catch (JSONException e) {
                                Log.e(TAG, "Failed to parse MASRequest as JSON: " + e.getMessage());
                                requestBuilder.delete(MASRequestBody.stringBody(content));
                            }
                        } else if (responseType.contains("byte")) {
                            requestBuilder.delete(MASRequestBody.byteArrayBody(content.getBytes()));
                        } else {
                            requestBuilder.delete(MASRequestBody.stringBody(content));
                        }
                        break;
                }

                if (scope != null && scope.trim().length() > 0) {
                    requestBuilder.scope(scope);
                }

                if (jsonObject.has("notifyOnCancel")) {
                    boolean notifyOnCancel = jsonObject.getBoolean("notifyOnCancel");
                    if (notifyOnCancel) {
                        requestBuilder.notifyOnCancel();
                    }
                }

                if (jsonObject.has("sign")) {
                    boolean sign = jsonObject.getBoolean("sign");
                    if (sign) {
                        requestBuilder.sign();
                    }
                }

                if (jsonObject.has("signWithClaims")) {
                    JSONObject signWithClaims = jsonObject.getJSONObject("signWithClaims");
                    requestBuilder.sign(new MASClaimsConverter().convert(signWithClaims.toString()));
                }

                if (jsonObject.has("signWithPrivateKey")) {
                    boolean signWithPrivateKey = jsonObject.getBoolean("signWithPrivateKey");
                    if (signWithPrivateKey) {
                        requestBuilder.sign(new PrivateKeyConverter().convert(null));
                    }
                }

                if (jsonObject.has("setPublic")) {
                    boolean isPublic = jsonObject.getBoolean("setPublic");
                    if (isPublic) {
                        requestBuilder.setPublic();
                    }
                }

                if (isTokenRequest) {
                    return new com.ca.mas.foundation.MASTokenRequest(requestBuilder.build());
                } else {
                    return requestBuilder.build();
                }
            } catch (Exception e) {
                Log.e(TAG, "Unable to build a " + MASRequest.class.getSimpleName() + " : " + e);
                return null;
            }
        }
    }

    public class MASTokenRequestParameterConverter extends Converter<MASRequest> {
        @Override
        public MASRequest convert(String value) {
            MASRequestParameterConverter masRequestBuilder = (MASRequestParameterConverter) builders.get(MASRequest.class);
            MASRequest request = masRequestBuilder.convert(value);
            return new MASTokenRequest(request);
        }
    }

    private class MASMessageConverter extends Converter<MASMessage> {
        @Override
        MASMessage convert(String value) {
            try {
                JSONObject jsonObject = new JSONObject(value);
                Integer qos = 2;
                if (jsonObject.has("qos")) {
                    qos = jsonObject.getInt("qos");
                }

                boolean retained = false;
                if (jsonObject.has("retained")) {
                    retained = jsonObject.getBoolean("retained");
                }

                String type = jsonObject.getString("type");
                switch (type) {
                    case "string":
                        MASMessage message = MASMessage.newInstance();
                        message.setQos(qos);
                        message.setRetained(retained);
                        message.setContentType(MessagingConsts.MT_TEXT_PLAIN);
                        if (jsonObject.has("content")) {
                            message.setPayload(jsonObject.getString("content").getBytes());
                        }
                        return message;
                    case "image":
                        MASMessage imageMessage = MASMessage.newInstance();
                        imageMessage.setQos(qos);
                        imageMessage.setRetained(retained);
                        imageMessage.setContentType("image/png");
                        if (jsonObject.has("content")) {
                            byte[] bytes = readImageFromFileSystem(jsonObject.getString("content"));
                            imageMessage.setPayload(bytes);
                        }
                        return imageMessage;
                    case "null":
                        // For a full null object, use type null, content null
                        if (jsonObject.has("content") && "null".equals(jsonObject.getString("content"))) {
                            return null;
                        } else {
                            // For a null payload MASMessage, use type null, content anything
                            MASMessage nullmessage = MASMessage.newInstance();
                            nullmessage.setPayload(null);
                            return nullmessage;
                        }
                    default:
                        throw new IllegalArgumentException(MASMessage.class.getSimpleName() + ": Invalid type");
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                MASMessage message = MASMessage.newInstance();
                message.setContentType(MessagingConsts.MT_TEXT_PLAIN);
                message.setPayload(value.getBytes());
                return message;
            }
        }

        private byte[] readImageFromFileSystem(String imagePath) throws IOException {
            InputStream stream = context.getAssets().open(imagePath);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            return buffer;
        }
    }

    private class ObjectParamConverter extends Converter<Object> {
        @Override
        Object convert(String value) {
            if (value != null) {
                try {
                    JSONObject jsonObject = new JSONObject(value);
                    String type = jsonObject.getString("type");
                    switch (type) {
                        case "bytes":
                            return jsonObject.getString("content").getBytes();
                        case "json":
                            return jsonObject.getJSONObject("content");
                        case "string":
                            return jsonObject.getString("content");
                        case "image":
                            return getBitmapFromAsset(jsonObject.getString("content"));
                        case "int":
                            return jsonObject.getInt("content");
                        default:
                            return null;
                    }
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        }
    }

    private Bitmap getBitmapFromAsset(String strName) throws IOException {
        /* Glide requires to run in background thread
        Bitmap result = null;
        try {
            result = Glide.with(mContextRef.get())
                    .load(strName)
                    .asBitmap()
                    .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();
            return result;
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
        */
        AssetManager assetManager = MAS.getContext().getAssets();
        InputStream istr = assetManager.open(strName);
        return BitmapFactory.decodeStream(istr);
    }



    private class FilteredRequestBuilderConverter extends Converter<MASFilteredRequest> {
        @Override
        MASFilteredRequest convert(String value) {
            /*
            {
              "filterType": "groupAttributesKey",
              "attributes": [
                "displayName",
                "owner.value",
                "owner.$ref",
                "owner.display",
                "members.value",
                "members.$ref",
                "members.type",
                "members.display"
              ],
              "totalResults": 100,
              "pagination": {
                "start": 0,
                "count": 10
              },
              "sortOrder": {
                "sortOrder": "ascending",
                "attribute": ""
              },
              "filterBy": {
                "filter": "isEqualTo",
                "attribute": "userName",
                "filterValue": "*"
              }
            }
             */
            try {
                JSONObject jsonObject = new JSONObject(value);
                JSONArray attributes = jsonObject.getJSONArray("attributes");
                List<String> atts = new ArrayList<>();
                for (int i = 0; i < attributes.length(); i++) {
                    atts.add(attributes.getString(i));
                }
                MASFilteredRequest builder = new MASFilteredRequest(atts, jsonObject.getString("filterType"));
                JSONObject paginationObject = jsonObject.getJSONObject("pagination");
                builder.setPagination(paginationObject.optInt("start"), paginationObject.optInt("count"));

                JSONObject sortOrderObject = jsonObject.getJSONObject("sortOrder");
                builder.setSortOrder(MASFilteredRequestBuilder.SortOrder.valueOf(sortOrderObject.optString("sortOrder")),
                        sortOrderObject.optString("attribute"));

                JSONObject filterBy = jsonObject.optJSONObject("filterBy");
                if (filterBy != null) {
                    String filter = filterBy.getString("filter");
                    String attribute = filterBy.getString("attribute");
                    String filterValue = filterBy.getString("filterValue");
                    switch (filter) {
                        case "contains":
                            builder.contains(attribute, filterValue);
                            break;
                        case "isEqualTo":
                            builder.isEqualTo(attribute, filterValue);
                            break;
                        case "isGreaterThan":
                            builder.isGreaterThan(attribute, filterValue);
                            break;
                        case "isGreaterThanOrEqual":
                            builder.isGreaterThanOrEqual(attribute, filterValue);
                            break;
                        case "isLessThan":
                            builder.isLessThan(attribute, filterValue);
                            break;
                        case "isLessThanOrEqual":
                            builder.isLessThanOrEqual(attribute, filterValue);
                            break;
                        case "isNotEqualTo":
                            builder.isNotEqualTo(attribute, filterValue);
                            break;
                        case "isPresent":
                            builder.isPresent(attribute);
                            break;
                        case "startsWith":
                            builder.startsWith(attribute, filterValue);
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported filter:" + filter);
                    }
                }
                return builder;
            } catch (JSONException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private class MASProximityLoginBLEPeripheralListenerConverter extends Converter<MASProximityLoginBLEPeripheralListener> {
        @Override
        MASProximityLoginBLEPeripheralListener convert(String value) {
            return new MASProximityLoginBLEPeripheralListener() {
                @Override
                public void onStatusUpdate(int state) {
                    switch (state) {
                        case MASProximityLoginBLEPeripheralListener.BLE_STATE_CONNECTED:
                            Log.d(TAG, "BLE Client Connected");
                            break;
                        case MASProximityLoginBLEPeripheralListener.BLE_STATE_DISCONNECTED:
                            Log.d(TAG, "BLE Client Disconnected");
                            break;
                        case MASProximityLoginBLEPeripheralListener.BLE_STATE_STARTED:
                            Log.d(TAG, "BLE peripheral mode started");
                            break;
                        case MASProximityLoginBLEPeripheralListener.BLE_STATE_STOPPED:
                            Log.d(TAG, "BLE peripheral mode stopped");
                            break;
                        case MASProximityLoginBLEPeripheralListener.BLE_STATE_SESSION_AUTHORIZED:
                            Log.d(TAG, "BLE session authorized");
                            break;
                    }
                }

                @Override
                public void onError(int errorCode) {
                    String message = null;
                    switch (errorCode) {
                        case MASProximityLoginBLEPeripheralListener.BLE_ERROR_ADVERTISE_FAILED:
                            message = "Advertise failed";
                            break;
                        case MASProximityLoginBLEPeripheralListener.BLE_ERROR_AUTH_FAILED:
                            message = "Auth failed";
                            break;
                        case MASProximityLoginBLEPeripheralListener.BLE_ERROR_CENTRAL_UNSUBSCRIBED:
                            message = "Central UnSubscribed";
                            break;
                        case MASProximityLoginBLEPeripheralListener.BLE_ERROR_PERIPHERAL_MODE_NOT_SUPPORTED:
                            message = "Peripheral mode not supported";
                            break;
                        case MASProximityLoginBLE.BLE_ERROR_DISABLED:
                            message = "Bluetooth Disabled";
                            break;
                        case MASProximityLoginBLE.BLE_ERROR_INVALID_UUID:
                            message = "Invalid UUID";
                            break;
                        case MASProximityLoginBLE.BLE_ERROR_NOT_SUPPORTED:
                            message = "Bluetooth not supported";
                            break;
                        case MASProximityLoginBLE.BLE_ERROR_SESSION_SHARING_NOT_SUPPORTED:
                            message = "Session sharing not supported";
                            break;
                        default:
                            message = Integer.toString(errorCode);
                    }
                    Log.i("BLE Error", message);
                }

                @Override
                public void onConsentRequested(final Context context, final String deviceName, final MASProximityLoginBLEUserConsentHandler handler) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage("Do you want to grant session to " + deviceName + "?").
                                    setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            handler.proceed();
                                        }
                                    }).setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    handler.cancel();
                                }
                            }).show();
                        }
                    });
                }
            };
        }
    }

    private class MASConnectOptionsConverter extends Converter<MASConnectOptions> {
        @Override
        MASConnectOptions convert(String value) {
            if (value == null) {
                return null;
            }

            MASConnectOptions connectOptions = new MASConnectOptions();
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(value);

                if (jsonObject.has("username")) {
                    connectOptions.setUserName(jsonObject.getString("username"));
                }

                if (jsonObject.has("password")) {
                    connectOptions.setPassword(jsonObject.getString("password").toCharArray());
                }

                if (jsonObject.has("keepalive")) {
                    connectOptions.setKeepAliveInterval(jsonObject.getInt("keepalive"));
                }

                if (jsonObject.has("serveruri")) {
                    connectOptions.setServerURIs(new String[]{jsonObject.getString("serveruri")});
                }

                if (jsonObject.has("cleansession")) {
                    connectOptions.setCleanSession(jsonObject.getBoolean("cleansession"));
                }

                if (jsonObject.has("will")) {
                    JSONObject willObject = jsonObject.getJSONObject("will");
                    String topic = willObject.getString("topic");
                    String payload = willObject.getString("payload");
                    Integer qos = willObject.getInt("qos");
                    Boolean retained = willObject.getBoolean("retained");
                    connectOptions.setWill(topic, payload.getBytes(), qos, retained);
                }

                if (jsonObject.has("usessl")) {
                    boolean useSsl = jsonObject.getBoolean("usessl");
                    if (useSsl) {
                        X509TrustManager trustManager = new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }
                        };
                        TrustManager[] myTrustManagerArray = new TrustManager[]{trustManager};

                        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                        keyStore.load(null, "".toCharArray());

                        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                        keyManagerFactory.init(keyStore, null);

                        SSLContext context = SSLContext.getInstance("TLSv1");
                        context.init(keyManagerFactory.getKeyManagers(), myTrustManagerArray, new java.security.SecureRandom());

                        connectOptions.setSocketFactory(context.getSocketFactory());
                    }
                }
            } catch (Exception e) {
                return null;
            }

            return connectOptions;
        }
    }

    private class MASIdTokenConverter extends Converter<MASIdToken> {
        @Override
        MASIdToken convert(String value) {
            if (value != null) {
                try {
                    JSONObject jsonObject = new JSONObject(value);
                    String v = jsonObject.getString("value");
                    String t = jsonObject.optString("type", null);
                    MASIdToken.Builder builder = new MASIdToken.Builder().value(value);
                    if (t != null) {
                        builder.type(t);
                    }
                    return builder.build();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }
    }

    private class MASAuthenticationListenerConverter extends Converter<MASAuthenticationListener> {
        @Override
        MASAuthenticationListener convert(String value) {
            if (value != null && value.trim().length() > 0) {
                //use default.
                return null;
            } else {
                return new MASAuthenticationListener() {
                    @Override
                    public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {

                    }

                    @Override
                    public void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler) {

                    }
                };
            }
        }
    }


    private class PrivateKeyConverter extends Converter<PrivateKey> {
        @Override
        PrivateKey convert(String value) {
            try {
                return KeyUtilsAsymmetric.generateRsaPrivateKey("TEST", "dn=test", false, false, -1, false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Takes in a Unix epoch time in seconds.
     */
    private class DateConverter extends Converter<Date> {
        @Override
        Date convert(String value) {
            try {
                return value != null ?
                        new Date(Long.parseLong(value) * 1000) :
                        null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class ListConverter extends Converter<List> {
        @Override
        List convert(String value) {
            if (value != null) {
                try {
                    JSONObject jsonObject = new JSONObject(value);
                    String type = jsonObject.getString("type");
                    if (type != null && "string".equalsIgnoreCase(type)) {
                        JSONArray array = jsonObject.getJSONArray("content");
                        if (array != null) {
                            List<String> list = new ArrayList<>();
                            for (int i = 0; i < array.length(); i++) {
                                String s = array.getString(i);
                                list.add(s);
                            }
                            return list;
                        }
                    }
                } catch (Exception e) {
                    return Collections.singletonList(null);
                }
            }
            return Collections.singletonList(null);
        }
    }




    private class MASClaimsConverter extends Converter<MASClaims> {
        @SuppressWarnings("ResourceType")
        @Override
        MASClaims convert(String value) {
            try {
                JSONObject jsonObject = new JSONObject(value);
                MASClaimsBuilder builder = new MASClaimsBuilder();
                for (Iterator<String> iter = jsonObject.keys(); iter.hasNext(); ) {
                    String key = iter.next();
                    builder.claim(key, jsonObject.get(key));
                }
                return builder.build();
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
    }


}
