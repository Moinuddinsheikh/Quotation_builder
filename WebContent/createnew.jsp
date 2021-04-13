<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<%@ page import="java.sql.*"%>
<%@ page import="java.io.*"%>
<%@ page import="javax.naming.InitialContext" %>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Create new product</title>
<link rel="icon" href="images/webicon.png">
<script src="js/jquery-3.5.0.min.js"></script>
<script type="text/javascript" src="js/datatables.min.js"></script>
<script type="text/javascript" src="js/main.js"></script>
<link rel="stylesheet" type="text/css" href="css/bootstrap.min.css" />
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
	
	<div class="row row-div">
		<div class="col-lg-12">
			<div class="row">
				<div class="col-lg-12">
					<div class="row">
						<div class="col-lg-11">
							<h2>Create new product.</h2>
						</div>
						<div class="col-lg-1">
							<a href="index.jsp">
								<button class="btn btn-outline-dark"> back </button>
							</a>
						</div>
					</div>
					<br>
					<p class="redtext">* required fields</p>
					<br>
				</div>
			</div>
			<div class="row">
				<div class="col-lg-4 fields">
					<label for="materialno">Material No/Catalog Id: <span class="redtext">*</span></label> <!-- creating textFields and dropdowns  -->
						<div class="input-group">
							<input type="text" id="materialno" class="form-control create-inputs-req"
								name="materialno" placeholder="Enter material no..." autofocus>
						</div>
				</div>
				<div class="col-lg-4 fields">
					<label for="brand">Brand/Make:</label>
						<div class="input-group">
							<input type="text" id="brand" class="form-control"  list="brandlist"
							name="brand" placeholder="Enter brand/make..." >
						</div>
						<datalist id="brandlist">
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
						</datalist>
				</div>
				<div class="col-lg-4 fields">
					<label for="packing">packing:</label>
						<div class="input-group">
							<input type="text" id="packing" class="form-control" 
							name="packing" placeholder="Enter packing type..." >
						</div>
				</div>
			</div>
			
			<div class="row">
				<div class="col-lg-12 fields">
					<label for="productname">Product Name: <span class="redtext">*</span></label>
						<div class="input-group">
							<textarea id="productname" class="form-control create-inputs-req" name="productname" 
							placeholder="Enter product description..." rows="2" ></textarea>
						</div>
				</div>
			</div>

			<div class="row">
				<div class="col-lg-2"></div>
				<div class="col-lg-4 fields">
					<label for="consumerrate">Consumer rate: <span class="redtext">*</span></label>
						<div class="input-group">
							<input type="text" id="consumerrate" class="form-control" 
							name="consumerrate" placeholder="Enter consumer rate..." >
						</div>
						<span class="errormsg">*only numbers allowed</span>
				</div>
				<div class="col-lg-4 fields">
					<label for="gstrate">GST rate:</label>
						<div class="input-group">
							<input type="text" id="gstrate" class="form-control create-inputs-num" 
							name="gstrate" placeholder="Enter GST rate..." >
						</div>
						<span class="errormsg">*only numbers allowed</span>
				</div>
				<div class="col-lg-2"></div>
			</div>

			<div class="row">
				<div class="col-lg-2"></div>
				<div class="col-lg-4 fields">
					<label for="hscode">HSN code:</label>
						<div class="input-group">
							<input type="text" id="hscode"class="form-control" list="hscodelist"
							name="hscode" placeholder="Enter HSN code..." >
						</div>						
						<datalist id="hscodelist">
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
						</datalist>
				</div>

				<div class="col-lg-4 fields">
					<label for="section">Section:</label>
						<div class="input-group">
							<input type="text" id="section" class="form-control" list="sectionlist"
							name="section" placeholder="Enter section name..." >
						</div>
						<datalist id="sectionlist">
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
						</datalist>
				</div>
				<div class="col-lg-2"></div>
			</div>
			<br>
			<div class="row">
				<div class="col-lg-4"></div>
				<div class="col-lg-4">
					<button id="createbtn" class="btn btn-primary btn-lg">Create Product</button>
					<img id="load-create" class="load-icon" src="images/load.gif">
				</div>
				<div class="col-lg-4">
					<span id="create-errormsg" class="errormsg">*Please fill atleast one of the inputs.</span>
				</div>
			</div>
		</div>
	</div>
	
	<%
		connection.close();
	} catch (Exception e) {
		e.printStackTrace();
		out.println("Unable to connect to database.");
	}
	%>
</body>
</html>