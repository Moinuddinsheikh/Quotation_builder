import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import com.google.gson.Gson;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.*;

@WebServlet("/ExportService")
public class ExportService extends HttpServlet {
	private static final long serialVersionUID = 1L;
     
    public ExportService() {
        super();
    }

	protected class HeadImg implements IEventHandler {
		@Override
		public void handleEvent(Event event) {
	        try {
				String filePath = getServletContext().getRealPath("/");
				File imgFile = new File(filePath + "/images/logo.jpg");			
				PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
				PdfDocument pdf = docEvent.getDocument();        
				PdfPage page = docEvent.getPage();
				int pageNumber = pdf.getPageNumber(page);
		        Rectangle pageSize = page.getPageSize();
		        PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdf);
		        
				FileInputStream fis = new FileInputStream(imgFile);
				byte[] bytes = IOUtils.toByteArray(fis);
				ImageData data = ImageDataFactory.create(bytes);
				pdfCanvas.addImage(data, 37.5F, 730F, 520, false);
				
		        Canvas canvas = new Canvas(pdfCanvas, pdf, pageSize);
				float x = (pageSize.getLeft() + pageSize.getRight()) / 2;
		        float y = pageSize.getBottom() + 15;
		        canvas.setFontSize(8f);
		        canvas.showTextAligned(
		            "page "+pageNumber, x, y, TextAlignment.CENTER);
		        canvas.close();
				pdfCanvas.release();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String action = request.getParameter("action");
		String[] tblheads = { "S. No.", "Catalogue Id", "Particulars", "Make", "Qty.", "Unit Price",	"Discount", "GST", "Price \n(incl. GST)", "HSN Code" };
		String prodlist[][] = new Gson().fromJson(request.getParameter("prodlist"), String[][].class);
		String otherfields[][] = new Gson().fromJson(request.getParameter("otherfields"), String[][].class);
		String[] checkboxenbl_arr = otherfields[10];
		int tblsize = Integer.valueOf(otherfields[9][0]);
        String filePath = getServletContext().getRealPath("/");
        File imgFile = new File(filePath + "/images/logo.jpg");	
				
		SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy-HHmmss");
		Date date = new Date();
		String filename = otherfields[0][0];
		filename = filename.replaceAll("[\\/?<>.\"]", " ") ;
		File desktop = new File(System.getProperty("user.home"), "/Desktop");
				
		if (action.equals("exportToPDF")) {
			try {
				
			    PdfWriter writer = new PdfWriter(desktop+"\\"+filename+".pdf");
				PdfDocument pdf = new PdfDocument(writer);
				pdf.addEventHandler(PdfDocumentEvent.START_PAGE, new HeadImg());
				
				pdf.addNewPage();
				pdf.getDocumentInfo().setCreator("Kasliwal Brothers Pharmaceuticals");
				pdf.getDocumentInfo().setAuthor("Kasliwal Brothers Pharmaceuticals");
				pdf.getDocumentInfo().addCreationDate();
				Document document = new Document(pdf, new PageSize(PageSize.A4));
				document.setTopMargin(130);
				
				formatter = new SimpleDateFormat("dd MMMM yyyy");
				
				Table table1 = new Table(new float[] {5f, 5f});
				table1.setWidth(UnitValue.createPercentValue(100));
				table1.addCell(new Cell().add(new Paragraph("Quotation  No: "+ otherfields[0][0]))
						.setTextAlignment(TextAlignment.LEFT)
						.setFontSize(10)
						.setBorder(new SolidBorder(new DeviceRgb(255, 255, 255), 0F)));
				table1.addCell(new Cell().add(new Paragraph(formatter.format(date)))
						.setTextAlignment(TextAlignment.RIGHT)
						.setFontSize(10)
						.setBorder(new SolidBorder(new DeviceRgb(255, 255, 255), 0F)));
				document.add(table1);
				
				Text text = new Text("To: \n"+ otherfields[1][0]).setFontSize(10);
				document.add(new Paragraph(text));
				
				text = new Text("Subject: "+ otherfields[2][0]).setFontSize(10);
				document.add(new Paragraph(text));
				
				Paragraph paragraph = new Paragraph();
				paragraph.add(new Text("Dear Sir,\n").setFontSize(10));
				paragraph.add(new Text("In response to your enquiry, we are pleased to offer our rates as under:-")
						.setFontSize(10));
				document.add(paragraph);
				
				int tblcol = 0;
				float[] pointColumnWidths = new float[tblsize];
				for (int i=0; i<10; i++) {
					if (checkboxenbl_arr[i].equals("1")) {
						pointColumnWidths[tblcol] = 5F;
						tblcol++;
					}
				}
					
				Table table = new Table(pointColumnWidths);
				table.setWidth(UnitValue.createPercentValue(100));
				table.setMarginTop(10);

				for (int i = 0; i < 10; i++) {
					if (checkboxenbl_arr[i].equals("0")) 
						continue;
					
					table.addCell(new Cell().add(new Paragraph(tblheads[i]))
							.setTextAlignment(TextAlignment.CENTER)
							.setVerticalAlignment(VerticalAlignment.MIDDLE)
							.setFontSize(10)
							.setBold()
							.setPaddings(5, 2, 5, 2)
							.setBorder(new SolidBorder(new DeviceRgb(216, 216, 216), 1F)));
				}

				for (int i = 0; i < prodlist.length; i++) {
					for (int j = 0; j < 10; j++) {
						if (checkboxenbl_arr[j].equals("0")) 
							continue;
						if (j == 2) {
							table.addCell(new Cell().add(new Paragraph(prodlist[i][j]))
									.setFontSize(10)
									.setPaddings(5, 2, 5, 2)
									.setBorder(new SolidBorder(new DeviceRgb(216, 216, 216), 1F)));
							continue;
						} 
						if (j==6 || j==7) {
							table.addCell(new Cell().add(new Paragraph(prodlist[i][j]+"%"))
									.setFontSize(10)
									.setPaddings(5, 2, 5, 2)
									.setTextAlignment(TextAlignment.CENTER)
									.setBorder(new SolidBorder(new DeviceRgb(216, 216, 216), 1F)));
							continue;
						}
						table.addCell(new Cell().add(new Paragraph(prodlist[i][j]))
								.setFontSize(10)
								.setPaddings(5, 2, 5, 2)
								.setTextAlignment(TextAlignment.CENTER)
								.setBorder(new SolidBorder(new DeviceRgb(216, 216, 216), 1F)));
					}
				}
				for (int j = 0; j < 10; j++) {
					if (checkboxenbl_arr[j].equals("0")) 
						continue;
					if (j==5) {
						table.addCell(new Cell(1,2).add(new Paragraph("Total Price: "))
								.setFontSize(10)
								.setPaddings(5, 2, 5, 2)
								.setTextAlignment(TextAlignment.CENTER)
								.setBorder(new SolidBorder(new DeviceRgb(255, 255, 255), 1F)));
						if (checkboxenbl_arr[6].equals("1")) {
							table.addCell(new Cell().add(new Paragraph(""))
									.setBorder(new SolidBorder(new DeviceRgb(255, 255, 255), 1F)));
						}
						table.addCell(new Cell().add(new Paragraph(String.valueOf(otherfields[8][0])))
								.setFontSize(10)
								.setPaddings(5, 2, 5, 2)
								.setTextAlignment(TextAlignment.CENTER)
								.setBorder(new SolidBorder(new DeviceRgb(255, 255, 255), 1F)));
						break;
					}
					if (j<5) {
						table.addCell(new Cell().add(new Paragraph(""))
								.setBorder(new SolidBorder(new DeviceRgb(255, 255, 255), 1F)));
					}	
				}
				
				document.add(table);
				
				paragraph = new Paragraph();
				paragraph.add(new Text("\n"));
				paragraph.add(new Text("Terms and Conditions : ").setFontSize(10).setBold());				
				document.add(paragraph);
				paragraph = new Paragraph();
				paragraph.addTabStops(new TabStop(100f, TabAlignment.LEFT));
				paragraph.add(new Text("1. Delivery: ").setFontSize(10).setBold());
				paragraph.add(new Text(otherfields[3][0]+ "\n").setFontSize(10));
				paragraph.add(new Text("2. Payment: ").setFontSize(10).setBold());
				paragraph.add(new Text(otherfields[4][0]+ "\n\n").setFontSize(10));
				paragraph.add(new Tab());
				paragraph.add(new Text("HDFC BANK").setFontSize(10));
				paragraph.add(new Tab()); paragraph.add(new Tab()); paragraph.add(new Tab());
				paragraph.add(new Text("Trade House Indore \n").setFontSize(10));
				paragraph.add(new Tab());
				paragraph.add(new Text("A/c No: 00362320001974").setFontSize(10).setBold());
				paragraph.add(new Tab()); paragraph.add(new Tab());
				paragraph.add(new Text("IFS/RTGS Code No: HDFC0000036 \n\n").setFontSize(10).setBold());
				
				paragraph.add(new Text("3. All quoted prices are valid for 30 days from date of quotation. \n").setFontSize(10));
				paragraph.add(new Text("4. Please reference your Kasliwal Brothers quotation number on your purchase orders. \n").setFontSize(10));
				paragraph.add(new Text("5. Please mention your CST/TIN No./GST No. in your Purchase Order. \n").setFontSize(10));
				paragraph.add(new Text("6. Please mention Quotation number on your Purchase Order and send it to on ").setFontSize(10));
				paragraph.add(new Text(otherfields[5][0]).setFontSize(10).setBold());
				paragraph.add(new Text(" and copy to ").setFontSize(10));
				paragraph.add(new Text(otherfields[6][0]+ "\n\n").setFontSize(10).setBold());

				if ( !(otherfields[7][0].equals("")) ) {
					paragraph.add(new Text("Note: "+ otherfields[7][0]+" \n\n").setFontSize(10));
				}
				
				paragraph.add(new Tab());
				paragraph.add(new Text("Subject to Indore Jurisdiction").setFontSize(10));
				paragraph.add(new Tab()); paragraph.add(new Tab());
				paragraph.add(new Text("FOR KASLIWAL BROTHERS, \n\n\n").setFontSize(10));
				paragraph.add(new Tab());
				paragraph.add(new Text("GST NO. 23AABFK2096H1Z").setFontSize(10));
				paragraph.add(new Tab()); paragraph.add(new Tab());
				paragraph.add(new Text("Authorized Signatory \n\n").setFontSize(10));
				
				document.add(paragraph);
				
				document.close();
				response.getWriter().write("success");

			} catch (IOException e) {
				response.getWriter().write("fail");
				e.printStackTrace();
			} catch (Exception e) {
				response.getWriter().write("fail");
				e.printStackTrace();
			}
		}
		
		if (action.equals("exportToExcel")) {
			try {
								
				HSSFWorkbook workbook = new HSSFWorkbook();
		        HSSFSheet sheet = workbook.createSheet();
		        int rowcount = 7;
		        
		        FileInputStream fis = new FileInputStream(imgFile);
				byte[] bytes = IOUtils.toByteArray(fis);
				int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);
				fis.close();
				CreationHelper helper = workbook.getCreationHelper();
				Drawing<?> drawing = sheet.createDrawingPatriarch();
				ClientAnchor anchor = helper.createClientAnchor();
				anchor.setCol1(2);
				anchor.setRow1(1);
				Picture pict = drawing.createPicture(anchor, pictureIdx);
				pict.resize(5);
		        
		        HSSFCellStyle style = workbook.createCellStyle();
		        HSSFFont font = workbook.createFont();
		        style.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
		        style.setFont(font);
		        
		        formatter = new SimpleDateFormat("dd MMMM yyyy");
		        HSSFRow rowint = sheet.createRow(rowcount);
		        rowint.setHeightInPoints((short) 15);
		        HSSFCell cellint = rowint.createCell(1);
		        cellint.setCellValue(new HSSFRichTextString("Quotation  No: "+otherfields[0][0]));
		        cellint.setCellStyle(style);
		        cellint = rowint.createCell(5);
		        cellint.setCellValue(new HSSFRichTextString(formatter.format(date)));
		        cellint.setCellStyle(style);
		        ++rowcount;

		        rowint = sheet.createRow(rowcount);
		        rowint.setHeightInPoints((short) 15);
		        cellint = rowint.createCell(1);
		        cellint.setCellValue(new HSSFRichTextString("To: "+ otherfields[1][0]));
		        cellint.setCellStyle(style);
		        ++rowcount;

		        rowint = sheet.createRow(rowcount);
		        rowint.setHeightInPoints((short) 15);
		        cellint = rowint.createCell(1);
		        cellint.setCellValue(new HSSFRichTextString("Subject: "+ otherfields[2][0]));
		        cellint.setCellStyle(style);
		        rowcount += 2;
		        		        
		        rowint = sheet.createRow(rowcount);
		        rowint.setHeightInPoints((short) 15);
		        cellint = rowint.createCell(1);
		        cellint.setCellValue(new HSSFRichTextString("Dear Sir,"));
		        cellint.setCellStyle(style);
		        ++rowcount;
		        rowint = sheet.createRow(rowcount);
		        rowint.setHeightInPoints((short) 15);
		        cellint = rowint.createCell(1);
		        cellint.setCellValue(new HSSFRichTextString("In response to your enquiry, we are pleased to offer our rates as under:-"));
		        cellint.setCellStyle(style);
		        rowcount += 2;	
		        
		        HSSFCellStyle stylehead = workbook.createCellStyle();
		        HSSFFont fonthead = workbook.createFont();
		        fonthead.setBold(true);
		        stylehead.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
		        stylehead.setFont(fonthead);
		        
		        HSSFCellStyle styleprodname = workbook.createCellStyle();
		        HSSFFont fontprodname = workbook.createFont();
		        styleprodname.setWrapText(true);
		        styleprodname.setFont(fontprodname);
		        
		        HSSFRow rowhead = sheet.createRow(rowcount);
		        rowhead.setHeightInPoints((short) 15);
		        int tblcol = 1;
				for (int i = 0; i < 10; i++) {
					if (checkboxenbl_arr[i].equals("0")) 
						continue;
					
					HSSFCell cellhead = rowhead.createCell(tblcol);
					cellhead.setCellValue(new HSSFRichTextString(tblheads[i]));
					cellhead.setCellStyle(stylehead);
					tblcol++;
				}
		        ++rowcount;

		        int tpcell = 8, tnccell = 3; 
				for (int i = 0; i < prodlist.length; i++) {
					HSSFRow rowproduct = sheet.createRow(i+rowcount);
			        tblcol = 1;
					for (int j = 0; j < 10; j++) {
						if (checkboxenbl_arr[j].equals("0")) 
							continue;
						
						if (j==2) {
							HSSFCell cellproduct = rowproduct.createCell(tblcol);
							cellproduct.setCellValue(new HSSFRichTextString(prodlist[i][j]));
							cellproduct.setCellStyle(styleprodname);
							sheet.setColumnWidth(tblcol, 65 * 256);	
							tnccell = tblcol;
							++tblcol;
							continue;
						} if (j==1 || j==3 || j==9) {
							HSSFCell cellproduct = rowproduct.createCell(tblcol);
							cellproduct.setCellValue(new HSSFRichTextString(prodlist[i][j]));
							cellproduct.setCellStyle(style);
							sheet.setColumnWidth(tblcol, 10 * 256);		
							++tblcol;
							continue;
						} if (j==6 || j==7) {		
							HSSFCell cellproduct = rowproduct.createCell(tblcol);
							cellproduct.setCellValue(new HSSFRichTextString(prodlist[i][j]+"%"));
							cellproduct.setCellStyle(style);	
							++tblcol;
							continue;
						} if (j==8) {
							HSSFCell cellproduct = rowproduct.createCell(tblcol);
							cellproduct.setCellValue(Double.valueOf(prodlist[i][j]));
							cellproduct.setCellStyle(style);
							sheet.setColumnWidth(tblcol, 18 * 256);	
							tpcell = tblcol;
							++tblcol;
							continue;
						}
												
						HSSFCell cellproduct = rowproduct.createCell(tblcol);
						cellproduct.setCellValue(Double.valueOf(prodlist[i][j]));
						cellproduct.setCellStyle(style);
						sheet.setColumnWidth(tblcol, 12 * 256);	
						++tblcol;
					}
				}
				rowcount += prodlist.length;
				
				rowint = sheet.createRow(rowcount);
		        rowint.setHeightInPoints((short) 15);
		        cellint = rowint.createCell(tpcell-1);
		        cellint.setCellValue(new HSSFRichTextString("Total Price "));
		        cellint.setCellStyle(style);
		        cellint = rowint.createCell(tpcell);
		        cellint.setCellValue(Double.valueOf(otherfields[8][0]));
		        cellint.setCellStyle(style);								
		        rowcount += 2;
		        
		        HSSFCellStyle stylefoot = workbook.createCellStyle();
		        HSSFFont fontfoot = workbook.createFont();
		        fontfoot.setBold(true);
		        stylefoot.setFont(fontfoot);
				
		        HSSFRow rowtfoot = sheet.createRow(rowcount);
		        rowtfoot.setHeightInPoints((short) 15);
		        HSSFCell cellfoot = rowtfoot.createCell(tnccell);
		        cellfoot.setCellValue(new HSSFRichTextString("Terms and Conditions : "));
		        cellfoot.setCellStyle(stylefoot);
		        rowcount+=2;
		        
		        rowtfoot = sheet.createRow(rowcount);
		        cellfoot = rowtfoot.createCell(tnccell);
		        cellfoot.setCellValue(new HSSFRichTextString("1. Delivery: "));
		        cellfoot.setCellStyle(stylefoot);
		        ++rowcount;
		        rowtfoot = sheet.createRow(rowcount);
		        cellfoot = rowtfoot.createCell(tnccell);
		        cellfoot.setCellValue(new HSSFRichTextString(otherfields[3][0]));
		        cellfoot.setCellStyle(styleprodname);
		        ++rowcount;
		        rowtfoot = sheet.createRow(rowcount);
		        cellfoot = rowtfoot.createCell(tnccell);
		        cellfoot.setCellValue(new HSSFRichTextString("2. Payment: "));
		        cellfoot.setCellStyle(stylefoot);
		        ++rowcount;
		        rowtfoot = sheet.createRow(rowcount);
		        cellfoot = rowtfoot.createCell(tnccell);
		        cellfoot.setCellValue(new HSSFRichTextString(otherfields[4][0]));
		        cellfoot.setCellStyle(styleprodname);
		        rowcount+=2;

		        rowtfoot = sheet.createRow(rowcount);
		        cellfoot = rowtfoot.createCell(tnccell);
		        cellfoot.setCellValue(new HSSFRichTextString("HDFC BANK................................Trade House Indore"));
		        cellfoot.setCellStyle(style);
		        ++rowcount;
		        rowtfoot = sheet.createRow(rowcount);
		        cellfoot = rowtfoot.createCell(tnccell);
		        cellfoot.setCellValue(new HSSFRichTextString("A/c No: 00362320001974..............IFS/RTGS Code No: HDFC0000036"));
		        cellfoot.setCellStyle(style);
		        rowcount+=2;
		        
		        rowtfoot = sheet.createRow(rowcount);
		        cellfoot = rowtfoot.createCell(tnccell);
		        cellfoot.setCellValue(new HSSFRichTextString("3. All quoted prices are valid for 30 days from date of quotation."));
		        cellfoot.setCellStyle(styleprodname);
		        ++rowcount;
		        rowtfoot = sheet.createRow(rowcount);
		        cellfoot = rowtfoot.createCell(tnccell);
		        cellfoot.setCellValue(new HSSFRichTextString("4. Please reference your Kasliwal Brothers quotation number on your purchase orders."));
		        cellfoot.setCellStyle(styleprodname);
		        ++rowcount;
		        rowtfoot = sheet.createRow(rowcount);
		        cellfoot = rowtfoot.createCell(tnccell);
		        cellfoot.setCellValue(new HSSFRichTextString("5. Please mention your CST/TIN No./GST No. in your Purchase Order."));
		        cellfoot.setCellStyle(styleprodname);
		        ++rowcount;
		        rowtfoot = sheet.createRow(rowcount);
		        cellfoot = rowtfoot.createCell(tnccell);
		        cellfoot.setCellValue(new HSSFRichTextString("6. Please mention Quotation number on your Purchase Order and send it to on "+ otherfields[5][0]+ " and copy to "+ otherfields[6][0]));
		        cellfoot.setCellStyle(styleprodname);
		        rowcount+=1;

		        if ( !(otherfields[7][0].equals("")) ) {
		        	rowtfoot = sheet.createRow(rowcount);
			        cellfoot = rowtfoot.createCell(tnccell);
			        cellfoot.setCellValue(new HSSFRichTextString("Note: "+ otherfields[7][0]));
			        cellfoot.setCellStyle(styleprodname);
			        ++rowcount;
				}
		        rowcount+=1;
		        
		        rowtfoot = sheet.createRow(rowcount);
		        cellfoot = rowtfoot.createCell(tnccell);
		        cellfoot.setCellValue(new HSSFRichTextString("Subject to Indore Jurisdiction......................FOR KASLIWAL BROTHERS,"));
		        cellfoot.setCellStyle(style);
		        ++rowcount;
		        rowtfoot = sheet.createRow(rowcount);
		        cellfoot = rowtfoot.createCell(tnccell);
		        cellfoot.setCellValue(new HSSFRichTextString("GST NO. 23AABFK2096H1Z..............Authorized Signatory"));
		        cellfoot.setCellStyle(style);
		        
		        		        
		        File xlsfile = new File(desktop+"\\"+filename+".xls");
		        workbook.write(xlsfile); 
		        workbook.close();
				response.getWriter().write("success");
		        			
			} catch (Exception e) {
				response.getWriter().write("fail");
				e.printStackTrace();
			}
		}

