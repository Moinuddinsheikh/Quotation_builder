import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileItemFactory;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;

import com.google.gson.Gson;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/ImportService")
public class ImportService extends HttpServlet {
	private static final long serialVersionUID = 1L;
    public ImportService() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			
		String action = request.getParameter("action");
		String uploadedFilePath = null, resp = "", fileExt = null; 
		
		if (action == null) {
			FileItemFactory factory = new DiskFileItemFactory();
		    ServletFileUpload upload = new ServletFileUpload(factory);
			FileItem fileItem = null;  
			File uploadedFile = null;
	        String filePath = getServletContext().getRealPath("/");
		    String myFileName;
	
		    try {
			    List<FileItem> uploadedItems = upload.parseRequest(new ServletRequestContext(request));
	            Iterator<FileItem> i = uploadedItems.iterator();
	            SimpleDateFormat formatter = new SimpleDateFormat("HHmmss");
	    		Date date = new Date();
	
		        while (i.hasNext()) {
			        fileItem = (FileItem) i.next();
				    if (fileItem.isFormField() == false) {
					    if (fileItem.getSize() > 0) {
							myFileName = fileItem.getName();
							fileExt = myFileName.substring(myFileName.lastIndexOf('.'));
							myFileName = "importfile-" + formatter.format(date) + fileExt;
							uploadedFilePath = filePath +"/temp/"+ myFileName;
							uploadedFile = new File(uploadedFilePath);
				            fileItem.write(uploadedFile);
					    }
	                }
		        }
	    	    uploadedFile.deleteOnExit();
			} catch (Exception e) {
			    e.printStackTrace();
				response.getWriter().write("fail");
	        }	
		}
	    
		if (action!= null && action.equals("contimport"))	{
			uploadedFilePath = request.getParameter("uploadedFilePath");
		}
				
        List<List<String>> productList = new ArrayList<>();
        List<String> brandList = new ArrayList<>();	
        List<String> hsncodeList = new ArrayList<>();
        List<String> sectionList = new ArrayList<>();
        int headrowflg=0, datacellflg=0;
                
