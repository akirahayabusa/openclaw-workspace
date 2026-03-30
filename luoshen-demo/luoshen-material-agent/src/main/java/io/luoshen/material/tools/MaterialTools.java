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
package io.luoshen.material.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 物料管理工具类
 * 
 * 提供物料查询、库存管理、采购申请等功能
 */
public class MaterialTools {
    
    private static final Random random = new Random();
    
    // 模拟物料库存数据
    private static final Map<String, MaterialInfo> materialInventory = new LinkedHashMap<>();
    
    // 模拟采购申请记录
    private static final List<PurchaseRequest> purchaseRequests = new ArrayList<>();
    
    static {
        materialInventory.put("MAT-001", new MaterialInfo("MAT-001", "电子元件A", "电子类", 1500, 100, "仓库A"));
        materialInventory.put("MAT-002", new MaterialInfo("MAT-002", "塑料外壳", "结构类", 3000, 500, "仓库B"));
        materialInventory.put("MAT-003", new MaterialInfo("MAT-003", "螺丝螺母", "五金类", 10000, 2000, "仓库C"));
        materialInventory.put("MAT-004", new MaterialInfo("MAT-004", "包装材料", "包装类", 5000, 800, "仓库A"));
        materialInventory.put("MAT-005", new MaterialInfo("MAT-005", "润滑油", "化工类", 200, 50, "仓库D"));
    }
    
