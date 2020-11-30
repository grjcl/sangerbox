package com.pubmedplus.server.dao.pubmed;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.pubmedplus.server.pojo.TcgaCancer;
import com.pubmedplus.server.pojo.TcgaFile;

@Mapper
public interface ITcgaMapper {




	/**
	 * 获取查询下拉
	 * @return
	 */
	@Select("SELECT `id`,`name` FROM `tcga_cancer_list`")
	List<TcgaCancer> getTcgaCancerSelectList();

	@Select("SELECT DISTINCT `tcga_file_list`.`data_category`,COUNT(tcga_file_list.file_id) AS `count` FROM `tcga_file_list` WHERE `tcga_file_list`.`project_id` = #{id} GROUP BY `tcga_file_list`.`data_category`")
	List<TcgaFile> getTcgaCancerCategoryFilesCount(@Param("id") String id);

	@Select("SELECT `n1` AS `data_category`,COUNT(n2) AS `count` FROM (SELECT DISTINCT `tcga_file_list`.`data_category` AS `n1`,`tcga_file_case`.`case_id` AS `n2` FROM `tcga_file_list` JOIN `tcga_file_case` ON `tcga_file_list`.`file_id`=`tcga_file_case`.`file_id` WHERE `tcga_file_list`.`project_id` = #{id} ) TEMP GROUP BY `n1`")
	List<TcgaFile> getTcgaCancerCategoryCaseCount(@Param("id") String id);

	@Select("SELECT DISTINCT tcga_file_list.experimental_strategy,Count(tcga_file_list.file_id) AS `count` FROM tcga_file_list WHERE tcga_file_list.project_id = #{id} GROUP BY tcga_file_list.experimental_strategy")
	List<TcgaFile> getTcgaCancerStrategyFilesCount(@Param("id") String id);

	@Select("SELECT n1 AS `experimental_strategy`,count(n2) AS `count` FROM (SELECT DISTINCT tcga_file_list.experimental_strategy AS n1,tcga_file_case.case_id AS n2 FROM tcga_file_list JOIN tcga_file_case ON tcga_file_list.file_id=tcga_file_case.file_id WHERE tcga_file_list.project_id = #{id} ) temp GROUP BY n1")
	List<TcgaFile> getTcgaCancerStrategyCaseCount(@Param("id") String id);

