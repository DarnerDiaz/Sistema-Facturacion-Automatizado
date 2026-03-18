package com.factura.invoice.service;

import com.factura.invoice.Invoice;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InvoicePdfService {

    private final Path storageDir;

    public InvoicePdfService(@Value("${app.pdf.storage-path:generated-pdfs}") String storagePath) {
        this.storageDir = Paths.get(storagePath);
    }

    public String generateAndStorePdf(Invoice invoice) {
        try {
            Files.createDirectories(storageDir);
            byte[] bytes = generatePdfBytes(invoice);
            String fileName = invoice.getInvoiceNumber() + ".pdf";
            Path target = storageDir.resolve(fileName);
            Files.write(target, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return target.toString();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to generate invoice PDF", ex);
        }
    }

    public byte[] loadPdf(String pdfPath) {
        if (pdfPath == null || pdfPath.isBlank()) {
            throw new IllegalArgumentException("Invoice does not have a generated PDF");
        }

        try {
            return Files.readAllBytes(Paths.get(pdfPath));
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read invoice PDF", ex);
        }
    }

    public byte[] generatePdfBytes(Invoice invoice) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
           try (PdfWriter writer = new PdfWriter(outputStream);
               PdfDocument pdfDocument = new PdfDocument(writer);
               Document document = new Document(pdfDocument)) {

            document.add(new Paragraph("Factura Electronica").setBold().setFontSize(18));
            document.add(new Paragraph("Numero: " + invoice.getInvoiceNumber()));
            document.add(new Paragraph("Fecha de emision: " + invoice.getIssueDate().format(DateTimeFormatter.ISO_DATE)));
            document.add(new Paragraph("Empresa: " + invoice.getCompany().getName()));
            document.add(new Paragraph("Cliente: " + invoice.getCustomer().getName()));
            document.add(new Paragraph("RUC/DNI Cliente: " + invoice.getCustomer().getTaxId()));

            Table table = new Table(new float[] {4, 1, 2, 1, 2});
            table.addHeaderCell("Descripcion");
            table.addHeaderCell("Cant");
            table.addHeaderCell("Precio");
            table.addHeaderCell("IGV %");
            table.addHeaderCell("Total linea");

            invoice.getItems().forEach(item -> {
                table.addCell(item.getDescription());
                table.addCell(item.getQuantity().toPlainString());
                table.addCell(item.getUnitPrice().toPlainString());
                table.addCell(item.getTaxPercentage().toPlainString());
                table.addCell(item.getLineTotal().toPlainString());
            });

            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Subtotal: PEN " + money(invoice.getSubtotal())));
            document.add(new Paragraph("IGV: PEN " + money(invoice.getTaxAmount())));
            document.add(new Paragraph("Total: PEN " + money(invoice.getTotal())).setBold());
            if (invoice.getNotes() != null && !invoice.getNotes().isBlank()) {
                document.add(new Paragraph("Notas: " + invoice.getNotes()));
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to render invoice PDF", ex);
        }

        return outputStream.toByteArray();
    }

    private String money(BigDecimal value) {
        return value == null ? "0.00" : value.setScale(2).toPlainString();
    }
}
