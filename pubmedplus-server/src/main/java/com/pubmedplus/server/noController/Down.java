package com.pubmedplus.server.noController;

import com.pubmedplus.server.pojo.Gene2GTEx;
import com.pubmedplus.server.utils.Util;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @Author : zp
 * @Description :
 * @Date : 2020/4/27
 */
@Controller
public class Down {

    private  Logger log = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/getGenesGTExInfos")
    public String getGenesGTExInfos(HttpServletResponse response, String[] genesId, String type) throws IOException {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        File file = new File(Util.R_DATA + uuid);
        FileWriter fileWriter = new FileWriter(file);
        Map<String, String> map = new HashMap<>();
        StringBuilder tit = new StringBuilder("sampleCode\tprimarySite");
        int i = 0;
        for (String str : genesId) {
            String geneId = Util.util.genesMapper.getGeneId(str.trim());
            String ensg = Util.util.genesMapper.get_Gid2ExpEnsg(geneId.trim());
            tit.append("\t").append(ensg);
            String syy = "CCLE".equals(type) ? "CCLE" : "PanCancerTPM";
            List<Gene2GTEx> gtExList = Util.util.sangerBoxMapper.list_downData("zz_gene_exp_" + ensg.toLowerCase(), type, syy);
            log.info(gtExList.toString());
            for (Gene2GTEx g : gtExList) {
                String key = g.getSampleCode() + "\t" + g.getPrimarySite();
                String s = map.get(key);
                if (s == null && i == 0) {
                    String val = g.getSampleCode() + "\t" + g.getPrimarySite() + "\t" + g.getVal();
                    map.put(key, val);
                } else if (s == null) {
                    String x = "";
                    for (int j = 0; j < i; j++) {
                        x = x + "NA\t";
                    }
                    String val = g.getSampleCode() + "\t" + g.getPrimarySite() + "\t" + x + g.getVal();
                    map.put(key, val);
                } else {
                    String val = s + "\t" + g.getVal();
                    map.put(key, val);
                }
            }
            i = i + 1;
        }
        fileWriter.write(tit.toString() + "\n");
        for (Map.Entry entry : map.entrySet()) {
            fileWriter.write(entry.getValue() + "\n");
        }
        fileWriter.close();
        response.setContentType("application/force-download");
        response.setHeader("content-disposition", "attachment;fileName=" + URLEncoder.encode(file.getName(), Charset.defaultCharset()));
        FileUtils.copyFile(file, response.getOutputStream());
        file.delete();
        return null;
    }

    @GetMapping("/getGenesPlotPancerTissueByInfos")
    public String getGenesPlotPancerTissueByInfos(@NotBlank String genesId, @NotBlank String m, @NotBlank String s, @NotBlank String a, @NotBlank String y, @NotBlank String z, String db, String type, HttpServletResponse response) throws IOException {
        type = Objects.isNull(type) ? "png" : type;
        var responseJson = new JSONObject();
        var cmd = new StringBuilder("Rscript " + Util.R_SCRIPT + "PlotPancerTissueByGeneID.R -i " + genesId.trim() + " -m " + m + " -d " + db + " -s " + s + " -f " + type + " -p 3.5 -w 8 -a " + a + " -y " + y + " -z " + z);
        getString(type, response, responseJson, Util.startDrawPng(cmd, type));
        return null;
    }

    @GetMapping("/getGenesPlotPancerDitByInfos")
    public void getGenesPlotPancerDitByInfo(@NotBlank String genesId, @NotBlank String g, @NotBlank String m, @NotBlank String s, @NotBlank String u, @NotBlank String d, @NotBlank String l, @NotBlank String a, @NotBlank String y, @NotBlank String z, String type, HttpServletResponse response) throws IOException {
        type = Objects.isNull(type) ? "png" : type;
        var cmd = new StringBuilder("Rscript " + Util.R_SCRIPT + "PlotPancerDitByGeneID.R -i " + genesId.trim() + " -g " + g + " -m " + m + " -s " + s + " -u " + u + " -d " + d + " -l " + l + " -f " + type + " -p 3.5 -w 8 -a " + a + " -y " + y + " -z " + z);
        var fileBase64 = Util.startDrawPng(cmd, type);
        if (fileBase64 != null) {
            File file = new File(fileBase64);
            response.setContentType("application/force-download");
            response.setHeader("content-disposition", "attachment;fileName=" + URLEncoder.encode(file.getName(), Charset.defaultCharset()));
            FileUtils.copyFile(file, response.getOutputStream());
        }
    }



