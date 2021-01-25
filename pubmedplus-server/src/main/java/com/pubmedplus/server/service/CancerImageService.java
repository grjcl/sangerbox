package com.pubmedplus.server.service;

import com.pubmedplus.server.pojo.CancerImage;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author : 小潘
 * @Description :
 * @Date : 2020/8/19
 */

public interface CancerImageService {

    /**
     * 根据cancerId返回图片
     * @param cancerId
     * @param response
     * @return
     */
    void getImageByCancerId(String cancerId, HttpServletResponse response);

    /**
     * 查询出所有的癌  cancerId  nane
     * @return
     */
    List<CancerImage> selectCancerAll();

    List<String> selectCancerType();
}
