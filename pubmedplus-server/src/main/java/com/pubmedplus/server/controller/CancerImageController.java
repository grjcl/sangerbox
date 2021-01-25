package com.pubmedplus.server.controller;

import com.pubmedplus.server.pojo.CancerImage;
import com.pubmedplus.server.service.CancerImageService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author : 小潘
 * @Description :
 * @Date : 2020/8/19
 */
@RestController
public class CancerImageController {

    @Autowired
    private CancerImageService cancerImageService;

//    获取图片,返回前端
    @GetMapping(value = "/get/img")
    public void getImageByCancerId(@RequestParam("id") String id, HttpServletResponse response) throws Exception{
        cancerImageService.getImageByCancerId(id, response);

    }

    @GetMapping("/getCancerAll")
    public String selectCancerAll(){
        var responseJson = new JSONObject();
        List<CancerImage> cancerAll = cancerImageService.selectCancerAll();
        responseJson.put("listCancer", cancerAll.toArray());
        return responseJson.toString();
    }

    @GetMapping("getCancerType")
    public String getCancerType(){
        var responseJson = new JSONObject();
        List<String> cancerAll = cancerImageService.selectCancerType();
        responseJson.put("listCancer", cancerAll.toArray());
        return responseJson.toString();
    }


}
