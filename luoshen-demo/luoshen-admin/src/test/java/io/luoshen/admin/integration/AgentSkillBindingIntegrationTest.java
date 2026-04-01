/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.integration;

import io.luoshen.admin.model.AgentSkillRelationEntity;
import io.luoshen.admin.model.AgentConfigEntity;
import io.luoshen.admin.model.SkillConfigEntity;
import io.luoshen.admin.repository.AgentSkillRelationRepository;
import io.luoshen.admin.repository.AgentConfigRepository;
import io.luoshen.admin.repository.SkillConfigRepository;
import io.luoshen.admin.service.AgentSkillLoader;
import io.luoshen.admin.service.SkillBindingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import io.agentscope.skill.AgentSkill;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Agent 技能绑定集成测试
 * <p>
 * 测试完整的 Agent 技能绑定流程
 */
@SpringBootTest
@Transactional
@TestTransaction
class AgentSkillBindingIntegrationTest {

    @Autowired
    private SkillConfigRepository skillConfigRepository;

    @Autowired
    private AgentConfigRepository agentConfigRepository;

    @Autowired
    private AgentSkillRelationRepository relationRepository;

    @Autowired
    private SkillBindingService skillBindingService;

    @Autowired
    private AgentSkillLoader agentSkillLoader;

    private AgentConfigEntity testAgent;
    private SkillConfigEntity skill1;
    private SkillConfigEntity skill2;

    @BeforeEach
    void setUp() {
        // 创建测试技能
        skill1 = new SkillConfigEntity();
        skill1.setSkillId("test-skill-1");
        skill1.setName("Test Skill 1");
        skill1.setDescription("第一个测试技能");
        skill1.setContent("# Test Skill 1\n\nContent 1");
        skill1.setCategory("test");
        skill1.setSource("user_upload");
        skill1.setEnabled(true);
        skill1 = skillConfigRepository.save(skill1);

        skill2 = new SkillConfigEntity();
        skill2.setSkillId("test-skill-2");
        skill2.setName("Test Skill 2");
        skill2.setDescription("第二个测试技能");
        skill2.setContent("# Test Skill 2\n\nContent 2");
        skill2.setCategory("test");
        skill2.setSource("user_upload");
        skill2.setEnabled(true);
        skill2 = skillConfigRepository.save(skill2);

        // 创建测试 Agent
        testAgent = new AgentConfigEntity();
        testAgent.setAgentId("test-agent-integration");
        testAgent.setName("Integration Test Agent");
        testAgent.setDescription("集成测试 Agent");
        testAgent.setType("SUB");
        testAgent.setParentAgentId("test-parent");
        testAgent.setEnabled(true);
        testAgent = agentConfigRepository.save(testAgent);
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        relationRepository.deleteByAgentId("test-agent-integration");
        agentConfigRepository.deleteByAgentId("test-agent-integration");
        skillConfigRepository.deleteBySkillId("test-skill-1");
        skillConfigRepository.deleteBySkillId("test-2");
    }

    @Test
    void testBindAndLoadSkills_CompleteFlow() {
        // Given: Agent 和技能已创建

        // When: 绑定技能
        AgentSkillRelationEntity relation1 = skillBindingService.bindSkill(
            "test-agent-integration",
            "test-skill-1",
            "custom",
            0,
            null
        );

        AgentSkillRelationEntity relation2 = skillBindingService.bindSkill(
            "test-agent-integration",
            "test-skill-2",
            "custom",
            1,
            null
        );

        // Then: 验证绑定
        assertThat(relation1).isNotNull();
        assertThat(relation1.getAgentId()).isEqualTo("test-agent-integration");
        assertThat(relation1.getSkillId()).isEqualTo("test-skill-1");
        assertThat(relation1.getBindMode()).isEqualTo("custom");

        // When: 加载技能
        List<AgentSkill> skills = agentSkillLoader.loadSkillsForAgent("test-agent-integration");

        // Then: 验证加载结果
        assertThat(skills).isNotNull();
        assertThat(skills).hasSize(2);

        // 验证排序（按 priority）
        assertThat(skills.get(0).getSkillConfig().getName()).isEqualTo("Test Skill 1"); // priority 0
        assertThat(skills.get(1).getSkillConfig().getName()).isEqualTo("Test Skill 2"); // priority 1
    }

    @Test
    void testBatchBindAndLoad() {
        // Given: Agent 和技能已创建

        // When: 批量绑定
        List<AgentSkillRelationEntity> relations = skillBindingService.bindSkillsBatch(
            "test-agent-integration",
            List.of("test-skill-1", "test-skill-2"),
            "custom"
        );

        // Then: 验证绑定
        assertThat(relations).hasSize(2);

        // When: 加载技能
        List<AgentSkill> skills = agentSkillLoader.loadSkillsForAgent("test-agent-integration");

        // Then: 验证加载结果
        assertThat(skills).hasSize(2);
    }

