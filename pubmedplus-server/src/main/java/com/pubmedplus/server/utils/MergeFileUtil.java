package com.pubmedplus.server.utils;

import net.sf.json.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

class NewTumor {
    public double time;
    public String status;
    public String site;
    public String type;
    public String cancerSatus;

    public int getPFS() {
        if (status.equals("YES")) return 1;
        if (cancerSatus != null && cancerSatus.equals("WITH TUMOR")) return 2;
        if (site != null && !site.equals("Not Applicable") && !site.equals("Not Available") && !site.equals("Unknown") && !site.equals("None"))
            return 3;
        if (type != null && !type.equals("Not Applicable") && !type.equals("Not Available") && !type.equals("Unknown") && !type.equals("None") && !type.equals("No New Tumor Event"))
            return 4;
        return 0;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCancerSatus() {
        return cancerSatus;
    }

    public void setCancerSatus(String cancerSatus) {
        this.cancerSatus = cancerSatus;
    }


}


class Files implements Comparable<Files> {

    private String[] strs;
    private String type = "";

    public Files(String[] strs) {
        this.strs = strs;
        if (strs.length > 6) {
            this.type = strs[6];
        }
    }

    @Override
    public int compareTo(Files arg0) {
        return this.type.compareTo(arg0.getType());
    }

    public String[] getStrs() {
        return strs;
    }

