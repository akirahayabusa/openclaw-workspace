/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.service;

import io.luoshen.admin.model.SkillConfigEntity;
import io.luoshen.admin.repository.SkillConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * SkillQueryService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SkillQueryServiceTest {

    @Mock
    private SkillConfigRepository skillConfigRepository;

    @InjectMocks
    private SkillQueryService skillQueryService;

    private SkillQueryService.CreateSkillRequest createRequest;
    private SkillConfigEntity mockSkill;

    @BeforeEach
    void setUp() {
        createRequest = new SkillQueryService.CreateSkillRequest();
        createRequest.setSkillId("test-skill");
        createRequest.setName("Test Skill");
        createRequest.setDescription("Test description");
        createRequest.setContent("# Test Skill\n\nThis is a test.");
        createRequest.setCategory("dev");
        createRequest.setRiskLevel("low");

        mockSkill = new SkillConfigEntity();
        mockSkill.setId(1L);
        mockSkill.setSkillId("test-skill");
        mockSkill.setName("Test Skill");
        mockSkill.setDescription("Test description");
        mockSkill.setContent("# Test Skill\n\nThis is a test.");
        mockSkill.setCategory("dev");
        mockSkill.setRiskLevel("low");
        mockSkill.setSource("user_upload");
        mockSkill.setEnabled(true);
    }

    @Test
    void testCreateTextSkill_Success() {
        // Given
        when(skillConfigRepository.existsBySkillId("test-skill")).thenReturn(false);
        when(skillConfigRepository.save(any(SkillConfigEntity.class))).thenReturn(mockSkill);

        // When
        SkillConfigEntity result = skillQueryService.createTextSkill(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSkillId()).isEqualTo("test-skill");
        assertThat(result.getName()).isEqualTo("Test Skill");
        assertThat(result.getCategory()).isEqualTo("dev");
        assertThat(result.getRiskLevel()).isEqualTo("low");
        assertThat(result.getSource()).isEqualTo("user_upload");
        assertThat(result.getHasScripts()).isFalse();

        verify(skillConfigRepository, times(1)).existsBySkillId("test-skill");
        verify(skillConfigRepository, times(1)).save(any(SkillConfigEntity.class));
    }

    @Test
    void testCreateTextSkill_SkillIdAlreadyExists() {
        // Given
        when(skillConfigRepository.existsBySkillId("test-skill")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> skillQueryService.createTextSkill(createRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("技能 ID 已存在");

        verify(skillConfigRepository, never()).save(any(SkillConfigEntity.class));
    }

    @Test
    void testCreateTextSkill_MissingRequiredFields() {
        // Given
        SkillQueryService.CreateSkillRequest emptyRequest = new SkillQueryService.CreateSkillRequest();

        // When & Then
        assertThatThrownBy(() -> skillQueryService.createTextSkill(emptyRequest))
            .isInstanceOf(IllegalArgumentException.class);

        verify(skillConfigRepository, never()).save(any(SkillConfigEntity.class));
    }

    @Test
    void testGetSkill_Found() {
        // Given
        when(skillConfigRepository.findBySkillId("test-skill")).thenReturn(Optional.of(mockSkill));

        // When
        Optional<SkillConfigEntity> result = skillQueryService.getSkill("test-skill");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSkillId()).isEqualTo("test-skill");

        verify(skillConfigRepository, times(1)).findBySkillId("test-skill");
    }

    @Test
    void testGetSkill_NotFound() {
        // Given
        when(skillConfigRepository.findBySkillId("non-existent")).thenReturn(Optional.empty());

        // When
        Optional<SkillConfigEntity> result = skillQueryService.getSkill("non-existent");

        // Then
        assertThat(result).isEmpty();

        verify(skillConfigRepository, times(1)).findBySkillId("non-existent");
    }

    @Test
    void testDeleteSkill_Success() {
        // Given
        when(skillConfigRepository.existsBySkillId("test-skill")).thenReturn(true);
        doNothing().when(skillConfigRepository).deleteBySkillId("test-skill");

        // When
        skillQueryService.deleteSkill("test-skill");

        // Then
        verify(skillConfigRepository, times(1)).existsBySkillId("test-skill");
        verify(skillConfigRepository, times(1)).deleteBySkillId("test-skill");
    }

    @Test
    void testDeleteSkill_NotFound() {
        // Given
        when(skillConfigRepository.existsBySkillId("non-existent")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> skillQueryService.deleteSkill("non-existent"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("技能不存在");

        verify(skillConfigRepository, never()).deleteBySkillId(anyString());
    }

    @Test
    void testToggleSkillEnabled_Success() {
        // Given
        when(skillConfigRepository.findBySkillId("test-skill")).thenReturn(Optional.of(mockSkill));
        when(skillConfigRepository.save(any(SkillConfigEntity.class))).thenReturn(mockSkill);

        // When
        boolean result = skillQueryService.toggleSkillEnabled("test-skill");

        // Then
        assertThat(result).isFalse(); // Initially true, should toggle to false

        verify(skillConfigRepository, times(1)).findBySkillId("test-skill");
        verify(skillConfigRepository, times(1)).save(any(SkillConfigEntity.class));
    }
}
