package com.familybudget.util;

import com.familybudget.model.Income;
import com.familybudget.model.Transaction;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelExporter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static byte[] exportFullReport(List<Transaction> transactions, List<Income> incomes) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // === ЛИСТ 1: Транзакции (Расходы) ===
            Sheet transactionSheet = workbook.createSheet("Расходы");
            createTransactionsSheet(transactionSheet, transactions);

            // === ЛИСТ 2: Доходы ===
            Sheet incomeSheet = workbook.createSheet("Доходы");
            createIncomesSheet(incomeSheet, incomes);

            // === ЛИСТ 3: Сводка ===
            Sheet summarySheet = workbook.createSheet("Сводка");
            createSummarySheet(summarySheet, transactions, incomes);

            // Автоподбор ширины колонок
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (int j = 0; j < 6; j++) {
                    sheet.autoSizeColumn(j);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private static void createTransactionsSheet(Sheet sheet, List<Transaction> transactions) {
        // Заголовки
        String[] headers = {"Дата", "Категория", "Описание", "Пользователь", "Сумма (₽)"};
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Данные
        CellStyle dateStyle = createDateStyle(sheet.getWorkbook());
        CellStyle amountStyle = createAmountStyle(sheet.getWorkbook(), true); // отрицательные

        int rowNum = 1;
        for (Transaction t : transactions) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(t.getDate().format(DATE_FORMAT));
            row.createCell(1).setCellValue(t.getCategory() != null ? t.getCategory().getName() : "-");
            row.createCell(2).setCellValue(t.getDescription() != null ? t.getDescription() : "-");
            row.createCell(3).setCellValue(t.getUser() != null ?
                    (t.getUser().getDisplayName() != null ? t.getUser().getDisplayName() : t.getUser().getUsername()) : "-");
            Cell amountCell = row.createCell(4);
            amountCell.setCellValue(t.getAmount().doubleValue());
            amountCell.setCellStyle(amountStyle);
        }
    }

    private static void createIncomesSheet(Sheet sheet, List<Income> incomes) {
        String[] headers = {"Дата", "Источник", "Описание", "Получатель", "Сумма (₽)"};
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        CellStyle dateStyle = createDateStyle(sheet.getWorkbook());
        CellStyle amountStyle = createAmountStyle(sheet.getWorkbook(), false); // положительные

        int rowNum = 1;
        for (Income inc : incomes) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(inc.getDate().format(DATE_FORMAT));
            row.createCell(1).setCellValue(inc.getSource());
            row.createCell(2).setCellValue(inc.getDescription() != null ? inc.getDescription() : "-");
            row.createCell(3).setCellValue(inc.getUser() != null ?
                    (inc.getUser().getDisplayName() != null ? inc.getUser().getDisplayName() : inc.getUser().getUsername()) : "-");
            Cell amountCell = row.createCell(4);
            amountCell.setCellValue(inc.getAmount().doubleValue());
            amountCell.setCellStyle(amountStyle);
        }
    }

    private static void createSummarySheet(Sheet sheet, List<Transaction> transactions, List<Income> incomes) {
        sheet.createRow(0).createCell(0).setCellValue("ОТЧЕТ ПО СЕМЕЙНОМУ БЮДЖЕТУ");
        sheet.getRow(0).getCell(0).setCellStyle(createTitleStyle(sheet.getWorkbook()));

        Row summaryRow = sheet.createRow(2);
        summaryRow.createCell(0).setCellValue("Период:");
        summaryRow.createCell(1).setCellValue("Ноябрь 2025 - Апрель 2026");

        double totalExpenses = transactions.stream().mapToDouble(t -> t.getAmount().doubleValue()).sum();
        double totalIncomes = incomes.stream().mapToDouble(i -> i.getAmount().doubleValue()).sum();
        double balance = totalIncomes - totalExpenses;

        Row statsRow = sheet.createRow(4);
        statsRow.createCell(0).setCellValue("Всего расходов:");
        statsRow.createCell(1).setCellValue(-totalExpenses);
        statsRow.getCell(1).setCellStyle(createAmountStyle(sheet.getWorkbook(), true));

        Row statsRow2 = sheet.createRow(5);
        statsRow2.createCell(0).setCellValue("Всего доходов:");
        statsRow2.createCell(1).setCellValue(totalIncomes);
        statsRow2.getCell(1).setCellStyle(createAmountStyle(sheet.getWorkbook(), false));

        Row statsRow3 = sheet.createRow(6);
        statsRow3.createCell(0).setCellValue("Баланс:");
        Cell balanceCell = statsRow3.createCell(1);
        balanceCell.setCellValue(balance);
        balanceCell.setCellStyle(createBalanceStyle(sheet.getWorkbook(), balance));
    }

    // === СТИЛИ ===
    private static CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createDateStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        CreationHelper helper = wb.getCreationHelper();
        style.setDataFormat(helper.createDataFormat().getFormat("dd.mm.yyyy hh:mm"));
        return style;
    }

    private static CellStyle createAmountStyle(Workbook wb, boolean isExpense) {
        CellStyle style = wb.createCellStyle();
        CreationHelper helper = wb.getCreationHelper();
        style.setDataFormat(helper.createDataFormat().getFormat("#,##0.00 ₽"));
        if (isExpense) {
            Font font = wb.createFont();
            font.setColor(IndexedColors.RED.getIndex());
            style.setFont(font);
        } else {
            Font font = wb.createFont();
            font.setColor(IndexedColors.GREEN.getIndex());
            style.setFont(font);
        }
        return style;
    }

    private static CellStyle createBalanceStyle(Workbook wb, double balance) {
        CellStyle style = wb.createCellStyle();
        CreationHelper helper = wb.getCreationHelper();
        style.setDataFormat(helper.createDataFormat().getFormat("#,##0.00 ₽"));
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(balance >= 0 ? IndexedColors.GREEN.getIndex() : IndexedColors.RED.getIndex());
        style.setFont(font);
        return style;
    }

    private static CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
}