    @Test
    void testUnbindAndReload() {
        // Given: 先绑定两个技能
        skillBindingService.bindSkill("test-agent-integration", "test-skill-1", "custom", 0, null);
        skillBindingService.bindSkill("test-agent-integration", "test-skill-2", "custom", 1, null);
        List<AgentSkill> skills = agentSkillLoader.loadSkillsForAgent("test-agent-integration");
        assertThat(skills).hasSize(2);

        // When: 解绑一个技能
        skillBindingService.unbindSkill("test-agent-integration", "test-skill-1");

        // Then: 重新加载验证
        skills = agentSkillLoader.loadSkillsForAgent("test-agent-integration");
        assertThat(skills).hasSize(1);
        assertThat(skills.get(0).getSkillConfig().getName()).isEqualTo("Test Skill 2");
    }

    @Test
	void testSetAgentSkillConfig_CustomMode() {
		// Given: 设置自定义配置
		skillBindingService.setAgentSkillConfig(
			"test-agent-integration",
			"custom",
			List.of("test-skill-1", "test-skill-2"),
			null
		);

		// When: 加载技能
		List<AgentSkill> skills = agentSkillLoader.loadSkillsForAgent("test-agent-integration");

		// Then: 验证只加载指定的技能
		assertThat(skills).hasSize(2);
		assertThat(skills.get(0).getSkillConfig().getName()).isEqualTo("Test Skill 1");
		assertThat(skills.get(1).getSkillConfig().getName()).isEqualTo("Test Skill 2");
	}

    @Test
    void testSetAgentSkillConfig_NoneMode() {
        // Given: 设置为 none 模式
        skillBindingService.setAgentSkillConfig(
            "test-agent-integration",
            "none",
            null,
            null
        );

        // When: 加载技能
        List<AgentSkill> skills = agentSkillLoader.loadSkillsForAgent("test-agent-integration");

        // Then: 应该返回空列表
        assertThat(skills).isEmpty();
    }

    @Test
    void testGetAgentSkills() {
        // Given: 绑定技能
        skillBindingService.bindSkill("test-agent-integration", "test-skill-1", "custom", 0, null);
        skillBindingService.bindSkill("test-agent-integration", "test-skill-2", "custom", 1, null);

        // When: 查询 Agent 的技能
        var skills = skillBindingService.getAgentSkills("test-agent-integration");

        // Then: 验证结果
        assertThat(skills).isNotNull();
        assertThat(skills).hasSize(2);

        // 验证技能详情
        var skill1 = skills.stream().filter(s -> "test-skill-1".equals(s.get("skillId"))).findFirst();
        assertThat(skill1).isPresent();
        assertThat(skill1.get().get("name")).isEqualTo("Test Skill 1");

        var skill2 = skills.stream().filter(s -> "test-skill-2".equals(s.get("skillId"))).findFirst();
        assertThat(skill2).isPresent();
        assertThat(skill2.get().get("name")).isEqualTo("Test Skill 2");
    }

    @Test
    void testRemoveAllAgentSkills() {
        // Given: 绑定技能
        skillBindingService.bindSkill("test-agent-integration", "test-skill-1", "custom", 0, null);
        skillBindingService.bindSkill("test-agent-integration", "test-skill-2", "custom", 1, null);

        // When: 删除所有绑定
        skillBindingService.removeAllAgentSkills("test-agent-integration");

        // Then: 重新加载应该是空的
        List<AgentSkill> skills = agentSkillLoader.loadSkillsForAgent("test-agent-integration");
        assertThat(skills).isEmpty();

        // 验证数据库
        List<AgentSkillRelationEntity> relations = relationRepository.findByAgentId("test-agent-integration");
        assertThat(relations).isEmpty();
    }

    @Test
    void testToggleSkillBinding() {
        // Given: 绑定技能
        AgentSkillRelationEntity relation = skillBindingService.bindSkill(
            "test-agent-integration",
            "test-skill-1",
            "custom",
            0,
            null
        );
        assertThat(relation.getEnabled()).isTrue();

        // When: 禁用技能
        skillBindingService.toggleSkillBinding("test-agent-integration", "test-skill-1", false);

        // Then: 验证状态
        var skills = skillBindingService.getAgentSkills("test-agent-integration");
        var skill1 = skills.stream().filter(s -> "test-skill-1".equals(s.get("skillId"))).findFirst();
        assertThat(skill1).isPresent();
        assertThat(skill1.get().get("enabled")).isFalse();

        // When: 重新启用
        skillBindingService.toggleSkillBinding("test-agent-integration", "test-skill-1", true);

        // Then: 验证恢复
        skills = skillBindingService.getAgentSkills("test-agent-integration");
        skill1 = skills.stream().filter(s -> "test-skill-1".equals(s.get("skillId"))).findFirst();
        assertThat(skill1).isPresent();
        assertThat(skill1.get().get("enabled")).isTrue();
    }
}
