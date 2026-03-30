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
package io.luoshen.device.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 设备管理工具类
 * 
 * 提供设备状态查询、控制、诊断等功能
 */
public class DeviceTools {
    
    private static final Random random = new Random();
    private static final Map<String, DeviceStatus> deviceStatusMap = new HashMap<>();
    
    // 初始化模拟设备数据
    static {
        deviceStatusMap.put("device-001", new DeviceStatus("device-001", "生产线A", "running", 85.5, 1200));
        deviceStatusMap.put("device-002", new DeviceStatus("device-002", "生产线B", "idle", 0, 0));
        deviceStatusMap.put("device-003", new DeviceStatus("device-003", "包装机", "running", 92.3, 800));
        deviceStatusMap.put("device-004", new DeviceStatus("device-004", "检测仪", "warning", 45.0, 150));
    }
    
    /**
     * 查询设备状态
     */
    @Tool(name = "query_device_status", description = "查询指定设备的运行状态")
    public String queryDeviceStatus(
            @ToolParam(name = "device_id", description = "设备ID，如 device-001") String deviceId) {
        
        DeviceStatus status = deviceStatusMap.get(deviceId);
        if (status == null) {
            return "设备不存在: " + deviceId + "\n可用设备: " + deviceStatusMap.keySet();
        }
        
        return String.format("""
                设备状态报告
                ============
                设备ID: %s
                设备名称: %s
                运行状态: %s
                运行效率: %.1f%%
                运行时长: %d 分钟
                查询时间: %s
                """,
                status.deviceId,
                status.name,
                status.status,
                status.efficiency,
                status.runTime,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }
    
    /**
     * 查询所有设备状态
     */
    @Tool(name = "query_all_devices", description = "查询所有设备的状态概览")
    public String queryAllDevices() {
        StringBuilder sb = new StringBuilder();
        sb.append("所有设备状态概览\n");
        sb.append("================\n\n");
        
        for (DeviceStatus status : deviceStatusMap.values()) {
            sb.append(String.format("- %s (%s): %s, 效率 %.1f%%\n",
                    status.deviceId, status.name, status.status, status.efficiency));
        }
        
        sb.append("\n统计:\n");
        sb.append("- 运行中: ").append(deviceStatusMap.values().stream()
                .filter(s -> "running".equals(s.status)).count()).append("\n");
        sb.append("- 空闲: ").append(deviceStatusMap.values().stream()
                .filter(s -> "idle".equals(s.status)).count()).append("\n");
        sb.append("- 警告: ").append(deviceStatusMap.values().stream()
                .filter(s -> "warning".equals(s.status)).count()).append("\n");
        
        return sb.toString();
    }
    
    /**
     * 控制设备
     */
    @Tool(name = "control_device", description = "控制设备执行指定操作")
    public String controlDevice(
            @ToolParam(name = "device_id", description = "设备ID") String deviceId,
            @ToolParam(name = "action", description = "操作类型: start, stop, reset, adjust") String action,
            @ToolParam(name = "params", description = "操作参数（可选）", required = false) String params) {
        
        DeviceStatus status = deviceStatusMap.get(deviceId);
        if (status == null) {
            return "设备不存在: " + deviceId;
        }
        
        // 模拟控制操作
        switch (action.toLowerCase()) {
            case "start":
                status.status = "running";
                status.efficiency = 80 + random.nextDouble() * 15;
                return String.format("设备 %s 已启动，当前效率 %.1f%%", deviceId, status.efficiency);
            case "stop":
                status.status = "idle";
                status.efficiency = 0;
                status.runTime = 0;
                return "设备 " + deviceId + " 已停止";
            case "reset":
                status.status = "idle";
                status.efficiency = 0;
                return "设备 " + deviceId + " 已重置";
            case "adjust":
                if (params != null && !params.isEmpty()) {
                    return "设备 " + deviceId + " 参数已调整: " + params;
                }
                return "请提供调整参数";
            default:
                return "未知操作: " + action + "\n支持的操作: start, stop, reset, adjust";
        }
    }
    
    /**
     * 设备故障诊断
     */
    @Tool(name = "diagnose_device", description = "诊断设备故障原因并提供解决方案")
    public String diagnoseDevice(
            @ToolParam(name = "device_id", description = "设备ID") String deviceId,
            @ToolParam(name = "symptom", description = "故障现象描述") String symptom) {
        
        DeviceStatus status = deviceStatusMap.get(deviceId);
        if (status == null) {
            return "设备不存在: " + deviceId;
        }
        
        // 模拟诊断结果
        return String.format("""
                设备故障诊断报告
                ================
                设备ID: %s
                设备名称: %s
                故障现象: %s
                
                诊断结果:
                - 可能原因: 传感器异常、参数配置错误、机械磨损
                - 建议措施:
                  1. 检查传感器连接状态
                  2. 校准设备参数
                  3. 检查机械部件磨损情况
                - 优先级: 中等
                - 预计修复时间: 30-60 分钟
                
                请联系维护人员进行现场检查。
                """,
                deviceId, status.name, symptom
        );
    }
    
    /**
     * 生成设备报告
     */
    @Tool(name = "generate_device_report", description = "生成设备运行报告")
    public String generateDeviceReport(
            @ToolParam(name = "device_id", description = "设备ID（可选，不填则生成所有设备报告）", required = false) String deviceId) {
        
        if (deviceId != null && !deviceId.isEmpty()) {
            DeviceStatus status = deviceStatusMap.get(deviceId);
            if (status == null) {
                return "设备不存在: " + deviceId;
            }
            return generateSingleDeviceReport(status);
        }
        
        return generateAllDevicesReport();
    }
    
    private String generateSingleDeviceReport(DeviceStatus status) {
        return String.format("""
                设备运行报告
                ============
                报告时间: %s
                
                基本信息:
                - 设备ID: %s
                - 设备名称: %s
                - 当前状态: %s
                
                运行指标:
                - 运行效率: %.1f%%
                - 累计运行时长: %d 分钟
                - 故障次数: %d
                
                维护建议:
                - 定期检查传感器状态
                - 每周校准设备参数
                - 每月检查机械部件
                
                下次维护时间: 建议在 7 天内安排维护
                """,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                status.deviceId, status.name, status.status,
                status.efficiency, status.runTime, random.nextInt(3)
        );
    }
    
    private String generateAllDevicesReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("设备运行总览报告\n");
        sb.append("================\n");
        sb.append("报告时间: ").append(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        
        double avgEfficiency = deviceStatusMap.values().stream()
                .filter(s -> "running".equals(s.status))
                .mapToDouble(s -> s.efficiency)
                .average()
                .orElse(0);
        
        sb.append("总体指标:\n");
        sb.append("- 设备总数: ").append(deviceStatusMap.size()).append("\n");
        sb.append("- 平均效率: ").append(String.format("%.1f%%", avgEfficiency)).append("\n");
        sb.append("- 运行设备: ").append(deviceStatusMap.values().stream()
                .filter(s -> "running".equals(s.status)).count()).append("\n");
        
        sb.append("\n各设备详情:\n");
        for (DeviceStatus status : deviceStatusMap.values()) {
            sb.append(String.format("- %s: %s, 效率 %.1f%%\n",
                    status.name, status.status, status.efficiency));
        }
        
        return sb.toString();
    }
    
    /**
     * 设备状态数据类
     */
    private static class DeviceStatus {
        String deviceId;
        String name;
        String status;
        double efficiency;
        int runTime;
        
        DeviceStatus(String deviceId, String name, String status, double efficiency, int runTime) {
            this.deviceId = deviceId;
            this.name = name;
            this.status = status;
            this.efficiency = efficiency;
            this.runTime = runTime;
        }
    }
}