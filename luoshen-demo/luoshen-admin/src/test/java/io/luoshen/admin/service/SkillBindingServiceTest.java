/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.service;

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
 * SkillBindingService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SkillBindingServiceTest {

    @Mock
    private AgentSkillRelationRepository relationRepository;

    @Mock
    private SkillConfigRepository skillConfigRepository;

    @InjectMocks
    private SkillBindingService skillBindingService;

    private SkillConfigEntity testSkill;
    private AgentSkillRelationEntity testRelation;

    @BeforeEach
    void setUp() {
        testSkill = new SkillConfigEntity();
        testSkill.setId(1L);
        testSkill.setSkillId("test-skill");
        testSkill.setName("Test Skill");
        testSkill.setEnabled(true);

        testRelation = new AgentSkillRelationEntity();
        testRelation.setId(1L);
        testRelation.setAgentId("test-agent");
        testRelation.setSkillId("test-skill");
        testRelation.setBindMode("custom");
        testRelation.setEnabled(true);
        testRelation.setPriority(0);
    }

    @Test
    void testBindSkill_Success() {
        // Given
        when(skillConfigRepository.existsBySkillId("test-skill")).thenReturn(true);
        when(relationRepository.existsByAgentIdAndSkillId("test-agent", "test-skill")).thenReturn(false);
        when(relationRepository.save(any(AgentSkillRelationEntity.class))).thenReturn(testRelation);

        // When
        AgentSkillRelationEntity result = skillBindingService.bindSkill(
            "test-agent", "test-skill", "custom", 0, "{}"
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAgentId()).isEqualTo("test-agent");
        assertThat(result.getSkillId()).isEqualTo("test-skill");

        verify(skillConfigRepository, times(1)).existsBySkillId("test-skill");
        verify(relationRepository, times(1)).existsByAgentIdAndSkillId("test-agent", "test-skill");
        verify(relationRepository, times(1)).save(any(AgentSkillRelationEntity.class));
    }

    @Test
    void testBindSkill_SkillNotFound() {
        // Given
        when(skillConfigRepository.existsBySkillId("non-existent")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> skillBindingService.bindSkill(
            "test-agent", "non-existent", "custom", 0, "{}"
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("技能不存在");

        verify(relationRepository, never()).save(any(AgentSkillRelationEntity.class));
    }

    @Test
    void testBindSkill_AlreadyBound() {
        // Given
        when(skillConfigRepository.existsBySkillId("test-skill")).thenReturn(true);
        when(relationRepository.existsByAgentIdAndSkillId("test-agent", "test-skill")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> skillBindingService.bindSkill(
            "test-agent", "test-skill", "custom", 0, "{}"
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("技能已绑定");

        verify(relationRepository, never()).save(any(AgentSkillRelationEntity.class));
    }

    @Test
    void testUnbindSkill_Success() {
        // Given
        when(relationRepository.existsByAgentIdAndSkillId("test-agent", "test-skill")).thenReturn(true);
        doNothing().when(relationRepository).deleteByAgentIdAndSkillId("test-agent", "test-skill");

        // When
        skillBindingService.unbindSkill("test-agent", "test-skill");

        // Then
        verify(relationRepository, times(1)).existsByAgentIdAndSkillId("test-agent", "test-skill");
        verify(relationRepository, times(1)).deleteByAgentIdAndSkillId("test-agent", "test-skill");
    }

    @Test
    void testBindSkillsBatch_Success() {
        // Given
        List<String> skillIds = Arrays.asList("skill1", "skill2", "skill3");
        when(skillConfigRepository.existsBySkillId(anyString())).thenReturn(true);
        when(relationRepository.existsByAgentIdAndSkillId(anyString(), anyString())).thenReturn(false);
        when(relationRepository.save(any(AgentSkillRelationEntity.class))).thenReturn(testRelation);

        // When
        List<AgentSkillRelationEntity> results = skillBindingService.bindSkillsBatch(
            "test-agent", skillIds, "custom"
        );

        // Then
        assertThat(results).hasSize(3);
        verify(skillConfigRepository, times(3)).existsBySkillId(anyString());
        verify(relationRepository, times(3)).save(any(AgentSkillRelationEntity.class));
    }

    @Test
    void testGetAgentSkills_Success() {
        // Given
        List<AgentSkillRelationEntity> relations = Arrays.asList(testRelation);
        when(relationRepository.findByAgentIdAndEnabled("test-agent", true)).thenReturn(relations);
        when(skillConfigRepository.findBySkillId("test-skill")).thenReturn(Optional.of(testSkill));

        // When
        List<io.luoshen.admin.model.AgentSkillRelationEntity> results = skillBindingService.getAgentSkillRelations("test-agent");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSkillId()).isEqualTo("test-skill");

        verify(relationRepository, times(1)).findByAgentIdAndEnabled("test-agent", true);
    }

    @Test
    void testSetAgentSkillConfig_CustomMode() {
        // Given
        List<String> allowed = Arrays.asList("skill1", "skill2");
        when(skillConfigRepository.existsBySkillId(anyString())).thenReturn(true);
        when(relationRepository.findByAgentId("test-agent")).thenReturn(Arrays.asList());
        doNothing().when(relationRepository).deleteByAgentId("test-agent");
        when(relationRepository.save(any(AgentSkillRelationEntity.class))).thenReturn(testRelation);

        // When
        skillBindingService.setAgentSkillConfig("test-agent", "custom", allowed, null);

        // Then
        verify(relationRepository, times(1)).deleteByAgentId("test-agent");
        verify(relationRepository, times(2)).save(any(AgentSkillRelationEntity.class));
    }

    @Test
    void testToggleSkillBinding() {
        // Given
        when(relationRepository.findByAgentIdAndSkillId("test-agent", "test-skill"))
            .thenReturn(Optional.of(testRelation));
        when(relationRepository.save(any(AgentSkillRelationEntity.class))).thenReturn(testRelation);

        // When
        skillBindingService.toggleSkillBinding("test-agent", "test-skill", true);

        // Then
        assertThat(testRelation.getEnabled()).isTrue();
        verify(relationRepository, times(1)).save(testRelation);
    }

    @Test
    void testRemoveAllAgentSkills() {
        // Given
        when(relationRepository.findByAgentId("test-agent")).thenReturn(Arrays.asList(testRelation));
        doNothing().when(relationRepository).deleteByAgentId("test-agent");

        // When
        skillBindingService.removeAllAgentSkills("test-agent");

        // Then
        verify(relationRepository, times(1)).findByAgentId("test-agent");
        verify(relationRepository, times(1)).deleteByAgentId("test-agent");
    }
}
