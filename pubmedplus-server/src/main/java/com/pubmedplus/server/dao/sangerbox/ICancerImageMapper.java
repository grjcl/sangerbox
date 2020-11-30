package com.pubmedplus.server.dao.sangerbox;

import com.pubmedplus.server.pojo.CancerImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author : 小潘
 * @Description :
 * @Date : 2020/8/19
 */
@Mapper
@Component
public interface ICancerImageMapper {


    /**
     * 根据cancerId查询出64的图片
     * @param cancerId
     * @return
     */
    @Select("select cancer_img as cancerImg from cancer_image where cancer_id=#{cancerId}")
    CancerImage selectImageById(String cancerId);

    /**
     * 查询出所有的癌  cancerId  nane
     * @return
     */
    @Select("select cancer_id as cancerId,cancer_name_CN as cancerNameCn,cancer_name_EN as cancerNameEn,cancer_name_EN_quan as cancerNameEnQuan from cancer_image ORDER BY id")
    List<CancerImage> selectCancerAll();

}