    @GetMapping("/getTCGASurvByGeneId")
    @ResponseBody
    public String getTcgaSurvByGeneId(@NotBlank String geneId, @NotBlank String c, @NotBlank String m, String p, String u, String d, String f, String v, String w, HttpServletResponse response) {
        var responseJson = new JSONObject();
        f = Objects.isNull(f) ? "png" : f;
        StringBuilder cmd = new StringBuilder("Rscript " + Util.R_SCRIPT + "getTCGASurvByGeneID.R -i " + geneId.trim() + " -c " +
                c + " -m " + m + " -p " + p + " -f " + f + " -v " + v + " -w " + w);
        if ("km".equals(p.trim())) {
            cmd.append(" -u ").append(u).append(" -d ").append(d);
        }
        return getString(f, response, responseJson, Util.startDrawPngs(cmd, f));
    }

    @GetMapping("/getCustomGeneCor")
    @ResponseBody
    public String getCustomGeneCor(@NotBlank String geneId, String j, @NotBlank String c, @NotBlank String m, String n, String d, String x,
                                   String f, String p, String l, String q, String z, String a, String v, String w, HttpServletResponse response) throws IOException {
        String uuid = UUID.randomUUID().toString().replaceAll("-","");
        File file = new File(Util.R_DATA+uuid);
        FileWriter fw = new FileWriter(file);
        fw.write(j);
        fw.close();
        var responseJson = new JSONObject();
        f = Objects.isNull(f) ? "png" : f;
        StringBuilder cmd = new StringBuilder("Rscript " + Util.R_SCRIPT + "getCustomGeneCor.R -i " + geneId.trim() + " -j " + file.getPath() + " -c " + c + " -l " + l + " -q " + q
                + " --min=" + n + " --med=" + d + " --max=" + x + " -m " + m + " -p " + p + " -f " + f + " -v " + v + " -w " + w + " -z " + z + " -a " + a);
        return getString(f, response, responseJson, Util.startDrawPngs(cmd, f));
    }

    @GetMapping("/getTCGAMafPlotByGenes")
    @ResponseBody
    public String getTcgaMafPlotByGenes(@NotBlank String geneId,String f,String c,String m, String v, String w, HttpServletResponse response) throws IOException {
        var responseJson = new JSONObject();
        f = Objects.isNull(f) ? "png" : f;
        if ("count".equals(m.trim())){
            String uuid = UUID.randomUUID().toString().replaceAll("-","");
            File file = new File(Util.R_DATA+uuid);
            FileWriter fw = new FileWriter(file);
            fw.write(geneId);
            fw.close();
            geneId = file.getPath();
        }
        StringBuilder cmd = new StringBuilder("Rscript " + Util.R_SCRIPT + "getTCGAMafPlotByGenes.R -i " + geneId.trim() + " -c " + c +
                " -m " + m + " -f " + f + " -v " + v + " -w " + w );
        return getString(f, response, responseJson, Util.startDrawPngs(cmd, f));
    }

    @GetMapping("/getTcgaMsiTmbNgenCorByGeneID")
    @ResponseBody
    public String getTcgaMsiTmbNgenCorByGeneID(@NotBlank String geneId, String f, String p, String d, String c, String m, String v, String w, HttpServletResponse response) throws IOException {
        var responseJson = new JSONObject();
        f = Objects.isNull(f) ? "png" : f;
        StringBuilder cmd = new StringBuilder("Rscript " + Util.R_SCRIPT + "getTCGAMSI_TMB_ngen_cor_ByGeneID.R -i " + geneId.trim() + " -c " + c +
                " -m " + m +" -p " + p +" -d " + d + " -f " + f + " -v " + v + " -w " + w );
        return getString(f, response, responseJson, Util.startDrawPngs(cmd, f));
    }

    @GetMapping("/getTCGAimmucorByGeneID")
    @ResponseBody
    public String getTCGAimmucorByGeneID(@NotBlank String geneId, String f, String p, String t, String r, String n, String x, String l, String q, String z
            , String a, String y, String d, String c, String m, String v, String w, HttpServletResponse response) throws IOException {
        var responseJson = new JSONObject();
        f = Objects.isNull(f) ? "png" : f;
        StringBuilder cmd = new StringBuilder("Rscript " + Util.R_SCRIPT + "getTCGA_immu_cor_ByGeneID.R -i " + geneId.trim() + " -c " + c +
                " -m " + m +" -t " + t +" -r " + r +" -n " + n +" -x " + x +" -l " + l +" -q " + q +" -z " + z +" -a " + a +" -y " + y +
                " -p " + p +" -d " + d + " -f " + f + " -v " + v + " -w " + w );
        return getString(f, response, responseJson, Util.startDrawPngs(cmd, f));
    }

    private String getString(String f, HttpServletResponse response, JSONObject responseJson, String fileBase64) {
        if ("png".equals(f)) {
            responseJson.put("fileBase64", fileBase64);
            return responseJson.toString();
        } else {
            if (!StringUtils.isEmpty(fileBase64)) {
                File file = new File(fileBase64);
                response.setContentType("application/force-download");
                response.setHeader("content-disposition", "attachment;fileName=" + URLEncoder.encode(file.getName(), Charset.defaultCharset()));
                try {
                    FileUtils.copyFile(file, response.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


}
