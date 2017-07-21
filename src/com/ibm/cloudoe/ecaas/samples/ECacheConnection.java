package com.ibm.cloudoe.ecaas.samples;

//import java.io.IOException;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.sync.RedisCommands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

/**
 * Define the elastic caching Operation, mainly in order to program operation.
 * 
 * You can refer to the Elastic Caching Java Native API Specifition
 * http://pic.dhe.ibm.com/infocenter/wdpxc/v2r5/index.jsp?topc=%2Fcom.ibm.websphere.datapower.xc.doc%2Fcxslibertyfeats.html
 */
public class ECacheConnection {

	private static RedisClient redisClient;
	private static RedisCommands<String, String> syncCommands; 

	private static List<String> keys;

	static {
		initECaaS();
	}

	/**
	 * Initialize the session instance of ObjectGrid.
	 */
	public static void initECaaS() {

		String uri = null;
		//String username = null;
		String password = null;
		String host = null;
		int port = 0;

		Map<String, String> env = System.getenv();
		String vcap = env.get("VCAP_SERVICES");

		boolean foundService = false;
		if (vcap == null) {
			System.out.println("No VCAP_SERVICES found");
		} else {
			try {
				// parse the VCAP JSON structure
				JSONObject obj = JSONObject.parse(vcap);
				for (Iterator<?> iter = obj.keySet().iterator(); iter.hasNext();) {
					String key = (String) iter.next();
					System.out.printf("Found service: %s\n", key);
					if (key.contains("compose-for-redis")) {
						JSONArray val = (JSONArray)obj.get(key)!=null?(JSONArray)obj.get(key):null;
						if(val!=null){
							JSONObject serviceAttr = val.get(0)!=null?(JSONObject)val.get(0):null;
							JSONObject credentials = serviceAttr!=null?(serviceAttr.get("credentials")!=null?(JSONObject)serviceAttr.get("credentials"):null):null;
							uri = credentials.get("uri") !=null?(String) credentials.get("uri"):"";
							System.out.println("Found configured uri: " + uri);
							foundService = true;
							int ustart = uri.indexOf("://")+3;
							//username = uri.substring(ustart,uri.indexOf(":",ustart));
							password = uri.substring(uri.indexOf(":",ustart)+1,uri.indexOf("@"));
							host  = uri.substring(uri.indexOf("@")+1,uri.indexOf(":",uri.indexOf("@")));
							port = Integer.parseInt(uri.substring(uri.indexOf(":",uri.indexOf("@"))+1));

							try {
								redisClient = RedisClient.create("redis://"+password+"@"+host+":"+port+"/0");
								StatefulRedisConnection<String, String> connection = redisClient.connect();
								syncCommands = connection.sync();

							} catch (Exception e) {
								System.out.println("Failed to connect to grid!");
								e.printStackTrace();
							}		
							break;
						}
					}
				}
			} catch (Exception e) {
			}
		}
		if (!foundService) {
			System.out.println("Did not find compose for redis service, using defaults");
		}
	}

	/* @SuppressWarnings("unused")
	private static String getDataCacheServiceName(){
		String dataCacheName = "";
		Map<String, String> env = System.getenv();
		String vcap = env.get("VCAP_SERVICES");

		if (vcap == null) {
			System.out.println("No VCAP_SERVICES found");
		} else {
			try {
				// parse the VCAP JSON structure
				JSONObject obj = JSONObject.parse(vcap);
				for (Iterator<?> iter = obj.keySet().iterator(); iter.hasNext();) {
					String key = (String) iter.next();
					System.out.printf("Found service: %s\n", key);
					if (key.contains("redis")) {
						JSONArray val = (JSONArray)obj.get(key)!=null?(JSONArray)obj.get(key):null;
						System.out.println("Found configured val: " + val);
						if(val!=null){
							JSONObject serviceAttr = val.get(0)!=null?(JSONObject)val.get(0):null;
							dataCacheName = serviceAttr!=null?(serviceAttr.get("name")!=null?(String)serviceAttr.get("name"):null):null;
							System.out.println("Found configured Redis: " + dataCacheName);
							break;
						}
					}
				}
			} catch (Exception e) {
			}
		}
		return dataCacheName;
	}

	@SuppressWarnings("unused")
	private static String getAppName() {
		String app = System.getenv("VCAP_APPLICATION");
		if (app == null) {
			System.out.println("No VCAP_APPLICATION found");
		} else {
			try {
				JSONObject obj = JSONObject.parse(app);
				String name = (String) obj.get("application_name");
				if (name == null) {
					System.out.println("VCAP_APPLICATION application_name not set");
				} else {
					return name;
				}
			} catch (IOException e) {
				System.out.println("Failed to parse VCAP_APPLICATION for application_name");
			}
		}
		return null;
	} */

	public static Object getData(String mapName, String key)
			throws Exception {
		return syncCommands.get(key);
	}

	public static void postData(String mapName, String key, String newValue)
			throws Exception {
		syncCommands.set(key, newValue);
	}

	public static void deleteData(String mapName, String key)
			throws Exception {
		syncCommands.del(key);
	}

	public static List<ECache> getAllData(String mapName)
			throws Exception {
		keys = syncCommands.keys("*");
		List<ECache> res = new ArrayList<ECache>();
		for (int i = 0; i < keys.size(); i++) {
			res.add(new ECache(keys.get(i), syncCommands.get(keys.get(i))));
		}
		return res;
	}

}