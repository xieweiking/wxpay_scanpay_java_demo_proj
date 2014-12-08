package com.tencent;

/**
 * User: rizenguo
 * Date: 2014/12/6
 * Time: 14:39
 */
public class BeforeTest {

    public static void initSDK(){
        WXPay.initSDKConfiguration(
                "40a8f8aa8ebe45a40bdc4e0f7307bc66",
                "wxf5b5e87a6a0fde94",
                "10000097",
                "",
                "C:/wxpay_scanpay_java_cert/10000097.p12",
                "10000097"
        );
    }

}
