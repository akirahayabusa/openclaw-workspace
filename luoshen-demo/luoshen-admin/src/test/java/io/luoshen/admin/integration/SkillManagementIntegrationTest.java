/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.integration;

import io.luoshen.admin.model.SkillConfigEntity;
import io.luoshen.admin.repository.SkillConfigRepository;
import io.luoshen.admin.service.SkillQueryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 技能管理集成测试
 */
@SpringBootTest
@Transactional
class SkillManagementIntegrationTest {

    @Autowired
    private SkillConfigRepository skillConfigRepository;

    @Autowired
    private SkillQueryService skillQueryService;

    private SkillConfigEntity testSkill;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        testSkill = new SkillConfigEntity();
        testSkill.setSkillId("integration-test-skill");
        testSkill.setName("Integration Test Skill");
        testSkill.setDescription("This is a test skill for integration testing");
        testSkill.setContent("# Test Skill\n\nIntegration test content");
        testSkill.setCategory("test");
        testSkill.setSource("user_upload");
        testSkill.setRiskLevel("low");
        testSkill.setEnabled(true);

        testSkill = skillConfigRepository.save(testSkill);
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        skillConfigRepository.deleteBySkillId("integration-test-skill");
    }

    @Test
    void testCreateAndRetrieveTextSkill() {
        // Given
        SkillQueryService.CreateSkillRequest request = new SkillQueryService.CreateSkillRequest();
        request.setSkillId("test-create-skill");
        request.setName("Test Create Skill");
        request.setDescription("Test create");
        request.setContent("# Test");
        request.setCategory("dev");

        // When
        SkillConfigEntity created = skillQueryService.createTextSkill(request);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.getSkillId()).isEqualTo("test-create-skill");
        assertThat(created.getName()).isEqualTo("Test Create Skill");

        // 查询验证
        var retrieved = skillQueryService.getSkill("test-create-skill");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("Test Create Skill");

        // 清理
        skillQueryService.deleteSkill("test-create-skill");
    }

    @Test
    void testListSkillsWithPagination() {
        // Given
        // 创建多个测试技能
        for (int i = 0; i < 5; i++) {
            SkillQueryService.CreateSkillRequest request = new SkillQueryService.CreateSkillRequest();
            request.setSkillId("pagination-test-" + i);
            request.setName("Pagination Test " + i);
            request.setDescription("Test " + i);
            request.setContent("# Test " + i);
            request.setCategory("test");

            skillQueryService.createTextSkill(request);
        }

        // When - 查询第一页（2条）
        Page<SkillConfigEntity> page1 = skillQueryService.listSkills(null, null, null, 0, 2);

        // Then
        assertThat(page1.getContent()).hasSize(2);
        assertThat(page1.getTotalElements()).isGreaterThanOrEqualTo(5);

        // 清理
        for (int i = 0; i < 5; i++) {
            skillQueryService.deleteSkill("pagination-test-" + i);
        }
    }

    @Test
    void testUpdateSkill() {
        // Given
        SkillQueryService.UpdateSkillRequest updateRequest = new SkillQueryService.UpdateSkillRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setDescription("Updated description");

        // When
        SkillConfigEntity updated = skillQueryService.updateSkill("integration-test-skill", updateRequest);

        // Then
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getDescription()).isEqualTo("Updated description");

        // 验证数据库中的值
        var retrieved = skillQueryService.getSkill("integration-test-skill");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("Updated Name");
    }

    @Test
    void testToggleSkillEnabled() {
        // Given
        assertThat(testSkill.getEnabled()).isTrue();

        // When - 禁用
        boolean disabled = skillQueryService.toggleSkillEnabled("integration-test-skill");

        // Then
        assertThat(disabled).isFalse();

        var retrieved = skillQueryService.getSkill("integration-test-skill");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getEnabled()).isFalse();

        // When - 启用
        boolean enabled = skillQueryService.toggleSkillEnabled("integration-test-skill");

        // Then
        assertThat(enabled).isTrue();

        retrieved = skillQueryService.getSkill("integration-test-skill");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getEnabled()).isTrue();
    }

    @Test
    void testDeleteSkill() {
        // Given
        assertThat(skillQueryService.getSkill("integration-test-skill")).isPresent();

        // When
        skillQueryService.deleteSkill("integration-test-skill");

        // Then
        assertThat(skillQueryService.getSkill("integration-test-skill")).isEmpty();
    }

    @Test
    void testSearchSkills() {
        // Given - 创建几个技能
        SkillQueryService.CreateSkillRequest request1 = new SkillQueryService.CreateSkillRequest();
        request1.setSkillId("search-test-github");
        request1.setName("GitHub Integration");
        request1.setDescription("GitHub operations");
        request1.setContent("# GitHub");
        skillQueryService.createTextSkill(request1);

        SkillQueryService.CreateSkillRequest request2 = new SkillQueryService.CreateSkillRequest();
        request2.setSkillId("search-test-git");
        request2.setName("Git Operations");
        request2.setDescription("Git version control");
        request2.setContent("# Git");
        skillQueryService.createTextSkill(request2);

        // When
        List<SkillConfigEntity> results = skillQueryService.searchSkills("git");

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results.stream().anyMatch(s -> s.getName().contains("Git"))).isTrue();

        // 清理
        skillQueryService.deleteSkill("search-test-github");
        skillQueryService.deleteSkill("search-test-git");
    }

    @Test
    void testGetAllCategories() {
        // Given - 创建不同分类的技能
        SkillQueryService.CreateSkillRequest request1 = new SkillQueryService.CreateSkillRequest();
        request1.setSkillId("category-test-dev");
        request1.setName("Dev Test");
        request1.setDescription("Test");
        request1.setContent("# Test");
        request1.setCategory("dev");
        skillQueryService.createTextSkill(request1);

        SkillQueryService.CreateSkillRequest request2 = new SkillQueryService.CreateSkillRequest();
        request2.setSkillId("category-test-ops");
        request2.setName("Ops Test");
        request2.setDescription("Test");
        request2.setContent("# Test");
        request2.setCategory("ops");
        skillQueryService.createTextSkill(request2);

        // When
        List<String> categories = skillQueryService.getAllCategories();

        // Then
        assertThat(categories).contains("dev", "ops");

        // 清理
        skillQueryService.deleteSkill("category-test-dev");
        skillQueryService.deleteSkill("category-test-ops");
    }
}
