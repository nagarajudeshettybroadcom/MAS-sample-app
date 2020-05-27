package com.ca.mas.masusermanagementsample.activity;

import android.Manifest;
import android.app.Application;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ca.mas.core.error.MAGError;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASAuthCredentialsAuthorizationCode;
import com.ca.mas.foundation.MASAuthenticationListener;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.foundation.MASConstants;
import com.ca.mas.foundation.MASDevice;
import com.ca.mas.foundation.MASException;
import com.ca.mas.foundation.MASFileObject;
import com.ca.mas.foundation.MASOtpAuthenticationHandler;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.MASSecurityConfiguration;
import com.ca.mas.foundation.MASSessionUnlockCallback;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.MultiPart;
import com.ca.mas.foundation.auth.MASAuthenticationProvider;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;
import com.ca.mas.foundation.auth.MASProximityLogin;
import com.ca.mas.foundation.auth.MASProximityLoginQRCode;
import com.ca.mas.masusermanagementsample.R;
import com.ca.mas.masusermanagementsample.adapter.CustomExpandableJSONListAdapter;
import com.ca.mas.masusermanagementsample.model.ExpandableListDataPump;
import com.ca.mas.masusermanagementsample.model.MASMenu;
import com.ca.mas.masusermanagementsample.model.Submenu;
import com.ca.mas.ui.MASCustomTabs;
import com.ca.mas.ui.MASLoginActivity;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;


public class MainActivity extends AppCompatActivity {

    ExpandableListView expandableListView;
    CustomExpandableJSONListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    HashMap<String, List<String>> expandableListDetail;
    private int REQUEST_CODE = 0x1000;
    protected static final int CAMERA_REQUEST = 111;

    protected String user = "spock";
    protected String pass = "StRonG5^)";
    private MASMenu data;
    private String lastSelectedMenu;
    private int lastSelectedPosition;
    private int lastSelectedGroupPosition;
    public static String MULTIPART_UPLOAD_ENDPOINT = "/test/multipart/";
    private View mContainer;
    private MASProximityLogin qrCode;
    private long mRequestId;
    private MASAuthenticationProviders mProviders;
    private AlertDialog qrCodeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        mContainer = findViewById(R.id.container);
        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        expandableListDetail = ExpandableListDataPump.getData();
        expandableListTitle = new ArrayList<String>(expandableListDetail.keySet());

        String jsonObject = loadAssetTextAsString(this, "menu.json");
        data = new MASMenu();
        Gson gson = new Gson();
        data = gson.fromJson(jsonObject, MASMenu.class);


