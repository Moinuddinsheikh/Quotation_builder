<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<%@ page import="java.sql.*"%>
<%@ page import="java.io.*"%>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Import product</title>
<link rel="icon" href="images/webicon.png">
<script src="js/jquery-3.5.0.min.js"></script>
<link rel="stylesheet" type="text/css" href="css/bootstrap.min.css" />
<link rel="stylesheet" type="text/css" href="css/style.css" />
</head>
<body>
	<div class="row row-div">
		<div class="col-lg-12">
			<div class="row">
				<div class="col-lg-11">
					<h2>Import product by excel file.</h2>
				</div>
				<div class="col-lg-1">
					<a href="index.jsp"><button class="btn btn-outline-dark">back</button></a>
				</div>
			</div>
			<br>
			<div>
				<h5>The excel file must be defined as per following instructions:</h5>
				<p>
					1. I<sup>st</sup> row must have column names as per respective
					order in each sheet. <br> "Catalogue ID", "Brand", "Packing",
					"Product Name", "Consumer Rate", "GST Rate", "HSN Code", "Section"
				</p>
				<p>2. Catelogue ID and Product Name couldn't be empty be empty
					in each row.</p>
				<p>3. Catelogue ID value shouldn't exceed length greater than 30
					characters.</p>
			</div>
			<br>
			<form id="importForm" enctype="multipart/form-data" method="post">
				<div class="row">
					<div class="col-lg-4"></div>
					<div class="col-lg-4">
						<input id="importFile" class="form-control" name="importFile"
							type="file" accept=".xls,.xlsx">
					</div>
					<div class="col-lg-4"></div>
				</div>
				<br>
				<div class="row">
					<div class="col-lg-3"></div>
					<div class="col-lg-3">
						<button id="scanimport" type="submit"
							class="form-control btn btn-primary">Scan excel file</button>
					</div>
					<div class="col-lg-3">
						<button id="contimport" class="form-control btn btn-success"
							disabled="true">Continue Import</button>
					</div>
					<div class="col-lg-3"></div>
				</div>
			</form>
			<br>
			<br> 
			<img class="load-icon scan" src="images/load.gif">
			<img class="load-icon conti" src="images/load.gif">
			<div id="resp"></div>
			<br>
		</div>
	</div>

</body>

<script>

	$(document).ready(function() {
		var uploadedFilePath;
		
		$('#importFile').on('change', function(){
			var fileExt = $('#importFile').val().split('.').pop().toLowerCase();
            if ($.inArray(fileExt, ['xls','xlsx']) == -1) {
            	$('#importFile').addClass('invalid');
            	alert("Please upload only Excel files (.xlsx, .xls extension) !");
            } else 
            	$('#importFile').removeClass('invalid');
		});	
	
		$('#importForm').on('submit', function(e) {
            e.stopPropagation();
            e.preventDefault();
            var data = new FormData(this);
            var fileExt = $('#importFile').val().split('.').pop().toLowerCase();
            if ($.inArray(fileExt, ['xls','xlsx']) == -1) {
            	$('#importFile').addClass('invalid');
            	alert("Please upload only Excel files (.xlsx, .xls) are allowed!");
            } else 
            	$('#importFile').removeClass('invalid');
            	
            $('.load-icon.scan').show();
            $.ajax({
                url :  'ImportService',
                type : 'POST',
                data : data,
                cache : false,
                processData : false,
                contentType : false,
                success : function(data) {
                	if (data == "fail") {
            			$('.load-icon.scan').hide();
                		alert("Something went wrong!");
                	} else {	
            			$('.load-icon.scan').hide();
               		    $('#resp').html(data[1]);
                    	uploadedFilePath = data[0];
                    	if (data[2] == "0")
                    		$('#contimport').prop('disabled', false);  
                    }                  	
                },
                fail : function(){
            		$('.load-icon.scan').hide();
                	alert("Something went wrong!");
                }
            }); 
        });
        
        $('#contimport').on('click', function(e) {
        	$('#resp').html("");
            $('.load-icon.conti').show();
            $.ajax({
                url :  'ImportService',
                type : 'POST',
                data : {
                	action: "contimport",
                	uploadedFilePath: uploadedFilePath
                },
                success : function(data) {  
                	if (data == "fail") {
            			$('.load-icon.conti').hide();
                		alert("Something went wrong!");
                	} else {	
            			$('.load-icon.conti').hide();                  
                    	$('#resp').html(data+" products are added to database successfully.");
                    }
                },
                fail : function(){
            		$('.load-icon.conti').hide();
                	alert("Something went wrong!");
                }
           }); 
        });
        
	});
</script>
</html>