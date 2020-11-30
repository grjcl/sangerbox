package com.pubmedplus.server.service;

import com.pubmedplus.server.pojo.CountSamplesNumModel;

import java.util.List;

/**
 * @Author : 小潘
 * @Description :
 * @Date : 2020/8/19
 */
public interface CountSamplesNumService {

    /**
     * 统计每个基因样本的数量(展示柱状图)
     */
    List<CountSamplesNumModel> countSamplesNum();
}
