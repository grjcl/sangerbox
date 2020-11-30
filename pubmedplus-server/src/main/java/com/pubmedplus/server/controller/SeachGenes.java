package com.pubmedplus.server.controller;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import com.pubmedplus.server.dao.sangerbox.ICountSamplesNumMapper;
import com.pubmedplus.server.pojo.*;
import com.pubmedplus.server.service.CountSamplesNumService;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregator;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregator.KeyedFilter;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pubmedplus.server.utils.ElasticSearchUtil;
import com.pubmedplus.server.utils.GeneImgUtil;
import com.pubmedplus.server.utils.Util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import one.util.streamex.StreamEx;

@RestController
@Validated
public class SeachGenes {

    @Autowired
    private CountSamplesNumService countSamplesNumService;

    @PostMapping("/searchGenesInfo")
    public String searchGenesInfo(PubmedGenes genes) throws Exception {
        var responseJson = new JSONObject();
        if (genes.query.length() == 0) {
            return responseJson.toString();
        }
        var multiSearchRequest = new MultiSearchRequest();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().minimumShouldMatch(1);
        boolQueryBuilder.should(QueryBuilders.termQuery("name", genes.query));
        boolQueryBuilder.should(QueryBuilders.termQuery("gene2AliasesNameNoStorageSet", genes.query));
        boolQueryBuilder.should(QueryBuilders.termQuery("gene2MrnasNameNoStorageSet", genes.query));
        boolQueryBuilder.should(QueryBuilders.termQuery("gene2ProteinProteinIDNoStorageSet", genes.query));
        boolQueryBuilder.should(QueryBuilders.termQuery("gene2PreviousNameNoStorageSet", genes.query));
        multiSearchRequest.add(genes.getSearchRequest(boolQueryBuilder));
        boolQueryBuilder = QueryBuilders.boolQuery().minimumShouldMatch(1);
        var regexpQuery = ".*?" + genes.query + ".*?";
        var rewrite = "scoring_boolean";
        boolQueryBuilder.should(QueryBuilders.regexpQuery("name", regexpQuery).rewrite(rewrite).boost(100));
        boolQueryBuilder.should(QueryBuilders.regexpQuery("gene2AliasesNameNoStorageSet", regexpQuery).rewrite(rewrite));
        boolQueryBuilder.should(QueryBuilders.regexpQuery("gene2MrnasNameNoStorageSet", regexpQuery).rewrite(rewrite));
        boolQueryBuilder.should(QueryBuilders.regexpQuery("gene2ProteinProteinIDNoStorageSet", regexpQuery).rewrite(rewrite));
        boolQueryBuilder.should(QueryBuilders.regexpQuery("gene2PreviousNameNoStorageSet", regexpQuery).rewrite(rewrite));
        multiSearchRequest.add(genes.getSearchRequest(boolQueryBuilder));
        var searchResponse = Util.util.restHighLevelClient.msearch(multiSearchRequest, RequestOptions.DEFAULT);
        org.elasticsearch.action.search.MultiSearchResponse.Item[] responsesItems = searchResponse.getResponses();
        for (org.elasticsearch.action.search.MultiSearchResponse.Item item : responsesItems) {
            if (item.getResponse() != null && item.getResponse().getHits().getTotalHits().value > 0) {
                responseJson.put("genesList", ElasticSearchUtil.getSearchElasticsearchData(item.getResponse()));
                break;
            }
        }
        return responseJson.toString();
    }

    @GetMapping("/getGenesInfo")
    public String getGenesInfo(@NotBlank String genesId) throws Exception {
        var responseJson = new JSONObject();
        var getRequest = new GetRequest(Util.SEARCH_GENES_INDEX, genesId.trim());
        var includes = new String[]{"summary", "transType", "geneID", "geneType", "name"
                , "gene2AliasesList", "gene2DescsList", "expTable", "desc", "imgBase64", "imgRstBase64"};
        getRequest.fetchSourceContext(new FetchSourceContext(true, includes, null));
        var searchResponse = Util.util.restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        responseJson.put("genesDetails", searchResponse.getSourceAsMap());
        return responseJson.toString();
    }