	@Select("<script>SELECT DISTINCT tcga_file_list.file_id,tcga_file_list.file_name,tcga_file_list.experimental_strategy,tcga_file_list.file_size,\r\n"
			+ "	tcga_file_list.data_category,tcga_file_list.platform,tcga_file_list.data_format,tcga_file_list.workflow_type,\r\n"
			+ "	tcga_file_list.location,tcga_file_list.data_type,tcga_file_case_type.sample_type,tcga_file_case.submitter_id\r\n"
			+ "FROM\r\n"
			+ "	tcga_file_list\r\n"
			+ "LEFT JOIN tcga_file_case ON tcga_file_case.file_id = tcga_file_list.file_id\r\n"
			+ "LEFT JOIN tcga_file_case_type ON tcga_file_case_type.file_id = tcga_file_list.file_id\r\n"
			+ "WHERE tcga_file_list.project_id = #{id}"
			+ "<if test='workflowType!=null'> AND tcga_file_list.workflow_type = #{workflowType}</if>" 
			+ "<if test='dataType!=null'> AND tcga_file_list.data_type = #{dataType}</if>" 
			+ "<if test='platform!=null'> AND tcga_file_list.platform = #{platform}</if>" 
			+ "<if test='dataCategory!=null'> AND tcga_file_list.data_category = #{dataCategory}</if>"
			+ "<if test='transferNoT!=null'> AND tcga_file_list.workflow_type NOT IN ('HTSeq - FPKM','HTSeq - FPKM-UQ','HTSeq - Counts') AND (tcga_file_list.workflow_type !='BCGSC miRNA Profiling' AND tcga_file_list.data_type NOT IN ('miRNA Expression Quantification','Isoform Expression Quantification'))</if>"
			+ "<if test='outsideNoT!=null'> AND tcga_file_list.workflow_type NOT IN ('VarScan2 Variant Aggregation and Masking','SomaticSniper Variant Aggregation and Masking','MuTect2 Variant Aggregation and Masking','MuSE Variant Aggregation and Masking') AND tcga_file_list.data_type='Masked Somatic Mutation'</if>"
			+ "<if test='platformNoT!=null'> AND tcga_file_list.platform NOT IN ('Illumina Human Methylation 450','Illumina Human Methylation 27') AND tcga_file_list.data_type='Methylation Beta Value' AND tcga_file_list.workflow_type='Liftover'</if>"
			+ "<if test='experimentalNoT!=null'> AND tcga_file_list.experimental_strategy LIKE '%Slide%' AND  tcga_file_list.data_category ='Biospecimen' AND  tcga_file_list.experimental_strategy NOT IN ('Diagnostic Slide','Tissue Slide')</if>"
			+ "<if test='experimental!=null'> AND tcga_file_list.experimental_strategy = #{experimental}</if>"
			+ "<if test='dataFormat!=null'> AND tcga_file_list.data_format = #{dataFormat}</if>"
			+ "<if test='dataFormatNoT!=null'> AND tcga_file_list.data_format != #{dataFormatNoT}</if>"
			+ "<if test='location!=null'> AND tcga_file_list.workflow_type NOT IN ('HTSeq - FPKM','HTSeq - FPKM-UQ','HTSeq - Counts','BCGSC miRNA Profiling','VarScan2 Variant Aggregation and Masking','MuSE Variant Aggregation and Masking','MuTect2 Variant Aggregation and Masking','SomaticSniper Variant Aggregation and Masking','Liftover','DNAcopy')"
			+ " AND tcga_file_list.data_type NOT IN ('miRNA Expression Quantification','Isoform Expression Quantification','Masked Somatic Mutation','Methylation Beta Value','Copy Number Segment','Masked Copy Number Segment')"
			+ " AND tcga_file_list.platform NOT IN ('Illumina Human Methylation 450','Illumina Human Methylation 27')"
			+ " AND tcga_file_list.data_category NOT IN ('Clinical','Biospecimen')"
			+ " AND tcga_file_list.location = 'local'</if>"
			+ "<if test='locationNot!=null'> AND tcga_file_list.workflow_type NOT IN ('HTSeq - FPKM','HTSeq - FPKM-UQ','HTSeq - Counts','BCGSC miRNA Profiling','VarScan2 Variant Aggregation and Masking','MuSE Variant Aggregation and Masking','MuTect2 Variant Aggregation and Masking','SomaticSniper Variant Aggregation and Masking','Liftover','DNAcopy')"
			+ " AND tcga_file_list.data_type NOT IN ('miRNA Expression Quantification','Isoform Expression Quantification','Masked Somatic Mutation','Methylation Beta Value','Copy Number Segment','Masked Copy Number Segment')"
			+ " AND tcga_file_list.platform NOT IN ('Illumina Human Methylation 450','Illumina Human Methylation 27')"
			+ " AND tcga_file_list.data_category NOT IN ('Clinical','Biospecimen')"
			+ " AND tcga_file_list.location != 'local'</if>"
			+ "</script>")
	List<TcgaFile> listTcgaTypeInfo(@Param("id") String id, @Param("workflowType") String workflowType, @Param("dataType") String dataType,
									@Param("platform") String platform, @Param("dataCategory") String dataCategory, @Param("dataFormat") String dataFormat,
									@Param("dataFormatNoT") String dataFormatNoT,@Param("location") String location,@Param("locationNot") String locationNot,
									@Param("experimental")String experimental,@Param("experimentalNoT")String experimentalNoT,@Param("transferNoT") String transferNoT,
									@Param("outsideNoT") String outsideNoT,@Param("platformNoT") String platformNoT);

	@Select("SELECT * FROM `tcga_file_list` WHERE `id`=#{id}")
	TcgaFile getTcgaFile(@Param("id") String id);


}
