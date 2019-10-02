package com.trantuan;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import vn.momo.momo_partner.AppMoMoLib;
import vn.momo.momo_partner.MoMoParameterNamePayment;

/**
 * This class echoes a string called from JavaScript.
 */
public class MomoWallet extends CordovaPlugin {

    private String amount = "10000";
    private String fee = "0";
    int environment = 0;// developer default
    private String merchantName = "Demo SDK";
    private String merchantCode = "SCB01";
    private String merchantNameLabel = "Nhà cung cấp";
    private String description = "Thanh toán dịch vụ ABC";

    private CallbackContext callbackContext;

    private interface FileOp {
        void run(JSONArray args) throws Exception;
    }

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.DEVELOPMENT); // AppMoMoLib.ENVIRONMENT.PRODUCTION

    }
 

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        if (action.equals("coolMethod")) {
            this.coolMethod();
            return true;
         }
     
         if (action.equals("requestPayment")) {
            String edAmount = args.getString(0);
            this.requestPayment(edAmount, callbackContext);
            return true;
         }
    
         return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {

        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    // Get token through MoMo app
    private void requestPayment(String edAmount, CallbackContext callbackContext) {
        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.PAYMENT);
        AppMoMoLib.getInstance().setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN);
        mount = edAmount.getText().toString().trim();

        if (mount != null && mount.length() > 0) {
            callbackContext.success(mount);
        } else {
            callbackContext.error("mount one non-empty string argument.");
        }

        Map<String, Object> eventValue = new HashMap<>();
        // client Required
        eventValue.put("merchantname", merchantName); // Tên đối tác. được đăng ký tại https://business.momo.vn. VD:
                                                      // Google, Apple, Tiki , CGV Cinemas
        eventValue.put("merchantcode", merchantCode); // Mã đối tác, được cung cấp bởi MoMo tại https://business.momo.vn
        eventValue.put("amount", total_amount); // Kiểu integer
        eventValue.put("orderId", "orderId123456789"); // uniqueue id cho Bill order, giá trị duy nhất cho mỗi đơn hàng
        eventValue.put("orderLabel", "Mã đơn hàng"); // gán nhãn

        // client Optional - bill info
        eventValue.put("merchantnamelabel", "Dịch vụ");// gán nhãn
        eventValue.put("fee", total_fee); // Kiểu integer
        eventValue.put("description", description); // mô tả đơn hàng - short description

        // client extra data
        eventValue.put("requestId", merchantCode + "merchant_billId_" + System.currentTimeMillis());
        eventValue.put("partnerCode", merchantCode);
        // Example extra data
        JSONObject objExtraData = new JSONObject();
        try {
            objExtraData.put("site_code", "008");
            objExtraData.put("site_name", "CGV Cresent Mall");
            objExtraData.put("screen_code", 0);
            objExtraData.put("screen_name", "Special");
            objExtraData.put("movie_name", "Kẻ Trộm Mặt Trăng 3");
            objExtraData.put("movie_format", "2D");
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.error( e.printStackTrace());
        }
        eventValue.put("extraData", objExtraData.toString());

        eventValue.put("extra", "");
        AppMoMoLib.getInstance().requestMoMoCallBack(this, eventValue);

    }

    // Get token callback from MoMo app an submit to server side
    private void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppMoMoLib.getInstance().REQUEST_CODE_MOMO && resultCode == -1) {
            if (data != null) {
                if (data.getIntExtra("status", -1) == 0) {
                    // TOKEN IS AVAILABLE
                    // tvMessage.setText("message: " + "Get token " + data.getStringExtra("message"));
                    Toast.makeText(cordova.getActivity(), "message: " + "Get token " + data.getStringExtra("message") , Toast.LENGTH_LONG).show();
                    String token = data.getStringExtra("data"); // Token response
                    String phoneNumber = data.getStringExtra("phonenumber");
                    String env = data.getStringExtra("env");
                    if (env == null) {
                        env = "app";
                    }

                    if (token != null && !token.equals("")) {
                        // TODO: send phoneNumber & token to your server side to process payment with
                        // MoMo server
                        // IF Momo topup success, continue to process your order
                        this.callbackContext.success(token);
                    } else {
                        // tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                        Toast.makeText(cordova.getActivity(), "message: " + this.getString(R.string.not_receive_info) , Toast.LENGTH_LONG).show();
                    }
                } else if (data.getIntExtra("status", -1) == 1) {
                    // TOKEN FAIL
                    String message = data.getStringExtra("message") != null ? data.getStringExtra("message")
                            : "Thất bại";
                    // tvMessage.setText("message: " + message);
                    Toast.makeText(cordova.getActivity(), "message: " + message, Toast.LENGTH_LONG).show();
                } else if (data.getIntExtra("status", -1) == 2) {
                    // TOKEN FAIL
                    // tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                    Toast.makeText(cordova.getActivity(), "message: " + this.getString(R.string.not_receive_info), Toast.LENGTH_LONG).show();
                } else {
                    // TOKEN FAIL
                    // tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                    Toast.makeText(cordova.getActivity(), "message: " +  this.getString(R.string.not_receive_info), Toast.LENGTH_LONG).show();
                }
            } else {
                // tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                Toast.makeText(cordova.getActivity(), "message: " + this.getString(R.string.not_receive_info), Toast.LENGTH_LONG).show();
            }
        } else {
            // tvMessage.setText("message: " + this.getString(R.string.not_receive_info_err));
            Toast.makeText(cordova.getActivity(), "message: " + this.getString(R.string.not_receive_info_err), Toast.LENGTH_LONG).show();
        }
    }

}
