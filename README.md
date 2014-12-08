# 微信支付之“被扫支付”JavaDemo

欢迎大家使用这个Demo，希望这个Demo可以大大提升大家的效率，大家遇到问题可以第一时间发邮件给到以下地址（grz@grzcn.com）或是提交你宝贵的Pull Request。  
该Demo用到了“被扫支付SDK”，SDK的详细说明请看<a href="https://github.com/grz/wxpay_scanpay_java_sdk" title="被扫支付SDK" target="_blank">这里</a>

## Demo快速上手指引
1. [Demo中包含的内容](#user-content-demo包含的内容)
2. [Demo依赖的配置项](#user-content-demo依赖的配置项)
3. [Demo需要商户自己实现的IBridge](#user-content-demo需要商户自己实现的ibridge)

## 商户系统接入最佳实践
1. [被扫支付业务流程最佳实践](#user-content-被扫支付业务流程最佳实践)  
2. [商户系统接入SDK最佳实践](#user-content-商户系统接入sdk最佳实践)
3. [商户系统部署最佳实践](#user-content-商户系统部署最佳实践)

## “被扫支付”高级知识  
1. [调用被扫支付API的协议规则](#user-content-调用被扫支付api的协议规则)
2. [支付验证密码规则](#user-content-支付验证密码规则)


## Demo包含的内容

![img](https://raw.githubusercontent.com/grz/wxpay_scanpay_java_demo_proj/master/docs/asset/scanpay_demo_structure.png "Demo整体结构")  

Demo里面需要大家关注的主要有两个地方：  

1. 四个业务Demo，里面把代码流程已经帮大家写好，只需要将里面代码会走到的所有Case加上对应逻辑，例如（客服端弹出提示）

2. 桥接器IBridge，这个东西是用来对接商户系统逻辑，产生SDK请求所需要的特定参数用的，请大家按照API文档的说明实现这些参数的产生逻辑。

## Demo依赖的配置项  
打开demo工程里的wxpay.properties文件可以看到里面有5个配置项（该demo里面用的是一个mchid为10000097的测试号）  
这5项关键配置项的作用分别为：

<table>
    <tbody>
        <tr>
            <td>名称</td>
            <td>用途</td>
            <td>来源</td>
        </tr>
        <tr>
            <td>KEY</td>
            <td>签名算法需要用到的秘钥</td><td>成功申请微信支付功能之后通过官方发出的邮件获得</td>
        </tr>
        <tr>
            <td>APPID</td>
            <td>公众账号ID</td>
            <td>成功申请公众账号后获得</td>
        </tr>
        <tr>
            <td>MCHID</td>
            <td>商户ID</td>
            <td>成功申请微信支付功能之后通过官方发出的邮件获得</td>
        </tr>
        <tr>
            <td>SUBMCHID</td>
            <td>子商户ID</td>
            <td>受理模式下必须要有的一个子商户ID</td>
        </tr>
        <tr>
            <td>CERT_LOCAL_PATH</td>
            <td>HTTP证书在服务器中的路径，用来加载证书用</td>
            <td>成功申请微信支付功能之后通过官方发出的邮件获得“HTTPS证书”，这个配置项就是“HTTP证书”在服务器上所部署的路径（demo中需要的证书文件就是asset文件夹中的10000097.cert）</td>
        </tr>
        <tr>
            <td>CERT_PASSWORD</td>
            <td>HTTP证书的密码，默认等于MCHID</td>
            <td>成功申请微信支付功能之后通过官方发出的邮件获得</td>
        </tr>
    </tbody>
</table>


## Demo需要商户自己实现的IBridge
![img](https://raw.githubusercontent.com/grz/wxpay_scanpay_java_demo_proj/master/docs/asset/ibridge.jpg "ibridge桥接器")  
从上图可见IBridge桥接器其实就是定义了请求API时需要提交的各种参数的产生接口，这些接口跟商户自己的系统是紧密结合的，商户自己需要根据具体业务系统的实际情况，按照API文档定义的格式来产生相应的参数给到调用API时使用。  
举个例子，IBridge里面定义了一个非常关键的接口，叫getAuthCode()，这个接口的作用就是用来返回一个合法的“授权码”供调用API时用。  
```java
/**
 * 获取auth_code，这个是扫码终端设备从用户手机上扫取到的支付授权号，这个号是跟用户用来支付的银行卡绑定的，有效期是1分钟
 * @return 授权码
 */
public String getAuthCode(){
    //由于这个authCode有效期只有1分钟，所以实际测试SDK的时候也可以手动将微信刷卡界面一维码下的那串数字输入进来
    return "120242957324236112";
}
```
以上只是简单的hardcode（用来先简单手动输入“授权码”调试API是否能正常返回数据时用），实际上商户自己在实现这个接口的时候就需要根据自己实际系统来进行设计了，例如需要去监听“扫码枪”等具备一维码/二维码扫描功能的外设，当成功扫描到这串“授权码”的时候，将其保存下来，然后触发提交支付的API调用，调用时让IBridge桥接器中的getAuthCode()接口取得刚刚扫描到的授权码，作为参数传给支付API。  

## 被扫支付业务流程最佳实践  
被扫支付整个完成流程将会涉及到“查询”和“撤销”等请求，这里给出建议实现的流程供大家参考，本Demo里面就是按照这个流程来设计的：  
![img](https://raw.githubusercontent.com/grz/wxpay_scanpay_java_demo_proj/master/docs/asset/scanpay_flow.png "被扫支付流程")  
当用户遇到支付异常，请按如下说明处理：  
1. 用户微信端弹出系统错误提示框，用户可在交易列表查看交易情况，如果未找到订单，需要商户重新发起支付交易；如果订单显示成功支付，商户收银系统再次调用【查询订单API】查询实际支付结果；
2. 用户微信端弹出支付失败提示，例如：余额不足，信用卡失效。需要重新发起支付；
3. 当交易超时或支付交易失败，商户收银系统必须调用【撤销支付API】（详见公共API），撤销此交易；
4. 由于银行系统异常、用户余额不足、不支持用户卡种等原因使当前支付交易失败，商户收银系统应该把错误提示明确展示给收银员；
5. 跟据返回的错误码，判断是否需要撤销交易，具体详见API返回错误码列表；


## 商户系统接入SDK最佳实践
1. 生成一个新的订单out_trade_no
2. 输入订单金额total_fee
3. 启动扫码枪功能供用户进行扫码
4. 扫码器获取授权码auth_code，并回传给SDK
5. SDK提交支付请求
6. SDK处理API返回的数据
![img](https://raw.githubusercontent.com/grz/wxpay_scanpay_java_demo_proj/master/docs/asset/best_cdraw.png "商户系统接入最佳实践")


## 商户系统部署最佳实践  
1. 由于整套系统必须采用HTTPS来保证安全性，所以这里的支付请求必须由商户的后台系统来发起；
2. 商户系统跟SDK的对接主要就是实现IBridge里面的接口；
3. 从本demo里面有JUnit单元测试用例，商户开发者可以参考下这个示例；
![img](https://raw.githubusercontent.com/grz/wxpay_scanpay_java_demo_proj/master/docs/asset/system_structure.png "商户系统部署最佳实践")


## 调用被扫支付API的协议规则  

<table>
    <tbody>
        <tr>
            <td>传输方式</td>
            <td>为保证交易安全性，采用HTTPS传输</td>
        </tr>
        <tr>
            <td>提交方式</td>
            <td>采用POST方法提交</td>
        </tr>
        <tr>
            <td>数据格式</td>
            <td>提交和返回数据都为XML格式，根节点名为xml</td>
        </tr>
        <tr>
            <td>字符编码</td>
            <td>统一采用UTF-8字符编码</td>
        </tr>
        <tr>
            <td>签名算法</td>
            <td>MD5</td>
        </tr>
        <tr>
            <td>签名要求</td>
            <td>请求和接收数据均需要校验签名，签名的方法在SDK里面已经封装好了</td>
        </tr>
        <tr>
            <td>证书要求/td>
            <td>调用申请退款、撤销订单接口需要商户证书</td>
        </tr>
        <tr>
            <td>判断逻辑</td>
            <td>先判断协议字段返回，再判断业务返回，最后判断交易状态</td>
        </tr>
    </tbody>
</table>


## 支付验证密码规则  
1. 支付金额>300元的交易需要验证用户支付密码；
2. 用户账号每天最多有10笔交易可以免密，超过后需要验证密码；
3. 微信支付后台判断用户支付行为有异常情况，符合免密规则的交易也会要求验证密码；


###### ([返回目录](#user-content-demo快速上手指引))  