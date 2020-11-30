package com.pubmedplus.server.dao.pubmed;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.pubmedplus.server.pojo.GeneTf;
import com.pubmedplus.server.pojo.GeneUser;
import com.pubmedplus.server.pojo.MirnaTargetrna;

@Mapper
public interface IGenesMapper {

	/**http://calculate.mysci.online:9000/pubmed/getGenesInfo?genesId=10ff9b5ba9a2e5cad3c623604f9ba473
	 * 单基因的泛癌启动子甲基化与基因表达相关性
	 * @param geneId
	 * @return
	 */
	@Select("SELECT `ENSG` FROM `gid2exp` WHERE `GeneID`=#{geneId}")
	String get_Gid2ExpEnsg(@Param("geneId") String geneId);


	@Select("SELECT `GeneID` FROM `gene_info` WHERE `Name`=#{geneId}")
	String getGeneId(@Param("geneId") String geneId);
	/**
	 * 基因相关的pubmed文章列表
	 * @param geneId
	 * @return
	 */
	@Select("SELECT `pmid` FROM `gene2pubmed` WHERE `geneID`=#{geneId}")
	Set<String> list_gene2pubmed(@Param("geneId") String geneId);

	/**
	 * 查询gene_info的ExpTable
	 * @param genesId
	 * @return
	 */
	@Select("SELECT `ExpTable` FROM `gene_info` WHERE `geneID`=#{geneId}")
	String get_GseInfomationExpTable(@Param("geneId") String genesId);

	/**
	 * 21、单基因转录因子查询
	 * @return
	 * @throws Exception
	 */
	@Select("SELECT * FROM `gene_tf` WHERE `gene_id_exp`=#{geneId}")
	List<GeneTf> list_GeneTf(@Param("geneId") String genesId);

	/**
	 * 通路图可视化标识：
	 * @return
	 */
	@Select("SELECT `Link` FROM `gene2keggmap` WHERE `pathwayid`=#{pathwayid} AND (`GeneID`=#{geneId} OR `GeneID`=#{geneId2})")
	List<String> get_GenesRoadVisual(@Param("geneId") String genesId,@Param("geneId2") String genesId2, @Param("pathwayid") String pathwayid);

	/**
	 * 读取基因标题
	 * @param string
	 * @return
	 */
	@Select("SELECT `GeneID`,`Name` FROM `gene_info` WHERE `GeneID` IN (${geneIds})")
	@MapKey("geneID")
	Map<String,GeneUser> get_geneInfoTitles(@Param("geneIds") String geneIds);

	/**
	 * mRNA靶miRNA查询
	 * @param geneId
	 * @param string
	 */
	@Select("SELECT * FROM `mirna_targetrna` WHERE `geneID`=#{geneId} AND `database` IN (${databases})")
	List<MirnaTargetrna> list_GenesMiRNATargetRNAByGeneIDandDatabasesInfo(@Param("geneId")String geneId,@Param("databases") String databases);
	
}
