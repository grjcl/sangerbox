package com.pubmedplus.server.dao.sangerbox;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.pubmedplus.server.pojo.GeoFileList;
import com.pubmedplus.server.pojo.GsmInfomation;

@Mapper
public interface IGeoMapper {

	@Select("SELECT COUNT(*) FROM `gse_infomation`")
	String countGeoAmount();


	/**
	 * 读取基因标题
	 * @param string
	 * @return
	 */
	@Select("SELECT `title` FROM `gse_infomation` WHERE `Accession`=#{accession}")
	String get_gseInfomationTitle(@Param("accession") String accession);
	
	/**
	 * 根据gpl获取表达谱数据
	 * @param trim
	 * @return
	 */
	@Select("SELECT `platform_infomation`.`DataSummaryJson` FROM `gse_gpl`,`platform_infomation` WHERE `gse_gpl`.`Gse`=#{accession}  AND `platform_infomation`.`Accession`=#{gpl} AND `gse_gpl`.`Gpl`=`platform_infomation`.`Accession` LIMIT 1")
	Object get_PlatformInfomation(@Param("accession") String accession,@Param("gpl") String gpl);
	
	/**
	 * 根据gpl获取表达谱数据
	 * @param gpl 
	 * @param trim
	 * @return
	 */
	@Select("SELECT `gsm_infomation`.`DataSummaryJson` FROM `gse_gsm`,`gsm_infomation` WHERE `gse_gsm`.`Gse`=#{accession} AND `gsm_infomation`.`PlatformRef`=#{gpl} AND `gse_gsm`.`Gsm`=`gsm_infomation`.`Accession` LIMIT 1")
	Object get_GsmInfomation(@Param("accession") String accession,@Param("gpl") String gpl);
	
	/**
	 *  4、样本信息获取
	 * @param accession
	 * @return
	 */
	@Select("SELECT `gsm_infomation`.* FROM `gsm_infomation`,`gse_gsm` WHERE `gse_gsm`.`Gse`=#{accession} AND `gsm_infomation`.`Accession`=`gse_gsm`.`Gsm`")
	List<GsmInfomation> list_GetSampleInfos(@Param("accession")String accession);

	/**
	 * 查询文件
	 * @param accession
	 * @return
	 */
	@Select("SELECT `FileName`,`FullName` FROM `geo_file_list` WHERE `Accession`=#{accession} AND `SubFolder`='suppl' AND `Location`='local'")
	List<GeoFileList> get_geoFileListIsLocal(@Param("accession")String accession);

	@Select("SELECT PlatformIDs FROM gse_infomation WHERE Accession=#{accession}")
	String findPlatformIDsByAccession(String accession);

	@Select("SELECT Title FROM platform_infomation WHERE Accession=#{accession}")
	String findTitleByAccession(String accession);

	@Select("SELECT COUNT(*) FROM gsm_infomation WHERE PlatformRef=#{platform} AND Accession in (SELECT Gsm FROM gse_gsm WHERE Gse=#{accession} )")
	String countSample(String accession,String platform);
	
	
}
