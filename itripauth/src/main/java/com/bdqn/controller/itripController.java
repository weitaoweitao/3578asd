package com.bdqn.controller;

import cn.itrip.common.*;
import cn.itrip.dao.itripHotel.ItripHotelMapper;
import cn.itrip.dao.itripUser.ItripUserMapper;
import cn.itrip.pojo.ItripHotel;
import cn.itrip.pojo.ItripUser;
import cn.itrip.pojo.ItripUserVO;
import com.alibaba.fastjson.JSONArray;

import com.cloopen.rest.sdk.BodyType;
import com.cloopen.rest.sdk.CCPRestSmsSDK;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
public class itripController {

   @Resource
    ItripHotelMapper dao;
   @Resource
    ItripUserMapper dao1;
   @Resource
    TokenBiz biz;
   @Resource
    RedisUtli redisUtli;
    @Resource
    ItripUserMapper dao4;
    @RequestMapping(value="/api/registerbyphone",produces="application/json;charset=utf-8")
    @ResponseBody
    public Dto Register(@RequestBody ItripUserVO vo) throws Exception {
        System.out.println(vo.getUserCode());
        ItripUser user=new ItripUser();
        user.setUserCode(vo.getUserCode());
        user.setUserPassword(vo.getUserPassword());
        user.setUserName(vo.getUserName());
        dao4.insertItripUser(user);

        Random random=new Random(4);
        int sj=random.nextInt(9999);
        sentSms(vo.getUserCode(),sj+"");

        redisUtli.setRedis(vo.getUserCode(),""+sj);
        return DtoUtil.returnSuccess();
    }


    @RequestMapping(value="/api/dologin",produces="application/json;charset=utf-8")
    @ResponseBody
    public Object login(String name, String password, HttpServletRequest request) throws Exception {
        Map map = new HashMap();
        map.put("a",name);
        map.put("b",password);
        ItripUser user = dao1.getItripUserListByMap(map);
        if (user!=null) {
            //模拟session 的票据---------------
            String token = biz.generateToken(request.getHeader("User-Agent"), user);
            //把这个token存储到redis中
            //fastjson把当前用户转成字符串
            redisUtli.setRedis(token,JSONArray.toJSONString(user));

            ItripTokenVO obj = new ItripTokenVO(token, Calendar.getInstance().getTimeInMillis()*3600*2,Calendar.getInstance().getTimeInMillis());

            return DtoUtil.returnDataSuccess(obj);
        }
        return DtoUtil.returnFail("登陆失败","1000");
        /*return JSONArray.toJSONString(user);*/

    }
    public static void sentSms(String Phone,String message){
        //生产环境请求地址：app.cloopen.com
        String serverIp = "app.cloopen.com";
        //请求端口
        String serverPort = "8883";
        //主账号,登陆云通讯网站后,可在控制台首页看到开发者主账号ACCOUNT SID和主账号令牌AUTH TOKEN
        String accountSId = "8a216da87f63aaf1017f6c35de2f01c7";
        String accountToken = "b2d7d973e17c42b6852f78c6404c5141";
        //请使用管理控制台中已创建应用的APPID
        String appId = "8a216da87f63aaf1017f6c35dfc701cd";
        CCPRestSmsSDK sdk = new CCPRestSmsSDK();
        sdk.init(serverIp, serverPort);
        sdk.setAccount(accountSId, accountToken);
        sdk.setAppId(appId);
        sdk.setBodyType(BodyType.Type_XML);
        String to = "18637283931";
        String templateId= "1";
        String[] datas = {message};
        //  String subAppend="1234";  //可选	扩展码，四位数字 0~9999
        //  String reqId="***";  //可选 第三方自定义消息id，最大支持32位英文数字，同账号下同一自然天内不允许重复
        HashMap<String, Object> result = sdk.sendTemplateSMS(to,templateId,datas);
        //  HashMap<String, Object> result = sdk.sendTemplateSMS(to,templateId,datas,subAppend,reqId);
        if("000000".equals(result.get("statusCode"))){
            //正常返回输出data包体信息（map）
            HashMap<String,Object> data = (HashMap<String, Object>) result.get("data");
            Set<String> keySet = data.keySet();
            for(String key:keySet){
                Object object = data.get(key);
                System.out.println(key +" = "+object);
            }
        }else{
            //异常返回输出错误码和错误信息
            System.out.println("错误码=" + result.get("statusCode") +" 错误信息= "+result.get("statusMsg"));
        }
    }

    @RequestMapping(value="clist",produces="application/json;charset=utf-8")

    @ResponseBody
    public String glist(String pid) throws Exception {
        ItripHotel list=dao.getItripHotelById(new Long(56));
        return JSONArray.toJSONString(list);
    }

    @RequestMapping("/clist1")
    public String clist(){
        return "clist1";
    }

}