    /**
     * 查询物料库存
     */
    @Tool(name = "query_material_inventory", description = "查询指定物料的库存信息")
    public String queryMaterialInventory(
            @ToolParam(name = "material_id", description = "物料ID，如 MAT-001") String materialId) {
        
        MaterialInfo info = materialInventory.get(materialId);
        if (info == null) {
            return "物料不存在: " + materialId + "\n可用物料: " + materialInventory.keySet();
        }
        
        String status = info.quantity < info.minStock ? "⚠️ 库存不足" : "✓ 库存正常";
        
        return String.format("""
                物料库存信息
                ============
                物料ID: %s
                物料名称: %s
                物料类别: %s
                当前库存: %d
                安全库存: %d
                存放位置: %s
                库存状态: %s
                查询时间: %s
                """,
                info.materialId, info.name, info.category,
                info.quantity, info.minStock, info.location, status,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }
    
    /**
     * 查询所有物料库存概览
     */
    @Tool(name = "query_all_materials", description = "查询所有物料的库存概览")
    public String queryAllMaterials() {
        StringBuilder sb = new StringBuilder();
        sb.append("物料库存概览\n");
        sb.append("============\n\n");
        
        int warningCount = 0;
        for (MaterialInfo info : materialInventory.values()) {
            String status = info.quantity < info.minStock ? "⚠️" : "✓";
            sb.append(String.format("%s %s (%s): 库存 %d, 安全库存 %d\n",
                    status, info.materialId, info.name, info.quantity, info.minStock));
            if (info.quantity < info.minStock) warningCount++;
        }
        
        sb.append("\n统计:\n");
        sb.append("- 物料总数: ").append(materialInventory.size()).append("\n");
        sb.append("- 库存不足: ").append(warningCount).append(" 种\n");
        sb.append("- 库存正常: ").append(materialInventory.size() - warningCount).append(" 种\n");
        
        if (warningCount > 0) {
            sb.append("\n⚠️ 建议及时补充库存不足的物料\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 申请物料采购
     */
    @Tool(name = "request_material_purchase", description = "提交物料采购申请")
    public String requestMaterialPurchase(
            @ToolParam(name = "material_id", description = "物料ID") String materialId,
            @ToolParam(name = "quantity", description = "采购数量") int quantity,
            @ToolParam(name = "reason", description = "采购原因") String reason) {
        
        MaterialInfo info = materialInventory.get(materialId);
        if (info == null) {
            return "物料不存在: " + materialId;
        }
        
        String requestId = "PR-" + System.currentTimeMillis();
        PurchaseRequest request = new PurchaseRequest(
                requestId, materialId, info.name, quantity, reason, LocalDateTime.now()
        );
        purchaseRequests.add(request);
        
        return String.format("""
                采购申请已提交
                ==============
                申请编号: %s
                物料ID: %s
                物料名称: %s
                采购数量: %d
                采购原因: %s
                申请时间: %s
                状态: 待审批
                
                请等待采购部门审批。
                """,
                requestId, materialId, info.name, quantity, reason,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }
    
    /**
     * 查询采购申请状态
     */
    @Tool(name = "query_purchase_requests", description = "查询采购申请状态")
    public String queryPurchaseRequests(
            @ToolParam(name = "status", description = "申请状态: pending, approved, rejected, all", required = false) String status) {
        
        if (status == null || status.isEmpty()) {
            status = "all";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("采购申请查询结果\n");
        sb.append("================\n\n");
        
        List<PurchaseRequest> filtered = purchaseRequests.stream()
                .filter(r -> "all".equals(status) || r.status.equals(status))
                .toList();
        
        if (filtered.isEmpty()) {
            sb.append("暂无相关采购申请\n");
        } else {
            for (PurchaseRequest r : filtered) {
                sb.append(String.format("- %s: %s (%d) [%s]\n",
                        r.requestId, r.materialName, r.quantity, r.status));
            }
            sb.append("\n总计: ").append(filtered.size()).append(" 条申请\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 物料出入库记录
     */
    @Tool(name = "record_material_transaction", description = "记录物料出入库操作")
    public String recordMaterialTransaction(
            @ToolParam(name = "material_id", description = "物料ID") String materialId,
            @ToolParam(name = "type", description = "操作类型: in(入库), out(出库)") String type,
            @ToolParam(name = "quantity", description = "数量") int quantity,
            @ToolParam(name = "reason", description = "原因说明") String reason) {
        
        MaterialInfo info = materialInventory.get(materialId);
        if (info == null) {
            return "物料不存在: " + materialId;
        }
        
        if ("out".equals(type)) {
            if (info.quantity < quantity) {
                return String.format("库存不足！当前库存: %d, 需要出库: %d", info.quantity, quantity);
            }
            info.quantity -= quantity;
        } else if ("in".equals(type)) {
            info.quantity += quantity;
        } else {
            return "未知操作类型: " + type + "\n支持类型: in, out";
        }
        
        return String.format("""
                物料出入库记录
                ==============
                物料ID: %s
                物料名称: %s
                操作类型: %s
                操作数量: %d
                操作原因: %s
                当前库存: %d
                操作时间: %s
                
                操作成功！
                """,
                materialId, info.name,
                "in".equals(type) ? "入库" : "出库",
                quantity, reason, info.quantity,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }
    
    /**
     * 生成物料报告
     */
    @Tool(name = "generate_material_report", description = "生成物料管理报告")
    public String generateMaterialReport(
            @ToolParam(name = "report_type", description = "报告类型: inventory, purchase, all", required = false) String reportType) {
        
        if (reportType == null || reportType.isEmpty()) {
            reportType = "all";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("物料管理报告\n");
        sb.append("============\n");
        sb.append("报告时间: ").append(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        
        if ("inventory".equals(reportType) || "all".equals(reportType)) {
            sb.append("库存概况:\n");
            sb.append("- 物料种类: ").append(materialInventory.size()).append("\n");
            sb.append("- 总库存量: ").append(materialInventory.values().stream()
                    .mapToInt(m -> m.quantity).sum()).append("\n");
            sb.append("- 库存不足: ").append(materialInventory.values().stream()
                    .filter(m -> m.quantity < m.minStock).count()).append(" 种\n\n");
        }
        
        if ("purchase".equals(reportType) || "all".equals(reportType)) {
            sb.append("采购概况:\n");
            sb.append("- 待审批: ").append(purchaseRequests.stream()
                    .filter(r -> "pending".equals(r.status)).count()).append(" 条\n");
            sb.append("- 已审批: ").append(purchaseRequests.stream()
                    .filter(r -> "approved".equals(r.status)).count()).append(" 条\n");
            sb.append("- 已拒绝: ").append(purchaseRequests.stream()
                    .filter(r -> "rejected".equals(r.status)).count()).append(" 条\n\n");
        }
        
        sb.append("建议:\n");
        sb.append("- 定期盘点库存\n");
        sb.append("- 及时补充库存不足物料\n");
        sb.append("- 优化采购流程\n");
        
        return sb.toString();
    }
    
    /**
     * 物料信息数据类
     */
    private static class MaterialInfo {
        String materialId;
        String name;
        String category;
        int quantity;
        int minStock;
        String location;
        
        MaterialInfo(String materialId, String name, String category, int quantity, int minStock, String location) {
            this.materialId = materialId;
            this.name = name;
            this.category = category;
            this.quantity = quantity;
            this.minStock = minStock;
            this.location = location;
        }
    }
    
    /**
     * 采购申请数据类
     */
    private static class PurchaseRequest {
        String requestId;
        String materialId;
        String materialName;
        int quantity;
        String reason;
        LocalDateTime time;
        String status = "pending";
        
        PurchaseRequest(String requestId, String materialId, String materialName, int quantity, String reason, LocalDateTime time) {
            this.requestId = requestId;
            this.materialId = materialId;
            this.materialName = materialName;
            this.quantity = quantity;
            this.reason = reason;
            this.time = time;
        }
    }
}