    public void setStrs(String[] strs) {
        this.strs = strs;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

public class MergeFileUtil {

    public List<String[]> getFileInfo(String path) throws IOException {//id	filename	md5	size	state sample_id sample_type
        BufferedReader br = new BufferedReader(new FileReader(new File(path)));
        br.readLine();
        List<String[]> list = new ArrayList<>();
        List<Files> list1 = new ArrayList<>();
        String line = br.readLine();
        while (line != null) {
            if (!line.isBlank()) {
                String[] strs = line.split("\t");
                Files fls = new Files(strs);
                list1.add(fls);
            }
            line = br.readLine();
        }
        br.close();
        Collections.sort(list1);
        for (Files fls : list1) {
            list.add(fls.getStrs());
        }
        return list;
    }

    private void follow_ups(Node node, Map<String, String> map) {
        Set<String> set = new HashSet<>();
        List<Map<String, String>> list = new ArrayList<>();
        for (int j = 0; j < node.getChildNodes().getLength(); j++) {
            if (!node.getChildNodes().item(j).getNodeName().equals("#text")) {
                Map<String, String> map1 = new HashMap<>();
                moveChange(node.getChildNodes().item(j), "", map1);
                list.add(map1);
                set.addAll(map1.keySet());
            }
        }
        for (String k : set) {
            for (int i = 0; i < list.size(); i++) {
                String str = list.get(i).get(k);
                if (str == null) str = "None";
                if (map.get(".follow_ups" + k) != null)
                    map.put(".follow_ups" + k, map.get(".follow_ups" + k) + ";" + str);
                else map.put(".follow_ups" + k, str);
            }
        }
    }

    private void moveChange(Node node, String prix, Map<String, String> map) {
        String name = node.getNodeName();
        if (name.indexOf(":") > -1) name = name.substring(name.indexOf(":") + 1);
        if (node.hasChildNodes() && (node.getChildNodes().getLength() > 2 || !node.getFirstChild().getNodeName().equals("#text"))) {
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                moveChange(node.getChildNodes().item(i), prix + "." + name, map);
            }
        } else if (!node.getNodeName().equals("#text")) {
            String str = node.getTextContent().trim();
            String v = "";
            if (node.getAttributes().getNamedItem("procurement_status") != null) {
                v = node.getAttributes().getNamedItem("procurement_status").toString().substring(20);
                v = v.substring(0, v.length() - 1);
            }
            if (str.equals("")) str = v;
//			if(str.endsWith("\n")) str=str.substring(0, str.indexOf(""));
            if (map.get(prix + "." + name) != null) map.put(prix + "." + name, map.get(prix + "." + name) + ";" + str);
            else map.put(prix + "." + name, str);
        } else {
//			System.out.println("============="+name);
        }
    }

    private void flows(Node node, Map<String, String> map, String prix) {
        Set<String> set = new HashSet<>();
        List<Map<String, String>> list = new ArrayList<>();
        for (int j = 0; j < node.getChildNodes().getLength(); j++) {
            if (!node.getChildNodes().item(j).getNodeName().equals("#text")) {
                Map<String, String> map1 = new HashMap<>();
                moveChange(node.getChildNodes().item(j), "", map1);
                list.add(map1);
                set.addAll(map1.keySet());
            }
        }
        for (String k : set) {
            for (int i = 0; i < list.size(); i++) {
                String str = list.get(i).get(k);
                if (str == null) str = "None";
                if (map.get("." + prix + k) != null) map.put("." + prix + k, map.get("." + prix + k) + ";" + str);
                else map.put("." + prix + k, str);
            }
        }
    }

    private void addTumorSize(Map<String, String> map) {

        String size = map.get(".primary_pathology.tumor_sizes");
        String pdp = map.get(".primary_pathology.tumor_sizes.tumor_size.pathologic_tumor_depth");
        String pl = map.get(".primary_pathology.tumor_sizes.tumor_size.pathologic_tumor_length");
        String pw = map.get(".primary_pathology.tumor_sizes.tumor_size.pathologic_tumor_width");

        String rdp = map.get(".primary_pathology.tumor_sizes.tumor_size.radiologic_tumor_depth");
        String rl = map.get(".primary_pathology.tumor_sizes.tumor_size.radiologic_tumor_length");
        String rw = map.get(".primary_pathology.tumor_sizes.tumor_size.radiologic_tumor_width");

        if (size != null) map.put(".A10_Tumor_Size", size);
        if (pdp != null) map.put(".A11_Pathologic_Tumor_Size_Depth", pdp);
        if (pl != null) map.put(".A12_Pathologic_Tumor_Size_Length", pl);
        if (pw != null) map.put(".A13_Pathologic_Tumor_Size_Width", pw);
        if (rdp != null) map.put(".A14_Radiologic_Tumor_Size_Depth", rdp);
        if (rl != null) map.put(".A15_Radiologic_Tumor_Size_Length", rl);
        if (rw != null) map.put(".A16_Radiologic_Tumor_Size_Width", rw);

    }

    private void addAgeHeightWeightBMI(Map<String, String> map) {

        String age = map.get(".primary_pathology.age_at_initial_pathologic_diagnosis");
        if (age == null || age.trim().equals("")) {
            age = map.get(".age_at_initial_pathologic_diagnosis");
        }
        String sex = map.get(".gender");
        String height = map.get(".height");
        String weight = map.get(".weight");
        try {
            map.put(".A21_BMI", "" + Double.parseDouble(weight) * 10000 / (Double.parseDouble(height) * Double.parseDouble(height)));
        } catch (Exception e) {
        }
        if (age != null) map.put(".A17_Age", age);

        if (sex != null) map.put(".A18_Sex", sex);
        if (height != null) map.put(".A19_Height", height);
        if (weight != null) map.put(".A20_Weight", weight);

        String hst = map.get(".histological_type");
        String hste1 = map.get(".histological_type_other");
        String hste2 = map.get(".primary_pathology.histological_type");
        String hste3 = map.get(".primary_pathology.histological_type_list.histological_type");
        String hste4 = map.get(".primary_pathology.histology_list.histology.histological_type");
        String hste5 = map.get(".primary_pathology.tumor_morphology_list.tumor_morphology.histological_type");

        if (hst != null) map.put(".A22_histological_type", hst);
        else if (hste1 != null) map.put(".A22_histological_type", hste1);
        else if (hste2 != null) map.put(".A22_histological_type", hste2);
        else if (hste3 != null) map.put(".A22_histological_type", hste3);
        else if (hste4 != null) map.put(".A22_histological_type", hste4);
        else if (hste5 != null) map.put(".A22_histological_type", hste5);

        String str = map.get(".tumor_tissue_site");
        String str2 = map.get(".primary_pathology.tumor_tissue_site");
        String str3 = map.get(".primary_pathology.tumor_tissue_site_list.tumor_tissue_site");
        String str4 = map.get(".primary_pathology.tumor_tissue_sites.tumor_tissue_site");
        String str5 = map.get(".sites_of_primary_melanomas.site.tumor_tissue_site");
        if (str != null) map.put(".A23_Tumor_tissue_site", str);
        else if (str2 != null) map.put(".A23_Tumor_tissue_site", str2);
        else if (str3 != null) map.put(".A23_Tumor_tissue_site", str3);
        else if (str4 != null) map.put(".A23_Tumor_tissue_site", str4);
        else if (str5 != null) map.put(".A23_Tumor_tissue_site", str5);


    }


    private List<NewTumor> checkAddEvent(Map<String, String> map) {
        List<NewTumor> newTumors = new ArrayList<>();

        String newEventTime0 = map.get(".follow_ups.follow_up.days_to_new_tumor_event_after_initial_treatment");//复发时间
        String newEventTime1 = map.get(".follow_ups.follow_up.new_tumor.days_to_new_tumor_event_after_initial_treatment");//复发时间
        String newEventTime2 = map.get(".follow_ups.follow_up.new_tumor_events.new_tumor_event.days_to_new_tumor_event_after_initial_treatment");//复发时间
        String newEventTime3 = map.get(".new_tumor_events.new_tumor_event.days_to_new_tumor_event_after_initial_treatment");//复发时间

        String newEventType0 = map.get(".follow_ups.follow_up.new_tumor_event_after_initial_treatment");//复发状态
        String newEventType1 = map.get(".follow_ups.follow_up.new_tumor.new_tumor_event_after_initial_treatment");
        String newEventType2 = map.get(".follow_ups.follow_up.new_tumor_events.new_tumor_event_after_initial_treatment");
        String newEventType3 = map.get(".new_tumor_events.new_tumor_event_after_initial_treatment");

        String newEventTissueType1 = map.get(".follow_ups.follow_up.new_neoplasm_event_type");//复发类型
        String newEventTissueType2 = map.get(".follow_ups.follow_up.new_tumor.new_neoplasm_event_type");
        String newEventTissueType3 = map.get(".follow_ups.follow_up.new_tumor_events.new_tumor_event.new_neoplasm_event_type");
        String newEventTissueType4 = map.get(".new_tumor_events.new_tumor_event.new_neoplasm_event_type");

        String newEventTissueType5 = map.get(".follow_ups.follow_up.new_neoplasm_event_types.new_neoplasm_event_type");
        String newEventTissueType6 = map.get(".follow_ups.follow_up.new_tumor_events.new_tumor_event.new_neoplasm_event_types.new_neoplasm_event_type");
        String newEventTissueType7 = map.get(".new_tumor_events.new_tumor_event.new_neoplasm_event_types.new_neoplasm_event_type");

        String newEventTissue1 = map.get(".follow_ups.follow_up.new_neoplasm_event_occurrence_anatomic_site");//新肿瘤部位
        String newEventTissue2 = map.get(".follow_ups.follow_up.new_tumor.new_neoplasm_event_occurrence_anatomic_site");
        String newEventTissue3 = map.get(".follow_ups.follow_up.new_tumor_events.new_tumor_event.new_neoplasm_event_occurrence_anatomic_site");
        String newEventTissue4 = map.get(".new_tumor_events.new_tumor_event.new_neoplasm_event_occurrence_anatomic_site");
        String newEventTissue5 = map.get(".new_tumor_events.new_tumor_event.metastatic_procedure.new_neoplasm_event_occurrence_anatomic_site");

        String cancerStatus1 = map.get(".follow_ups.follow_up.person_neoplasm_cancer_status");//肿瘤状态
        String[] css = null;
        if (cancerStatus1 != null) {
            css = cancerStatus1.split(";");
        }


        if (newEventTime0 != null && newEventType0 != null) {
            String evts[] = newEventType0.split(";");
            String evtm[] = newEventTime0.split(";");
            String evtt[] = new String[evtm.length];
            String evtts[] = new String[evtm.length];
            if (newEventTissueType1 != null) {
                evtt = newEventTissueType1.split(";");
            } else if (newEventTissueType5 != null) evtt = newEventTissueType5.split(";");
            if (newEventTissue1 != null) evtts = newEventTissue1.split(";");

            for (int i = 0; i < evts.length; i++) {
                NewTumor newTumor = new NewTumor();
                try {
                    newTumor.setTime(Double.parseDouble(evtm[i]));
                } catch (Exception e) {
                }
                newTumor.setStatus(evts[i]);
                newTumor.setType(evtt[i]);
                newTumor.setSite(evtts[i]);
                if (css != null) newTumor.setCancerSatus(css[i]);
                newTumors.add(newTumor);
            }
        }

        if (newEventTime1 != null && newEventType1 != null) {
            String evts[] = newEventType1.split(";");
            String evtm[] = newEventTime1.split(";");
            String evtt[] = new String[evtm.length];
            String evtts[] = new String[evtm.length];
            if (newEventTissueType2 != null) {
                evtt = newEventTissueType2.split(";");
            }
            if (newEventTissue2 != null) evtts = newEventTissue2.split(";");

            for (int i = 0; i < evts.length; i++) {
                NewTumor newTumor = new NewTumor();
                try {
                    newTumor.setTime(Double.parseDouble(evtm[i]));
                } catch (Exception e) {
                }
                newTumor.setStatus(evts[i]);
                newTumor.setType(evtt[i]);
                newTumor.setSite(evtts[i]);
                if (css != null) newTumor.setCancerSatus(css[i]);
                newTumors.add(newTumor);
            }
        }

        if (newEventTime2 != null && newEventType2 != null) {
            String evts[] = newEventType2.split(";");
            String evtm[] = newEventTime2.split(";");
            String evtt[] = new String[evtm.length];
            String evtts[] = new String[evtm.length];
            if (newEventTissueType3 != null) {
                evtt = newEventTissueType3.split(";");
            } else if (newEventTissueType6 != null) evtt = newEventTissueType6.split(";");
            if (newEventTissue3 != null) evtts = newEventTissue3.split(";");

            for (int i = 0; i < evts.length; i++) {
                NewTumor newTumor = new NewTumor();
                try {
                    newTumor.setTime(Double.parseDouble(evtm[i]));
                } catch (Exception e) {
                    newTumor.setTime(Double.NaN);
                }
                newTumor.setStatus(evts[i]);
                newTumor.setType(evtt[i]);
                newTumor.setSite(evtts[i]);
                if (css != null) newTumor.setCancerSatus(css[i]);
                newTumors.add(newTumor);
            }
        }

        if (newEventTime3 != null && newEventType3 != null) {
            String evts[] = newEventType3.split(";");
            String evtm[] = newEventTime3.split(";");
            String evtt[] = new String[evtm.length];
            String evtts[] = new String[evtm.length];
            if (newEventTissueType4 != null) {
                evtt = newEventTissueType4.split(";");
            } else if (newEventTissueType7 != null) evtt = newEventTissueType7.split(";");
            if (newEventTissue4 != null) evtts = newEventTissue4.split(";");
            else if (newEventTissue5 != null) evtts = newEventTissue5.split(";");

            for (int i = 0; i < evts.length; i++) {
                NewTumor newTumor = new NewTumor();
                try {
                    newTumor.setTime(Double.parseDouble(evtm[i]));
                } catch (Exception e) {
                    newTumor.setTime(Double.NaN);
                }
                newTumor.setStatus(evts[i]);
                newTumor.setType(evtt[i]);
                newTumor.setSite(evtts[i]);
                newTumors.add(newTumor);
            }
        }
        return newTumors;
    }

    public void addEventMap(Map<String, String> map) {
        List<NewTumor> newTumors = checkAddEvent(map);
        String cancerStatus2 = map.get(".person_neoplasm_cancer_status");
        map.put(".A9_Cancer_Status", cancerStatus2);
        List<NewTumor> newTumors1 = new ArrayList<>();
        double min = Double.MAX_VALUE;
        for (NewTumor nt : newTumors) {
            int pfs = nt.getPFS();
            if (pfs > 0) {
                newTumors1.add(nt);
                if (!Double.isNaN(nt.getTime()) && nt.getTime() < min && nt.getTime() > 0) {
                    min = nt.getTime();
                }
            }
        }
        if (newTumors1.size() == 0) {
            map.put(".A8_New_Event_Type", "");
            map.put(".A8_New_Event_Tissue", "");
            map.put(".A8_New_Event", "0");
            map.put(".A8_New_Event_Time", map.get(".A1_OS"));
        } else if (min == Double.MAX_VALUE) {
            map.put(".A8_New_Event", "1");
            map.put(".A8_New_Event_Time", "");
            for (NewTumor nt : newTumors1) {
                String site = nt.getSite();
                if (site != null && !site.equals("Not Applicable") && !site.equals("Not Available") && !site.equals("Unknown") && !site.equals("None")) {
                    map.put(".A8_New_Event_Tissue", site);
                }
                String type = nt.getType();
                if (type != null && !type.equals("Not Applicable") && !type.equals("Not Available") && !type.equals("Unknown") && !type.equals("None") && !type.equals("No New Tumor Event")) {
                    map.put(".A8_New_Event_Type", type);
                }
            }
        } else {
//			System.out.println(min);
            for (NewTumor nt : newTumors1) {
                map.put(".A8_New_Event", "1");
                map.put(".A8_New_Event_Time", min + "");
                if (nt.getTime() == min) {
                    String site = nt.getSite();
                    if (site != null && !site.equals("Not Applicable") && !site.equals("Not Available") && !site.equals("Unknown") && !site.equals("None")) {
                        map.put(".A8_New_Event_Tissue", site);
                    }
                    String type = nt.getType();
                    if (type != null && !type.equals("Not Applicable") && !type.equals("Not Available") && !type.equals("Unknown") && !type.equals("None") && !type.equals("No New Tumor Event")) {
                        map.put(".A8_New_Event_Type", type);
                    }
                }
            }
        }
    }

    public void addMap(Map<String, String> map) {
        String bcr = map.get(".bcr_patient_barcode");
        if (bcr != null) map.put(".A0_Samples", bcr);
        String death = map.get(".follow_ups.follow_up.vital_status");
        String time1 = map.get(".follow_ups.follow_up.days_to_death");
        String time2 = map.get(".follow_ups.follow_up.days_to_last_followup");
        String last = map.get(".days_to_last_followup");
        String lastD = map.get(".days_to_death");
        String lastS = map.get(".vital_status");


        int lt1 = -1;
        int lt2 = -1;
        int max = -1;
        String status = "";
//		System.out.println(lastS);
        if (lastS.toLowerCase().equals("alive") || lastS.toLowerCase().equals("dead")) {
            try {
                lt1 = Integer.parseInt(last);
            } catch (Exception e) {
            }
            try {
                lt2 = Integer.parseInt(lastD);
            } catch (Exception e) {
            }
//			System.out.println(lt1+"==="+lt2);
            if (lt1 > lt2) {
                max = lt1;
                status = lastS;
            } else {
                max = lt2;
                status = lastS;
            }
//			System.out.println(status);
        }
//		System.out.println(death);
        if (death != null) {
            String[] deaths = death.split(";");
            String[] times1 = time1.split(";");
            String[] times2 = time2.split(";");

            for (int i = 0; i < deaths.length; i++) {
                if (deaths[i].toLowerCase().equals("alive") || deaths[i].toLowerCase().equals("dead")) {
                    int t1 = -1;
                    int t2 = -1;
                    try {
                        t1 = Integer.parseInt(times1[i]);
                    } catch (Exception e) {
                    }
                    try {
                        t2 = Integer.parseInt(times2[i]);
                    } catch (Exception e) {
                    }
                    if (t1 > t2 && t1 > max) {
                        max = t1;
                        status = deaths[i];
                    } else if (t2 > t1 && t2 > max) {
                        max = t2;
                        status = deaths[i];
                    }
                }
            }
        }
//		System.out.println("====="+status+"==="+max);
        if (max > -1) {
            map.put(".A2_Event", status);
            map.put(".A1_OS", max + "");
        }

        String m1 = map.get(".stage_event.tnm_categories.clinical_categories.clinical_M");
        String m2 = map.get(".stage_event.tnm_categories.pathologic_categories.pathologic_M");
        if (m1 != null && m1.startsWith("M")) map.put(".A5_M", m1);
        else if (m2 != null && m2.startsWith("M")) map.put(".A5_M", m2);

        String t1 = map.get(".stage_event.tnm_categories.clinical_categories.clinical_T");
        String t2 = map.get(".stage_event.tnm_categories.pathologic_categories.pathologic_T");
        if (t1 != null && t1.startsWith("T")) map.put(".A3_T", t1);
        else if (t2 != null && t2.startsWith("T")) map.put(".A3_T", t2);

        String n1 = map.get(".stage_event.tnm_categories.clinical_categories.clinical_N");
        String n2 = map.get(".stage_event.tnm_categories.pathologic_categories.pathologic_N");
        if (n1 != null && n1.startsWith("N") && !n1.startsWith("Not")) map.put(".A4_N", n1);
        else if (n2 != null && n2.startsWith("N") && !n2.startsWith("Not")) map.put(".A4_N", n2);

        String s1 = map.get(".stage_event.pathologic_stage");
        String s2 = map.get(".stage_event.clinical_stage");
        if (s1 != null && s1.startsWith("Stage")) map.put(".A6_Stage", s1);
        else if (s2 != null && s2.startsWith("Stage")) map.put(".A6_Stage", s2);

        String grade = map.get(".neoplasm_histologic_grade");
        if (grade != null) map.put(".A7_Grade", grade);

        try {
            addEventMap(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            addTumorSize(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            addAgeHeightWeightBMI(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> parseClinicalXML(String path) throws TransformerException, SAXException, IOException, ParserConfigurationException {
//		String path="C:/Users/biocc/Desktop/TCGA/ationwidechildrens.org_clinical.TCGA-02-0001.xml";
        InputSource is = new InputSource(new FileReader(path));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(is);
        //XML转字符串
        NodeList nls = doc.getChildNodes().item(0).getChildNodes();
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < nls.getLength(); i++) {
//			System.out.println(nls.item(i).getNodeName());
            if (nls.item(i).getNodeName().endsWith(":patient")) {
//				System.out.println(nls.item(i).getNodeName());
                for (int j = 0; j < nls.item(i).getChildNodes().getLength(); j++) {
//					System.out.println(nls.item(i).getChildNodes().item(j).getNodeName());
                    if (nls.item(i).getChildNodes().item(j).getNodeName().endsWith(":follow_ups")) {
                        follow_ups(nls.item(i).getChildNodes().item(j), map);
                    } else if (nls.item(i).getChildNodes().item(j).getNodeName().endsWith(":drugs")) {
                        flows(nls.item(i).getChildNodes().item(j), map, "drugs");
                    } else if (nls.item(i).getChildNodes().item(j).getNodeName().endsWith(":radiations")) {
                        flows(nls.item(i).getChildNodes().item(j), map, "radiations");
                    } else {
                        moveChange(nls.item(i).getChildNodes().item(j), "", map);
                    }
//					System.out.println("++"+nls.item(i).getChildNodes().item(j).getTextContent().trim()+"++");
                }
            }
        }
//		System.out.println(map.size());
//		String xmlStr1 = bos.toString();
//		System.out.println(xmlStr1);
        try {
            addMap(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
//		for(String k:map.keySet()){
//			System.out.println(k+"\t"+map.get(k));
//		}
        return map;
    }

    public void mergeIsoform(List<Map<String, Float>> list, List<String> header, String outPath) throws IOException {
        Set<String> keys = new HashSet<>();
        for (Map<String, Float> m : list) {
            keys.addAll(m.keySet());
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(outPath));
        bw.write("mirbase21_ID");
        for (String h : header) {
            bw.write("\t" + h);
        }
        bw.newLine();

        for (String k : keys) {
            bw.write(k);
            for (int i = 0; i < header.size(); i++) {
                Map<String, Float> map = list.get(i);
                Float fl = map.get(k);
                if (fl == null) bw.write("\t0");
                else bw.write("\t" + fl);
            }
            bw.newLine();
        }
        bw.close();
    }

    public void mergeIsoformCount(List<Map<String, Integer>> list, List<String> header, String outPath) throws IOException {
        Set<String> keys = new HashSet<>();
        for (Map<String, Integer> m : list) {
            keys.addAll(m.keySet());
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(outPath));
        bw.write("mirbase21_ID");
        for (String h : header) {
            bw.write("\t" + h);
        }
        bw.newLine();

        for (String k : keys) {
            bw.write(k);
            for (int i = 0; i < header.size(); i++) {
                Map<String, Integer> map = list.get(i);
                Integer fl = map.get(k);
                if (fl == null) bw.write("\t0");
                else bw.write("\t" + fl);
            }
            bw.newLine();
        }
        bw.close();
    }

    public boolean cbindClinical(String folder, String outfileName) throws TransformerException, SAXException, IOException, ParserConfigurationException {
        List<String[]> list = getFileInfo(folder + "/MANIFEST.txt");
        List<Map<String, String>> list2 = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            File file = new File(folder + "/" + list.get(i)[1]);
            if (file.exists() && file.isFile()) {
                Map<String, String> map = parseClinicalXML(folder + "/" + list.get(i)[1]);
                list2.add(map);
                keys.addAll(map.keySet());
            }
        }
        List<String> list3 = new ArrayList<>();
        list3.addAll(keys);
        Collections.sort(list3);
        BufferedWriter bw = new BufferedWriter(new FileWriter(folder + "/" + outfileName));
        if (list3.size() > 0) {
            bw.write(list3.get(0).substring(1));
            for (int i = 1; i < list3.size(); i++) {
                bw.write("\t" + list3.get(i).substring(1));
            }
            bw.newLine();
            for (int i = 0; i < list2.size(); i++) {
                Map<String, String> map = list2.get(i);
                bw.write((map.get(list3.get(0)) == null ? "" : map.get(list3.get(0))));
                for (int j = 1; j < list3.size(); j++) {
                    bw.write("\t" + (map.get(list3.get(j)) == null ? "" : map.get(list3.get(j))));
                }
                bw.newLine();
            }
            bw.close();
            return true;
        }
        return false;
    }

    private boolean cbindRNASeqSimple(String[] strs, String folder, String outfile, boolean header, int col, int i, Set<String> set, boolean st) throws IOException {
        BufferedReader br = null;
        BufferedReader br1 = null;
        if (!st) br1 = new BufferedReader(new FileReader(folder + "/" + outfile + ".bak"));
        System.out.println(JSONArray.fromObject(strs));
        File file = new File(folder + "/" + strs[1]);
        BufferedWriter bw = new BufferedWriter(new FileWriter(folder + "/" + outfile, !st));

        if (file.exists() && file.isFile()) {
            if (strs[1].endsWith(".gz")) {
                InputStream in = new GZIPInputStream(new FileInputStream(folder + "/" + strs[1]));
                br = new BufferedReader(new InputStreamReader(in, "utf-8"));
            } else {
                br = new BufferedReader(new FileReader(folder + "/" + strs[1]));
            }
            if (header) br.readLine();
            String co = strs[0];
            if (strs.length > 6) {
                co = strs[5] + "-" + strs[6];
            }
            if (set.contains(co)) co += "_Rep" + i;
            else set.add(co);
            if (st) bw.write("Tags\t" + co);
            else bw.write(br1.readLine() + "\t" + co);
        }
        bw.newLine();
        String line = br.readLine();
        while (line != null) {
            if (st) {
                String[] strs1 = line.split("\t");
                bw.write(strs1[0] + "\t" + strs1[col]);
            } else bw.write(br1.readLine() + "\t" + line.split("\t")[col]);
            bw.newLine();
            line = br.readLine();
        }
        br.close();
        if (!st) br1.close();
        bw.close();
        return true;
    }

    private boolean cbindRNASeq(String folder, String outfile, boolean header, int col) throws FileNotFoundException, IOException {
        Set<String> set = new HashSet<>();
        File f0 = new File(folder + "/" + outfile);
        System.out.println(f0.delete());
        List<String[]> list = getFileInfo(folder + "/MANIFEST.txt");
        for (int i = 0; i < list.size(); i++) {
            File f = new File(folder + "/" + outfile);
            File f1 = new File(folder + "/" + outfile + ".bak");
            if (f1.exists() & f1.isFile()) {
                f1.delete();
            }
            if (f.exists() & f.isFile()) {
                f.renameTo(f1);
            }
            cbindRNASeqSimple(list.get(i), folder, outfile, header, col, i, set, i == 0);
        }
        return true;
    }

    public Map<String, Float> getIsoformData(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        br.readLine();
        String str = br.readLine();
        Map<String, Float> map = new HashMap<>();
        while (str != null) {
            String[] strs = str.split("\t");
            if (strs[5].startsWith("mature,")) {
                try {
                    String k = strs[5].substring(strs[5].indexOf(",") + 1);
                    Float fls = map.get(k);
                    if (fls == null) fls = 0.0f;
                    Float f2 = Float.parseFloat(strs[3]);
                    map.put(k, f2 + fls);
                } catch (Exception e) {
                }
            }
            str = br.readLine();
        }
        br.close();
        return map;
    }

    private boolean cbindRNASeqIsoForm(String folder, String outfile) throws FileNotFoundException, IOException {
        Set<String> set = new HashSet<>();
        File f0 = new File(folder + "/" + outfile);
        System.out.println(f0.delete());
        List<Map<String, Float>> listMap = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        List<String[]> list = getFileInfo(folder + "/MANIFEST.txt");
        for (int i = 0; i < list.size(); i++) {
            Map<String, Float> map = getIsoformData(folder + "/" + list.get(i)[1]);
            listMap.add(map);
            String co = list.get(i)[0];
            if (list.get(i).length > 6) {
                co = list.get(i)[5] + "-" + list.get(i)[6];
            }
            if (set.contains(co)) co += "_Rep" + i;
            else set.add(co);
            list2.add(co);
        }
        mergeIsoform(listMap, list2, folder + "/" + outfile);
        return true;
    }

    public Map<String, Integer> getIsoformDataCount(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        br.readLine();
        String str = br.readLine();
        Map<String, Integer> map = new HashMap<>();
        while (str != null) {
            String[] strs = str.split("\t");
            if (strs[5].startsWith("mature,")) {
                try {
                    String k = strs[5].substring(strs[5].indexOf(",") + 1);
                    Integer fls = map.get(k);
                    if (fls == null) fls = 0;
                    Integer f2 = Integer.parseInt(strs[2]);
                    map.put(k, f2 + fls);
                } catch (Exception e) {
                }
            }
            str = br.readLine();
        }
        br.close();
        return map;
    }

    private boolean cbindRNASeqIsoFormCount(String folder, String outfile) throws FileNotFoundException, IOException {
        Set<String> set = new HashSet<>();
        File f0 = new File(folder + "/" + outfile);
        System.out.println(f0.delete());
        List<Map<String, Integer>> listMap = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        List<String[]> list = getFileInfo(folder + "/MANIFEST.txt");
        for (int i = 0; i < list.size(); i++) {
            String co = list.get(i)[0];
            if (list.get(i).length > 6) {
                co = list.get(i)[5] + "-" + list.get(i)[6];
            }
            if (!set.contains(co)) {
                Map<String, Integer> map = getIsoformDataCount(folder + "/" + list.get(i)[1]);
                listMap.add(map);
                set.add(co);
                list2.add(co);
            }
        }
        mergeIsoformCount(listMap, list2, folder + "/" + outfile);
        return true;
    }

    private boolean cbindCNV(String folder, String outfile) throws FileNotFoundException, IOException {
        Set<String> set = new HashSet<>();
        File f0 = new File(folder + "/" + outfile);
        BufferedWriter bw = new BufferedWriter(new FileWriter(f0));
        bw.write("Sample\tChromosome\tStart\tEnd\tNum_Probes\tSegment_Mean\n");
        List<String[]> list = getFileInfo(folder + "/MANIFEST.txt");
        for (int i = 0; i < list.size(); i++) {
            String barcode = list.get(i)[0];
            if (list.get(i).length > 6) {
                barcode = list.get(i)[5] + "-" + list.get(i)[6];
            }
            if (!set.contains(barcode)) {
                BufferedReader br = new BufferedReader(new FileReader(folder + "/" + list.get(i)[1]));
                br.readLine();
                String line = br.readLine();
                while (line != null) {

                    String[] cols = line.split("\t");

//ENSG00000223972.5	DDX11L1	chr1	11869	14409
// 1ebb68cc-f0f0-4195-ac48-4d4a72ccb3c9	X	6418536	6419869	3	-1.4811
                    if(cols.length==5){
                        bw.write(barcode + "\t" + cols[1] + "\t" + cols[2] + "\t" + cols[3] + "\t" + cols[4] + "\n");
                    }else {
                        bw.write(barcode + "\t" + cols[1] + "\t" + cols[2] + "\t" + cols[3] + "\t" + cols[4] + "\t" + cols[5] + "\n");
                    }
                    line = br.readLine();
                }
                br.close();
                set.add(barcode);
            }
        }
        bw.close();
        return true;
    }

    public boolean mergeClinical(String folder, String outfileName) throws TransformerException, SAXException, IOException, ParserConfigurationException {
        return cbindClinical(folder, outfileName);
    }

    public boolean mergeRNASeqCountFPKM(String folder, String outfileName) throws TransformerException, SAXException, IOException, ParserConfigurationException {
        return cbindRNASeq(folder, outfileName, false, 1);
    }

    public boolean mergemiRNASeq(String folder, String outfileName) throws TransformerException, SAXException, IOException, ParserConfigurationException {
        cbindRNASeq(folder, outfileName + "_FPKM.txt", true, 2);
        cbindRNASeq(folder, outfileName + "_Count.txt", true, 1);
        return true;
    }

    public boolean mergemiRNASeqIsoform(String folder, String outfileName) throws TransformerException, SAXException, IOException, ParserConfigurationException {
        cbindRNASeqIsoForm(folder, outfileName + "_FPKM.txt");
        cbindRNASeqIsoFormCount(folder, outfileName + "_Count.txt");
        return true;
    }

    public boolean mergeMethylation(String folder, String outfileName) throws TransformerException, SAXException, IOException, ParserConfigurationException {
        return cbindRNASeq(folder, outfileName, true, 1);
    }

    public boolean mergeCNV(String folder, String outfileName) throws TransformerException, SAXException, IOException, ParserConfigurationException {
        return cbindCNV(folder, outfileName);
    }

    public static Boolean startMergeFile(String filePath, String fielName, String type) {
        try {
            MergeFileUtil mege = new MergeFileUtil();
            if ("Clinical".equals(type)) {
                mege.mergeClinical(filePath, fielName);
            } else if ("FPKM".equals(type) || "FPKM-UQ".equals(type) || "Counts".equals(type)) {
                mege.mergeRNASeqCountFPKM(filePath, fielName);
            } else if ("前体miRNA".equals(type)) {
                mege.mergemiRNASeq(filePath, fielName);
            } else if ("成熟体miRNA".equals(type)) {
                mege.mergemiRNASeqIsoform(filePath, fielName);
            } else if ("450k".equals(type) || "27k".equals(type)) {
                mege.mergeMethylation(filePath, fielName);
            } else if ("CNV".equals(type) || "MaskCNV".equals(type) || type.contains("CNV")) {
                mege.mergeCNV(filePath, fielName);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) throws TransformerException, SAXException, IOException, ParserConfigurationException {
        // TODO Auto-generated method stub

        //API为VarScan2、MuSE、MuTect2、SomaticSniper、Other、Biospecimen无需合并
        MergeFileUtil mege = new MergeFileUtil();
        String folder = "";//文件目录，根据目录下MANIFEST.txt文件进行提取文件内容进行合并；
        String outfileName = "All_File_Merge.txt";//文件合并后输出的文件名称；
        mege.mergeClinical(folder, outfileName);//对应的API为Clinical的数据结果用这个合并
        mege.mergeRNASeqCountFPKM(folder, outfileName);//对应API为FPKM、FPKM-UQ、Counts
        mege.mergemiRNASeq(folder, outfileName);//对应API为 前体miRNA
        mege.mergemiRNASeqIsoform(folder, outfileName);//对应API为 成熟体miRNA
        mege.mergeMethylation(folder, outfileName);//对应API为450k、27k
        mege.mergeCNV(folder, outfileName);//对应API为CNV、MaskCNV

    }

}
