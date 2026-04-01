/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.luoshen.admin.model.SkillConfigEntity;
import io.luoshen.admin.repository.SkillConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * 技能包管理服务
 * <p>
 * 负责技能包的上传、解压、验证、安装
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SkillPackageService {

    private final SkillConfigRepository skillConfigRepository;
    private final ObjectMapper objectMapper;

    @Value("${luoshen.skills.storage-path:${user.home}/.luoshen/skills}")
    private String skillsStoragePath;

    @Value("${luoshen.skills.max-file-size:52428800}") // 50MB
    private long maxFileSize;

    /**
     * 上传并安装技能包
     */
    public SkillConfigEntity uploadSkillPackage(
        MultipartFile file,
        String skillId,
        String name,
        String description,
        String category,
        String subCategory,
        String tags,
        String customType
    ) throws IOException {
        // 1. 验证文件
        validateFile(file);

        // 2. 解压到临时目录
        Path tempDir = extractToTemp(file);

        try {
            // 3. 验证技能包结构
            ValidationResult validationResult = validateSkillPackage(tempDir);
            if (!validationResult.isValid()) {
                throw new IllegalArgumentException("技能包验证失败: " + String.join(", ", validationResult.getErrors()));
            }

            // 4. 提取元数据
            SkillMetadata metadata = extractMetadata(tempDir, skillId, name, description);

            // 5. 安全检查
            List<String> securityIssues = checkSecurity(tempDir);
            if (!securityIssues.isEmpty()) {
                log.warn("技能包存在安全问题: {}", securityIssues);
                // 降低风险等级
                if ("low".equals(metadata.getRiskLevel())) {
                    metadata.setRiskLevel("medium");
                }
            }

            // 6. 计算校验和
            String checksum = calculateChecksum(file.getInputStream());

            // 7. 移动到目标位置
            Path targetDir = moveSkillToTarget(tempDir, metadata.getSkillId());

            // 8. 保存到数据库
            SkillConfigEntity skill = saveSkillToDatabase(
                metadata,
                targetDir.toString(),
                file.getSize(),
                checksum,
                category,
                subCategory,
                tags,
                customType
            );

            log.info("技能包上传成功: {} ({})", skill.getSkillId(), skill.getName());
            return skill;

        } finally {
            // 清理临时目录
            cleanupTemp(tempDir);
        }
    }

    /**
     * 验证上传的文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("文件大小超过限制: " + (maxFileSize / 1024 / 1024) + "MB");
        }

        // 检查文件扩展名
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".skill") && !filename.endsWith(".zip"))) {
            throw new IllegalArgumentException("只支持 .skill 或 .zip 格式的文件");
        }
    }

    /**
     * 解压到临时目录
     */
    private Path extractToTemp(MultipartFile file) throws IOException {
        Path tempDir = Files.createTempDirectory("skill-upload-");

        try (ZipFile zipFile = new ZipFile(file.getInputStream())) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                Path entryPath = tempDir.resolve(entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    // 安全检查：防止 Zip Slip
                    if (!entryPath.normalize().startsWith(tempDir.normalize())) {
                        throw new IOException("恶意文件路径: " + entry.getName());
                    }

                    Files.createDirectories(entryPath.getParent());
                    try (InputStream in = zipFile.getInputStream(entry)) {
                        Files.copy(in, entryPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }

        return tempDir;
    }

    /**
     * 验证技能包结构
     */
    private ValidationResult validateSkillPackage(Path skillDir) throws IOException {
        ValidationResult result = new ValidationResult();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 检查必需的 SKILL.md 文件
        Path skillMd = skillDir.resolve("SKILL.md");
        if (!Files.exists(skillMd)) {
            errors.add("缺少必需的 SKILL.md 文件");
            result.setValid(false);
            result.setErrors(errors);
            return result;
        }

        // 验证 SKILL.md 格式
        String content = Files.readString(skillMd);
        if (!content.contains("---")) {
            warnings.add("SKILL.md 缺少 YAML frontmatter");
        }

        // 检查是否有大文件
        try (Stream<Path> files = Files.walk(skillDir)) {
            files.filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        long size = Files.size(file);
                        if (size > 10 * 1024 * 1024) { // 10MB
                            warnings.add(String.format("包含大文件: %s (%.2f MB)",
                                file.getFileName(), size / 1024.0 / 1024.0));
                        }
                    } catch (IOException e) {
                        // ignore
                    }
                });
        }

        // 检查是否有 README.md（不应该在技能包中）
        if (Files.exists(skillDir.resolve("README.md"))) {
            warnings.add("技能包不应包含 README.md（说明应放在 SKILL.md 中）");
        }

        result.setValid(errors.isEmpty());
        result.setErrors(errors);
        result.setWarnings(warnings);
        return result;
    }

    /**
     * 提取技能元数据
     */
    private SkillMetadata extractMetadata(Path skillDir, String skillId, String name, String description) throws IOException {
        SkillMetadata metadata = new SkillMetadata();

        // 从 SKILL.md 提取元数据
        Path skillMd = skillDir.resolve("SKILL.md");
        String content = Files.readString(skillMd);

        // 解析 YAML frontmatter
        if (content.startsWith("---")) {
            int end = content.indexOf("---", 3);
            if (end > 0) {
                String frontmatter = content.substring(3, end);
                metadata.setFrontmatter(frontmatter);

                // 提取 name 和 description
                for (String line : frontmatter.split("\n")) {
                    if (line.startsWith("name:")) {
                        String extractedName = line.substring(5).trim();
                        if (name == null || name.isEmpty()) {
                            metadata.setName(extractedName);
                        } else {
                            metadata.setName(name);
                        }
                    } else if (line.startsWith("description:")) {
                        String extractedDesc = line.substring(12).trim();
                        if (description == null || description.isEmpty()) {
                            metadata.setDescription(extractedDesc);
                        } else {
                            metadata.setDescription(description);
                        }
                    }
                }
            }
        }

        // 如果用户提供了参数，使用用户提供的值
        if (skillId != null && !skillId.isEmpty()) {
            metadata.setSkillId(skillId);
        } else {
            // 从 frontmatter 或自动生成
            if (metadata.getName() != null) {
                metadata.setSkillId(metadata.getName().toLowerCase().replaceAll("\\s+", "-"));
            } else {
                metadata.setSkillId("skill-" + System.currentTimeMillis());
            }
        }

        if (name != null && !name.isEmpty()) {
            metadata.setName(name);
        }
        if (description != null && !description.isEmpty()) {
            metadata.setDescription(description);
        }

        // 扫描脚本文件
        List<String> scriptFiles = findScriptFiles(skillDir);
        metadata.setHasScripts(!scriptFiles.isEmpty());
        try {
            metadata.setScriptFiles(objectMapper.writeValueAsString(scriptFiles));
        } catch (JsonProcessingException e) {
            log.warn("序列化脚本列表失败", e);
        }

        // 检测能力特征
        Map<String, Boolean> capabilities = new HashMap<>();
        capabilities.put("hasScripts", !scriptFiles.isEmpty());
        capabilities.put("hasAssets", Files.exists(skillDir.resolve("assets")));
        capabilities.put("hasReferences", Files.exists(skillDir.resolve("references")));
        try {
            metadata.setCapabilities(objectMapper.writeValueAsString(capabilities));
        } catch (JsonProcessingException e) {
            log.warn("序列化能力特征失败", e);
        }

        // 默认风险等级
        metadata.setRiskLevel("low");

        return metadata;
    }

    /**
     * 查找所有脚本文件
     */
    private List<String> findScriptFiles(Path skillDir) throws IOException {
        List<String> scriptExtensions = List.of(".sh", ".bash", ".py", ".js", ".ts", ".java", ".go", ".rs");
        List<String> scripts = new ArrayList<>();

        try (Stream<Path> files = Files.walk(skillDir)) {
            files.filter(Files::isRegularFile)
                .filter(file -> {
                    String fileName = file.getFileName().toString().toLowerCase();
                    return scriptExtensions.stream().anyMatch(fileName::endsWith);
                })
                .forEach(file -> scripts.add(skillDir.relativize(file).toString()));
        }

        return scripts;
    }

    /**
     * 安全检查
     */
    private List<String> checkSecurity(Path skillDir) throws IOException {
        List<String> issues = new ArrayList<>();

        // 检测敏感文件
        List<String> sensitivePatterns = List.of(".ssh", ".aws", "token", "password", "secret", "key");
        try (Stream<Path> files = Files.walk(skillDir)) {
            files.filter(Files::isRegularFile)
                .forEach(file -> {
                    String fileName = file.getFileName().toString().toLowerCase();
                    for (String pattern : sensitivePatterns) {
                        if (fileName.contains(pattern)) {
                            issues.add("检测到敏感文件: " + file.getFileName());
                        }
                    }
                });
        }

        // 检测脚本中的危险操作
        List<Path> scripts;
        try {
            scripts = findScriptFiles(skillDir).stream()
                .map(skillDir::resolve)
                .toList();
        } catch (IOException e) {
            return issues;
        }

        for (Path script : scripts) {
            try {
                String content = Files.readString(script);

                // 危险模式
                if (content.contains("rm -rf") || content.contains("rm -Rf")) {
                    issues.add("脚本包含危险删除操作: " + script.getFileName());
                }
                if (content.contains("eval(")) {
                    issues.add("脚本包含动态执行: " + script.getFileName());
                }
            } catch (IOException e) {
                // ignore
            }
        }

        return issues;
    }

    /**
     * 计算文件的 SHA-256 校验和
     */
    private String calculateChecksum(InputStream inputStream) throws IOException {
        return DigestUtils.sha256Hex(inputStream);
    }

    /**
     * 移动技能到目标位置
     */
    private Path moveSkillToTarget(Path tempDir, String skillId) throws IOException {
        Path userSkillsDir = Paths.get(skillsStoragePath, "user");
        Files.createDirectories(userSkillsDir);

        Path targetDir = userSkillsDir.resolve(skillId);

        // 如果已存在，先删除
        if (Files.exists(targetDir)) {
            deleteDirectory(targetDir);
        }

        Files.move(tempDir, targetDir, StandardCopyOption.REPLACE_EXISTING);
        return targetDir;
    }

    /**
     * 删除目录
     */
    private void deleteDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            try (Stream<Path> paths = Files.walk(dir)) {
                paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.warn("删除文件失败: {}", path, e);
                        }
                    });
            }
        }
    }

    /**
     * 清理临时目录
     */
    private void cleanupTemp(Path tempDir) {
        try {
            deleteDirectory(tempDir);
        } catch (IOException e) {
            log.warn("清理临时目录失败: {}", tempDir, e);
        }
    }

    /**
     * 保存技能到数据库
     */
    private SkillConfigEntity saveSkillToDatabase(
        SkillMetadata metadata,
        String packagePath,
        Long fileSize,
        String checksum,
        String category,
        String subCategory,
        String tags,
        String customType
    ) {
        SkillConfigEntity skill = new SkillConfigEntity();
        skill.setSkillId(metadata.getSkillId());
        skill.setName(metadata.getName());
        skill.setDescription(metadata.getDescription());
        skill.setPackagePath(packagePath);
        skill.setSource("user_upload");
        skill.setRiskLevel(metadata.getRiskLevel());
        skill.setFileSize(fileSize);
        skill.setChecksum(checksum);
        skill.setHasScripts(metadata.getHasScripts());
        skill.setScriptFiles(metadata.getScriptFiles());
        skill.setCapabilities(metadata.getCapabilities());
        skill.setEnabled(true);

        // 新增字段
        skill.setCategory(category);
        skill.setSubCategory(subCategory);
        skill.setTags(tags);
        skill.setCustomType(customType);

        return skillConfigRepository.save(skill);
    }

    /**
     * 验证结果
     */
    @Data
    private static class ValidationResult {
        private boolean valid = true;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
    }

    /**
     * 技能元数据
     */
    @Data
    private static class SkillMetadata {
        private String skillId;
        private String name;
        private String description;
        private String frontmatter;
        private boolean hasScripts;
        private String scriptFiles;
        private String capabilities;
        private String riskLevel;
    }
}
