import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.sql.*;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@WebServlet("/SearchService")
public class SearchService extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Statement statement, statementSect, statementHscode, statementBrand = null;
	private static ResultSet rs, rs2, rs3, rs4 = null;

	public SearchService() {
		super();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
				
		try {
			InitialContext initialContext = new InitialContext();
			String connectionURL = (String) initialContext.lookup("java:comp/env/connectionURL");
			String classforName = (String) initialContext.lookup("java:comp/env/classforName");
			String username = (String) initialContext.lookup("java:comp/env/username");
			String password = (String) initialContext.lookup("java:comp/env/password");
			Connection connection = null;
			Class.forName(classforName).newInstance();
			connection = DriverManager.getConnection(connectionURL, username, password);
			
			String action = request.getParameter("action");
			
			
			if (action.equals("searchProduct")) {	
				statement = connection.createStatement();
				statementSect = connection.createStatement();
				statementHscode = connection.createStatement();
				statementBrand = connection.createStatement();
			
				String QueryString = "SELECT * from product_table where ";
				String materialno = request.getParameter("materialno").trim();				// get form parameters from request
				String brand = request.getParameter("brand").trim();
				String productname = request.getParameter("productname").trim();
				String hscode = request.getParameter("hscode").trim();
				String section = request.getParameter("section").trim();					// check which attributes are provided by user, based on that query is written and executed.
				int qrfor1 = 0 ;		
				
				if (!materialno.equals("")) {
					if (qrfor1 == 0) 	qrfor1 = 1;
					else 				QueryString += "and ";
					
					QueryString += "material_no like '%"+ materialno +"%' ";
				}
				if (!brand.equals("")){
					if (qrfor1 == 0) 	qrfor1 = 1;
					else 				QueryString += "and ";
					
					QueryString += "brand_id = (select brand_id from brand_table where brand = '"+ brand +"') ";
				}
				if (!productname.equals("")){
					if (qrfor1 == 0) 	qrfor1 = 1;
					else 				QueryString += "and ";
					
					QueryString += "product_name like '%"+ productname +"%' ";
				}
				if (!hscode.equals("")){
					if (qrfor1 == 0) 	qrfor1 = 1;
					else 				QueryString += "and ";
					
					QueryString += "hsncode_id = (select hsncode_id from hsncode_table where hsncode = '"+ hscode +"') ";
				}
				if (!section.equals("")){
					if (qrfor1 == 0) 	qrfor1 = 1;
					else 				QueryString += "and ";
					
					QueryString += "section_id = (select section_id from section_table where section_name = '"+ section +"') ";
				}
				
				QueryString += ";";
				
//   			log("- "+materialno+" - "+brand+" - "+productname+" - "+hscode+" - "+section+" - "+custrateqr+" - "+QueryString);
	
				List<List<String>> responseArr = new ArrayList<List<String>>();
				
				rs = statement.executeQuery(QueryString);
				if (rs.isBeforeFirst()) {
					while(rs.next()) {
						List<String> temp = new ArrayList<String>();
						
						temp.add(rs.getString(1));
						if (rs.getString(2) == null)	temp.add("-");
						else 							temp.add(rs.getString(2));	
						
						String QueryStringBrand = "SELECT brand from brand_table where brand_id = " + (rs.getInt(3)) + ";";
						rs4 = statementBrand.executeQuery(QueryStringBrand);
						while (rs4.next()) { temp.add(rs4.getString(1)); }
						rs4.close(); 
	
						for (int i=4; i<=7; i++) {
							if (rs.getString(i) == null)	temp.add("-");
							else 							temp.add(rs.getString(i));	
						}
						
						String QueryStringHscode = "SELECT hsncode from hsncode_table where hsncode_id = " + (rs.getInt(8)) + ";";
						rs3 = statementHscode.executeQuery(QueryStringHscode);
						while (rs3.next()) { 
							if (rs3.getString(1) == null)	temp.add("-");
							else 							temp.add(rs3.getString(1));
						}
						rs3.close(); 
						
						String QueryStringSect = "SELECT section_name from section_table where section_id = " + (rs.getInt(9)) + ";";
						rs2 = statementSect.executeQuery(QueryStringSect);
						while (rs2.next()) { 
							if (rs2.getString(1) == null)	temp.add("-");
							else 							temp.add(rs2.getString(1)); 
						}
						rs2.close(); 
						
						responseArr.add(temp);
					}
				} else {
					responseArr.add(Arrays.asList("none"));
				}
				
				statementHscode.close(); statementSect.close(); statementBrand.close(); statement.close();
				connection.close();
				
				String resjson = new Gson().toJson(responseArr);
				response.setContentType("application/json");
				response.getWriter().write(resjson);
				
			} else if (action.equals("createProduct")) {
				String materialno = request.getParameter("materialno").trim();				
				String brand = request.getParameter("brand").trim();
				String packing = request.getParameter("packing");
				String productname = request.getParameter("productname");
				String consumerrate = request.getParameter("consumerrate").trim();
				String gstrate = request.getParameter("gstrate").trim();
				String hscode = request.getParameter("hscode").trim();
				String section = request.getParameter("section").trim();
				int brand_id, hsncode_id, section_id, rowsInserted;
				String QueryString = "";
				
//				log("- "+materialno+" - "+brand+" - "+productname+" - "+consumerrate+" - "+gstrate+" - "+hscode+" - "+section);
				
				if (brand.equals(""))	brand_id = 1;
				else {
					statement = connection.createStatement();
					QueryString = "select brand_id FROM brand_table WHERE brand = '" + brand + "'";
					rs = statement.executeQuery(QueryString);
					if (rs.isBeforeFirst()) {
						rs.next();
						brand_id = rs.getInt(1);
					} else {
						statementBrand = connection.createStatement();
						QueryString = "INSERT INTO brand_table(brand) VALUES ('" + brand + "')";
						statementBrand.executeUpdate(QueryString);
						statementBrand.close();
						statementBrand = connection.createStatement();
						QueryString = "select brand_id FROM brand_table WHERE brand = '" + brand + "'";
						rs2 = statementBrand.executeQuery(QueryString);
						rs2.next();
						brand_id = rs2.getInt(1);
						rs2.close();
						statementBrand.close();
					}
					rs.close();
					statement.close();
				}
				
				if (hscode.equals(""))	hsncode_id = 1;
				else {
					statement = connection.createStatement();
					QueryString = "select hsncode_id FROM hsncode_table WHERE hsncode = '"+ hscode +"'";
					rs = statement.executeQuery(QueryString);
					if (rs.isBeforeFirst()) {
						rs.next();
						hsncode_id = rs.getInt(1);
					} else {
						statementHscode = connection.createStatement();
						QueryString = "INSERT INTO hsncode_table(hsncode) VALUES ('"+ hscode +"')";
						statementHscode.executeUpdate(QueryString);
						statementHscode.close();
						statementHscode = connection.createStatement();
						QueryString = "select hsncode_id FROM hsncode_table WHERE hsncode = '"+ hscode +"'";
						rs2 = statementHscode.executeQuery(QueryString);
						rs2.next();
						hsncode_id = rs2.getInt(1);
						rs2.close();
						statementHscode.close();
					}
					rs.close();
					statement.close();
				}
				
				if (section.equals(""))	section_id = 1;
				else {
					statement = connection.createStatement();
					QueryString = "select section_id FROM section_table WHERE section_name = '"+ section +"'";
					rs = statement.executeQuery(QueryString);
					if (rs.isBeforeFirst()) {
						rs.next();
						section_id = rs.getInt(1);
					} else {
						statementSect = connection.createStatement();
						QueryString = "INSERT INTO section_table(section_name) VALUES ('"+ section +"')";
						statementSect.executeUpdate(QueryString);
						statementSect.close();
						statementSect = connection.createStatement();
						QueryString = "select section_id FROM section_table WHERE section_name = '"+ section +"'";
						rs2 = statementSect.executeQuery(QueryString);
						rs2.next();
						section_id = rs2.getInt(1);
						rs2.close();
						statementSect.close();
					}
					rs.close();
					statement.close();
				}

				if (packing.equals(""))			packing = "NULL";
				else							packing = "'"+packing+"'";
				if (consumerrate.equals(""))	consumerrate = "NULL";
				else							consumerrate = "'"+consumerrate+"'";
				if (gstrate.equals(""))			gstrate = "NULL";
						
				
				statement = connection.createStatement();
				QueryString = "INSERT INTO product_table(material_no, brand_id, packing, product_name, consumer_rate, gst_rate, hsncode_id, section_id) VALUES ("
						+ "'"+materialno+"', "+brand_id+", "+packing+", '"+productname+"', "+consumerrate+", "+gstrate+", "+hsncode_id+", "+section_id+")";
				rowsInserted = statement.executeUpdate(QueryString);
				statement.close();
				connection.close();
				
				if (rowsInserted>0)	response.getWriter().write("success");
				else				response.getWriter().write("fail");
				
			}
			
		} catch (SQLException ex) { 
			response.getWriter().write("fail");
			ex.printStackTrace();
		} catch (Exception ex) {
			response.getWriter().write("fail");
			ex.printStackTrace();
		}

	}

}
