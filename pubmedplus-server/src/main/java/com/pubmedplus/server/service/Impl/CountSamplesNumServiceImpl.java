package com.pubmedplus.server.service.Impl;

import com.pubmedplus.server.dao.sangerbox.ICancerImageMapper;
import com.pubmedplus.server.dao.sangerbox.ICountSamplesNumMapper;
import com.pubmedplus.server.pojo.CountSamplesNumModel;
import com.pubmedplus.server.service.CountSamplesNumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author : 小潘
 * @Description : 获取基因柱状图
 * @Date : 2020/8/19
 */
@Service
public class CountSamplesNumServiceImpl implements CountSamplesNumService {
    @Autowired
    private ICountSamplesNumMapper countSamplesNumMapper;

    @Override
    public List<CountSamplesNumModel> countSamplesNum() {
        List<CountSamplesNumModel> listSample = countSamplesNumMapper.countSamplesNum();
//        排除空基因样本
        for (int i=0;i<listSample.size();i++){
            if(listSample.get(i).getTCGACode()==null){
                listSample.remove(i);
            }
//            设置url

        }

        return listSample;
    }
}
