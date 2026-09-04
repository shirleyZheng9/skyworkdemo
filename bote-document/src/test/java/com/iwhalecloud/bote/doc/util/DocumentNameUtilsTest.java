package com.iwhalecloud.bote.doc.util;

import com.iwhalecloud.bote.doc.common.utils.DocumentNameUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DocumentNameUtils 工具类测试
 *
 * @author Aiqing
 * @since 2025/8/15
 */
class DocumentNameUtilsTest {

  @Test
  void testGenerateDefaultDocumentNameEmptyList() {
    // 测试空列表情况
    String result = DocumentNameUtils.generateDefaultDocumentName("无标题文档", Collections.emptyList());
    assertEquals("无标题文档", result);
  }

  @Test
  void testGenerateDefaultDocumentNameNoExistingNames() {
    // 测试没有相同基础名称的情况
    List<String> existingNames = Arrays.asList("其他文档", "测试表格");
    String result = DocumentNameUtils.generateDefaultDocumentName("无标题文档", existingNames);
    assertEquals("无标题文档", result);
  }

  @Test
  void testGenerateDefaultDocumentNameFirstNumber() {
    // 测试第一个序号的情况：存在基础名称文档
    List<String> existingNames = Arrays.asList("无标题文档", "其他文档");
    String result = DocumentNameUtils.generateDefaultDocumentName("无标题文档", existingNames);
    assertEquals("无标题文档（1）", result);
  }

  @Test
  void testGenerateDefaultDocumentNameFirstNumberNoBaseName() {
    // 测试第一个序号的情况：不存在基础名称文档，但有其他序号文档
    List<String> existingNames = Arrays.asList("无标题文档（1）", "其他文档");
    String result = DocumentNameUtils.generateDefaultDocumentName("无标题文档", existingNames);
    assertEquals("无标题文档（2）", result);
  }

  @Test
  void testGenerateDefaultDocumentNameWithGap() {
    // 测试序号有间隔的情况：不存在基础名称文档，序号1被使用，序号2可用
    List<String> existingNames = Arrays.asList("无标题文档（1）", "无标题文档（3）", "其他文档");
    String result = DocumentNameUtils.generateDefaultDocumentName("无标题文档", existingNames);
    assertEquals("无标题文档（2）", result);
  }

  @Test
  void testGenerateDefaultDocumentNameNoExistingNumbers() {
    // 测试没有现有序号的情况：不存在基础名称文档，也没有序号文档
    List<String> existingNames = Arrays.asList("其他文档", "测试文档");
    String result = DocumentNameUtils.generateDefaultDocumentName("无标题文档", existingNames);
    assertEquals("无标题文档", result);
  }

  @Test
  void testGenerateDefaultDocumentNameFirstNumberDebug() {
    // 调试测试：验证逻辑是否正确
    List<String> existingNames = Arrays.asList("无标题文档", "其他文档");

    // 手动验证逻辑
    boolean hasBaseName = existingNames.stream()
      .anyMatch(name -> "无标题文档".equals(name));
    assertTrue(hasBaseName, "应该检测到存在基础名称文档");

    List<Integer> usedNumbers = existingNames.stream()
      .filter(name -> name.startsWith("无标题文档"))
      .map(name -> DocumentNameUtils.extractNumberFromName("无标题文档", name))
      .filter(Objects::nonNull)
      .sorted()
      .collect(Collectors.toList());

    assertTrue(usedNumbers.isEmpty(), "应该没有提取到序号");

    String result = DocumentNameUtils.generateDefaultDocumentName("无标题文档", existingNames);
    assertEquals("无标题文档（1）", result);
  }

  @Test
  void testGenerateDefaultDocumentNameSequentialNumbers() {
    // 测试连续序号的情况
    List<String> existingNames = Arrays.asList("无标题文档", "无标题文档（1）", "无标题文档（2）");
    String result = DocumentNameUtils.generateDefaultDocumentName("无标题文档", existingNames);
    assertEquals("无标题文档（3）", result);
  }

