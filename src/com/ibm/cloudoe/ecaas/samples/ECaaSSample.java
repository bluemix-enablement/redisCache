package com.ibm.cloudoe.ecaas.samples;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lombardisoftware.core.xml.WPSDataValue.Base64;


/**
 * Servlet implementation class ECaaSSample
 * A ElasticCaching Java Native APIs Sample application.
 * <p>
 * This sample application demonstrates how to use ElasticCaching Java Native
 * APIs in a Java Web application and deploy it on Bluemix.
 * 
 * You can refer to the Elastic Caching Java Native API Specification
 * http://pic.dhe.ibm.com/infocenter/wdpxc/v2r5/index.jsp?topic=%2Fcom.ibm.websphere.datapower.xc.doc%2Fcxslibertyfeats.html
 * 
 */
@WebServlet(urlPatterns ="/ecaas", loadOnStartup=1)
public class ECaaSSample extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static Cipher encryptCipher;  
    private static Cipher decryptCipher;
    private static String encryptionKey = "0123456789abcdef";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ECaaSSample() {
		super();
		ECacheConnection.initECaaS();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * Process /ecaas reuqest and return the relevant processing results
	 *  
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		//set request and response configuration
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(200);
		try {
			//get request data
			String key = request.getParameter("key");
			String operation = request.getParameter("operation");
			String newValue = request.getParameter("value");
			String encrypt = request.getParameter("encrypt");
			System.out.println("Encrypt value ?=" + encrypt);
			if (encrypt == null || encrypt ==""){
				System.out.println("Some Rest call did not set the encrypt value");
				encrypt="false";
			}
			Object retrievedValue;
			String mapName = "sample.NONE.P";
			//Process operation value and return processing results 
			if ("get".equals(operation)) {
				// get value of this key.
				retrievedValue = ECacheConnection.getData(mapName, key);
				response.getWriter().write(retrievedValue == null ? "null" : retrievedValue.toString());
				System.out.println("retrieved: " + retrievedValue);
			} else if ("put".equals(operation)) {
				// update or insert this value.
				if (encrypt.equals("true")){
					System.out.println("Encrypted flag set lets encrypt the value");
					String encryptedData = encryptValues(newValue);
					ECacheConnection.postData(mapName, key, encryptedData);
				}else{
					System.out.println("Encrypted flag not set do nothing");
					ECacheConnection.postData(mapName, key, newValue);
				}
				response.getWriter().write("Put successfull.");
				System.out.println("put key=" + key + " value=" + newValue);
			} else if ("delete".equals(operation)) {
				// delete this key/value.
				ECacheConnection.deleteData(mapName, key);
				response.getWriter().write("Remove successfull.");
				System.out.println("deleted key=" + key);
			} else if ("all".equals(operation)) {
				// get all key/value
				List<ECache> list = ECacheConnection.getAllData(mapName);
				String res = list.toString();
				response.getWriter().write(res);
				System.out.println("grid entries:" + res);
				System.out.println("grid entries size:" + list == null ? 0: list.size());
			}
		} catch (Exception e) {
			System.out.println("Failed to perform operation on map.");
			e.printStackTrace();
			response.setStatus(500);
		}
	}
	
    private static String encryptValues(String data)  
    	      throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException {
    	
    		SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes("UTF-8"),"AES");
	        encryptCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");  
	        encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey); 
	        System.out.println("Data Before Encryption :" + data);  
	        byte[] dataToEncrypt = data.getBytes();  
	        byte[] encryptedData = encryptCipher.doFinal(dataToEncrypt);  
   	     	System.out.println("Encryted Data: " + encryptedData);  
   			return Base64.encode(encryptedData);
			  
    }

    @SuppressWarnings("unused")
	private static String decryptValues(String data)  
  	      throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException {
  	
  			SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes("UTF-8"),"DES");
	        decryptCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");  
	        decryptCipher.init(Cipher.DECRYPT_MODE, secretKey); 
	        byte[] ds = Base64.decode(data);
	        byte[] textDecrypted = decryptCipher.doFinal(ds);  
	        System.out.println("Decryted Data: " + new String(textDecrypted));
 			return new String(textDecrypted);
    }
  }

