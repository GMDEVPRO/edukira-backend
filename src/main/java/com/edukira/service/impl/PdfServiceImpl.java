package com.edukira.service.impl;

import com.edukira.entity.Grade;
import com.edukira.entity.Payment;
import com.edukira.entity.School;
import com.edukira.entity.Student;
import com.edukira.enums.GradePeriod;
import com.edukira.exception.EdukiraException;
import com.edukira.repository.GradeRepository;
import com.edukira.repository.StudentRepository;
import com.edukira.service.PdfService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfServiceImpl implements PdfService {

    private final GradeRepository   gradeRepo;
    private final StudentRepository  studentRepo;

    // Cores Edukira
    private static final DeviceRgb GREEN      = new DeviceRgb(29, 158, 117);
    private static final DeviceRgb NAVY       = new DeviceRgb(11, 30, 66);
    private static final DeviceRgb LIGHT_GREY = new DeviceRgb(248, 250, 249);
    private static final DeviceRgb GREY_TEXT  = new DeviceRgb(107, 114, 128);
    private static final DeviceRgb RED        = new DeviceRgb(220, 38, 38);

    // ════════════════════════════════════════════════════════
    // BOLETIM PDF
    // ════════════════════════════════════════════════════════

    @Override
    public byte[] generateReportCard(UUID studentId, UUID schoolId, String period, String year) {
        Student student = studentRepo.findByIdAndSchoolId(studentId, schoolId)
                .orElseThrow(() -> EdukiraException.notFound("Aluno"));

        School school = student.getSchool();

        List<Grade> grades = gradeRepo
                .findByStudentIdAndSchoolIdAndPeriodAndYear(
                        studentId, schoolId,
                        GradePeriod.valueOf(period.toUpperCase()), year);

        if (grades.isEmpty()) {
            throw EdukiraException.badRequest("Nenhuma nota encontrada para este período.");
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter   writer = new PdfWriter(out);
            PdfDocument pdf    = new PdfDocument(writer);
            Document    doc    = new Document(pdf, PageSize.A4);
            doc.setMargins(36, 36, 36, 36);

            PdfFont bold    = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

            // ── CABEÇALHO ────────────────────────────────────────────
            Table header = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100));

            // Coluna esquerda — info escola
            Cell leftCell = new Cell().setBorder(Border.NO_BORDER)
                    .setPaddingBottom(12);
            leftCell.add(new Paragraph("🎓 EDUKIRA")
                    .setFont(bold).setFontSize(18).setFontColor(GREEN));
            leftCell.add(new Paragraph(school.getName())
                    .setFont(bold).setFontSize(11).setFontColor(NAVY));
            leftCell.add(new Paragraph(school.getCity() != null
                    ? school.getCity() + " · " + school.getCountry()
                    : school.getCountry())
                    .setFont(regular).setFontSize(9).setFontColor(GREY_TEXT));
            header.addCell(leftCell);

            // Coluna direita — título boletim
            Cell rightCell = new Cell().setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT).setPaddingBottom(12);
            rightCell.add(new Paragraph("BOLETIM ESCOLAR")
                    .setFont(bold).setFontSize(14).setFontColor(NAVY));
            rightCell.add(new Paragraph(formatPeriod(period) + " · " + year)
                    .setFont(regular).setFontSize(10).setFontColor(GREY_TEXT));
            rightCell.add(new Paragraph("Emitido em " + LocalDateTime.now(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " UTC")
                    .setFont(regular).setFontSize(8).setFontColor(GREY_TEXT));
            header.addCell(rightCell);
            doc.add(header);

            // Linha separadora
            doc.add(new Table(1).setWidth(UnitValue.createPercentValue(100))
                    .addCell(new Cell().setHeight(3).setBackgroundColor(GREEN)
                            .setBorder(Border.NO_BORDER)));

            // ── INFO ALUNO ───────────────────────────────────────────
            doc.add(new Paragraph("").setMarginTop(14));

            Table studentInfo = new Table(UnitValue.createPercentArray(new float[]{33, 33, 34}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBackgroundColor(LIGHT_GREY);

            studentInfo.addCell(infoCell("ALUNO(A)",
                    student.getFirstName() + " " + student.getLastName(), bold, regular));
            studentInfo.addCell(infoCell("TURMA",
                    student.getClassLevel() != null ? student.getClassLevel() : "—", bold, regular));
            studentInfo.addCell(infoCell("RESPONSÁVEL",
                    student.getGuardianName() != null ? student.getGuardianName() : "—", bold, regular));
            doc.add(studentInfo);

            // ── TABELA DE NOTAS ──────────────────────────────────────
            doc.add(new Paragraph("").setMarginTop(20));
            doc.add(new Paragraph("NOTAS DO PERÍODO")
                    .setFont(bold).setFontSize(10).setFontColor(GREY_TEXT));
            doc.add(new Paragraph("").setMarginTop(6));

            Table table = new Table(UnitValue.createPercentArray(new float[]{38, 12, 12, 12, 14, 12}))
                    .setWidth(UnitValue.createPercentValue(100));

            // Cabeçalho da tabela
            String[] headers = {"Disciplina", "Nota 1", "Nota 2", "Média", "Coef.", "Pond."};
            for (String h : headers) {
                table.addHeaderCell(new Cell()
                        .setBackgroundColor(NAVY)
                        .setBorder(Border.NO_BORDER)
                        .setPadding(8)
                        .add(new Paragraph(h)
                                .setFont(bold).setFontSize(9)
                                .setFontColor(ColorConstants.WHITE)
                                .setTextAlignment(TextAlignment.CENTER)));
            }

            BigDecimal totalPondered = BigDecimal.ZERO;
            BigDecimal totalCoef     = BigDecimal.ZERO;
            boolean    alternate     = false;

            for (Grade g : grades) {
                DeviceRgb rowBg = alternate ? LIGHT_GREY : new DeviceRgb(255, 255, 255);
                alternate = !alternate;

                BigDecimal avg  = g.getAverage() != null ? g.getAverage() : BigDecimal.ZERO;
                BigDecimal coef = g.getCoefficient() != null ? g.getCoefficient() : BigDecimal.ONE;
                BigDecimal pond = avg.multiply(coef).setScale(2, RoundingMode.HALF_UP);

                totalPondered = totalPondered.add(pond);
                totalCoef     = totalCoef.add(coef);

                DeviceRgb avgColor = avg.doubleValue() >= 10 ? NAVY : RED;

                table.addCell(gradeCell(g.getSubjectName(), rowBg, bold, regular, false, NAVY, TextAlignment.LEFT));
                table.addCell(gradeCell(fmt(g.getGrade1()), rowBg, bold, regular, false, GREY_TEXT, TextAlignment.CENTER));
                table.addCell(gradeCell(fmt(g.getGrade2()), rowBg, bold, regular, false, GREY_TEXT, TextAlignment.CENTER));
                table.addCell(gradeCell(fmt(avg), rowBg, bold, regular, true, avgColor, TextAlignment.CENTER));
                table.addCell(gradeCell(fmt(coef), rowBg, bold, regular, false, GREY_TEXT, TextAlignment.CENTER));
                table.addCell(gradeCell(fmt(pond), rowBg, bold, regular, false, NAVY, TextAlignment.CENTER));
            }

            // Linha de média geral
            BigDecimal generalAvg = totalCoef.compareTo(BigDecimal.ZERO) > 0
                    ? totalPondered.divide(totalCoef, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            DeviceRgb avgFinalColor = generalAvg.doubleValue() >= 10 ? GREEN : RED;

            table.addCell(new Cell(1, 3)
                    .setBackgroundColor(NAVY).setBorder(Border.NO_BORDER).setPadding(8)
                    .add(new Paragraph("MÉDIA GERAL DO PERÍODO")
                            .setFont(bold).setFontSize(9).setFontColor(ColorConstants.WHITE)));
            table.addCell(new Cell(1, 3)
                    .setBackgroundColor(NAVY).setBorder(Border.NO_BORDER).setPadding(8)
                    .add(new Paragraph(generalAvg + " / 20  —  " + appreciation(generalAvg))
                            .setFont(bold).setFontSize(10).setFontColor(avgFinalColor)
                            .setTextAlignment(TextAlignment.RIGHT)));

            doc.add(table);

            // ── RODAPÉ ───────────────────────────────────────────────
            doc.add(new Paragraph("").setMarginTop(24));
            Table footer = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100));

            footer.addCell(new Cell().setBorder(Border.NO_BORDER)
                    .add(new Paragraph("Assinatura do Director(a)")
                            .setFont(bold).setFontSize(9).setFontColor(GREY_TEXT))
                    .add(new Paragraph("\n\n_______________________")
                            .setFont(regular).setFontSize(9).setFontColor(GREY_TEXT)));

            footer.addCell(new Cell().setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .add(new Paragraph("Documento gerado por Edukira")
                            .setFont(regular).setFontSize(8).setFontColor(GREY_TEXT))
                    .add(new Paragraph("edukira.com")
                            .setFont(bold).setFontSize(8).setFontColor(GREEN)));

            doc.add(footer);
            doc.close();

            log.info("[PDF] Boletim gerado | aluno={} período={} ano={}", studentId, period, year);
            return out.toByteArray();

        } catch (EdukiraException e) {
            throw e;
        } catch (Exception e) {
            log.error("[PDF] Erro ao gerar boletim: {}", e.getMessage(), e);
            throw EdukiraException.badRequest("Erro ao gerar boletim PDF: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════
    // RECIBO DE PAGAMENTO PDF
    // ════════════════════════════════════════════════════════

    @Override
    public byte[] generatePaymentReceipt(Payment payment) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter   writer = new PdfWriter(out);
            PdfDocument pdf    = new PdfDocument(writer);
            Document    doc    = new Document(pdf, PageSize.A5);
            doc.setMargins(32, 32, 32, 32);

            PdfFont bold    = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

            School  school  = payment.getSchool();
            Student student = payment.getStudent();

            // ── CABEÇALHO ────────────────────────────────────────────
            doc.add(new Paragraph("🎓 EDUKIRA")
                    .setFont(bold).setFontSize(20).setFontColor(GREEN)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph(school.getName())
                    .setFont(bold).setFontSize(12).setFontColor(NAVY)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph(school.getCity() != null
                    ? school.getCity() + " · " + school.getCountry()
                    : school.getCountry())
                    .setFont(regular).setFontSize(9).setFontColor(GREY_TEXT)
                    .setTextAlignment(TextAlignment.CENTER));

            // Linha separadora
            doc.add(new Paragraph("").setMarginTop(8));
            doc.add(new Table(1).setWidth(UnitValue.createPercentValue(100))
                    .addCell(new Cell().setHeight(3).setBackgroundColor(GREEN)
                            .setBorder(Border.NO_BORDER)));

            // Título
            doc.add(new Paragraph("RECIBO DE PAGAMENTO")
                    .setFont(bold).setFontSize(14).setFontColor(NAVY)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(14));

            // Badge PAGO
            doc.add(new Paragraph("✓ PAGO")
                    .setFont(bold).setFontSize(11)
                    .setFontColor(GREEN)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(4));

            // ── VALOR DESTAQUE ───────────────────────────────────────
            doc.add(new Paragraph("").setMarginTop(10));
            doc.add(new Paragraph(payment.getAmount().toPlainString() + " " + payment.getCurrency())
                    .setFont(bold).setFontSize(28).setFontColor(GREEN)
                    .setTextAlignment(TextAlignment.CENTER));

            // ── DETALHES ─────────────────────────────────────────────
            doc.add(new Paragraph("").setMarginTop(16));

            Table details = new Table(UnitValue.createPercentArray(new float[]{45, 55}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBackgroundColor(LIGHT_GREY);

            details.addCell(receiptRow("Aluno(a)",
                    student.getFirstName() + " " + student.getLastName(), bold, regular));
            details.addCell(receiptRow("Turma",
                    student.getClassLevel() != null ? student.getClassLevel() : "—", bold, regular));
            details.addCell(receiptRow("Mês de referência",
                    payment.getMonth(), bold, regular));
            details.addCell(receiptRow("Método",
                    payment.getMethod().name(), bold, regular));
            if (payment.getTransactionId() != null) {
                details.addCell(receiptRow("ID Transação",
                        payment.getTransactionId(), bold, regular));
            }
            details.addCell(receiptRow("Data de pagamento",
                    payment.getPaidAt() != null
                            ? LocalDateTime.ofInstant(payment.getPaidAt(), ZoneOffset.UTC)
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " UTC"
                            : "—", bold, regular));

            doc.add(details);

            // ── RODAPÉ ───────────────────────────────────────────────
            doc.add(new Paragraph("").setMarginTop(20));
            doc.add(new Paragraph("Documento gerado automaticamente por Edukira · edukira.com")
                    .setFont(regular).setFontSize(8).setFontColor(GREY_TEXT)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Ref: " + payment.getId())
                    .setFont(regular).setFontSize(7).setFontColor(GREY_TEXT)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.close();
            log.info("[PDF] Recibo gerado | payment={}", payment.getId());
            return out.toByteArray();

        } catch (Exception e) {
            log.error("[PDF] Erro ao gerar recibo: {}", e.getMessage(), e);
            throw EdukiraException.badRequest("Erro ao gerar recibo PDF: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════

    private Cell infoCell(String label, String value, PdfFont bold, PdfFont regular) {
        return new Cell().setBorder(Border.NO_BORDER).setPadding(10)
                .add(new Paragraph(label)
                        .setFont(regular).setFontSize(8).setFontColor(GREY_TEXT))
                .add(new Paragraph(value)
                        .setFont(bold).setFontSize(10).setFontColor(NAVY));
    }

    private Cell gradeCell(String text, DeviceRgb bg, PdfFont bold, PdfFont regular,
                           boolean isBold, DeviceRgb color, TextAlignment align) {
        return new Cell()
                .setBackgroundColor(bg)
                .setBorderBottom(new SolidBorder(new DeviceRgb(229, 237, 233), 0.5f))
                .setBorderTop(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setPadding(7)
                .add(new Paragraph(text)
                        .setFont(isBold ? bold : regular)
                        .setFontSize(9).setFontColor(color)
                        .setTextAlignment(align));
    }

    private Cell receiptRow(String label, String value, PdfFont bold, PdfFont regular) {
        return new Cell(1, 1).setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(new DeviceRgb(229, 237, 233), 0.5f))
                .setPadding(8)
                .add(new Paragraph(label)
                        .setFont(regular).setFontSize(8).setFontColor(GREY_TEXT))
                .add(new Paragraph(value)
                        .setFont(bold).setFontSize(10).setFontColor(NAVY));
    }

    private String fmt(BigDecimal v) {
        return v != null ? v.setScale(2, RoundingMode.HALF_UP).toPlainString() : "—";
    }

    private String formatPeriod(String period) {
        return switch (period.toUpperCase()) {
            case "BIMESTRE_1" -> "1º Bimestre";
            case "BIMESTRE_2" -> "2º Bimestre";
            case "BIMESTRE_3" -> "3º Bimestre";
            default -> period;
        };
    }

    private String appreciation(BigDecimal avg) {
        if (avg == null) return "—";
        double v = avg.doubleValue();
        if (v >= 18) return "Excellent";
        if (v >= 16) return "Très bien";
        if (v >= 14) return "Bien";
        if (v >= 12) return "Assez bien";
        if (v >= 10) return "Passable";
        return "Insuffisant";
    }
}
