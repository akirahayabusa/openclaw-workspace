/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.service;

import io.agentscope.skill.AgentSkill;
import io.luoshen.admin.model.AgentSkillRelationEntity;
import io.luoshen.admin.model.SkillConfigEntity;
import io.luoshen.admin.repository.AgentSkillRelationRepository;
import io.luoshen.admin.repository.SkillConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AgentSkillLoader 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AgentSkillLoaderTest {

    @Mock
    private SkillConfigRepository skillConfigRepository;

    @Mock
    private AgentSkillRelationRepository relationRepository;

    @InjectMocks
    private AgentSkillLoader agentSkillLoader;

    private SkillConfigEntity fileSkill;
    private SkillConfigEntity contentSkill;
    private AgentSkillRelationEntity relation1;
    private AgentSkillRelationEntity relation2;

    @BeforeEach
    void setUp() {
        // 文件系统技能
        fileSkill = new SkillConfigEntity();
        fileSkill.setId(1L);
        fileSkill.setSkillId("file-skill");
        fileSkill.setName("File Skill");
        fileSkill.setDescription("Skill from file system");
        fileSkill.setPackagePath("/root/.luoshen/skills/user/file-skill");
        fileSkill.setSource("user_upload");
        fileSkill.setEnabled(true);

        // 内容技能
        contentSkill = new SkillConfigEntity();
        contentSkill.setId(2L);
        contentSkill.setSkillId("content-skill");
        contentSkill.setName("Content Skill");
        contentSkill.setDescription("Skill from content");
        contentSkill.setContent("# Content Skill\n\nThis is a content-based skill");
        contentSkill.setSource("user_upload");
        contentSkill.setEnabled(true);

        // 绑定关系
        relation1 = new AgentSkillRelationEntity();
        relation1.setId(1L);
        relation1.setAgentId("test-agent");
        relation1.setSkillId("file-skill");
        relation1.setBindMode("custom");
        relation1.setEnabled(true);
        relation1.setPriority(0);

        relation2 = new AgentSkillRelationEntity();
        relation2.setId(2L);
        relation2.setAgentId("test-agent");
        relation2.setSkillId("content-skill");
        relation2.setBindMode("custom");
        relation2.setEnabled(true);
        relation2.setPriority(1);
    }

    @Test
    void testLoadSkillsForAgent_CustomMode() {
        // Given
        when(relationRepository.findByAgentIdAndEnabled("test-agent", true))
            .thenReturn(Arrays.asList(relation1, relation2));
        when(skillConfigRepository.findBySkillId("file-skill")).thenReturn(Optional.of(fileSkill));
        when(skillConfigRepository.findBySkillId("content-skill")).thenReturn(Optional.of(contentSkill));

        // When
        List<AgentSkill> skills = agentSkillLoader.loadSkillsForAgent("test-agent");

        // Then
        assertThat(skills).isNotNull();
        assertThat(skills).hasSize(2);

        verify(relationRepository, times(1)).findByAgentIdAndEnabled("test-agent", true);
        verify(skillConfigRepository, times(1)).findBySkillId("file-skill");
        verify(skillConfigRepository, times(1)).findBySkillId("content-skill");
    }

    @Test
    void testLoadSkillsForAgent_NoBindings() {
        // Given
        when(relationRepository.findByAgentIdAndEnabled("test-agent", true)).thenReturn(List.of());

        // When
        List<AgentSkill> skills = agentSkillLoader.loadSkillsForAgent("test-agent");

        // Then
        assertThat(skills).isEmpty();

        verify(relationRepository, times(1)).findByAgentIdAndEnabled("test-agent", true);
        verify(skillConfigRepository, never()).findBySkillId(anyString());
    }

    @Test
    void testLoadSkillsForAgent_SkillDisabled() {
        // Given
        when(relationRepository.findByAgentIdAndEnabled("test-agent", true))
            .thenReturn(Arrays.asList(relation1));
        when(skillConfigRepository.findBySkillId("file-skill")).thenReturn(Optional.of(fileSkill));
        fileSkill.setEnabled(false);  // 技能已禁用

        // When
        List<AgentSkill> skills = agentSkillLoader.loadSkillsForAgent("test-agent");

        // Then
        assertThat(skills).isEmpty();  // 禁用的技能不会被加载
    }

    @Test
    void testLoadSkillsForAgent_SkillNotFound() {
        // Given
        when(relationRepository.findByAgentIdAndEnabled("test-agent", true))
            .thenReturn(Arrays.asList(relation1));
        when(skillConfigRepository.findBySkillId("file-skill")).thenReturn(Optional.empty());

        // When
        List<AgentSkill> skills = agentSkillLoader.loadSkillsForAgent("test-agent");

        // Then
        assertThat(skills).isEmpty();  // 不存在的技能会被跳过
    }

    @Test
    void testLoadSkillsForAgent_InheritMode() {
        // Given
        AgentSkillRelationEntity inheritRelation = new AgentSkillRelationEntity();
        inheritRelation.setAgentId("test-agent");
        inheritRelation.setSkillId("any-skill");
        inheritRelation.setBindMode("inherit");
        inheritRelation.setEnabled(true);

        when(relationRepository.findByAgentIdAndEnabled("test-agent", true))
            .thenReturn(Arrays.asList(inheritRelation));
        when(skillConfigRepository.findByEnabled(true))
            .thenReturn(Arrays.asList(fileSkill, contentSkill));

        // When
        List<AgentSkill> skills = agentSkillLoader.loadSkillsForAgent("test-agent");

        // Then
        assertThat(skills).isNotNull();
        assertThat(skills).hasSize(2);

        verify(skillConfigRepository, times(1)).findByEnabled(true);
    }

    @Test
    void testGetAgentSkillSummary() {
        // Given
        when(relationRepository.findByAgentId("test-agent"))
            .thenReturn(Arrays.asList(relation1, relation2));

        // When
        var summary = agentSkillLoader.getAgentSkillSummary("test-agent");

        // Then
        assertThat(summary).isNotNull();
        assertThat(summary.get("agentId")).isEqualTo("test-agent");
        assertThat(summary.get("totalSkills")).isEqualTo(2);
        assertThat(summary.get("enabledSkills")).isEqualTo(2);

        verify(relationRepository, times(1)).findByAgentId("test-agent");
    }

    @Test
    void testLoadSkillsForAgent_PriorityOrdering() {
        // Given
        relation1.setPriority(1);
        relation2.setPriority(0);  // priority 更小，优先级更高

        when(relationRepository.findByAgentIdAndEnabled("test-agent", true))
            .thenReturn(Arrays.asList(relation1, relation2));
        when(skillConfigRepository.findBySkillId("file-skill")).thenReturn(Optional.of(fileSkill));
        when(skillConfigRepository.findBySkillId("content-skill")).thenReturn(Optional.of(contentSkill));

        // When
        List<AgentSkill> skills = agentSkillLoader.loadSkillsForAgent("test-agent");

        // Then
        assertThat(skills).isNotNull();
        assertThat(skills).hasSize(2);
        // priority 0 的 skill 应该在前面
        assertThat(skills.get(0).getSkillConfig().getName()).isEqualTo("content-skill");
        assertThat(skills.get(1).getSkillConfig().getName()).isEqualTo("file-skill");
    }
}