        try {
        	FileInputStream fis = new FileInputStream(new File(uploadedFilePath));
        	XSSFWorkbook workbookx = null;	HSSFWorkbook workbook = null;
        	fileExt = uploadedFilePath.substring(uploadedFilePath.lastIndexOf('.'));
        	
        	if (fileExt.equals(".xls")) 
        		workbook = new HSSFWorkbook(fis);
        	else
        		workbookx = new XSSFWorkbook(fis);
        	
        	String[] tblheads = {"Catalogue ID", "Brand", "Packing", "Product Name", "Consumer Rate", "GST Rate", "HSN Code", "Section" };
        	List<Set<Integer>> skipdrows = new ArrayList<Set<Integer>>();
        	
        	int sheet_count = (fileExt.equals(".xls")) ? workbook.getNumberOfSheets() : workbookx.getNumberOfSheets();
			
        	for (int k = 0; k < sheet_count; k++) {
				Iterator<Row> rows = null;
				if (fileExt.equals(".xls")) { 
					HSSFSheet sheet = workbook.getSheetAt(k);
					rows = sheet.rowIterator();
				} else {
	        		XSSFSheet sheet = workbookx.getSheetAt(k);
	        		rows = sheet.rowIterator();
				}
								
				int cellcount; 
				datacellflg=0; headrowflg=0;
				skipdrows.add(new HashSet<Integer>());
				Row headrow = (Row) rows.next();
				for (cellcount = 0; cellcount < 8; cellcount++) {
					Cell cell = (Cell) headrow.getCell(cellcount);
					if (!( cell.getStringCellValue().equalsIgnoreCase(tblheads[cellcount]) )) {	
						headrowflg=1;
						skipdrows.get(k).add(1);
						break;
					}
				}
				
				if (headrowflg == 1){
					break;
				} else {
					while (rows.hasNext()) {
						Row row = (Row) rows.next();
						List<String> data = new ArrayList<>();

						for (cellcount = 0; cellcount < 8; cellcount++) {
							Cell cell = (Cell) row.getCell(cellcount);
							if (cell == null || cell.getCellType() == CellType.BLANK) {
								if (cellcount == 0 || cellcount == 3) {
									skipdrows.get(k).add(row.getRowNum() + 1);
									datacellflg = 1;
								} else if (cellcount == 1 || cellcount == 6 || cellcount == 7) {
									data.add("1");
								} else {
									data.add("NULL");
								}
							} else {
								CellType type = cell.getCellType();
								if (type == CellType.STRING) {
									String str = cell.getStringCellValue();
									if (str.equals("")) {
										if (cellcount == 0 || cellcount == 3) {
											skipdrows.get(k).add(row.getRowNum() + 1);
											datacellflg = 1;
										} else if (cellcount == 1 || cellcount == 6 || cellcount == 7) {
											data.add("1");
										} else {
											data.add("NULL");
										}
									} else {
										if (cellcount == 1) {
											data.add(str);
											if (!(brandList.contains(str))) 
												brandList.add(str);
										} else if (cellcount == 6) {
											data.add(str);
											if (!(hsncodeList.contains(str))) 
												hsncodeList.add(str);
										} else if (cellcount == 7) {
											data.add(str);
											if (!(sectionList.contains(str))) 
												sectionList.add(str);
										} else if (cellcount == 3) {
											str = str.replaceAll("\'", "\'\'");
											data.add(str);
										} else if (cellcount == 0) {
											if (str.length() > 30) {
												skipdrows.get(k).add(row.getRowNum() + 1);
												datacellflg = 1;
											} else {
												data.add(str);
											}
										} else {
											data.add(str);
										}
									}
								} else if (type == CellType.NUMERIC) {
									String num = String.valueOf((long) cell.getNumericCellValue());
									if (cellcount == 6) {
										data.add(num);
										if (!(hsncodeList.contains(num))) 
											hsncodeList.add(num);
									} else if (cellcount == 7) {
										data.add(num);
										if (!(sectionList.contains(num))) 
											sectionList.add(num);
									} else
										data.add(num);
								} else {
									datacellflg = 1;
								}
							}
						}
						if (datacellflg == 0)
							productList.add(data);
					}
				}
			}
			
        	if (fileExt.equals(".xls")) 
        		workbook.close();
        	else
        		workbookx.close();
                        
            if (action == null) {
	            resp = "Excel file scanned. <br>";
	            
	            int errflg = 0;
	            for (int i=0; i<skipdrows.size(); i++) {
	            	if (skipdrows.get(i).contains(1)) {
	            		errflg = 1;
	            		resp += "1st row is not correct of sheet "+String.valueOf(i+1)+"<br><br>"; 
	            	} else if (skipdrows.get(i).size() > 0){
	            		errflg = 1;
	            		resp += "Following rows are not defined as per format of sheet "+String.valueOf(i+1)+": <br>";
	            		resp += skipdrows.get(i).toString().replace('[',' ').replace(']',' ') + "<br><br>";
	            	}
	            }
	            
	            if (errflg == 0) {
	            	resp += "0 errors found. <br>"
	            			+ productList.size()+ " products found. <br>"
	            			+ "Continue to import products to the database.<br><br>";
	            }
	            
	            List<String> responseArr = new ArrayList<String>();
				responseArr.add(uploadedFilePath);
				responseArr.add(resp);
				responseArr.add(String.valueOf(errflg));
	            
	            String resjson = new Gson().toJson(responseArr);
				response.setContentType("application/json");
				response.getWriter().write(resjson);
				
            } else if (action!= null && action.equals("contimport")) {
            
            	InitialContext initialContext = new InitialContext();
    			String connectionURL = (String) initialContext.lookup("java:comp/env/connectionURL");
//    			String connectionURL = "jdbc:mysql://localhost:3306/labtech2?serverTimezone=UTC";
    			String classforName = (String) initialContext.lookup("java:comp/env/classforName");
    			String username = (String) initialContext.lookup("java:comp/env/username");
    			String password = (String) initialContext.lookup("java:comp/env/password");
    			Connection connection = null;
    			Class.forName(classforName).newInstance();
    			connection = DriverManager.getConnection(connectionURL, username, password);
	    		Statement statementcheck = connection.createStatement(); 
	    		Statement statementcheck2 = connection.createStatement(); 
	    		ResultSet rs = null;
	    		
	            List<String> db_sql = new ArrayList<String>();		String insert_str ="", QueryString;
	            
	            for (int j=0; j<brandList.size(); j++) {
	            	QueryString = "select brand_id FROM brand_table WHERE brand = '" + brandList.get(j) + "'";
					rs = statementcheck.executeQuery(QueryString);					
					if (!(rs.isBeforeFirst())) {
						QueryString = "INSERT INTO brand_table(brand) VALUES ('" + brandList.get(j) + "')";
						statementcheck2.executeUpdate(QueryString);
					}
					rs.close();
	            }
	
	            for (int j=0; j<hsncodeList.size(); j++) {
	            	QueryString = "select hsncode_id FROM hsncode_table WHERE hsncode = '" + hsncodeList.get(j) + "'";
					rs = statementcheck.executeQuery(QueryString);					
					if (!(rs.isBeforeFirst())) {
						statementcheck2 = connection.createStatement();
						QueryString = "INSERT INTO hsncode_table(hsncode) VALUES ('" + hsncodeList.get(j) + "')";
						statementcheck2.executeUpdate(QueryString);
					}
					rs.close();
	            }
	
	            for (int j=0; j<sectionList.size(); j++) {
	            	QueryString = "select section_id FROM section_table WHERE section_name = '" + sectionList.get(j) + "'";
					rs = statementcheck.executeQuery(QueryString);					
					if (!(rs.isBeforeFirst())) {
						QueryString = "INSERT INTO section_table(section_name) VALUES ('" + sectionList.get(j) + "')";
						statementcheck2.executeUpdate(QueryString);
					}
					rs.close();
	            }
	
	            for (int i=0; i<productList.size(); i++) {
	                insert_str = "";
	            	insert_str += "INSERT INTO product_table(material_no, brand_id, packing, product_name, consumer_rate, gst_rate, hsncode_id, section_id) VALUES (";
	            	List<String> t = productList.get(i);
	            	for (int j=0; j<t.size(); j++) {
	            		if (j==0) {
	            			if (t.get(j).equals("NULL")) 	insert_str += "NULL" ;
	            			else 							insert_str += "'"+t.get(j)+"'";
	            		} else if (j==5) {	            			
	            			insert_str += ", "+ t.get(j) ;
	            		} else if (j==1) {
	            			if (t.get(j).equals("1")) 	
	            				insert_str += ", 1" ;
	            			else {							
		            			QueryString = "select brand_id FROM brand_table WHERE brand = '" + t.get(j) + "'";
								rs = statementcheck.executeQuery(QueryString);
								rs.next();
		            			insert_str += ", "+ rs.getInt(1) ;
		            			rs.close();
	            			}
	            		} else if (j==6) {
	            			if (t.get(j).equals("1")) 	
	            				insert_str += ", 1" ;
	            			else {
		            			QueryString = "select hsncode_id FROM hsncode_table WHERE hsncode = '" + t.get(j) + "'";
								rs = statementcheck.executeQuery(QueryString);
								rs.next();
		            			insert_str += ", "+ rs.getInt(1) ;
		            			rs.close();
	            			}
	            		} else if (j==7) {
	            			if (t.get(j).equals("1")) 
	            				insert_str += ", 1" ;
	            			else {
		            			QueryString = "select section_id FROM section_table WHERE section_name = '" + t.get(j) + "'";
								rs = statementcheck.executeQuery(QueryString);
								rs.next();
		            			insert_str += ", "+ rs.getInt(1) ;
		            			rs.close();
            				}
	            		} else {
	            			if (t.get(j).equals("NULL")) 	insert_str += ", NULL" ;
	            			else 							insert_str += ", '"+t.get(j)+"'";
	            		}
	                }
	            	insert_str += "); ";
	            	db_sql.add(insert_str);
	            }
	            statementcheck.close();
	            statementcheck2.close();
	            	            
	            Statement statement = connection.createStatement();
	    		for (int i=0; i<db_sql.size(); i++) {
	    			statement.addBatch(db_sql.get(i));
	    		}
	    		statement.executeBatch();
	    		response.getWriter().write(" "+productList.size());
            }    	    
            
        } catch (IOException e) {
			response.getWriter().write("fail");
            e.printStackTrace();
        } catch (Exception e) {
			response.getWriter().write("fail");
            e.printStackTrace();
        }

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
