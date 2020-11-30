package com.pubmedplus.server.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;



/**
 * @Author : zp
 * @Description :
 * @Date : 2020/1/16
 */
public class GeneImgUtil {

    private static Pattern PATTERN = Pattern.compile("\\((.*?,.*?)\\)");
    private static final String KEGG_WEB = "http://www.kegg.jp";
    private static final String IMG_PATH = "/pub1/data/public_database/KEGG/img/";
    private static final String CONF_PATH = "/pub1/data/public_database/KEGG/conf/";

    /**
     *
     * @param path 图片读取地址
     * @param links 根据基因id获取的link
     * @param map 读取文件生成map
     * @param fileout 图片输出路径
     * @return
     * @throws IOException
     */
	public static String getImg(String pathWayId, List<String> links) throws IOException {
		Map<String, String> map = read(CONF_PATH + pathWayId);
		BufferedImage image = ImageIO.read(new File(IMG_PATH + pathWayId));
		Graphics g = image.getGraphics();
		g.setColor(Color.red);
		// todo 查数据库获得link
		for (String str : links) {
			String line = map.get(str);
			List<String> list = extractMessageByRegular(line);
			if (list.size() == 2) {
				String[] s1 = list.get(0).split(",");
				String[] s2 = list.get(1).split(",");
				int width = Integer.valueOf(s2[0]) - Integer.valueOf(s1[0]);
				int height = Integer.valueOf(s2[1]) - Integer.valueOf(s1[1]);
				g.drawRect(Integer.valueOf(s1[0]), Integer.valueOf(s1[1]), width, height);
			}
		}
		g.dispose();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "png", baos);
		byte[] bytes = baos.toByteArray();
		return new Base64().encodeToString(bytes);
	}

    //读取
    public static Map<String,String> read(String path) throws IOException {
        BufferedReader bf = new BufferedReader(new FileReader(new File(path)));
        Map<String,String> map = new HashMap<>();
        String str;
        while (null!=(str=bf.readLine())){
            String[] split = str.split("\t");
            if (split.length>1) {
                map.put(KEGG_WEB + split[1], split[0]);
            }
        }
        bf.close();
        return map;
    }

    private static List<String> extractMessageByRegular(String msg){
        List<String> list=new ArrayList<>();
        Matcher m = PATTERN.matcher(msg);
        while(m.find()){
            list.add(m.group().substring(1, m.group().length()-1));
        }
        return list;
    }
}
