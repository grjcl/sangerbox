package com.pubmedplus.server.dao.sangerbox;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.pubmedplus.server.pojo.ExpByGenes;
import com.pubmedplus.server.pojo.ExpSampleInfo;
import com.pubmedplus.server.pojo.Gene2GTEx;
import com.pubmedplus.server.pojo.Gene2Pancancer;
import com.pubmedplus.server.pojo.Gene2PancancerMethy;
import com.pubmedplus.server.pojo.GeoUser;

@Mapper
public interface ISangerBoxMapper {
	
	/**
	 * 单基因的泛癌差异表达
	 * @param expTable
	 * @return
	 */
	@Select("SELECT `exp`.`SampleCode`,`exp`.`Val`,`exp_sample_info`.`TCGA_code` AS `TCGACode`,`exp_sample_info`.`Primary_site` AS `primarySite`,`exp_sample_info`.`Db_type` AS `dbType`,`exp_sample_info`.`Site_type` AS `siteType` FROM ${expTable} AS `exp`,`exp_sample_info` WHERE `exp`.`CalBatch`='PanCancerTPM' AND `exp`.`SampleCode`=`exp_sample_info`.`Sample`")
	List<Gene2Pancancer> list_SetGene2Pancancer(@Param("expTable") String expTable);

	/**
	 * 单基因在33个组织中的表达水平
	 * @param expTable
	 * @return
	 */
	@Select("SELECT `exp`.`SampleCode`,`exp`.`Val`,`exp_sample_info`.`Primary_site` AS `primarySite` FROM ${expTable} AS `exp`,`exp_sample_info` WHERE `exp`.`CalBatch`='PanCancerTPM' AND `exp`.`SampleCode`=`exp_sample_info`.`Sample` AND `exp_sample_info`.`Db_type`='GTEx'")
	List<Gene2GTEx> list_SetGene2GTEx(@Param("expTable") String expTable);

	@Select("SELECT `exp`.`SampleCode`,`exp`.`Val`,`exp_sample_info`.`Primary_site` AS `primarySite` FROM ${expTable} AS `exp`,`exp_sample_info` WHERE `exp`.`CalBatch`=#{weType} AND `exp`.`SampleCode`=`exp_sample_info`.`Sample` AND `exp_sample_info`.`Db_type`=#{dbType}")
	List<Gene2GTEx> list_downData(@Param("expTable") String expTable,@Param("dbType") String dbType,@Param("weType")String weType);
	/**
	 * 、单基因的泛癌启动子差异甲基化
	 * @param expTable
	 * @return
	 */
	@Select("SELECT `exp`.`SampleCode`,`exp`.`Val`,`exp_sample_info`.`TCGA_code` AS `TCGACode`,`exp_sample_info`.`Primary_site` AS `primarySite`,`exp_sample_info`.`Db_type` AS `dbType`,`exp_sample_info`.`Site_type` AS `siteType` FROM ${expTable} AS `exp`,`exp_sample_info` WHERE `exp`.`CalBatch`='PanCancerMethy' AND `exp`.`SampleCode`=`exp_sample_info`.`Sample`")
	List<Gene2PancancerMethy> list_SetPancancerMethy(@Param("expTable") String expTable);

	/**
	 * 单基因的泛癌启动子甲基化与基因表达相关性
	 * @param expTable
	 * @return
	 */
	@Select("SELECT `exp`.`SampleCode`,`exp`.`Val`,`exp`.CalBatch,`exp_sample_info`.`TCGA_code` AS `TCGACode`,`exp_sample_info`.`Primary_site` AS `primarySite`,`exp_sample_info`.`Db_type` AS `dbType`,`exp_sample_info`.`Site_type` AS `siteType` FROM ${expTable} AS `exp`,`exp_sample_info` WHERE (`exp`.`CalBatch`='PanCancerMethy' OR  `exp`.`CalBatch`='PanCancerTPM') AND `exp`.`SampleCode`=`exp_sample_info`.`Sample`")
	List<Gene2PancancerMethy> list_SetPancancerMethyExpCor(@Param("expTable") String expTable);

	/**
	 * 17、指定肿瘤下基因之间的表达
	 * @param expTable
	 * @param tcgaCode 
	 * @param siteType 
	 * @param dbType 
	 * @param calBatch 
	 * @param expTable2
	 */
	@Select("<script>SELECT `exp`.`SampleCode`,`exp`.`Val` FROM ${expTable} AS `exp`,`exp_sample_info` WHERE `exp`.`SampleCode`=`exp_sample_info`.`Sample`"
			+ "<if test='calBatch!=null'> AND `calBatch`=#{calBatch}</if>"
			+ "<if test='dbType!=null'> AND `exp_sample_info`.`Db_type`=#{dbType}</if>"
			+ "<if test='siteType!=null'> AND `exp_sample_info`.`Site_type`=#{siteType}</if>"
			+ "<if test='tcgaCode!=null'> AND `exp_sample_info`.`TCGA_cdoe`=#{tcgaCode}</if>"
			+ " ORDER BY `exp`.`SampleCode`</script>")
	List<ExpByGenes> list_getExpByGenes(@Param("expTable") String expTable,@Param("calBatch") String calBatch,@Param("dbType") String dbType,@Param("siteType") String siteType,@Param("tcgaCode") String tcgaCode);

	/**
	 * 20、单个基因的定制查询API
	 * @param expTable
	 * @param calBatch
	 * @return
	 */
	@Select("SELECT `exp`.`Val`,`exp_sample_info`.* FROM ${expTable} AS `exp`,`exp_sample_info` WHERE `exp`.`CalBatch`=#{calBatch} AND `exp`.`SampleCode`=`exp_sample_info`.`Sample`")
	List<ExpSampleInfo> get_GenesSingleInfo(@Param("expTable") String expTable,@Param("calBatch") String calBatch);

	/**
	 * 批量数据集名称
	 * @param accession
	 * @return
	 */
	@Select("SELECT `accession`,`title` FROM `gse_infomation` WHERE `Accession` IN (${accession})")
	@MapKey("accession")
	Map<String,GeoUser> get_gseInfomationTitles(@Param("accession") String accession);

	
	
}