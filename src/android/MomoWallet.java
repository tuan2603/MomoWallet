package cordova.plugin.momo.wallet;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

    private CallbackContext callbackContext;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.DEVELOPMENT); // AppMoMoLib.ENVIRONMENT.PRODUCTION

    }
 

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        
     
         if (action.equals("requestPayment")) {
            this.requestPayment(args);
            return true;
         }
    
         return false;
    }

   

    // Get token through MoMo app
    private void requestPayment(JSONArray args) {
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

                this.amount  = objRequestData.getString("amout").toString();
                this.merchantName  = objRequestData.getString("merchantName").toString();
                this.merchantCode  = objRequestData.getString("merchantCode").toString();
                this.orderId  = objRequestData.getString("orderId").toString();
                this.orderLabel  = objRequestData.getString("orderLabel").toString();
                this.merchantNameLabel  = objRequestData.getString("merchantNameLabel").toString();
                this.total_fee  = objRequestData.getString("total_fee").toString();
                this.description  = objRequestData.getString("description").toString();
                this.objExtraData  =  args.getJSONOject(0).getJSONObject("extraData");

            }catch( Exception ex) {
                this.callbackContext.error("loi json oblect");
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
        AppMoMoLib.getInstance().requestMoMoCallBack(cordova.getActivity(), eventValue);

    }

    // Get token callback from MoMo app an submit to server side
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                        this.callbackContext.error("not_receive_info"); 
                    }
                } else if (data.getIntExtra("status", -1) == 1) {
                    // TOKEN FAIL
                    String message = data.getStringExtra("message") != null ? data.getStringExtra("message")
                            : "Thất bại";
                    // tvMessage.setText("message: " + message);
                    this.callbackContext.error(message);
                } else if (data.getIntExtra("status", -1) == 2) {
                    // TOKEN FAIL
                    // tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                    this.callbackContext.error("not_receive_info");
                } else {
                    // TOKEN FAIL
                    // tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                    this.callbackContext.error("not_receive_info");
                }
            } else {
                // tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                this.callbackContext.error("not_receive_info");
            }
        } else {
            // tvMessage.setText("message: " + this.getString(R.string.not_receive_info_err));
            this.callbackContext.error("not_receive_info");
        }
    }

}