    @GetMapping("/getCancerCount")
    public String getCancerCount() {
        var responseJson = new JSONObject();
//        获取列表
        List<CountSamplesNumModel> countSamplesNumModels = countSamplesNumService.countSamplesNum();
//        System.out.println("基因样本="+listSample.toString());
        responseJson.put("listCancer", countSamplesNumModels.toArray());
        return responseJson.toString();
    }

    @PostMapping("/oldIdsToGeneIdAllType")
    public String oldIdsToGeneId(@NotEmpty @RequestParam("oldIds") Set<String> oldIds) throws Exception {
        Set<String> types = new HashSet<String>();
        types.add("Genebank2Symbol");
        types.add("Ensembl2Symbol");
        types.add("Protein2Symbol");
        types.add("ENSEMBLProtein2Symbol");
        types.add("REFSEQProteins2Symbol");
        types.add("ENSEMBLmRNA2Symbol");
        types.add("REFSEQ2Symbol");
        types.add("Entrez2Symbol");
        types.add("Symbol2Symbol");
        return oldIdsToGeneId(oldIds, types);
    }

    @PostMapping("/oldIdsToGeneId")
    public String oldIdsToGeneId(@NotEmpty @RequestParam("oldIds") Set<String> oldIds, @NotEmpty @RequestParam("types") Set<String> types) throws Exception {
        var responseJson = new JSONObject();
        var oldIdLists = Util.splitList(new ArrayList<String>(oldIds), 1000);
        var searchResponseJson = new JSONObject();
        for (List<String> oldIdList : oldIdLists) {
            var responseAggsJson = sendGeneIdToIdAggs(oldIdList, types);
            responseAggsJson.remove("searchTime");
            var it = responseAggsJson.keys();
            while (it != null && it.hasNext()) {
                var key = it.next();
                if (searchResponseJson.containsKey(key)) {
                    searchResponseJson.getJSONObject(key.toString()).putAll(responseAggsJson.getJSONObject(key.toString()));
                } else {
                    searchResponseJson.put(key, responseAggsJson.get(key));
                }
            }
        }
        responseJson.put("aggs", searchResponseJson);
        return responseJson.toString();
    }

    private JSONObject sendGeneIdToIdAggs(List<String> oldIdList, Set<String> types) throws IOException {
        var searchRequest = new SearchRequest(Util.SEARCH_GENES_ID_TO_ID_INDEX);
        var searchSourceBuilder = new SearchSourceBuilder().size(0);
        var topHitsAggs = AggregationBuilders.topHits("genesList").fetchSource(new String[]{"name", "gene2AliasesEntrezGeneSet", "geneID"}, null);

        var aggsMap = new HashMap<String, Map<String, Object>>();
        if (types.contains("Genebank2Symbol")) {
            Map<String, Object> mapGenebank2Symbol = Stream.concat(
                    oldIdList.stream().collect(Collectors.toMap(Function.identity(), value -> value + "/Additional mRNA sequences")).entrySet().stream()
                    , oldIdList.stream().collect(Collectors.toMap(Function.identity(), value -> value + "/Selected AceView")).entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (value1, value2) -> QueryBuilders.termsQuery("gene2MrnaNoStorageSet", Arrays.asList(value1, value2))));
            aggsMap.put("Genebank2Symbol", mapGenebank2Symbol);
        }
        if (types.contains("Ensembl2Symbol")) {
            aggsMap.put("Ensembl2Symbol", oldIdList.stream().collect(Collectors.toMap(Function.identity(), value -> QueryBuilders.termQuery("gene2AliasesNoStorageSet", value + "/Ensembl"))));
        }
        if (types.contains("Protein2Symbol")) {
            aggsMap.put("Protein2Symbol", oldIdList.stream().collect(Collectors.toMap(Function.identity(), value -> QueryBuilders.termQuery("gene2ProteinNoStorageSet", value + "/UniProtKB"))));
        }
        if (types.contains("ENSEMBLProtein2Symbol")) {
            aggsMap.put("ENSEMBLProtein2Symbol", oldIdList.stream().collect(Collectors.toMap(Function.identity(), value -> QueryBuilders.termQuery("gene2ProteinNoStorageSet", value + "/ENSEMBL proteins:"))));
        }
        if (types.contains("REFSEQProteins2Symbol")) {
            aggsMap.put("REFSEQProteins2Symbol", oldIdList.stream().collect(Collectors.toMap(Function.identity(), value -> QueryBuilders.termQuery("gene2ProteinNoStorageSet", value + "/REFSEQ proteins:"))));
        }
        if (types.contains("ENSEMBLmRNA2Symbol")) {
            aggsMap.put("ENSEMBLmRNA2Symbol", oldIdList.stream().collect(Collectors.toMap(Function.identity(), value -> QueryBuilders.termQuery("gene2MrnaNoStorageSet", value + "/Ensembl transcripts including schematic representations, and UCSC links to gene/alias where relevant"))));
        }
        if (types.contains("REFSEQ2Symbol")) {
            aggsMap.put("REFSEQ2Symbol", oldIdList.stream().collect(Collectors.toMap(Function.identity(), value -> QueryBuilders.termQuery("gene2MrnaNoStorageSet", value + "/REFSEQ mRNAs"))));
        }
        if (types.contains("Entrez2Symbol")) {
            aggsMap.put("Entrez2Symbol", oldIdList.stream().collect(Collectors.toMap(Function.identity(), value -> QueryBuilders.termQuery("gene2AliasesNoStorageSet", value + "/Entrez Gene"))));
        }
        if (types.contains("Symbol2Symbol")) {
            aggsMap.put("Symbol2Symbol", oldIdList.stream().collect(Collectors.toMap(Function.identity(), value -> QueryBuilders.termQuery("name", value))));
            aggsMap.put("Symbol2Symbol2", oldIdList.stream().collect(Collectors.toMap(Function.identity(), value -> QueryBuilders.termQuery("gene2PreviousNameNoStorageSet", value))));
        }

