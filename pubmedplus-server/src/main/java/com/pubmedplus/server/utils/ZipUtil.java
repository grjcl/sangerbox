package com.pubmedplus.server.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;		

public class ZipUtil {

	public static Boolean zipFileIsCEL(String filePath) {
		TarArchiveInputStream tarIn = null;
		TarArchiveEntry entry = null;
		BufferedReader br = null;
		try {
			tarIn = new TarArchiveInputStream(new FileInputStream(filePath));
			while ((entry = tarIn.getNextTarEntry()) != null) {
				if(Pattern.compile(".*?CEL.*?").matcher(entry.getName()).find()) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (tarIn != null) {
				IOUtils.closeQuietly(tarIn);
			}
		}
		return false;
	}
	
	public static HashMap<String, HashMap<String, String>> unpackFileTgz(String fileTgzPath, ArrayList<String> filePlatformRefList) {
		fileTgzPath = "C:/Users/zyx/Desktop/GSE100003_RAW.tar";
		var tgzContentMap = new HashMap<String, HashMap<String, String>>();
		TarArchiveInputStream tarIn = null;
		TarArchiveEntry entry = null;
		BufferedReader br = null;
		String line = null;
		HashMap<String, String> textMap = null;
		try {
			tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(fileTgzPath)));
			while ((entry = tarIn.getNextTarEntry()) != null) {
				if(!filePlatformRefList.contains(entry.getName())) {
					continue;
				}
				textMap = new HashMap<>();
				br = new BufferedReader(new InputStreamReader(tarIn, Charset.forName("UTF-8")));
				while ((line = br.readLine()) != null) {
					textMap.put(line.split("\t")[0], line);
				}
				tgzContentMap.put(entry.getName(), textMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (tarIn != null) {
				IOUtils.closeQuietly(tarIn);
			}
		}
		return tgzContentMap;
	}
	
	public static byte[] tarGz(Map<String, byte[]> sourceData) throws Exception{
		var byteArrayOutputStream = new ByteArrayOutputStream();
		var bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
		var zipOutputStream = new ZipOutputStream(bufferedOutputStream);
		for (Entry<String, byte[]> entry : sourceData.entrySet()) {
			ZipEntry zipEntry = new ZipEntry(entry.getKey());
			zipOutputStream.putNextEntry(zipEntry);
			zipOutputStream.write(entry.getValue());
			zipOutputStream.flush();
		}
		zipOutputStream.close();
		bufferedOutputStream.close();
		byteArrayOutputStream.close();
		return byteArrayOutputStream.toByteArray();
	}
	
}
