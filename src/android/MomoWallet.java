package cordova.plugin.momo.wallet;

import org.apache.cordova.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    private String total_fee = "0";
    private int environment = 0;// developer default
    private String merchantName = "MoMo";
    private String merchantCode = "SCB01";
    private String description = "Thanh toán dịch vụ ABC";
    private String merchantNameLabel = "Dịch vụ";
    private String orderId  = "000003232";
    private String orderLabel = "Mã đơn hàng";

    private CallbackContext callbackContext = null;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.DEVELOPMENT); // AppMoMoLib.ENVIRONMENT.PRODUCTION

    }
 

    @Override
    public boolean execute(String action,final JSONArray args,final CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        
     
         if (action.equals("requestPayment")) {

             cordova.getThreadPool().execute(new Runnable() {
                 @Override
                 public void run() {
                     requestPayment(args);
                 }
             });
            
         }
         
         // Send no result, to execute the callbacks later
        PluginResult pluginResult = new  PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true); // Keep callback

        return true;
    }

   

    // Get token through MoMo app
    public void requestPayment(JSONArray args) {
        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.PAYMENT);
        AppMoMoLib.getInstance().setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN);
        

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
        }
        
         

        if ( args != null ) {
            // Example request data
            JSONObject objRequestData = new JSONObject();
            try {
                objRequestData = args.getJSONObject(0);

                Log.d("aaa", objRequestData.toString());

                this.amount  = objRequestData.getString("amount").toString();
                this.merchantName  = objRequestData.getString("merchantName").toString();
                this.merchantCode  = objRequestData.getString("merchantCode").toString();
                this.orderId  = objRequestData.getString("orderId").toString();
                this.orderLabel  = objRequestData.getString("orderLabel").toString();
                this.merchantNameLabel  = objRequestData.getString("merchantNameLabel").toString();
                this.total_fee  = objRequestData.getString("total_fee").toString();
                this.description  = objRequestData.getString("description").toString();
                objExtraData  =  new JSONObject(args.getJSONObject(0).getString("objExtraData"));

            }catch( Exception ex) {
                PluginResult resultado = new PluginResult(PluginResult.Status.OK, "canceled convert data ");
                resultado.setKeepCallback(true);
                this.callbackContext.sendPluginResult(resultado);
            }
        }

        Map<String, Object> eventValue = new HashMap<>();
        // client Required
        eventValue.put("merchantname", this.merchantName); // Tên đối tác. được đăng ký tại https://business.momo.vn. VD:
                                                      // Google, Apple, Tiki , CGV Cinemas
        eventValue.put("merchantcode", this.merchantCode); // Mã đối tác, được cung cấp bởi MoMo tại https://business.momo.vn
        eventValue.put("amount", this.amount); // Kiểu integer
        eventValue.put("orderId", this.orderId); // uniqueue id cho Bill order, giá trị duy nhất cho mỗi đơn hàng
        eventValue.put("orderLabel", this.orderLabel); // gán nhãn

        // client Optional - bill info
        eventValue.put("merchantnamelabel", this.merchantNameLabel);// gán nhãn
        eventValue.put("fee", this.total_fee); // Kiểu integer
        eventValue.put("description", this.description); // mô tả đơn hàng - short description

        // client extra data
        eventValue.put("requestId", this.merchantCode + "merchant_billId_" + System.currentTimeMillis());
        eventValue.put("partnerCode", this.merchantCode );

        eventValue.put("extraData", objExtraData.toString());
        eventValue.put("requestType", "payment");
        eventValue.put("language", "vi");
        eventValue.put("extra", "");

        AppMoMoLib.getInstance().requestMoMoCallBack( this.cordova.getActivity(), eventValue);



    }

    // Get token callback from MoMo app an submit to server side
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
       
        if (requestCode == AppMoMoLib.getInstance().REQUEST_CODE_MOMO && resultCode == -1) {
            Log.d("requestpayment", String.valueOf(data));
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
                        PluginResult resultado = new PluginResult(PluginResult.Status.OK, "this value will be sent to cordova");
                        resultado.setKeepCallback(true);
                        this.callbackContext.sendPluginResult(resultado);
                        return;
                    } else {
                        // tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                        Log.d("requestpayment", "1");
                        PluginResult resultado = new PluginResult(PluginResult.Status.OK, "canceled action, process this in javascript");
                        resultado.setKeepCallback(true);
                        this.callbackContext.sendPluginResult(resultado);
                        return;
                    }
                } else if (data.getIntExtra("status", -1) == 1) {
                    // TOKEN FAIL
                    String message = data.getStringExtra("message") != null ? data.getStringExtra("message")
                            : "Thất bại";
                    // tvMessage.setText("message: " + message);
                    Log.d("requestpayment", "2" + message);
                    PluginResult resultado = new PluginResult(PluginResult.Status.OK, message);
                    resultado.setKeepCallback(true);
                    this.callbackContext.sendPluginResult(resultado);
                    return;
                } else if (data.getIntExtra("status", -1) == 2) {
                    // TOKEN FAIL
                    // tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                    Log.d("requestpayment", "3");
                    PluginResult resultado = new PluginResult(PluginResult.Status.OK, "TOKEN FAIL");
                    resultado.setKeepCallback(true);
                    this.callbackContext.sendPluginResult(resultado);
                    return;
                } else {
                    // TOKEN FAIL
                    // tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                    Log.d("requestpayment", "4");
                    PluginResult resultado = new PluginResult(PluginResult.Status.OK, "TOKEN FAIL");
                        resultado.setKeepCallback(true);
                        this.callbackContext.sendPluginResult(resultado);
                        return;

                }
            } else {
                // tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                Log.d("requestpayment", "5");
                PluginResult resultado = new PluginResult(PluginResult.Status.OK, "TOKEN FAIL");
                resultado.setKeepCallback(true);
                this.callbackContext.sendPluginResult(resultado);
                return;
            }
        } else {
            // tvMessage.setText("message: " + this.getString(R.string.not_receive_info_err));
            Log.d("requestpayment", "6");
            PluginResult resultado = new PluginResult(PluginResult.Status.OK, "canceled action, process this in javascript");
            resultado.setKeepCallback(true);
            this.callbackContext.sendPluginResult(resultado);
            return;
        }

        // Handle other results if exists.
        // super.onActivityResult(requestCode, resultCode, data);
    }

}