  @Test
  void testGenerateDefaultDocumentNameGapInNumbers() {
    // 测试序号有间隔的情况
    List<String> existingNames = Arrays.asList("无标题文档", "无标题文档（1）", "无标题文档（3）");
    String result = DocumentNameUtils.generateDefaultDocumentName("无标题文档", existingNames);
    assertEquals("无标题文档（2）", result);
  }

  @Test
  void testGenerateDefaultDocumentNameMultipleGaps() {
    // 测试多个间隔的情况
    List<String> existingNames = Arrays.asList("无标题文档", "无标题文档（1）", "无标题文档（4）", "无标题文档（7）");
    String result = DocumentNameUtils.generateDefaultDocumentName("无标题文档", existingNames);
    assertEquals("无标题文档（2）", result);
  }

  @Test
  void testGenerateDefaultDocumentNameExcelType() {
    // 测试Excel类型文档
    List<String> existingNames = Arrays.asList("无标题表格", "无标题表格（1）", "无标题表格（2）");
    String result = DocumentNameUtils.generateDefaultDocumentName("无标题表格", existingNames);
    assertEquals("无标题表格（3）", result);
  }

  @Test
  void testExtractNumberFromNameNoNumber() {
    // 测试没有序号的情况
    Integer result = DocumentNameUtils.extractNumberFromName("无标题文档", "无标题文档");
    assertNull(result);
  }

  @Test
  void testExtractNumberFromNameWithNumber() {
    // 测试有序号的情况
    Integer result = DocumentNameUtils.extractNumberFromName("无标题文档", "无标题文档（5）");
    assertEquals(5, result);
  }

  @Test
  void testExtractNumberFromNameDifferentBaseName() {
    // 测试不同基础名称的情况
    Integer result = DocumentNameUtils.extractNumberFromName("无标题文档", "无标题表格（5）");
    assertNull(result);
  }

  @Test
  void testExtractNumberFromNameInvalidFormat() {
    // 测试无效格式的情况
    Integer result = DocumentNameUtils.extractNumberFromName("无标题文档", "无标题文档（abc）");
    assertNull(result);
  }

  @Test
  void testHasNumberSuffixTrue() {
    // 测试有序号后缀的情况
    assertTrue(DocumentNameUtils.hasNumberSuffix("无标题文档（1）"));
    assertTrue(DocumentNameUtils.hasNumberSuffix("无标题表格（42）"));
  }

  @Test
  void testHasNumberSuffixFalse() {
    // 测试没有序号后缀的情况
    assertFalse(DocumentNameUtils.hasNumberSuffix("无标题文档"));
    assertFalse(DocumentNameUtils.hasNumberSuffix("其他文档"));
    assertFalse(DocumentNameUtils.hasNumberSuffix("无标题文档（abc）"));
  }

  @Test
  void testGetBaseNameWithNumber() {
    // 测试获取基础名称（有序号的情况）
    String result = DocumentNameUtils.getBaseName("无标题文档（5）");
    assertEquals("无标题文档", result);
  }

  @Test
  void testGetBaseNameWithoutNumber() {
    // 测试获取基础名称（没有序号的情况）
    String result = DocumentNameUtils.getBaseName("无标题文档");
    assertEquals("无标题文档", result);
  }

  @Test
  void testGetNumberSuffixWithNumber() {
    // 测试获取序号（有序号的情况）
    Integer result = DocumentNameUtils.getNumberSuffix("无标题文档（5）");
    assertEquals(5, result);
  }

  @Test
  void testGetNumberSuffixWithoutNumber() {
    // 测试获取序号（没有序号的情况）
    Integer result = DocumentNameUtils.getNumberSuffix("无标题文档");
    assertNull(result);
  }