		if (action.equals("exportToWord")) {
			try {
				
				XWPFDocument doc = new XWPFDocument(); 
				XWPFParagraph par = doc.createParagraph();
				XWPFRun run = par.createRun();
				run.addPicture(new FileInputStream(imgFile), XWPFDocument.PICTURE_TYPE_JPEG, "logo.jpg", Units.pixelToEMU(600), Units.pixelToEMU(100)); 
				
	            XWPFParagraph paragraph = doc.createParagraph();  
	            run = paragraph.createRun(); 
	            formatter = new SimpleDateFormat("dd MMMM yyyy");
	            
	            run.addBreak();
				run.setText("Quotation  No: "+ otherfields[0][0]);
				run.addTab(); run.addTab(); run.addTab(); run.addTab(); run.addTab(); run.addTab(); 
				run.setText(formatter.format(date));
	            run.addBreak(); run.addBreak();
				
				run.setText("To: ");
				run.addBreak();
				run.setText(otherfields[1][0]); 
	            run.addBreak(); run.addBreak();
				
				run.setText("Subject: "+ otherfields[2][0]); 
	            run.addBreak(); run.addBreak();
				
				run.setText("Dear Sir,\n");
				run.addBreak();
				run.setText("In response to your enquiry, we are pleased to offer our rates as under:-\n");
				run.addBreak();
				
				int tblcol = 1, tpcell = 8;
				XWPFTable tab = doc.createTable(1, 1);
	            XWPFTableRow row = tab.getRow(0); 
	            for (int i = 0; i < 10; i++) {
	            	if (i==0) {
	            		XWPFParagraph p1 = row.getCell(0).getParagraphs().get(0);
	    				p1.setAlignment(ParagraphAlignment.CENTER);
	    				XWPFRun r1 = p1.createRun();
	    				r1.setText(tblheads[i]);
	    				r1.setBold(true);
	    				continue;
	            	}
					if (checkboxenbl_arr[i].equals("0")) 
						continue;
					
					XWPFParagraph p1 = row.addNewTableCell().getParagraphs().get(0);
					p1.setAlignment(ParagraphAlignment.CENTER);
					XWPFRun r1 = p1.createRun();
					r1.setText(tblheads[i]);
    				r1.setBold(true);
				}
	            
	            for (int i = 0; i < prodlist.length; i++) {
	            	row = tab.createRow();
			        tblcol = 0;
					for (int j = 0; j < 10; j++) {
						if (checkboxenbl_arr[j].equals("0")) 
							continue;
						
						if (j == 2) {
							row.getCell(tblcol).setText(prodlist[i][j]);
							++tblcol;
							continue;
						} 
						if (j==6 || j==7) {
							XWPFParagraph p1 = row.getCell(tblcol).getParagraphs().get(0);
							p1.setAlignment(ParagraphAlignment.CENTER);
							XWPFRun r1 = p1.createRun();
							r1.setText(prodlist[i][j]+"%");
							++tblcol;
							continue;
						}						
						XWPFParagraph p1 = row.getCell(tblcol).getParagraphs().get(0);
						p1.setAlignment(ParagraphAlignment.CENTER);
						XWPFRun r1 = p1.createRun();
						r1.setText(prodlist[i][j]);
						++tblcol;
					}
				}
	            
	            row = tab.createRow();	tblcol = 0;
	            for (int j = 0; j < 10; j++) {
					if (checkboxenbl_arr[j].equals("0")) 
						continue;

					if (j==7) {
						XWPFParagraph p1 = row.getCell(tblcol).getParagraphs().get(0);
						p1.setAlignment(ParagraphAlignment.CENTER);
						XWPFRun r1 = p1.createRun();
						r1.setText("Total Price: ");
						++tblcol;
						continue;
					}
					if (j==8) {
						XWPFParagraph p1 = row.getCell(tblcol).getParagraphs().get(0);
						p1.setAlignment(ParagraphAlignment.CENTER);
						XWPFRun r1 = p1.createRun();
						r1.setText(otherfields[8][0]);		
						++tblcol;
						continue;
					}
					row.getCell(tblcol)
						.getParagraphs().get(0)
						.createRun()
						.setText("");		
					++tblcol;
				}
	            
	            paragraph = doc.createParagraph(); 
	            paragraph.setWordWrapped(true);
	            run = paragraph.createRun(); 
	            run.addBreak(); run.addBreak(); 
	            run.setBold(true);
				run.setText("Terms and Conditions : ");
				
				paragraph = doc.createParagraph(); 
	            paragraph.setWordWrapped(true);
	            run = paragraph.createRun();
				run.setText("1. Delivery: "+otherfields[3][0]);
				run.addBreak();
				run.setText("2. Payment: "+otherfields[4][0]);
				run.addBreak(); 
				
				run.addTab();
				run.setText("HDFC BANK");
				run.addTab(); run.addTab(); run.addTab(); run.addTab();
				run.setText("Trade House Indore");
				run.addBreak(); 
				run.addTab(); 
				run.setText("A/c No: 00362320001974");
				run.addTab(); run.addTab();
				run.setText("IFS/RTGS Code No: HDFC0000036");
				run.addBreak(); run.addBreak();
				
				run.setText("3. All quoted prices are valid for 30 days from date of quotation.");
				run.addBreak();
				run.setText("4. Please reference your Kasliwal Brothers quotation number on your purchase orders.");
				run.addBreak();
				run.setText("5. Please mention your CST/TIN No./GST No. in your Purchase Order.");
				run.addBreak();
				run.setText("6. Please mention Quotation number on your Purchase Order and send it to on "+otherfields[5][0]+" and copy to "+otherfields[6][0]);
				run.addBreak(); run.addBreak();

				if ( !(otherfields[7][0].equals("")) ) {
					run.setText("Note: "+otherfields[7][0]);
					run.addBreak(); run.addBreak(); 
				}
				
				run.addTab();
				run.setText("Subject to Indore Jurisdiction");
				run.addTab(); run.addTab();
				run.setText("FOR KASLIWAL BROTHERS,");
				run.addBreak(); run.addBreak();
				run.addTab();
				run.setText("GST NO. 23AABFK2096H1ZJ");
				run.addTab(); run.addTab();
				run.setText("Authorized Signatory");
				run.addBreak(); 				
		        			
				File docfile = new File(desktop+"\\"+filename+".docx");
				FileOutputStream out = new FileOutputStream(docfile);
		        doc.write(out); 
				doc.close();
				out.flush();
				out.close();
				response.getWriter().write("success");
				
			} catch (Exception e) {
				response.getWriter().write("fail");
				e.printStackTrace();
			}
		}
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
		doGet(request, response);
	}

}


