<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<%@ page import="java.sql.*"%>
<%@ page import="java.io.*"%>
<%@ page import="com.google.gson.Gson"%>
<%@ page import="javax.naming.InitialContext" %>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Quotation Builder</title>
<link rel="icon" href="images/webicon.png">
<script src="js/jquery-3.5.0.min.js"></script>
<script type="text/javascript" src="js/datatables.min.js"></script>
<script type="text/javascript" src="js/main.js"></script>
<link rel="stylesheet" type="text/css" href="css/bootstrap.min.css" />
<link rel="stylesheet" type="text/css" href="css/datatables.min.css" />
<link rel="stylesheet" type="text/css" href="css/style.css" />
</head>
<body>
	<%
		try {
			InitialContext initialContext = new InitialContext();
			String connectionURL = (String) initialContext.lookup("java:comp/env/connectionURL");
			String classforName = (String) initialContext.lookup("java:comp/env/classforName");
			String username = (String) initialContext.lookup("java:comp/env/username");
			String password = (String) initialContext.lookup("java:comp/env/password");
			Connection connection = null;
			Class.forName(classforName).newInstance();
			connection = DriverManager.getConnection(connectionURL, username, password);
			Statement statement = null;
			ResultSet rs = null;		
	%>
	
	<a href="#" id="scrolltop-icon"><img src="images/scrolltop.png"></a>

	<!-- Product Search block starts here -->
	<div class="row row-div">
		<div class="col-lg-12">
		
			<!-- header links -->
			<div class="row">
				<div class="col-lg-3">
					<img id="main-logo" src="images/main-logo.png">
				</div>
				<div id="main-head" class="col-lg-6"><h1>Quotation Builder.</h1></div>
				<div class="col-lg-3"></div>
			</div>
			<br>
			<div class="row header">
				<div class="col-lg-3"></div>
				<div class="col-lg-2">
					<button id="billtable-btn" class="btn btn-outline-dark btn-sm head-links">go to Bill</button>
				</div>
				<div class="col-lg-2">
					<a href="createnew.jsp"><button id="createnew-btn" class="btn btn-outline-success btn-sm head-links">add new product</button></a>
				</div>
				<div class="col-lg-2">
					<a href="importExcel.jsp"><button id="importbtn-xlsx" class="btn btn-outline-success btn-sm head-links">excel import</button></a>
				</div>
				<div class="col-lg-3"></div>
			</div>
			<br><br>
			
			<!-- Textfields for searching of product -->
			<div class="row billfields-tbl">
				<br>
				<div class="col-lg-12">
					<div class="row">
						<div class="col-lg-5 fields">
							<label for="quotationno">Quotation No:</label>
							<div class="input-group">
								<input type="text" id=quotationno class="form-control otherfields"
									name="quotationno" placeholder="Enter quotation no..." autofocus>
								<img class="input-group-text clr-srchinp-btn"
									src="images/clear.png">
							</div>
						</div>
						<div class="col-lg-2"></div>
						<div class="col-lg-5 fields">
							<label for="customername">Customer Name:</label>
							<div class="input-group">
								<textarea class="form-control otherfields" rows="3" id="customername otherfields"
								placeholder="Enter customer name and address..."></textarea>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-lg-12 fields">
							<label for="subject">Subject:</label>
							<div class="input-group">
								<textarea class="form-control otherfields" rows="2" id="subject" name="subject"
									placeholder="Add Subject to the Quotation..."></textarea>
							</div>
						</div>
					</div>
				</div>
			</div>
			<br><br>
			<div class="row srchfields-tbl">
				<div class="col-lg-12">
					<div class="row">
						<div class="col-lg-8 fields">
							<label for="materialno">Catalogue Id:</label>
							<div class="input-group">
								<input type="text" id="materialno" class="form-control"
									name="materialno" placeholder="Enter material no...">
								<img class="input-group-text clr-srchinp-btn" src="images/clear.png">
							</div>
						</div>
						
						<div class="col-lg-4 fields">
							<label for="brand">Brand/Make:</label>
								<div class="input-group">
									<input type="text" id="brand" list="brandlist"
										class="form-control" name="brand" placeholder="Choose brand/make..." >
									<img class="input-group-text clr-srchinp-btn" src="images/clear.png">
								</div> <datalist id="brandlist">
									<%
										statement = connection.createStatement();
									String QueryStringFields = "select brand from brand_table";
									rs = statement.executeQuery(QueryStringFields);		rs.next();
									while (rs.next()) {
									%>
									<option value="<%=rs.getString(1)%>"><%=rs.getString(1)%></option>
									<%
										}
									rs.close();
									statement.close();
									%>
								</datalist> <span id="brand-errormsg" class="errormsg">*Enter value
									only from list.</span>
							
						</div>
					</div>
					
					<div class="row">						
						<div class="col-lg-12 fields">
							<label for="productname">Product Name:</label>
								<div class="input-group">
									<input type="text" id="productname" class="form-control"
										name="productname" placeholder="Enter product keyword..." >
									<img class="input-group-text clr-srchinp-btn" src="images/clear.png">
								</div>
						</div>
					</div>
					
					<div class="row">		
						<div class="col-lg-2"></div>				
						<div class="col-lg-4 fields">
							<label for="hscode">HSN code:</label>
								<div class="input-group">
									<input type="text" id="hscode" list="hscodelist"
										class="form-control" name="hscode" placeholder="Choose HSN code..." >
									<img class="input-group-text clr-srchinp-btn" src="images/clear.png">
								</div> <datalist id="hscodelist">
									<%
										statement = connection.createStatement();
									QueryStringFields = "select hsncode from hsncode_table";
									rs = statement.executeQuery(QueryStringFields);		rs.next();
									while (rs.next()) {
									%>
									<option value="<%=rs.getString(1)%>"><%=rs.getString(1)%></option>
									<%
										}
									rs.close();
									statement.close();
									%>
								</datalist> <span id="hscode-errormsg" class="errormsg">*Enter value
									only from list.</span>
						</div>
						
						<div class="col-lg-4 fields">
							<label for="section">Section:</label>
								<div class="input-group">
									<input type="text" id="section" list="sectionlist"
										class="form-control" name="section" placeholder="Choose section name..." >
									<img class="input-group-text clr-srchinp-btn" src="images/clear.png">
								</div> <datalist id="sectionlist">
									<%
										statement = connection.createStatement();
									QueryStringFields = "select section_name from section_table";
									rs = statement.executeQuery(QueryStringFields);		rs.next();
									while (rs.next()) {
									%>
									<option value="<%=rs.getString(1)%>"><%=rs.getString(1)%></option>
									<%
										}
									rs.close();
									statement.close();
									%>
								</datalist> <span id="section-errormsg" class="errormsg">*Enter value
									only from list.</span>
						</div>
						<div class="col-lg-2"></div>
					</div>
					<br>
					
					<!-- "Get products" search button -->
					<div class="row">
						<div class="col-lg-4"></div>
						<div class="col-lg-4">
							<button id="resultbtn" class="btn btn-primary btn-lg">GET PRODUCTS</button>
							<img id="load-result" class="load-icon" src="images/load.gif">
						</div>
						<div class="col-lg-4">
							<span id="submit-errormsg" class="errormsg">*Please fill atleast one of the inputs.</span> 
						</div>
					</div>
					<br><br>
				</div>
			</div>
		</div>
	</div>
	<%
		connection.close();
	} catch (Exception e) {
		out.write("<br><br><br>Unable to connect to database. Please Turn on Database server and try again.");
		out.write("<script type='text/javascript'> alert(\"Unable to connect to database. Please Turn on Database server and try again.\");</script> ");
		e.printStackTrace();
	}
	%>
	<!-- Product Search block ends here -->
		
	<!-- Search results block starts here -->
	<div class="row row-div">
		<div class="col-lg-12">
			<div class="row">
				<div class="col-lg-2"><h4>Search Results</h4></div>
				<div class="col-lg-10"><button class="btn btn-outline-danger btn-sm" id="clr-searchresult-btn">clear search items</button></div>
			</div>
		<br><br>
			<div class="row">
				<div class="col-lg-12">
					<div id="resulttext">
						<table id='main-tbl' class='table table-sm table-bordered'>
							<thead>
								<tr>
									<th>S No</th>
									<th>Catalogue Id</th>
									<th>Make/Brand</th>
									<th>Packing</th>
									<th>Product Name</th>
									<th>Consumer rate</th>
									<th>GST rate</th>
									<th>HSN code</th>
									<th>Section</th>
									<th>add item to Bill</th>
								</tr>
							</thead>
							<tbody>
							</tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
	</div> 
	<!-- Search results block ends here -->
	
	<hr>

	<!-- Product Bill block starts here -->
	<div class="row row-div">
		<div class="col-lg-12">
			<div class="row">
				<div class="col-lg-3">
					<h4>Quotation Generated</h4>
				</div>
				<div class="col-lg-9">
					<button class="btn btn-outline-danger btn-sm" id="clr-billresult-btn">clear bill items</button>
				</div>
			</div>
			<br><br>
			<div class="row">
				<table id="bill-tbl" class="table table-bordered">
					<!-- table for adding products in bill -->
					<thead>
						<tr>
							<th><input type="checkbox" id="checkbox-enbl-all" value="allcols-enbl" checked> </th>
							<th>S no.
								<input type="checkbox" class="checkbox-enbl" value="sno-enbl" checked> </th>
							<th>Catalogue Id
								<input type="checkbox" class="checkbox-enbl" value="catalogid-enbl" checked> </th>
							<th style="width: 350px;">Particulars
								<input type="checkbox" class="checkbox-enbl" value="particulars-enbl" checked> </th>
							<th>Make
								<input type="checkbox" class="checkbox-enbl" value="make-enbl" checked> </th>
							<th class="input-cols">Quantity
								<input type="checkbox" class="checkbox-enbl" value="qty-enbl" checked> </th>
							<th class="input-cols">Unit Price
								<input type="checkbox" class="checkbox-enbl" value="unitprc-enbl" checked> </th>
							<th class="input-cols"> Discount
								<input type="checkbox" class="checkbox-enbl" value="discount-enbl" checked> </th>
							<th style="width: 120px;">GST
								<input type="checkbox" class="checkbox-enbl" value="gst-enbl" checked> </th>
							<th class="input-cols">Price (incl. GST)
								<input type="checkbox" class="checkbox-enbl" value="totalprc-enbl" checked> </th>
							<th style="width: 120px;">HSN code
								<input type="checkbox" class="checkbox-enbl" value="hsncode-enbl" checked> </th>
							<th>remove item</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td id="billtbl-empty" colspan="12">Their are no products currently on the Bill.</td>
						</tr>
					</tbody>
					<tfoot>
						<tr>
							<th colspan="8"></th>
							<th class="input-cols">Total Price</th>
							<th id="billtotal" class="input-cols td-center">0.0</th>
							<th colspan="2"></th>
						</tr>
					</tfoot>
				</table>
			</div>
			<br>
			<hr>
			<br>
			<div class="row">
				<div class="col-lg-12">
					<h4>Terms and Conditions:</h4>
					<div class="form-group delivery-div">
						<label for="delivery">1. Delivery: </label>
						<textarea class="form-control otherfields" rows="2" id="delivery"
							placeholder="Enter terms of delivery..."></textarea>
					</div>
					<div class="form-group payment-div">
						<label for="payment">2. Payment:</label>
						<textarea class="form-control otherfields" rows="2" id="payment"
							placeholder="Enter terms of payment..."></textarea>
					</div><br>
					<div class="row">
						<div class="col-lg-2"></div>
						<div class="col-lg-4">HDFC BANK </div>
						<div class="col-lg-4">Trade House Indore</div>
					</div>
					<div class="row">
						<div class="col-lg-2"></div>
						<div class="col-lg-4"><b>A/c No: 00362320001974</b> </div>
						<div class="col-lg-4"><b>IFS/RTGS Code No: HDFC0000036</b></div>
					</div>
					<br><br>
					<p>3. All quoted prices are valid for 30 days from date of quotation.</p>
					<p>4. Please reference your Kasliwal Brothers quotation number on your purchase orders.</p>
					<p>5. Please mention your CST/TIN No./GST No. in your Purchase Order.</p>
					<p>6. Please mention Quotation number on your Purchase Order and send it to on</p>
					<div class="row">
						<div class="col-lg-4"><input type="email" id=email1 class="form-control otherfields" name="email1" placeholder="Email 1..."></div>
						<div class="col-lg-2" style="text-align: center;">and copy to </div>
						<div class="col-lg-4"><input type="email" id=email2 class="form-control otherfields" name="email2"placeholder="Email 2..."></div>
					</div>
					<br>
					<div class="form-group extranote-div">
						<label for="extranote">Note:</label>
						<input type="text" class="form-control" id="extranote" placeholder="Add note if any...">
					</div>
					<br><br>
					<div class="row">
						<div class="col-lg-2"></div>
						<div class="col-lg-4">Subject to Indore Jurisdiction </div>
						<div class="col-lg-4">FOR KASLIWAL BROTHERS</div>
					</div>
					<br><br>
					<div class="row">
						<div class="col-lg-2"></div>
						<div class="col-lg-4">GST NO. 23AABFK2096H1ZJ </div>
						<div class="col-lg-4">Authorized Signatory</div>
					</div>
					<br><br>
				</div>
			</div>
			<br>
			<hr>
			<br><br>
			<div class="row">
				<div class="col-lg-4">
					<div class="row">
						<div class="col-lg-2"></div>
						<div class="col-lg-2"><img class="export-icon" src="images/pdf.png"></div>
						<div class="col-lg-6">
							<button id="exportbtn-pdf" class="btn btn-dark exportbtn">export to pdf</button>
							<img id="load-export-pdf" class="load-icon" src="images/load.gif">
							<br><br>
						</div>
						<div class="col-lg-2"></div>
					</div>
				</div>
				<div class="col-lg-4">
					<div class="row">	
						<div class="col-lg-2"></div>				
						<div class="col-lg-2"><img class="export-icon" src="images/word.png"></div>
						<div class="col-lg-6">
							<button id="exportbtn-doc" class="btn btn-dark exportbtn">export to word</button>
							<img id="load-export-doc" class="load-icon" src="images/load.gif">
							<br><br>
						</div>
						<div class="col-lg-2"></div>	
					</div>
				</div>
				<div class="col-lg-4">
					<div class="row">
						<div class="col-lg-2"></div>	
						<div class="col-lg-2"><img class="export-icon" src="images/excel.png"></div>
						<div class="col-lg-6">
							<button id="exportbtn-xls" class="btn btn-dark exportbtn">export to excel</button>							
							<img id="load-export-xls" class="load-icon" src="images/load.gif">
							<br><br>
						</div>
						<div class="col-lg-2"></div>	
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-lg-12"></div>
				<div class="col-lg-10">
					<span id="exportbtn-errormsg" class="errormsg">
					*Some required fields are missing or Quotation table is empty.</span> 
				</div>
			</div>
		</div>
	</div>
	<!-- Product Bill block ends here -->
</body>

</html>