  @Test
  void testComplexScenario() {
    // 测试复杂场景：删除中间文档后重用序号
    List<String> existingNames = Arrays.asList(
      "无标题文档",           // 基础名称
      "无标题文档（1）",      // 序号1
      "无标题文档（3）",      // 序号3（序号2被删除）
      "无标题文档（5）"       // 序号5（序号4被删除）
    );

    String result = DocumentNameUtils.generateDefaultDocumentName("无标题文档", existingNames);
    assertEquals("无标题文档（2）", result);
  }

  @Test
  void testMixedDocumentTypes() {
    // 测试混合文档类型的情况
    List<String> existingNames = Arrays.asList(
      "无标题文档",           // Word文档
      "无标题文档（1）",      // Word文档序号1
      "无标题表格",           // Excel表格
      "无标题表格（1）"       // Excel表格序号1
    );

    // 测试Word文档
    String wordResult = DocumentNameUtils.generateDefaultDocumentName("无标题文档", existingNames);
    assertEquals("无标题文档（2）", wordResult);

    // 测试Excel表格
    String excelResult = DocumentNameUtils.generateDefaultDocumentName("无标题表格", existingNames);
    assertEquals("无标题表格（2）", excelResult);
  }

  @Test
  void testCrossDirectoryNumberManagement() {
    // 测试跨目录序号管理的情况
    // 模拟文档库中有不同目录下的文档
    List<String> existingNames = Arrays.asList(
      "无标题文档",           // 根目录下的文档
      "无标题文档（1）",      // 文件夹A下的文档
      "无标题文档（2）",      // 文件夹B下的文档
      "无标题文档（4）",      // 文件夹C下的文档（序号3被删除）
      "无标题表格",           // 根目录下的表格
      "无标题表格（1）"       // 文件夹A下的表格
    );

    // 测试Word文档 - 应该重用被释放的序号3
    String wordResult = DocumentNameUtils.generateDefaultDocumentName("无标题文档", existingNames);
    assertEquals("无标题文档（3）", wordResult);

    // 测试Excel表格 - 应该使用序号2
    String excelResult = DocumentNameUtils.generateDefaultDocumentName("无标题表格", existingNames);
    assertEquals("无标题表格（2）", excelResult);
  }

  @Test
  void testLargeDocumentLibrary() {
    // 测试大型文档库的情况
    List<String> existingNames = Arrays.asList(
      "无标题文档",           // 基础名称
      "无标题文档（1）",      // 序号1
      "无标题文档（2）",      // 序号2
      "无标题文档（3）",      // 序号3
      "无标题文档（5）",      // 序号5（序号4被删除）
      "无标题文档（6）",      // 序号6
      "无标题文档（8）",      // 序号8（序号7被删除）
      "无标题文档（9）",      // 序号9
      "无标题文档（10）"      // 序号10
    );

    // 应该重用被释放的序号4
    String result = DocumentNameUtils.generateDefaultDocumentName("无标题文档", existingNames);
    assertEquals("无标题文档（4）", result);
  }

  @Test
  void testDocumentLibraryWithGaps() {
    // 测试文档库中有多个间隔的情况
    List<String> existingNames = Arrays.asList(
      "无标题文档",           // 基础名称
      "无标题文档（2）",      // 序号2（序号1被删除）
      "无标题文档（4）",      // 序号4（序号3被删除）
      "无标题文档（7）",      // 序号7（序号5、6被删除）
      "无标题文档（10）"      // 序号10（序号8、9被删除）
    );

    // 应该重用最小的被释放序号1
    String result = DocumentNameUtils.generateDefaultDocumentName("无标题文档", existingNames);
    assertEquals("无标题文档（1）", result);
  }

  @Test
  void testLogicVerification() {
    // 验证逻辑：当存在基础名称文档时，新文档应该使用序号1
    List<String> existingNames = Arrays.asList("无标题文档", "其他文档");

    // 手动验证逻辑步骤
    boolean hasBaseName = existingNames.stream()
      .anyMatch(name -> "无标题文档".equals(name));
    assertTrue(hasBaseName, "应该检测到存在基础名称文档");

    // 由于存在基础名称文档，新文档应该使用序号1
    String result = DocumentNameUtils.generateDefaultDocumentName("无标题文档", existingNames);
    assertEquals("无标题文档（1）", result, "当存在基础名称文档时，新文档应该使用序号1");
  }

