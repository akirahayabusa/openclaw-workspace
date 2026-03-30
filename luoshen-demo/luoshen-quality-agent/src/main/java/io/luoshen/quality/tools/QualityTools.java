/*
 * Copyright 2024-2026 Luoshen Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.luoshen.quality.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 质量管理工具类
 * 
 * 提供质量检测、问题分析、报告生成等功能
 */
public class QualityTools {
    
    private static final Random random = new Random();
    
    // 模拟质量标准
    private static final Map<String, QualityStandard> standards = new LinkedHashMap<>();
    
    // 模拟检测记录
    private static final List<InspectionRecord> inspectionRecords = new ArrayList<>();
    
    static {
        standards.put("外观", new QualityStandard("外观", "产品外观无划痕、无变形、颜色均匀", 95.0));
        standards.put("尺寸", new QualityStandard("尺寸", "产品尺寸符合设计公差要求", 98.0));
        standards.put("功能", new QualityStandard("功能", "产品功能正常，无异常", 99.0));
        standards.put("包装", new QualityStandard("包装", "包装完整，标识清晰", 97.0));
    }
    
    /**
     * 执行质量检测
     */
    @Tool(name = "inspect_quality", description = "执行产品质量检测")
    public String inspectQuality(
            @ToolParam(name = "product_id", description = "产品ID或批次号") String productId,
            @ToolParam(name = "inspection_type", description = "检测类型: full(全检), sample(抽检)", required = false) String inspectionType) {
        
        if (inspectionType == null || inspectionType.isEmpty()) {
            inspectionType = "sample";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("质量检测报告\n");
        sb.append("============\n");
        sb.append("产品/批次: ").append(productId).append("\n");
        sb.append("检测类型: ").append("full".equals(inspectionType) ? "全检" : "抽检").append("\n");
        sb.append("检测时间: ").append(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        
        sb.append("检测结果:\n");
        sb.append("----------------------------------------\n");
        
        double totalScore = 0;
        int passCount = 0;
        
        for (Map.Entry<String, QualityStandard> entry : standards.entrySet()) {
            String item = entry.getKey();
            QualityStandard standard = entry.getValue();
            
            // 模拟检测结果
            double score = 85 + random.nextDouble() * 15;
            boolean passed = score >= standard.threshold;
            
            sb.append(String.format("- %s: %.1f分 %s\n", 
                    item, score, passed ? "✓ 合格" : "✗ 不合格"));
            
            totalScore += score;
            if (passed) passCount++;
            
            // 记录检测结果
            inspectionRecords.add(new InspectionRecord(
                    productId, item, score, passed, LocalDateTime.now()
            ));
        }
        
        sb.append("----------------------------------------\n");
        sb.append(String.format("\n综合评分: %.1f分\n", totalScore / standards.size()));
        sb.append(String.format("合格项: %d/%d\n", passCount, standards.size()));
        sb.append(String.format("总体评价: %s\n", 
                passCount == standards.size() ? "合格" : "需复检"));
        
        return sb.toString();
    }
    
    /**
     * 分析质量问题
     */
    @Tool(name = "analyze_quality_issue", description = "分析质量问题原因并提供解决方案")
    public String analyzeQualityIssue(
            @ToolParam(name = "issue_description", description = "问题描述") String issueDescription,
            @ToolParam(name = "product_id", description = "相关产品ID（可选）", required = false) String productId) {
        
        return String.format("""
                质量问题分析报告
                ================
                问题描述: %s
                相关产品: %s
                分析时间: %s
                
                问题分类:
                - 类型: 工艺问题
                - 严重程度: 中等
                - 影响范围: 局部
                
                根因分析:
                1. 直接原因: 操作不规范
                2. 间接原因: 培训不足、流程不清晰
                3. 系统原因: 质量控制点设置不合理
                
                改进措施:
                短期:
                - 加强操作培训
                - 优化作业指导书
                - 增加抽检频次
                
                长期:
                - 完善质量管理体系
                - 建立预防机制
                - 引入自动化检测
                
                责任部门: 生产部、质量部
                完成时限: 7个工作日
                """,
                issueDescription,
                productId != null ? productId : "未指定",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }
    
    /**
     * 生成质量报告
     */
    @Tool(name = "generate_quality_report", description = "生成质量统计报告")
    public String generateQualityReport(
            @ToolParam(name = "period", description = "统计周期: daily, weekly, monthly", required = false) String period) {
        
        if (period == null || period.isEmpty()) {
            period = "daily";
        }
        
        // 模拟统计数据
        int totalInspections = 50 + random.nextInt(50);
        int passCount = (int) (totalInspections * (0.92 + random.nextDouble() * 0.05));
        double passRate = (double) passCount / totalInspections * 100;
        
        return String.format("""
                质量统计报告
                ============
                报告周期: %s
                生成时间: %s
                
                检测统计:
                - 检测总数: %d 次
                - 合格次数: %d 次
                - 不合格次数: %d 次
                - 合格率: %.1f%%
                
                问题分布:
                - 外观问题: %d 次
                - 尺寸问题: %d 次
                - 功能问题: %d 次
                - 包装问题: %d 次
                
                趋势分析:
                - 与上期相比: 合格率 %s
                - 主要问题: 外观问题占比最高
                - 改进建议: 加强外观检测标准
                
                下期重点:
                1. 优化外观检测流程
                2. 加强操作培训
                3. 完善质量追溯体系
                """,
                getPeriodName(period),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                totalInspections, passCount, totalInspections - passCount, passRate,
                random.nextInt(5) + 1, random.nextInt(3) + 1, random.nextInt(2) + 1, random.nextInt(3) + 1,
                random.nextBoolean() ? "上升 2.3%" : "下降 1.5%"
        );
    }
    
    /**
     * 查询质量标准
     */
    @Tool(name = "query_quality_standards", description = "查询质量标准和检测规范")
    public String queryQualityStandards(
            @ToolParam(name = "category", description = "标准类别（可选）", required = false) String category) {
        
        StringBuilder sb = new StringBuilder();
        sb.append("质量标准查询结果\n");
        sb.append("================\n\n");
        
        if (category != null && !category.isEmpty()) {
            QualityStandard standard = standards.get(category);
            if (standard != null) {
                sb.append(String.format("标准名称: %s\n", standard.name));
                sb.append(String.format("标准要求: %s\n", standard.description));
                sb.append(String.format("合格阈值: %.1f分\n", standard.threshold));
            } else {
                sb.append("未找到该类别的标准\n");
                sb.append("可用类别: ").append(standards.keySet());
            }
        } else {
            sb.append("所有质量标准:\n\n");
            for (QualityStandard standard : standards.values()) {
                sb.append(String.format("- %s: %s (阈值: %.1f分)\n",
                        standard.name, standard.description, standard.threshold));
            }
        }
        
        return sb.toString();
    }
    
    private String getPeriodName(String period) {
        return switch (period) {
            case "daily" -> "日报";
            case "weekly" -> "周报";
            case "monthly" -> "月报";
            default -> period;
        };
    }
    
    /**
     * 质量标准数据类
     */
    private static class QualityStandard {
        String name;
        String description;
        double threshold;
        
        QualityStandard(String name, String description, double threshold) {
            this.name = name;
            this.description = description;
            this.threshold = threshold;
        }
    }
    
    /**
     * 检测记录数据类
     */
    private static class InspectionRecord {
        String productId;
        String item;
        double score;
        boolean passed;
        LocalDateTime time;
        
        InspectionRecord(String productId, String item, double score, boolean passed, LocalDateTime time) {
            this.productId = productId;
            this.item = item;
            this.score = score;
            this.passed = passed;
            this.time = time;
        }
    }
}