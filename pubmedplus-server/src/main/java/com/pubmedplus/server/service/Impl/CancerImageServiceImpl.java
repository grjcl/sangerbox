package com.pubmedplus.server.service.Impl;

import com.pubmedplus.server.dao.pubmed.ICancerImageMapper;
import com.pubmedplus.server.pojo.CancerImage;
import com.pubmedplus.server.service.CancerImageService;
import com.pubmedplus.server.utils.BASE64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author : 小潘
 * @Description :
 * @Date : 2020/8/19
 */
@Service
public class CancerImageServiceImpl implements CancerImageService {
    @Autowired
    private ICancerImageMapper cancerImageMapper;

    @Override
   public List<String> selectCancerType(){
        return cancerImageMapper.selectCancerType();
    }

/**
   根据cancerId查询出64的图片
 */
    @Override
    public void getImageByCancerId(String cancerId, HttpServletResponse response) {
        try {
            CancerImage CancerImage = cancerImageMapper.selectImageById(cancerId);
            byte[] image =CancerImage.getCancerImg().getBytes();
            String value = new String(image,"UTF-8");
//            BASE64Decoder decoder = new BASE64Decoder();
//            64解密
            byte[] bytes = BASE64.decryptBASE64(value);

            for(int i=0;i<bytes.length;i++){
                if(bytes[i]<0){
                    bytes[i]+=256;
                }
            }
//            response.setHeader("Content-Disposition", "attachment;filename="+ URLEncoder.encode(CancerImage.getCancerName().replaceAll(" ",""), "utf-8"));
            response.setContentType("image/png");
            ServletOutputStream out = response.getOutputStream();
            out.write(bytes);
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }

//        return "ok";
    }

    @Override
    public List<CancerImage> selectCancerAll() {
        List<CancerImage> cancerAllList = cancerImageMapper.selectCancerAll();
//        拼接url   http://localhost:8080/get/img?id=e39c9df51489475088c0426dfb608ee1
        for (CancerImage cancerList: cancerAllList
             ) {
           String id = cancerList.getCancerId();
           cancerList.setUrl("http://calculate.mysci.online:9000/pubmed/get/img?id="+id);

        }

        return cancerAllList;


    }
}
