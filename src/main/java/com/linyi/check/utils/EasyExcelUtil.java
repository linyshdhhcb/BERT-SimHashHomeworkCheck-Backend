package com.linyi.check.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.linyi.check.entity.vo.files.PlagiarizeVo;
import com.linyi.check.entity.SimilarityOutcome;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.nio.file.Paths;
import java.util.List;

public class EasyExcelUtil {

    /**
     * 导出 Excel：一个 sheet，带表头.
     *
     * @param filepath          导出的文件名
     * @param detailList        详细结果数据
     * @param sortMaxResultList 简略结果数据
     * @param plagiarizeVoList    抄袭名单数据
     */
    public static void writeExcel(String filepath, List<SimilarityOutcome> detailList, List<SimilarityOutcome> sortMaxResultList,
                                  List<PlagiarizeVo> plagiarizeVoList) {
        // 参数校验
        if (filepath == null || detailList == null || sortMaxResultList == null || plagiarizeVoList == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 文件路径处理
        filepath = Paths.get(filepath).toAbsolutePath().normalize().toString();

        try {
            // 设置表头样式
            WriteCellStyle headWriteCellStyle = createHeadCellStyle();
            // 设置内容样式
            WriteCellStyle contentWriteCellStyle = createContentCellStyle();
            // 创建样式策略
            HorizontalCellStyleStrategy cellStyleStrategy = new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);

            // 写入Excel
            try (ExcelWriter excelWriter = EasyExcel.write(filepath)
                    .registerWriteHandler(cellStyleStrategy)
                    .build()) {

                // 写入详细结果数据
                WriteSheet sheetDetail = EasyExcel.writerSheet(0, "详细结果").head(SimilarityOutcome.class).build();
                excelWriter.write(detailList, sheetDetail);

                // 写入简略结果数据
                WriteSheet sheetMax = EasyExcel.writerSheet(1, "简略结果").head(SimilarityOutcome.class).build();
                excelWriter.write(sortMaxResultList, sheetMax);

                // 写入抄袭名单数据
                WriteSheet sheetPlagiarize = EasyExcel.writerSheet(2, "抄袭名单").head(PlagiarizeVo.class).build();
                excelWriter.write(plagiarizeVoList, sheetPlagiarize);
            }
        } catch (Exception e) {
            throw new RuntimeException("文件写入失败", e);
        }
    }

    /**
     * 创建用于表头的单元格样式
     *
     * 此方法旨在设计一个特定的单元格样式，用于Excel表格中的表头行该样式设置了居中对齐、浅蓝色背景、
     * 特定字体大小，并且启用了自动换行这些样式选择是为了提高表头的可读性和视觉吸引力
     *
     * @return WriteCellStyle  返回配置好的表头单元格样式对象
     */
    private static WriteCellStyle createHeadCellStyle() {
        // 初始化表头单元格样式对象
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        // 设置水平居中对齐方式
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        // 设置填充前景色为浅蓝色，用于区分表头
        headWriteCellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());

        // 初始化表头字体样式对象
        WriteFont headWriteFont = new WriteFont();
        // 设置字体大小为10点，以适应表头内容
        headWriteFont.setFontHeightInPoints((short) 10);
        // 将配置好的字体样式应用到表头单元格样式中
        headWriteCellStyle.setWriteFont(headWriteFont);
        // 启用自动换行，以适应较长的表头文本
        headWriteCellStyle.setWrapped(true);

        // 返回配置完成的表头单元格样式对象
        return headWriteCellStyle;
    }


    /**
     * 创建用于内容的单元格样式
     *
     * 此方法用于定义Excel表格中内容单元格的样式设置
     * 主要目的是为了统一内容单元格的显示风格，如对齐方式等
     *
     * @return WriteCellStyle 返回配置好的内容单元格样式
     */
    private static WriteCellStyle createContentCellStyle() {
        // 创建一个写入单元格样式对象
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        // 设置内容单元格的水平对齐方式为居中
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        // 返回配置好的内容单元格样式
        return contentWriteCellStyle;
    }
}
