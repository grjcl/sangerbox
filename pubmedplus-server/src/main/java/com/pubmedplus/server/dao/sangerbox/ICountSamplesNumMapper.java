package com.pubmedplus.server.dao.sangerbox;



import com.pubmedplus.server.pojo.CountSamplesNumModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @Author : 小潘
 * @Description :
 * @Date : 2020/8/16
 */
@Mapper
@Component
public interface ICountSamplesNumMapper {

    /**
     * 统计每个基因样本的数量(展示柱状图)
     */
    @Select("select TCGA_code AS TCGACode,count(TCGA_code) as count from exp_sample_info group by TCGA_code")
    List<CountSamplesNumModel> countSamplesNum();



}