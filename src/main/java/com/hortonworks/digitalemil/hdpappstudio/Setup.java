package com.hortonworks.digitalemil.hdpappstudio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.Properties;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class Setup {
	public static final String MARKER1 = "DO NOT REMOVE ME NEEDED FOR THE SOLRWORKSHOP ITEM1";
	public static final String MARKER2 = "DO NOT REMOVE ME NEEDED FOR THE SOLRWORKSHOP ITEM2";
	public static final String MARKER3 = "<MYMARKER1/>";
	public static final String MARKER4 = "/*MYMARKER2*/";
	public static final String MARKER5 = "bg.jpg";
	public static final String MARKER6 = "<MYTITLE>";
	public static final String MARKER7 = "MYMARKER7";
	public static final String APPSFOLDER = "apps";
	public static final String OTHERS = " indexed=\"true\" stored=\"true\" multiValued=\"false\"";

	public static void main(String[] args) throws IOException,
			InterruptedException {
		Properties props = new Properties();
		String appname = args[0];
		String path = APPSFOLDER + "/" + appname + "/";
		String currentDir = System.getProperty("user.dir");

		Runtime.getRuntime().exec("mkdir -p " + path + "jar/WEB-INF").waitFor();
		try {
			System.out.println("cuurent dir: "+currentDir+" "+"cp -fr com WEB-INF META-INF listdata.html map.html icon32.png icon64.png " + currentDir
					+ "/" + path + "jar");
			Runtime.getRuntime().exec(
					"cp -fr com WEB-INF META-INF  listdata.html map.html icon32.png icon64.png  " + currentDir
							+ "/" + path + "jar").waitFor();
			Runtime.getRuntime().exec(
					"rm -f " + currentDir
							+ "/" + path + "jar/WEB-INF/web.xml").waitFor();
			
			
		} catch (Exception e) {

		}
		Runtime.getRuntime()
				.exec("/usr/lib/jvm/java-1.7.0-openjdk.x86_64/bin/jar xvf ../../../target/HDPAppStudio-0.1.8-distribution.jar",
						new String[0],
						new File(currentDir + "/" + path + "jar")).waitFor();
		
		BufferedReader br;
		BufferedWriter bw;

		String line;

		try {
			InputStream is = new FileInputStream(args[1]);
			props.load(is);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		String solrcore = props.getProperty("solrcore");
		String showLocation = props.getProperty("showLocation");
		String hbasetable = props.getProperty("hbasetable");
		String topic = props.getProperty("topic");
		String hivetable = props.getProperty("hivetable");
		String pivotfield = props.getProperty("pivotfield");
		String fields = "\"id location ";

		String war = props.getProperty("createWAR");
		boolean createWAR = false;

		if ("true".equals(war)) {
			createWAR = true;
			Runtime.getRuntime()
					.exec("mkdir -p " + path + "war/WEB-INF/classes/com")
					.waitFor();
			try {
				System.out.println("cuurent dir: "+currentDir+" "+"cp -fr com WEB-INF META-INF listdata.html map.html icon32.png icon64.png " + currentDir
						+ "/" + path + "war");
				Runtime.getRuntime().exec(
						"cp -fr com WEB-INF META-INF  listdata.html map.html icon32.png icon64.png " + currentDir
								+ "/" + path + "jar").waitFor();
			} catch (Exception e) {

			}
			Runtime.getRuntime()
					.exec("/usr/lib/jvm/java-1.7.0-openjdk.x86_64/bin/jar xvf ../../../target/HDPAppStudio-0.1.8-distribution.jar",
							new String[0],
							new File(currentDir + "/" + path + "war"))
					.waitFor();
			Runtime.getRuntime()
					.exec("mv " + path + "war/com " + path
							+ "war/WEB-INF/classes/").waitFor();
		}

		if (topic == null) {
			topic = "default";
		}
		if (solrcore == null) {
			solrcore = "hdpcore";
		}
		if (hivetable == null) {
			hivetable = "HDPAppStudio";
		}
		if (hbasetable == null) {
			hbasetable = "mytab";
		}
		if (showLocation == null) {
			showLocation = "true";
		}
		if (pivotfield == null) {
			pivotfield = "";
		}
		String ddl = "\"Create External Table " + hivetable
				+ " (id String, location String, ";

		int i = 5;
		do {
			String name = props.getProperty("name_" + i);
			if (name == null)
				break;
			String type = props.getProperty("type_" + i);
			if (type == null)
				type = "String";
			if ("long".equals(type))
				type = "BigInt";
			if (i > 5)
				ddl = ddl + ", ";
			ddl = ddl + name + " " + type;
			fields = fields + name + " ";
			i++;
		} while (true);
		fields = fields + "\"";
		ddl = ddl
				+ ") ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' STORED AS TEXTFILE LOCATION '/user/guest/hdpappstudio/hive/"
				+ hivetable + "';\"";

		try {
			br = new BufferedReader(new InputStreamReader(props.getClass()
					.getResourceAsStream("/view.xml")));
			bw = new BufferedWriter(new FileWriter(path + "jar/view.xml"));

			while ((line = br.readLine()) != null) {
				if (line.contains("<name>HDPAppStudio</name>")) {
					line = "<name>" + appname + "</name>";
				}
				if (line.contains("<label>HDPAppStudio</label>")) {
					line = "<label>" + appname + "</label>";
				}
				bw.write(line + "\n");
			}
			br.close();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			br = new BufferedReader(new InputStreamReader(props.getClass()
					.getResourceAsStream("/schema.xml")));
			bw = new BufferedWriter(new FileWriter(path + "schema.xml"));

			while ((line = br.readLine()) != null) {
				if (line.contains(MARKER1)) {
					i = 0;
					do {
						String name = props.getProperty("name_" + i);
						String type = props.getProperty("type_" + i);
						String others = props.getProperty("other_" + i);

						if (name == null)
							break;
						if (type == null)
							type = "string";
						if (others == null)
							others = OTHERS;
						System.out.println("adding: " + name);
						bw.write("<field name=\"" + name + "\" type=\"" + type
								+ "\" " + others + "/>\n");
						i++;
					} while (true);
				} else {
					if (line.contains(MARKER2)) {
						i = 4;
						do {
							String name = props.getProperty("name_" + i);
							if (name == null)
								break;
							bw.write("<copyField source=\"" + name
									+ "\" dest=\"text\"/>\n");
							i++;
						} while (true);
					} else {
						bw.write(line + "\n");
					}
				}
			}
			br.close();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			br = new BufferedReader(new InputStreamReader(props.getClass()
					.getResourceAsStream("/srcweb.xml")));
			bw = new BufferedWriter(
					new FileWriter(path + "jar/WEB-INF/web.xml"));

			while ((line = br.readLine()) != null) {
				if (line.contains("TOPIC")) {
					line = "<param-value>" + topic + "</param-value>";
				}
				if (line.contains("HBASETABLE")) {
					line = "<param-value>" + hbasetable + "</param-value>";
				}
				if (line.contains("SOLRURL")) {
					line = "<param-value>" + "http://127.0.0.1:8983/solr/"
							+ solrcore + "</param-value>";
				}
				if (line.contains("PIVOTFIELD")) {
					line = "<param-value>" + pivotfield + "</param-value>";
				}
				bw.write(line + "\n");
			}
			br.close();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			br = new BufferedReader(new InputStreamReader(props.getClass()
					.getResourceAsStream("/srcindex.html")));
			bw = new BufferedWriter(new FileWriter(path + "jar/index.html"));

			while ((line = br.readLine()) != null) {
				if (line.contains("MYMARKER")) {
					if (showLocation.equals("false")) {
						line = "var showLocation= false;";
					} else {
						line = "var showLocation= true;";
					}
				}
				bw.write(line + "\n");
			}
			br.close();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			br = new BufferedReader(new InputStreamReader(props.getClass()
					.getResourceAsStream("/data.html")));
			bw = new BufferedWriter(new FileWriter(path + "jar/data.html"));

			while ((line = br.readLine()) != null) {

				if (line.contains(MARKER5)) {
					String imgpath = props.getProperty("bgimg");
					String imgname = imgpath;
					if (imgpath.contains("/")) {
						imgname = imgpath
								.substring(imgpath.lastIndexOf("/") + 1);
					}
					Runtime.getRuntime()
							.exec("cp " + imgpath + " " + path + "jar/bg.jpg")
							.waitFor();
					// line = line.replaceFirst(MARKER5, imgname);
				}
				if (line.contains(MARKER6)) {
					String title = props.getProperty("title");
					line = line.replaceFirst(MARKER6, title);
				}
				if (line.contains(MARKER7)) {
					line = line.replaceFirst(MARKER7, showLocation);
				}
				if (line.contains(MARKER3)) {
					i = 5;
					do {
						String name = props.getProperty("name_" + i);
						if (name == null)
							break;
						bw.write("<tr><td>" + name
								+ "</td><td><input type=\"text\" name=\""
								+ name + "\" id=\"" + name
								+ "\" size=\"40\"/></td></tr>");
						i++;
					} while (true);
				} else {
					if (line.contains(MARKER4)) {
						i = 5;
						bw.write("+'");
						do {
							String name = props.getProperty("name_" + i);
							if (name == null) {
								bw.write("'");
								break;
							}
							bw.write(", \"" + name
									+ "\":\"'+document.getElementById(\""
									+ name + "\").value+'\"");
							i++;
						} while (true);
					} else {
						bw.write(line + "\n");
					}
				}
			}
			br.close();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Runtime.getRuntime()
					.exec("cp -fr /opt/solr/solr/hdp/solr/hdp1 /opt/solr/solr/hdp/solr/"
							+ solrcore).waitFor();

			Runtime.getRuntime()
					.exec("cp " + path + "schema.xml /opt/solr/solr/" + "hdp"
							+ "/solr/" + solrcore + "/conf/schema.xml")
					.waitFor();

			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					"/opt/solr/solr/" + "hdp" + "/solr/" + solrcore
							+ "/conf/solrconfig.xml")));
			bw = new BufferedWriter(new FileWriter(path + "solrconfig.xml"));
			boolean inDirFac = false;
			while ((line = br.readLine()) != null) {
				if (line.contains("<lockType>")) {
					line = "<lockType>hdfs</lockType>";
				}
				if (line.contains("<directoryFactory")) {
					inDirFac = true;
				}
				if (line.contains("</directoryFactory>")) {
					inDirFac = false;
					bw.write("<directoryFactory name=\"DirectoryFactory\" class=\"solr.HdfsDirectoryFactory\">\n"
							+ "<str name=\"solr.hdfs.home\">hdfs://sandbox:8020/user/solr</str>\n"
							+ "<bool name=\"solr.hdfs.blockcache.enabled\">true</bool>\n"
							+ "<int name=\"solr.hdfs.blockcache.slab.count\">1</int>\n"
							+ "<bool name=\"solr.hdfs.blockcache.direct.memory.allocation\">true</bool>\n"
							+ "<int name=\"solr.hdfs.blockcache.blocksperbank\">16384</int>\n"
							+ "<bool name=\"solr.hdfs.blockcache.read.enabled\">true</bool>\n"
							+ "<bool name=\"solr.hdfs.blockcache.write.enabled\">true</bool>\n"
							+ "<bool name=\"solr.hdfs.nrtcachingdirectory.enable\">true</bool>\n"
							+ "<int name=\"solr.hdfs.nrtcachingdirectory.maxmergesizemb\">16</int>\n"
							+ "<int name=\"solr.hdfs.nrtcachingdirectory.maxcachedmb\">192</int>\n");
				}
				if (inDirFac) {
					line = "";
				}
				bw.write(line + "\n");
			}
			br.close();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Runtime.getRuntime()
				.exec("cp " + path + "solrconfig.xml /opt/solr/solr/" + "hdp"
						+ "/solr/" + solrcore + "/conf/solrconfig.xml")
				.waitFor();

		try {
			bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("/opt/solr/solr/" + "hdp" + "/solr/"
							+ solrcore + "/core.properties")));
			bw.write("name=" + solrcore + "\n");
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out
				.println("Creating Ambari-View jar: "
						+ "/usr/lib/jvm/java-1.7.0-openjdk.x86_64/bin/jar cf /var/lib/ambari-server/resources/views/"
						+ appname + ".jar -C " + currentDir + "/" + path
						+ "jar/ .");
		Runtime.getRuntime()
				.exec("/usr/lib/jvm/java-1.7.0-openjdk.x86_64/bin/jar cf /var/lib/ambari-server/resources/views/"
						+ appname
						+ ".jar -C "
						+ currentDir
						+ "/"
						+ path
						+ "jar/ .").waitFor();

		try {
			br = new BufferedReader(new InputStreamReader(props.getClass()
					.getResourceAsStream("/start.sh")));
			bw = new BufferedWriter(new FileWriter(path + "start.sh"));

			while ((line = br.readLine()) != null) {
				if (line.contains("export APPNAME")) {
					line = "export APPNAME=" + appname;
				}
				if (line.contains("export HBASETABLE")) {
					line = "export HBASETABLE=" + hbasetable;
				}
				if (line.contains("export SOLRCORE")) {
					line = "export SOLRCORE=" + solrcore;
				}
				if (line.contains("export TOPIC")) {
					line = "export TOPIC=" + topic;
				}
				if (line.contains("export FIELDS")) {
					line = "export FIELDS=" + fields;
				}
				if (line.contains("export DDL")) {
					line = "export DDL=" + ddl;
				}
				if (line.contains("export HIVETABLE")) {
					line = "export HIVETABLE=" + hivetable;
				}
				bw.write(line + "\n");
			}
			br.close();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (createWAR) {
			Runtime.getRuntime().exec("cp " + path + "jar/* " + path + "war/")
					.waitFor();
			Runtime.getRuntime()
					.exec("cp " + path + "jar/index.html " + path
							+ "war/index.html").waitFor();
			Runtime.getRuntime()
					.exec("cp " + path + "jar/WEB-INF/web.xml " + path
							+ "war/WEB-INF/web.xml").waitFor();
			Runtime.getRuntime()
					.exec("cp " + path + "jar/bg.jpg " + path + "war/bg.jpg")
					.waitFor();

			System.out.println("Creating WAR: "
					+ "/usr/lib/jvm/java-1.7.0-openjdk.x86_64/bin/jar cf "
					+ path + appname + ".war -C " + currentDir + "/" + path
					+ "war/ .");
			Runtime.getRuntime()
					.exec("/usr/lib/jvm/java-1.7.0-openjdk.x86_64/bin/jar cf "
							+ path + appname + ".war -C " + currentDir + "/"
							+ path + "war/ .").waitFor();
		}

		System.out.println("Please find your app in " + path);
		System.out.println("Start it: sh ./" + path + "start.sh");
	}

}

// storm jar HDPAppStudioStormTopology-0.1.1-distribution.jar
// com.hortonworks.digitalemil.hdpappstudio.storm.Topology HDPAppStudio
// 127.0.0.1:2181 http://127.0.0.1:8983/solr/hdp/update/json?commit=true mytab
// all default id location foo bar