        Map<String, Object> map = null;
        ArrayList<KeyedFilter> keyedFilterList = new ArrayList<KeyedFilter>();
        for (Entry<String, Map<String, Object>> entry : aggsMap.entrySet()) {
            map = entry.getValue();
            for (Entry<String, Object> entry2 : map.entrySet()) {
                keyedFilterList.add(new FiltersAggregator.KeyedFilter(entry2.getKey(), (QueryBuilder) entry2.getValue()));
            }
            searchSourceBuilder.aggregation(AggregationBuilders.filters(entry.getKey(), keyedFilterList.toArray(new FiltersAggregator.KeyedFilter[keyedFilterList.size()])).subAggregation(topHitsAggs));
            keyedFilterList.clear();
        }

        searchRequest.source(searchSourceBuilder);
        var searchResponse = Util.util.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        var searchResponseJson = ElasticSearchUtil.getSearchElasticsearchData(searchResponse);
        if (searchResponseJson.getJSONObject("Symbol2Symbol").isEmpty()) {
            searchResponseJson.put("Symbol2Symbol", searchResponseJson.get("Symbol2Symbol2"));
        }
        searchResponseJson.remove("Symbol2Symbol2");
        return searchResponseJson;
    }

    @GetMapping("/getGenesGoInfo")
    public String getGenesGoInfo(@NotBlank String genesId) throws Exception {
        var responseJson = new JSONObject();
        var getRequest = new GetRequest(Util.SEARCH_GENES_INDEX, genesId.trim());
        getRequest.fetchSourceContext(new FetchSourceContext(true, new String[]{"gene2GoList"}, null));
        var searchResponse = Util.util.restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        responseJson.put("genesDetails", searchResponse.getSourceAsMap());
        return responseJson.toString();
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/getGenesKEGGInfo")
    public String getGenesKEGGInfo(@NotBlank String genesId) throws Exception {
        var responseJson = new JSONObject();
        var getRequest = new GetRequest(Util.SEARCH_GENES_INDEX, genesId.trim());
        var includesPrefix = "gene2KeggMapList.";
        var includes = new String[]{includesPrefix + "pathwayid", includesPrefix + "info", includesPrefix + "oneType", includesPrefix + "twoType"};
        getRequest.fetchSourceContext(new FetchSourceContext(true, includes, null));
        var searchResponse = Util.util.restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        var sourceAsMap = searchResponse.getSourceAsMap();
        if (sourceAsMap != null && sourceAsMap.containsKey("gene2KeggMapList")) {
            var oldGene2KeggMapList = JSONArray.fromObject(sourceAsMap.get("gene2KeggMapList"));
            var isDistinctMap = new HashMap<String, Boolean>();
            var nowGene2KeggMapList = new JSONArray();
            oldGene2KeggMapList.forEach(value -> {
                if (isDistinctMap.put(JSONObject.fromObject(value).getString("pathwayid"), true) == null) {
                    nowGene2KeggMapList.add(value);
                }
            });
            responseJson.put("genesDetails", nowGene2KeggMapList);
        }
        return responseJson.toString();
    }

    @GetMapping("/getGenesRelationInfo")
    public String getGenesRelationInfo(@NotBlank String genesId) throws Exception {
        var responseJson = new JSONObject();
        var getRequest = new GetRequest(Util.SEARCH_GENES_INDEX, genesId.trim());
        var includesPrefix = "gene2KeggMapList.kegg2RelationList.";
        var includes = new String[]{includesPrefix + "name2", includesPrefix + "type", includesPrefix + "name1", includesPrefix + "pathwayID"};
        getRequest.fetchSourceContext(new FetchSourceContext(true, includes, null));
        var searchResponse = Util.util.restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        var sourceAsMap = searchResponse.getSourceAsMap();
        if (sourceAsMap == null || !sourceAsMap.containsKey("gene2KeggMapList")) {
            return responseJson.toString();
        }
        var gene2KeggMapList = JSONArray.fromObject(sourceAsMap.get("gene2KeggMapList"));
        var joinGene = new JSONArray();
        var joinGeneIsMap = new HashMap<String, Boolean>();
        for (var gene2KeggMap : gene2KeggMapList) {
            var gene2KeggMapJson = JSONObject.fromObject(gene2KeggMap);
            if (!gene2KeggMapJson.containsKey("kegg2RelationList")) {
                continue;
            }
            var kegg2RelationList = gene2KeggMapJson.getJSONArray("kegg2RelationList");
            for (var kegg2Relation : kegg2RelationList) {
                var kegg2RelationJson = JSONObject.fromObject(kegg2Relation);
                if (kegg2RelationJson.containsKey("name1")) {
                    var name1 = kegg2RelationJson.getJSONArray("name1");
                    for (var name : name1) {
                        var nameJson = JSONObject.fromObject(name);
                        if (genesId.equals(nameJson.getString("geneID"))) {
                            responseJson.put("hostGene", nameJson);
                            break;
                        }
                        if (joinGeneIsMap.put(nameJson.getString("name"), true) == null) {
                            joinGene.add(nameJson);
                        }
                    }
                }
                if (kegg2RelationJson.containsKey("name2")) {
                    var name2 = kegg2RelationJson.getJSONArray("name2");
                    for (var name : name2) {
                        var nameJson = JSONObject.fromObject(name);
                        if (genesId.equals(nameJson.getString("geneID"))) {
                            responseJson.put("hostGene", nameJson);
                            break;
                        }
                        if (joinGeneIsMap.put(nameJson.getString("name"), true) == null) {
                            joinGene.add(nameJson);
                        }
                    }
                }
            }
        }
        responseJson.put("joinGene", joinGene);
        return responseJson.toString();
    }

    @GetMapping("/getGenesPancancerInfo")
    public String getGenesPancancerInfo(@NotBlank String genesId) throws Exception {
        var responseJson = new JSONObject();
        var ensg = Util.util.genesMapper.get_Gid2ExpEnsg(genesId.trim());
        if (ensg == null) {
            return responseJson.toString();
        }
        var gene2PancancerList = Util.util.sangerBoxMapper.list_SetGene2Pancancer("zz_gene_exp_" + ensg.toLowerCase());
        responseJson.put("gene2PancancerList", gene2PancancerList);
        return responseJson.toString();
    }

    @GetMapping("/getGenesGTExInfo")
    public String getGenesGTExInfo(@NotBlank String genesId, String type) throws Exception {
        if (Objects.isNull(type)){
            type = "GTEx";
        }
        var responseJson = new JSONObject();
        var ensg = Util.util.genesMapper.get_Gid2ExpEnsg(genesId.trim());
        if (ensg == null) {
            return responseJson.toString();
        }
        System.out.println(type +":" + ensg);
        String s = "GTEx".equals(type) ? "PanCancerTPM" : type;
        var gene2PancancerList = Util.util.sangerBoxMapper.list_downData("zz_gene_exp_" + ensg.toLowerCase(),type,s);
//        System.out.println(gene2PancancerList.toString());
        responseJson.put("gene2GTExList", gene2PancancerList);
        return responseJson.toString();
    }


    @GetMapping("/getGenesPancancerMethyInfo")
    public String getGenesPancancerMethyInfo(@NotBlank String genesId) throws Exception {
        var responseJson = new JSONObject();
        var ensg = Util.util.genesMapper.get_Gid2ExpEnsg(genesId.trim());
        if (ensg == null) {
            return responseJson.toString();
        }
        var gene2PancancerList = Util.util.sangerBoxMapper.list_SetPancancerMethy("zz_gene_exp_" + ensg.toLowerCase());
        responseJson.put("genesPancancerMethyList", gene2PancancerList);
        return responseJson.toString();
    }

    @GetMapping("/getGenesPancancerMethyExpCorInfo")
    public String getGenesPancancerMethyExpCorInfo(@NotBlank String genesId) throws Exception {
        var responseJson = new JSONObject();
        var ensg = Util.util.genesMapper.get_Gid2ExpEnsg(genesId.trim());
        if (ensg == null) {
            return responseJson.toString();
        }
        var gene2PancancerList = Util.util.sangerBoxMapper.list_SetPancancerMethyExpCor("zz_gene_exp_" + ensg.toLowerCase());
        responseJson.put("genesPancancerMethyExpCorList", gene2PancancerList);
        return responseJson.toString();
    }

    @GetMapping("/getGenesPubmedInfo")
    public String getGenesPubmedInfo(@NotBlank String genesId) throws Exception {
        var responseJson = new JSONObject();
        var pubmedSet = Util.util.genesMapper.list_gene2pubmed(genesId.trim());
        if (pubmedSet.size() == 0) {
            return responseJson.toString();
        }
        var searchRequest = new SearchRequest(Util.SEARCH_PUBMED_INDEX);
        var searchSourceBuilder = new SearchSourceBuilder().fetchSource(new String[]{"PMID", "title", "journal"}, new String[]{"journal.comments"});
        var IdsQueryBuilder = new IdsQueryBuilder();
        IdsQueryBuilder.addIds(pubmedSet.toArray(new String[pubmedSet.size()]));
        searchSourceBuilder.query(IdsQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        var searchResponse = Util.util.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        responseJson.put("articleList", ElasticSearchUtil.getSearchElasticsearchData(searchResponse));
        return responseJson.toString();
    }

    @GetMapping("/getGenesSingleInfo")
    public String getGenesSingleInfo(@NotBlank String genesId, @NotBlank String calBatch) throws Exception {
        var responseJson = new JSONObject();
        var ensg = Util.util.genesMapper.get_Gid2ExpEnsg(genesId.trim());
        if (ensg == null) {
            return responseJson.toString();
        }
        var gene2PancancerList = Util.util.sangerBoxMapper.get_GenesSingleInfo("zz_gene_exp_" + ensg.toLowerCase(), calBatch.trim());
        responseJson.put("expSampleInfoList", gene2PancancerList);
        return responseJson.toString();
    }

    @GetMapping("/getGenesTfInfo")
    public String getGenesTfInfo(@NotBlank String genesId) throws Exception {
        var responseJson = new JSONObject();
        var geneTfList = Util.util.genesMapper.list_GeneTf(genesId.trim());
        responseJson.put("geneTfList", geneTfList);
        return responseJson.toString();
    }

    @GetMapping("getGenesIdConversionTypeInfo")
    public String getGenesIdConversionTypeInfo() throws Exception {
        var responseJson = new JSONObject();
        var idTypeList = new ArrayList<>(Arrays.asList("Genebank", "Ensembl", "UniProtKB", "ENSEMBL Proteins", "REFSEQ Proteins", "Ensembl transcripts", "REFSEQ mRNAs", "Entrez", "Symbol", "miRNA Name", "miRNA ID"));
        var file = new File(Util.GEO_FILE_PREFIX + "geo/GPL_anno");
        if (file.exists()) {
            var files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                var fileName = files[i].getName();
                var fileSuffix = fileName.split("\\.");
                if (fileSuffix.length > 1 && "txt".equals(fileSuffix[fileSuffix.length - 1])) {
                    idTypeList.add(fileName.split("-")[0]);
                }
            }
            responseJson.put("idTypeList", idTypeList);
        }
        return responseJson.toString();
    }

    @PostMapping("/getGenesDataIdConvertAuthInfo")
    public void getGenesDataIdConvertAuthInfo(String text, String i, int m, int f, @NotBlank String p, int t, HttpServletRequest request) throws Exception {
        var userPhone = request.getHeader("userPhone");
        if (!Util.isPhone(userPhone) || (text == null && i == null)) {
            return;
        }
        var userFile = new File(Util.USER_DATA + userPhone + "/");
        if (!userFile.exists()) {
            userFile.mkdirs();
        }
        var nowTime = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        if (text != null && text.trim().length() > 0) {
            if (!Util.writerText(Util.R_DATA + userPhone + "_" + nowTime + ".txt", text)) {
                Util.sendRabbitmqRoutingMessage(Util.RABBITMQ_TACK_WEB, userPhone, "ID转换失败！");
                return;
            }
            i = Util.R_DATA + userPhone + "_" + nowTime + ".txt";
        } else if (i != null && i.trim().length() > 0) {
            i = userFile + i;
            text = Util.readerFile(i).toString();
        }
        var successFilePath = "/" + nowTime + ".txt";
        var cmd = "Rscript " + Util.R_SCRIPT + "DataIdConvert.R -i " + i + " -f " + f + " -p " + p.trim() + " -t " + t + " -o " + (userFile + "/" + nowTime + ".txt") + " -e " + (userPhone + "_" + nowTime + "_error.log");
        if (m >= 1 && m <= 4) {
            cmd = "Rscript " + Util.R_SCRIPT + "DataIdConvert.R -i " + i + " -m " + m + " -f " + f + " -p " + p.trim() + " -t " + t + " -o " + (userFile + "/" + nowTime + ".txt") + " -e " + userPhone + "_" + nowTime + "_error.log";
        }
        var cmdMd5 = Util.strToMd5(userPhone + ":" + text + " -m " + m + " -f " + f + " -p " + p.trim() + " -t " + t);
        var amqpRabbitListener = new AmqpRabbitListener(cmdMd5, cmd, "ID转换", userPhone, "rabbitmqGeoDataIdConvert", userFile.toString(), successFilePath, successFilePath + ".log");
        Util.sendRabbitmqTask(amqpRabbitListener);
    }

    @PostMapping("/getGenesMiRNATargetRNAByGeneIDandDatabasesInfo")
    public String getGenesMiRNATargetRNAByGeneIDandDatabasesInfo(@NotBlank String genesId, @NotEmpty @RequestParam("databases") Set<String> databases) throws Exception {
        var responseJson = new JSONObject();
        var mirnaTargetrnaList = Util.util.genesMapper.list_GenesMiRNATargetRNAByGeneIDandDatabasesInfo(genesId, "'" + String.join("','", databases) + "'");
        responseJson.put("mirnaTargetrnaList", mirnaTargetrnaList);
        return responseJson.toString();
    }

    @PostMapping("/getGenesRoadVisualInfo")
    public String getGenesRoadVisualInfo(@NotBlank String genesId, @NotBlank String genesId2, @NotBlank String pathwayid) throws Exception {
        var responseJson = new JSONObject();
        var linkList = Util.util.genesMapper.get_GenesRoadVisual(genesId.trim(), genesId2.trim(), pathwayid.trim());
        if (linkList.size() > 0) {
            responseJson.put("fileBase64", GeneImgUtil.getImg(pathwayid.trim(), linkList));
        }
        return responseJson.toString();
    }

    @PostMapping("/getGenesPlotPancerDitByInfo")
    public String getGenesPlotPancerDitByInfo(@NotBlank String genesId, @NotBlank String g, @NotBlank String m, @NotBlank String s, @NotBlank String u, @NotBlank String d, @NotBlank String l, @NotBlank String a, @NotBlank String y, @NotBlank String z, String type, HttpServletResponse response) throws IOException {
        type = Objects.isNull(type) ? "png" : type;
        var responseJson = new JSONObject();
        var cmd = new StringBuilder("Rscript " + Util.R_SCRIPT + "PlotPancerDitByGeneID.R -i " + genesId.trim() + " -g " + g + " -m " + m + " -s " + s + " -u " + u + " -d " + d + " -l " + l + " -f " + type + " -p 3.5 -w 8 -a " + a + " -y " + y + " -z " + z);
        var fileBase64 = Util.startDrawPng(cmd, type);
        if ("png".equals(type) && fileBase64 != null) {
            responseJson.put("fileBase64", fileBase64);
            return responseJson.toString();
        } else {
            if (fileBase64 != null) {
                File file = new File(fileBase64);
                response.setContentType("application/force-download");
                response.setHeader("content-disposition", "attachment;fileName=" + URLEncoder.encode(file.getName(), Charset.defaultCharset()));
                FileUtils.copyFile(file, response.getOutputStream());
            }
        }
        return null;
    }

    @PostMapping("/getGenesPlotPancerTissueByInfo")
    public String getGenesPlotPancerTissueByInfo(@NotBlank String genesId, @NotBlank String m, @NotBlank String s, @NotBlank String a, @NotBlank String y, @NotBlank String z,String db, String type, HttpServletResponse response) throws IOException {
        db = Objects.isNull(db) ? "GTEx" : db;
        type = Objects.isNull(type) ? "png" : type;
        var responseJson = new JSONObject();
        var cmd = new StringBuilder("Rscript " + Util.R_SCRIPT + "PlotPancerTissueByGeneID.R -i " + genesId.trim() + " -m " + m + " -s " + s + " -f " + type + " -p 3.5 -w 8 -a " + a + " -y " + y +" -d "+db+ " -z " + z);
        var fileBase64 = Util.startDrawPng(cmd, type);
        if ("png".equals(type) && fileBase64 != null) {
            responseJson.put("fileBase64", fileBase64);
            return responseJson.toString();
        } else {
            if (!StringUtils.isEmpty(fileBase64)) {
                File file = new File(fileBase64);
                response.setContentType("application/pdf");
                response.setHeader("content-disposition", "attachment;fileName=" + URLEncoder.encode(file.getName(), Charset.defaultCharset()));
                FileUtils.copyFile(file, response.getOutputStream());
            }
        }
        return null;
    }

    @PostMapping("/getGenesCorKEGGPathwya2ExpByInfo")
    public String getGenesCorKEGGPathwya2ExpByInfo(@NotBlank String genesId, @NotBlank String m, @NotEmpty @RequestParam("s") Set<String> s, @NotBlank String p, @NotBlank String r, String t) {
        var responseJson = new JSONObject();
        var cmd = new StringBuilder("Rscript " + Util.R_SCRIPT + "getCorKEGGPathway2ExpByGeneID.R -i " + genesId.trim() + " -m " + m + " -s " + String.join(",", s) + " -p " + p + " -r " + r);
        var f = "txt";
        if (t != null) {
            f = "png";
            cmd.append(" -t " + t);
        }
        var fileContent = Util.startDrawPng(cmd, f);
        if (fileContent != null) {
            responseJson.put("fileContent", fileContent);
        }
        return responseJson.toString();
    }

    @PostMapping("/getGenesCorKEGGGene2ExpByInfo")
    public String getGenesCorKEGGGene2ExpByInfo(@NotBlank String genesId, @NotBlank String m, @NotBlank String s, @NotBlank String p, @NotBlank String r, String t) {
        var responseJson = new JSONObject();
        var cmd = new StringBuilder("Rscript " + Util.R_SCRIPT + "getCorKEGGGene2ExpByGeneID.R -i " + genesId.trim() + " -m " + m + " -s " + s + " -p " + p + " -r " + r);
        var f = "txt";
        if (t != null) {
            f = "png";
            cmd.append(" -t " + t);
        }
        var fileContent = Util.startDrawPng(cmd, f);
        if (fileContent != null && fileContent.trim().length() >= 3) {
            var textList = new ArrayList<>(Arrays.asList(fileContent.subSequence(1, fileContent.length() - 1).toString().split(",")));
            var geneIdSet = textList.stream().filter(text -> text.split("\t")[0].trim().length() == 32).map(text -> text.split("\t")[0].trim()).collect(Collectors.toSet());
            var geneMap = Util.util.genesMapper.get_geneInfoTitles("'" + String.join("','", geneIdSet) + "'");
            var textList2 = textList.stream().map(text -> {
                var gene = geneMap.get(text.split("\t")[0].trim());
                return gene != null ? text + "\t" + gene.getName() : text + "\tname";
            }).collect(Collectors.toList());
            responseJson.put("fileContent", textList2.toString());
        }
        return responseJson.toString();
    }

    @PostMapping("/getGenesCorGOTerm2ExpByInfo")
    public String getGenesCorGOTerm2ExpByInfo(@NotBlank String genesId, @NotBlank String m, @NotEmpty @RequestParam("s") Set<String> s, @NotBlank String p, @NotBlank String r, @NotBlank String n, String t) {
        var responseJson = new JSONObject();
        var cmd = new StringBuilder("Rscript " + Util.R_SCRIPT + "getCorGOTerm2ExpByGeneID.R -i " + genesId.trim() + " -m " + m + " -s " + String.join(",", s) + " -p " + p + " -r " + r + " -n " + n);
        var f = "txt";
        if (t != null) {
            f = "png";
            cmd.append(" -t " + t);
        }
        var fileContent = Util.startDrawPng(cmd, f);
        if (fileContent != null) {
            responseJson.put("fileContent", fileContent);
        }
        return responseJson.toString();
    }


    @PostMapping("/getGenesExpByRInfo")
    public String getGenesExpByRInfo(@NotEmpty @RequestParam("genesIds") Set<String> genesIdSet, @NotEmpty @RequestParam("calBatch") String calBatch, String dbType, String siteType, String tcgaCode) throws Exception {
        var responseJson = new JSONObject();
        var cmd = "Rscript PlotPancerDitByGeneID.R -i 53afe6bdb9afe29d141c38f96853acb5 -g F -m T -s 0.3 -u ff0000 -d 00ff00 -l tr -f png -p 6 -w 10 -a 45 -y 6 -z 0 -o xxxx1.png -e xxxx1.png.error.Rdata";
        var cmdMd5 = Util.strToMd5(cmd);
        var filePath = Util.R_DATA + cmdMd5 + ".txt";
        var isWriter = Util.writerText(filePath, cmd);
        if (isWriter) {
            // Util.runCmd(uuid);
        }
        responseJson.put("a", isWriter);
        responseJson.put("filePath", filePath);
        return responseJson.toString();
    }

    @PostMapping("/getGenesExpByInfo")
    public String getGenesExpByInfo(@RequestParam("genesIds") Set<String> genesIdSet, @RequestParam("calBatch") String calBatch, String dbType, String siteType, String tcgaCode) throws Exception {
        var responseJson = new JSONObject();
        if (genesIdSet.size() == 0 || calBatch == null || calBatch.isBlank()) {
            return responseJson.toString();
        }
        dbType = (dbType == null || dbType.isBlank()) ? null : dbType.trim();
        siteType = (siteType == null || siteType.isBlank()) ? null : siteType.trim();
        tcgaCode = (tcgaCode == null || tcgaCode.isBlank()) ? null : tcgaCode.trim();
        String expTable = null;
        List<ExpByGenes> expTableList = null;
        StreamEx<Entry<String, Double>> expTableStream = StreamEx.of();
        for (String genesId : genesIdSet) {
            expTable = Util.util.genesMapper.get_GseInfomationExpTable(genesId.trim());
            if (expTable == null || expTable.isBlank()) {
                continue;
            }
            expTableList = Util.util.sangerBoxMapper.list_getExpByGenes("zz_gene_exp_" + expTable, calBatch, dbType, siteType, tcgaCode);
            expTableStream = expTableStream.append(expTableList.stream().collect(Collectors.toMap(ExpByGenes::getSampleCode, ExpByGenes::getVal)).entrySet().stream());
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> expTableMap = expTableStream.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (value1, value2) -> {
            List<Object> valList = null;
            if (value1.toString().length() == 0) {
                return new ArrayList<>(Arrays.asList(value1));
            }
            if ("[".equals(value1.toString().substring(0, 1)) && "]".equals(value1.toString().substring(value1.toString().length() - 1))) {
                valList = (List<Object>) value1;
            } else {
                valList = new ArrayList<>(Arrays.asList(value1));
            }
            valList.add(value2);
            return valList;
        }));
        if (expTableMap.size() > 0) {
            expTableMap.put("name", genesIdSet);
        }
        responseJson.put("expTableMap", expTableMap);
        return responseJson.toString();
    }


}