        expandableListAdapter = new CustomExpandableJSONListAdapter(this, data);
        expandableListView.setAdapter(expandableListAdapter);

        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        data.getList().get(groupPosition).getName() + " List Expanded.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        data.getList().get(groupPosition).getName() + " List Collapsed.",
                        Toast.LENGTH_SHORT).show();

            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {


            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Toast.makeText(
                        getApplicationContext(),
                        data.getList().get(groupPosition).getName()
                                + " -> "
                                + data.getList().get(groupPosition).getSubmenu().get(childPosition).getMenu(), Toast.LENGTH_SHORT
                ).show();
                Submenu subMenu = data.getList().get(groupPosition).getSubmenu().get(childPosition);
                String childName = subMenu.getMenu();
                String type = subMenu.getType();
                lastSelectedMenu = childName;
                lastSelectedGroupPosition = groupPosition;
                lastSelectedPosition = childPosition;
                menuItemSelected(childName);
                return false;
            }


        });
        JSONObject jsonObject1 = getConfig("msso_config.json");
        MAS.start(this,jsonObject1);
        MAS.debug();

        lastSelectedMenu = "MAS Start";
        updateData("Started");

        MASAuthenticationProviders.getAuthenticationProviders(new MASCallback<MASAuthenticationProviders>() {
            @Override
            public void onSuccess(MASAuthenticationProviders result) {
                MASAuthenticationProviders providers = result;
            }

            @Override
            public void onError(Throwable e) {

            }
        });

        if (MASUser.getCurrentUser() != null) {
            ((TextView) findViewById(R.id.usernametextfield)).setText(MASUser.getCurrentUser().getUserName());
        }
    }
    private JSONObject getConfig(String filename) {
        InputStream is = null;
        StringBuilder jsonConfig = new StringBuilder();

        try {
            is = getAssets().open(filename);
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String str;
            while ((str = in.readLine()) != null) {
                jsonConfig.append(str);
            }
            return new JSONObject(jsonConfig.toString());
        } catch (IOException | JSONException e) {
            showSnackbar("Config file could not be located, see log for details.");
            Log.e(MainActivity.class.getSimpleName(), e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    //Ignore
                }
            }
        }

        return new JSONObject();
    }
    private void menuItemSelected(String childName) {
        if (childName.equals("MAS Start")) {
            MAS.debug();
            MAS.start(MainActivity.this);
            updateData("Started");
        } else if ("MAS Login".equals(childName)) {
            loginDialogue();
        } else if (childName.equals("MAS Logout")) {
            logout();
        } else if (childName.equals("Get Products")) {
            getProducts();
        } else if (childName.equals("otpProtected")) {
            otpProtected();
        } else if (childName.equals("MAS Stop")) {
            if (MAS.getState(MainActivity.this) != MASConstants.MAS_STATE_NOT_INITIALIZED) {
                MAS.stop();
            }
            updateData("MAS Stopped");
        } else if (childName.equals("MASUser getCurrentUser")) {
            updateData(MASUser.getCurrentUser() != null ? MASUser.getCurrentUser().getDisplayName() : "");
        } else if (childName.equals("MASUser getUsername")) {
            updateData(MASUser.getCurrentUser() != null ? MASUser.getCurrentUser().getUserName() : "");
        } else if (childName.equals("MASUser isSessionLocked")) {
            updateData(MASUser.getCurrentUser() != null ? MASUser.getCurrentUser().isSessionLocked() + "" : "");
        } else if (childName.equals("MASUser LockSession")) {
            if (MASUser.getCurrentUser() != null) {
                lockSession();
            }
        } else if (childName.equals("MASUser unLockSession")) {
            if (MASUser.getCurrentUser() != null) {
                MASUser.getCurrentUser().unlockSession(getUnlockCallback(MainActivity.this));
            }
        } else if (childName.equals("MASUser getAccessToken")) {
            updateData(MASUser.getCurrentUser() != null ? MASUser.getCurrentUser().getAccessToken() : "");
        } else if (childName.equals("MASUser isAuthenticated")) {
            updateData(MASUser.getCurrentUser() != null ? MASUser.getCurrentUser().isAuthenticated() + "" : "");
        } else if (childName.equals("MASDevice isDeviceRegistered")) {
            updateData(MASDevice.getCurrentDevice().isRegistered() + "");
        } else if (childName.equals("MASDevice getDeviceIdentifier")) {
            updateData(MASDevice.getCurrentDevice().getIdentifier() + "");
        } else if (childName.equals("MASDevice resetLocally")) {
            if (MASDevice.getCurrentDevice() != null) {
                MASDevice.getCurrentDevice().resetLocally();
            }
        } else if (childName.equals("Login")) {

            socialLogin();

        } else if (childName.equals("Upload")) {
            try {
                uploadMultipartFileOnlyTest();
            } catch (Exception e) {
                e.printStackTrace();
            } catch (MASException e) {
                e.printStackTrace();
            }
        } else if (childName.equals("https://swapi.co:443")) {
            try {
                new ExternalSecureURLTask().execute(new URL("https://swapi.co:443"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else if (childName.equals("http://api.plos.org")) {
            getExternalWebData("http://api.plos.org/search?q=title:DNA");
        } else if (childName.equals("Scan QR Code")) {
            int cameraCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (cameraCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_REQUEST);
            } else {
                if (!MainActivity.this.isFinishing()){
                    IntentIntegrator intentIntegrator = new IntentIntegrator(this);
                    intentIntegrator.initiateScan();
                }
            }
        } else if (childName.equals("Show QR Code")) {
            showQRCode();

        } else if (childName.equals("My QR Code")) {
            generateQRCODE("Broadcom CA Company");
        }

    }
    private void initProximity(MASProximityLogin masProximityLogin) {
        boolean init = masProximityLogin.init(this, mRequestId, mProviders);
        if (init) {
            masProximityLogin.start();
        }
    }

    private MASProximityLoginQRCode getQrCode() {
        return new MASProximityLoginQRCode() {
            @Override
            public void onError(int errorCode, final String m, Exception e) {
                // Hide QR Code option
                cancelQRCodeDialog(m);
            }

            @Override
            protected void onAuthCodeReceived(String code, String state) {
                super.onAuthCodeReceived(code, state);
                cancelQRCodeDialog("Auth code received");
                onProximityAuthenticated(code, state);

            }

            void cancelQRCodeDialog(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        View qrButton = findViewById(com.ca.mas.ui.R.id.activity_mas_login_qr_code);
//                        if (mGridLayout != null) {
//                            mGridLayout.removeView(qrButton);
//                        }
//                        if (qrCodeDialog != null && qrCodeDialog.isShowing()) {
//                            qrCodeDialog.cancel();
//                        }
//                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

        };
    }
    private void onProximityAuthenticated(String code, String state) {
        //Fetch the user profile
        MASUser.login(new MASAuthCredentialsAuthorizationCode(code, state), new MASCallback<MASUser>() {
            @Override
            public void onSuccess(MASUser result) {
                finish();
            }

            @Override
            public void onError(final Throwable e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void showQRCode() {
        MAS.setAuthenticationListener(new MASAuthenticationListener() {
            @Override
            public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {

                mProviders = providers;
                mRequestId = requestId;
                qrCode = getQrCode();
                initProximity(qrCode);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(com.ca.mas.ui.R.layout.qr_code_dialog, null);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(500, 500);
                        ImageView imageView = (ImageView) qrCode.render();
                        imageView.setLayoutParams(layoutParams);
                        linearLayout.addView(imageView);
                        qrCodeDialog = new AlertDialog.Builder(MainActivity.this)
                                .setView(linearLayout)
                                .setNegativeButton(getString(com.ca.mas.ui.R.string.done), null)
                                .show();
                    }
                });

            }

            @Override
            public void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler) {

            }
        });
        MASUser.login(new MASCallback<MASUser>() {
            @Override
            public void onSuccess(MASUser result) {
                updateData("Success");
            }

            @Override
            public void onError(Throwable e) {

            }
        });
    }

    public void generateQRCODE(String message) {
        LayoutInflater li = LayoutInflater.from(MainActivity.this);
        View promptsView = li.inflate(R.layout.qr_code_dialogue, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        alertDialogBuilder.setView(promptsView);


        final ImageView messageTextField = (ImageView) promptsView.findViewById(R.id.qrcodeimageview);

        String text = message; // Whatever you need to encode in the QR code
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            messageTextField.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    private void socialLogin() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                MAS.setAuthenticationListener(new MASAuthenticationListener() {
                    @Override
                    public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {

                        MASCustomTabs.socialLogin(context, providers.getProviders().get(1), new MASCallback<Void>() {
                            public void onSuccess(Void result) {
                                updateData("Success");
                            }

                            public void onError(Throwable e) {

                                Toast.makeText(MainActivity.this, "Launching Social Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler) {

                    }
                });

                MASUser.login(new MASCallback<MASUser>() {
                    @Override
                    public void onSuccess(MASUser result) {
                        updateData("Success");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });

            }
        });

    }

    public void getExternalWebData(String webURL) {
        URL url = null;
        try {
            url = new URL(webURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        MASSecurityConfiguration configuration = new MASSecurityConfiguration.Builder()
                .host(Uri.parse(webURL))
                .trustPublicPKI(true)
                .isPublic(true)//if set to false user login is required
                .build();
        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration);

        Uri uri = new Uri.Builder().encodedAuthority(url.getAuthority())
                .scheme(url.getProtocol())
                .build();
        MASRequest request = new MASRequest.MASRequestBuilder(Uri.parse(webURL)).build();
        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public Handler getHandler() {
                return new Handler(Looper.getMainLooper());
            }

            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                String str = result.getBody().getContent().toString().replace("\\", "/");
                showMessage(str.replace("//", "/"));
            }

            @Override
            public void onError(Throwable e) {
            }
        });
    }


    class ExternalSecureURLTask extends AsyncTask<URL, Void, Certificate[]> {


        protected Certificate[] doInBackground(URL... urls) {
            try {
                return getCert(urls[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Certificate[] feed) {


            URL url = null;
            try {
                url = new URL("https://swapi.co:443");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            MASSecurityConfiguration.Builder configuration = new MASSecurityConfiguration.Builder()
                    .host(new Uri.Builder().encodedAuthority(url.getHost() + ":" + url.getPort()).build());

            Certificate[] certificates = feed;
            for (Certificate certificate : certificates) {
                configuration.add(certificate);
            }

            MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration.build());

            Uri uri = new Uri.Builder().encodedAuthority(url.getAuthority())
                    .scheme(url.getProtocol())
                    .appendPath("api")
                    .appendPath("people")
                    .appendPath("1")
                    .build();
            MASRequest request = new MASRequest.MASRequestBuilder(uri)
                    .build();

            MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {
                @Override
                public Handler getHandler() {
                    return new Handler(Looper.getMainLooper());
                }

                @Override
                public void onSuccess(MASResponse<JSONObject> result) {
                    String str = result.getBody().getContent().toString().replace("\\", "/");
                    showMessage(str.replace("//", "/"));
                }

                @Override
                public void onError(Throwable e) {
                }
            });

        }
    }

    private Certificate[] getCert(URL url) throws Exception {
        //URL url = new URL("https://mobile-staging-androidautomation.l7tech.com:8443");
        //URL url = new URL("https://swapi.co");
        SSLContext sslCtx = SSLContext.getInstance("TLS");
        sslCtx.init(null, new TrustManager[]{new X509TrustManager() {

            private X509Certificate[] accepted;

            @Override
            public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                throw new CertificateException("This trust manager is only for clients");
            }

            @Override
            public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                accepted = xcs;
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return accepted;
            }
        }}, null);

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setSSLSocketFactory(sslCtx.getSocketFactory());
        connection.getResponseCode();
        Certificate[] certificates = connection.getServerCertificates();
        connection.disconnect();
        return certificates;
    }

    public void uploadMultipartFileOnlyTest() throws Exception, MASException {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {

                    MASFileObject filePart = new MASFileObject();
                    MultiPart multiPart = new MultiPart();

                    filePart.setFieldName("file1");
                    filePart.setFileName("photo.png");
                    filePart.setFilePath(getFilePath("photo.png"));
                    filePart.setFileType("application/pdf");

                    multiPart.addFilePart(filePart);

                    final MASRequest request = new MASRequest.MASRequestBuilder(new URI(MULTIPART_UPLOAD_ENDPOINT)).build();

                    MAS.postMultiPartForm(request, multiPart, null, new MASCallback<MASResponse>() {
                        @Override
                        public void onSuccess(MASResponse result) {
                            updateData("Successfully Uploaded ");

                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });


                } catch (Exception e) {
                    Log.d("Exception",e.getMessage());
                } catch (MASException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private String getFilePath(String fileName) throws IOException {
        InputStream is = this.getAssets().open(fileName);
        byte[] fileBytes = new byte[is.available()];
        is.read(fileBytes);
        is.close();

        String folder = this.getCacheDir().getAbsolutePath();
        File file = new File(folder, fileName);

        FileOutputStream outputStream = new FileOutputStream(file);

        outputStream.write(fileBytes);
        outputStream.close();
        return file.getAbsolutePath();

    }

    private void loginDialogue() {
        LayoutInflater li = LayoutInflater.from(MainActivity.this);
        View promptsView = li.inflate(R.layout.login_dialogue, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        alertDialogBuilder.setView(promptsView);

        final EditText userName = (EditText) promptsView.findViewById(R.id.usernametextfield);
        final EditText password = (EditText) promptsView.findViewById(R.id.passwordtextfield);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String userNameString = userName.getText().toString();
                                String passwordString = password.getText().toString();
//                                            String userNameString =   "spock";
//                                            String passwordString =   "StRonG5^)";

                                login(userNameString, passwordString);

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void getProducts() {
        String path = "/protected/resource/products";
        Uri.Builder uriBuilder = new Uri.Builder().encodedPath(path);
        uriBuilder.appendQueryParameter("operation", "listProducts");
        uriBuilder.appendQueryParameter("pName2", "pValue2");

        MASRequest.MASRequestBuilder requestBuilder = new MASRequest.MASRequestBuilder(uriBuilder.build());
        requestBuilder.header("hName1", "hValue1");
        requestBuilder.header("hName2", "hValue2");
        MASRequest request = requestBuilder.get().build();

        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public Handler getHandler() {
                return new Handler(Looper.getMainLooper());
            }

            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                try {
                    List<String> objects = parseProductListJson(result.getBody().getContent());
                    String objectString = "";
                    int size = objects.size();
                    for (int i = 0; i < size; i++) {
                        objectString += objects.get(i);
                        if (i != size - 1) {
                            objectString += "\n";
                        }
                    }

                    showMessage(objectString);
                } catch (JSONException e) {

                }
            }

            @Override
            public void onError(Throwable e) {
            }
        });
    }

    private void otpProtected() {
        String path = "/otpProtected";
        Uri.Builder uriBuilder = new Uri.Builder().encodedPath(path);
        uriBuilder.appendQueryParameter("operation", "listProducts");
        uriBuilder.appendQueryParameter("pName2", "pValue2");

        MASRequest.MASRequestBuilder requestBuilder = new MASRequest.MASRequestBuilder(uriBuilder.build());
        requestBuilder.header("hName1", "hValue1");
        requestBuilder.header("hName2", "hValue2");
        final MASRequest request = requestBuilder.get().build();

        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public Handler getHandler() {
                return new Handler(Looper.getMainLooper());
            }

            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                MASResponse<JSONObject> object = result;
            }

            @Override
            public void onError(Throwable e) {
            }
        });
    }

    private void lockSession() {
        MASUser.getCurrentUser().lockSession(new MASCallback<Void>() {

            @Override
            public Handler getHandler() {
                return new Handler(Looper.getMainLooper());
            }


            @Override
            public void onSuccess(Void result) {
                String str = "test";
                updateData("Session Locked");
//                    menuItemSelected(data.getList().get(lastSelectedGroupPosition).getSubmenu().get(lastSelectedPosition - 1).getMenu());
            }

            @Override
            public void onError(Throwable e) {
                String str = "test";
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Barcode 1", "" + requestCode);
        if (resultCode == RESULT_OK) {
            if (requestCode == IntentIntegrator.REQUEST_CODE) {
                IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (scanResult != null) {
                    String r = scanResult.getContents();
                    Log.d("Barcode 2", "" + r);
                    if (r != null) {
                        MASProximityLoginQRCode.authorize(r, new MASCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                Log.d("Barcode 4", requestCode + "");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d("Barcode 5", requestCode + "");
                                showSnackbar(e.toString());
                                if (e instanceof MAGError) {
                                    MAGError me = (MAGError) e;
                                }
                            }
                        });
                    }
                }
            } else if (requestCode == REQUEST_CODE) {
                Log.d("Barcode 3", requestCode + "");
                MASUser.getCurrentUser().unlockSession(getUnlockCallback(this));
            }
        }
    }

    protected void showSnackbar(String string) {
        Snackbar sb = Snackbar.make(mContainer, string, Snackbar.LENGTH_LONG);
        if (sb != null) {
            sb.getView().setBackgroundResource(R.color.colorPrimary);
            sb.show();
        }
    }

    private MASSessionUnlockCallback<Void> getUnlockCallback(final MainActivity activity) {
        return new MASSessionUnlockCallback<Void>() {
            @Override
            public void onUserAuthenticationRequired() {
                KeyguardManager keyguardManager = (KeyguardManager) activity.getSystemService(Application.KEYGUARD_SERVICE);
                Intent intent = keyguardManager.createConfirmDeviceCredentialIntent("Session Unlock", "Provide PIN or Fingerprint To unlock Session");
                activity.startActivityForResult(intent, REQUEST_CODE);
            }

            @Override
            public void onSuccess(Void result) {
                updateData("Success");
//                    menuItemSelected(data.getList().get(lastSelectedGroupPosition).getSubmenu().get(lastSelectedPosition - 2).getMenu());
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }
        };
    }


    public void showMessage(String message) {
        LayoutInflater li = LayoutInflater.from(MainActivity.this);
        View promptsView = li.inflate(R.layout.message_dialogue, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        alertDialogBuilder.setView(promptsView);

        final TextView messageTextField = (TextView) promptsView.findViewById(R.id.messagetextfield);
        messageTextField.setText(message);

        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    private static List<String> parseProductListJson(JSONObject json) throws JSONException {
        try {
            List<String> objects = new ArrayList<>();
            JSONArray items = json.getJSONArray("products");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = (JSONObject) items.get(i);
                Integer id = (Integer) item.get("id");
                String name = (String) item.get("name");
                String price = (String) item.get("price");
                objects.add(id + ": " + name + ", $" + price);
            }
            return objects;
        } catch (ClassCastException e) {
            throw (JSONException) new JSONException("Response JSON was not in the expected format").initCause(e);
        }
    }

    public void login(String username, String password) {

        MASUser.login(username, password.toCharArray(), new MASCallback<MASUser>() {
            @Override
            public Handler getHandler() {
                return new Handler(Looper.getMainLooper());
            }

            @Override
            public void onSuccess(MASUser result) {
                MASUser masUser = result;
                updateData("Login success");
                MASUser user = MASUser.getCurrentUser();
            }

            @Override
            public void onError(Throwable e) {

            }
        });
    }

    private void logout() {

        MAS.start(this, true);
        MASUser current = MASUser.getCurrentUser();
        if (current != null) {

            current.logout(true, new MASCallback<Void>() {
                @Override
                public Handler getHandler() {
                    return new Handler(Looper.getMainLooper());
                }

                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(getApplicationContext(), "login out ", Toast.LENGTH_SHORT).show();
                    updateData("Logout Success");
                }

                @Override
                public void onError(Throwable e) {

                }
            });
        }
    }

    private void updateData(String message) {
        if (MASUser.getCurrentUser() != null) {
            ((TextView) findViewById(R.id.usernametextfield)).setText(MASUser.getCurrentUser().getUserName());
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int listSize = data.getList().size();
                for (int i = 0; i < listSize; i++) {
                    List menuList = (List) data.getList().get(i).getSubmenu();
                    int subMenuSize = menuList.size();
                    for (int j = 0; j < subMenuSize; j++) {
                        if (lastSelectedMenu.equals(((Submenu) menuList.get(j)).getMenu())) {
                            if ("MAS Login".equals(data.getList().get(i).getSubmenu().get(j).getMenu())) {
                                data.getList().get(i).getSubmenu().get(j).setInputTextRequired(false);
                                data.getList().get(i).getSubmenu().get(j).setInputTextRequiredTwo(false);
                            }
                            data.getList().get(i).getSubmenu().get(j).setShowOutPut(true);
                            data.getList().get(i).getSubmenu().get(j).setMessage(message);

                            break;
                        }
                    }
                }
                expandableListAdapter.setMenuData(data);
                expandableListAdapter.notifyDataSetChanged();
            }
        });

    }

    private String loadAssetTextAsString(Context context, String name) {
        BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = context.getAssets().open(name);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ((str = in.readLine()) != null) {
                if (isFirst)
                    isFirst = false;
                else
                    buf.append('\n');
                buf.append(str);
            }
            JSONObject obj = new JSONObject(buf.toString());
            return buf.toString();
        } catch (IOException e) {
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }

        return null;
    }
}