  @Test
  void testBusinessLogicScenarios() {
    // 测试各种业务场景

    // 场景1：存在基础名称文档
    List<String> scenario1 = Arrays.asList("无标题文档", "其他文档");
    String result1 = DocumentNameUtils.generateDefaultDocumentName("无标题文档", scenario1);
    assertEquals("无标题文档（1）", result1, "场景1：存在基础名称文档，新文档使用序号1");

    // 场景2：不存在基础名称文档，序号1被使用
    List<String> scenario2 = Arrays.asList("无标题文档（1）", "其他文档");
    String result2 = DocumentNameUtils.generateDefaultDocumentName("无标题文档", scenario2);
    assertEquals("无标题文档（2）", result2, "场景2：不存在基础名称文档，序号1被使用，新文档使用序号2");

    // 场景3：不存在基础名称文档，序号1可用
    List<String> scenario3 = Arrays.asList("无标题文档（2）", "其他文档");
    String result3 = DocumentNameUtils.generateDefaultDocumentName("无标题文档", scenario3);
    assertEquals("无标题文档", result3, "场景3：不存在基础名称文档，序号1可用，新文档使用基础名称");

    // 场景4：空文档库
    List<String> scenario4 = Collections.emptyList();
    String result4 = DocumentNameUtils.generateDefaultDocumentName("无标题文档", scenario4);
    assertEquals("无标题文档", result4, "场景4：空文档库，新文档使用基础名称");
  }

  @Test
  void testRegexPattern() {
    // 测试正则表达式是否能正确匹配中文括号
    assertTrue(DocumentNameUtils.hasNumberSuffix("无标题文档（1）"));
    assertTrue(DocumentNameUtils.hasNumberSuffix("无标题表格（42）"));
    assertTrue(DocumentNameUtils.hasNumberSuffix("无标题文档(1)")); // 也支持英文括号
    assertTrue(DocumentNameUtils.hasNumberSuffix("无标题表格(42)"));

    assertFalse(DocumentNameUtils.hasNumberSuffix("无标题文档"));
    assertFalse(DocumentNameUtils.hasNumberSuffix("无标题文档（abc）"));
    assertFalse(DocumentNameUtils.hasNumberSuffix("无标题文档（1"));
    assertFalse(DocumentNameUtils.hasNumberSuffix("无标题文档1）"));
  }

  @Test
  void testExtractNumberFromNameWithChineseBrackets() {
    // 测试从中文括号的文档名称中提取序号
    Integer result1 = DocumentNameUtils.extractNumberFromName("无标题文档", "无标题文档（1）");
    assertEquals(1, result1);

    Integer result2 = DocumentNameUtils.extractNumberFromName("无标题表格", "无标题表格（42）");
    assertEquals(42, result2);

    Integer result3 = DocumentNameUtils.extractNumberFromName("无标题文档", "无标题文档(1)"); // 英文括号
    assertEquals(1, result3);
  }

  @Test
  void testRegexFix() {
    // 验证修复后的正则表达式能正确匹配中文括号
    String testName = "无标题文档（1）";

    // 测试hasNumberSuffix方法
    assertTrue(DocumentNameUtils.hasNumberSuffix(testName),
      "正则表达式应该能匹配中文括号的文档名称");

    // 测试extractNumberFromName方法
    Integer number = DocumentNameUtils.extractNumberFromName("无标题文档", testName);
    assertEquals(1, number, "应该能正确提取序号1");

    // 测试getBaseName方法
    String baseName = DocumentNameUtils.getBaseName(testName);
    assertEquals("无标题文档", baseName, "应该能正确提取基础名称");

    // 测试getNumberSuffix方法
    Integer suffix = DocumentNameUtils.getNumberSuffix(testName);
    assertEquals(1, suffix, "应该能正确提取序号后缀");
  }
}
