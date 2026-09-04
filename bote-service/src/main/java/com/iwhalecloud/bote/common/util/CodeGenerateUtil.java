package com.iwhalecloud.bote.common.util;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.dto.base.CodeGenerateDefinition;
import com.iwhalecloud.bote.dto.base.TableColumnDefinition;
import com.iwhalecloud.bote.dto.base.TableDefinition;
import com.iwhalecloud.bss.litchi.util.DateUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.ui.freemarker.SpringTemplateLoader;

/**
 * 代码生成器
 *
 * @author chen.linfa
 * @since 2024-09-12
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class CodeGenerateUtil {
  private CodeGenerateUtil() {
  }

  private static final Logger logger = LoggerFactory.getLogger(CodeGenerateUtil.class);

  /** FreeMarker 配置实例 */
  private static final Configuration configuration = buildFreemarkerConfiguration();

  //@formatter:off
  /** controller 包名 */
  private static final String PACKAGE_CONTROLLER = "controller";
  /** service 包名 */
  private static final String PACKAGE_SERVICE = "service";
  /** impl 包名 */
  private static final String PACKAGE_IMPL = "impl";
  /** mapper 包名 */
  private static final String PACKAGE_MAPPER = "mapper";
  /** dto 包名 */
  private static final String PACKAGE_DTO = "dto";
  /** query 包名 */
  private static final String PACKAGE_QUERY = "query";
  /** entity 包名 */
  private static final String PACKAGE_ENTITY = "entity";
  /** diffc 包名 */
  private static final String PACKAGE_DIFFC = "diffc";
  /** diffc - factory 包名 */
  private static final String PACKAGE_DIFFC_FACTORY = "factory";
  /** diffc - persist 包名 */
  private static final String PACKAGE_DIFFC_PERSIST = "persist";

  /** 模板文件路径 */
  private static final String CONTROLLER_TTL_PATH = "controller.java.ftl";
  private static final String SERVICE_TTL_PATH = "service.java.ftl";
  private static final String SERVICE_IMPL_TTL_PATH = "serviceImpl.java.ftl";
  private static final String MAPPER_TTL_PATH = "mapper.java.ftl";
  private static final String MAPPER_XML_TTL_PATH = "mapper.xml.ftl";
  private static final String DTO_TTL_PATH = "dto.java.ftl";
  private static final String ENTITY_TTL_PATH = "entity.java.ftl";
  private static final String QUERY_PARAM_TTL_PATH = "queryParams.java.ftl";
  private static final String DIFFC_PERSIST_FACTORY_TTL_PATH = "diffc_PersistenceFactoryInitializer.ftl";
  private static final String DIFFC_ID_SUPPLIER_FACTORY_TTL_PATH = "diffc_BaseIDSupplierFactoryInitializer.ftl";
  private static final String DIFFC_PERSIST_IMPL_TTL_PATH = "diffc_DifferentPersistence.ftl";

  /** 点分隔符 */
  private static final String DOT = ".";

  /** 文件扩展名.java */
  private static final String SUFFIX_JAVA = ".java";
  /** 文件扩展名.xml */
  private static final String SUFFIX_XML = ".xml";

  //@formatter:on

  /**
   * 生成代码
   *
   * @param definition 代码生成参数
   */
  public static void generate(CodeGenerateDefinition definition) {
    // 初始化工作空间
    createWorkspec(definition);

    List<Map<String, Object>> dataModels = new ArrayList<>();
    for (TableDefinition table : ListUtils.emptyIfNull(definition.getTables())) {
      // 按照表粒度构造动态参数，生成文件
      Map<String, Object> dataModel = setDataModel(definition, table);
      dataModels.add(dataModel);
      for (Pair<String, String> pair : definition.getFiles()) {
        write(dataModel, pair.getLeft(), pair.getRight());
      }
    }

    // 生成多表公共文件
    Map<String, Object> all = new HashMap<>(8);
    all.put("tableParams", dataModels);
    all.put("author", definition.getAuthor());
    all.put("currentTime", definition.getCurrentTime());
    all.put("package", ImmutableMap.of("diffcfactory", joinPackage(definition.getPackageDir(), PACKAGE_DIFFC, PACKAGE_DIFFC_FACTORY)));

    write(all, DIFFC_PERSIST_FACTORY_TTL_PATH,
      PathUtil.resolvePath(definition.getDecompressDir(), PACKAGE_DIFFC, PACKAGE_DIFFC_FACTORY, "PersistenceFactoryInitializer.java").toString());
    write(all, DIFFC_ID_SUPPLIER_FACTORY_TTL_PATH,
      PathUtil.resolvePath(definition.getDecompressDir(), PACKAGE_DIFFC, PACKAGE_DIFFC_FACTORY, "BaseIDSupplierFactoryInitializer.java").toString());

    // 压缩数据包
    compress(definition);
  }

  /**
   * 新建工作空间
   *
   * @param definition 条件
   */
  private static void createWorkspec(CodeGenerateDefinition definition) {
    try {
      String temp = Files.createTempDirectory("code-generate-").toString();
      Path baseDir = PathUtil.resolvePath(temp);

      // 压缩数据包，使用到的工作目录
      Path compressDir = PathUtil.resolvePath(baseDir.toString(), "compressDir");
      // 解压数据包，使用到的工作目录，会在此目录存储、采集数据
      Path decompressDir = PathUtil.resolvePath(baseDir.toString(), "decompressDir");

      FileUtils.forceMkdir(compressDir.toFile());
      FileUtils.forceMkdir(decompressDir.toFile());

      FileUtils.forceMkdir(PathUtil.resolvePath(decompressDir.toString(), PACKAGE_ENTITY).toFile());
      FileUtils.forceMkdir(PathUtil.resolvePath(decompressDir.toString(), PACKAGE_DTO).toFile());
      FileUtils.forceMkdir(PathUtil.resolvePath(decompressDir.toString(), PACKAGE_DTO, PACKAGE_QUERY).toFile());
      FileUtils.forceMkdir(PathUtil.resolvePath(decompressDir.toString(), PACKAGE_CONTROLLER).toFile());
      FileUtils.forceMkdir(PathUtil.resolvePath(decompressDir.toString(), PACKAGE_SERVICE).toFile());
      FileUtils.forceMkdir(PathUtil.resolvePath(decompressDir.toString(), PACKAGE_SERVICE, PACKAGE_IMPL).toFile());
      FileUtils.forceMkdir(PathUtil.resolvePath(decompressDir.toString(), PACKAGE_MAPPER).toFile());
      FileUtils.forceMkdir(PathUtil.resolvePath(decompressDir.toString(), PACKAGE_DIFFC).toFile());
      FileUtils.forceMkdir(PathUtil.resolvePath(decompressDir.toString(), PACKAGE_DIFFC, PACKAGE_DIFFC_FACTORY).toFile());
      FileUtils.forceMkdir(PathUtil.resolvePath(decompressDir.toString(), PACKAGE_DIFFC, PACKAGE_DIFFC_PERSIST, PACKAGE_IMPL).toFile());

      definition.setCompressDir(compressDir.toString());
      definition.setDecompressDir(decompressDir.toString());
      definition.setCurrentTime(DateUtil.formatDate());
    }
    catch (Exception e) {
      throw BaseErrorConstant.CREATE_CODE_GENERATE_WORKSPACE_ERROR.toException(e);
    }
  }

  /**
   * 清空工作空间
   */
  public static void clearWorkspace(CodeGenerateDefinition definition) {
    try {
      Path compressDir = PathUtil.resolvePath(definition.getCompressDir());
      File parentDir = compressDir.toFile().getParentFile();
      FileUtils.deleteQuietly(parentDir);
    }
    catch (Exception e) {
      throw BaseErrorConstant.CLEAR_CODE_GENERATE_WORKSPACE_ERROR.toException(e);
    }
  }

  /**
   * 将 decompressDir 目录下是数据包，进行压缩，压缩后的 zip 存储到 compressDir 目录下
   */
  public static void compress(CodeGenerateDefinition definition) {
    String fileName = "代码生成器";
    Path path = PathUtil.resolvePath(definition.getCompressDir(), fileName);
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(path))) {
      ZipUtil.zipDirectory(PathUtil.resolvePath(definition.getDecompressDir()), zos);
    }
    catch (IOException e) {
      logger.error("Failed to compress data. error={}", e.getMessage(), e);
    }
  }

  /**
   * 读取压缩包的内容
   */
  public static byte[] readZipToByteArray(CodeGenerateDefinition definition) {
    String fileName = "代码生成器";
    Path path = PathUtil.resolvePath(definition.getCompressDir(), fileName);
    try {
      return FileUtils.readFileToByteArray(path.toFile());
    }
    catch (IOException e) {
      logger.error("Failed to read zip to byte array. error={}", e.getMessage(), e);
    }
    return new byte[0];
  }

  /**
   * 按照表的粒度，组装 freemarker 动态参数
   *
   * @param table 表定义
   * @return 动态参数
   */
  private static Map<String, Object> setDataModel(CodeGenerateDefinition definition, TableDefinition table) {
    Map<String, Object> dataModel = new HashMap<>(16);
    dataModel.put("package", buildPackageInfo(definition.getPackageDir(), definition.getSubDir()));
    dataModel.put("table", buildTableInfo(table));

    // 添加类名
    String entityDesc = table.getEntityDesc();
    String entityCode = table.getEntityCode();
    String entityName = String.format("%sEntity", entityCode);
    String dtoName = String.format("%sDTO", entityCode);
    String queryParamsName = String.format("%sQueryParams", entityCode);
    String controllerName = String.format("%sManageController", entityCode);
    String serviceName = String.format("I%sManageService", entityCode);
    String serviceImplName = String.format("%sManageServiceImpl", entityCode);
    String mapperName = String.format("%sManageMapper", entityCode);

    String diffcPersistName = String.format("%sDifferencePersistence", entityCode);
    dataModel.put("entityDesc", entityDesc);
    dataModel.put("entityCode", entityCode);
    dataModel.put("entityName", entityName);
    dataModel.put("dtoName", dtoName);
    dataModel.put("queryParamsName", queryParamsName);
    dataModel.put("controllerName", controllerName);
    dataModel.put("serviceName", serviceName);
    dataModel.put("serviceImplName", serviceImplName);
    dataModel.put("mapperName", mapperName);
    dataModel.put("diffcPersistName", diffcPersistName);
    dataModel.put("sequenceName", table.getSequenceCode());
    dataModel.put("author", definition.getAuthor());
    dataModel.put("currentTime", definition.getCurrentTime());

    String baseDir = definition.getDecompressDir();
    List<Pair<String, String>> files = new ArrayList<>();
    files.add(Pair.of(ENTITY_TTL_PATH, PathUtil.resolvePath(baseDir, PACKAGE_ENTITY, entityName + SUFFIX_JAVA).toString()));
    files.add(Pair.of(DTO_TTL_PATH, PathUtil.resolvePath(baseDir, PACKAGE_DTO, dtoName + SUFFIX_JAVA).toString()));
    files.add(Pair.of(QUERY_PARAM_TTL_PATH, PathUtil.resolvePath(baseDir, PACKAGE_DTO, PACKAGE_QUERY, queryParamsName + SUFFIX_JAVA).toString()));
    files.add(Pair.of(CONTROLLER_TTL_PATH, PathUtil.resolvePath(baseDir, PACKAGE_CONTROLLER, controllerName + SUFFIX_JAVA).toString()));
    files.add(Pair.of(SERVICE_TTL_PATH, PathUtil.resolvePath(baseDir, PACKAGE_SERVICE, serviceName + SUFFIX_JAVA).toString()));
    files.add(Pair.of(SERVICE_IMPL_TTL_PATH, PathUtil.resolvePath(baseDir, PACKAGE_SERVICE, PACKAGE_IMPL, serviceImplName + SUFFIX_JAVA).toString()));
    files.add(Pair.of(MAPPER_TTL_PATH, PathUtil.resolvePath(baseDir, PACKAGE_MAPPER, mapperName + SUFFIX_JAVA).toString()));
    files.add(Pair.of(MAPPER_XML_TTL_PATH, PathUtil.resolvePath(baseDir, PACKAGE_MAPPER, mapperName + SUFFIX_XML).toString()));
    files.add(Pair.of(DIFFC_PERSIST_IMPL_TTL_PATH,
      PathUtil.resolvePath(baseDir, PACKAGE_DIFFC, PACKAGE_DIFFC_PERSIST, PACKAGE_IMPL, diffcPersistName + SUFFIX_JAVA).toString()));
    definition.setFiles(files);

    return dataModel;
  }

  /**
   * 构建包信息
   *
   * @param basePackage 父包名
   * @return 包信息
   */
  private static Map<String, String> buildPackageInfo(String basePackage, String subDir) {
    Map<String, String> packageInfo = new HashMap<>(16);
    packageInfo.put("entity", joinPackage(basePackage, PACKAGE_ENTITY, subDir));
    packageInfo.put("dto", joinPackage(basePackage, PACKAGE_DTO, subDir));
    packageInfo.put("query", joinPackage(basePackage, PACKAGE_QUERY, subDir));
    packageInfo.put("controller", joinPackage(basePackage, PACKAGE_CONTROLLER, subDir));
    packageInfo.put("service", joinPackage(basePackage, PACKAGE_SERVICE, subDir));
    packageInfo.put("serviceimpl", joinPackage(basePackage, PACKAGE_SERVICE, subDir, PACKAGE_IMPL));
    packageInfo.put("mapper", joinPackage(basePackage, PACKAGE_MAPPER, subDir));
    packageInfo.put("diffc", joinPackage(basePackage, PACKAGE_DIFFC));
    packageInfo.put("diffcpersistimpl", joinPackage(basePackage, PACKAGE_DIFFC, PACKAGE_DIFFC_PERSIST, PACKAGE_IMPL));
    return packageInfo;
  }

  /**
   * 连接父子包路径
   *
   * @param parent 父包路径
   * @param children 子包路径
   * @return 连接后的包名
   */
  private static String joinPackage(String parent, String... children) {
    String child = StringUtils.join(children, DOT);
    return StringUtils.isBlank(parent) ? child : (StringUtils.stripEnd(parent, DOT) + DOT + child);
  }

  /**
   * ${table.name} ${table.fields} ${field.propertyType} ${field.propertyName} ${field.columnType} ${field.columnName} ${table.primaryKey}
   * ${primaryKey.propertyName} ${primaryKey.columnName}
   *
   * @param table 表参数
   * @return 数据集
   */
  private static Map<String, Object> buildTableInfo(TableDefinition table) {
    Map<String, Object> tableMap = new HashMap<>(3);
    tableMap.put("name", table.getTableCode());
    tableMap.put("fields", table.getColumns());
    tableMap.put("containDateColumn", Boolean.TRUE.equals(table.getContainDateColumn()));
    Map<Boolean, List<TableColumnDefinition>> fieldGroups = table.getColumns().stream()
      .collect(Collectors.groupingBy(p -> Boolean.TRUE.equals(p.getIsPrimaryKey())));
    tableMap.put("fieldsWithoutPrimaryKey", fieldGroups.get(false));
    tableMap.put("primaryKey", fieldGroups.get(true).get(0));
    return tableMap;
  }

  /**
   * 模版代码生成
   *
   * @param objectMap 需要填充到模版文件的数据
   * @param templatePath 模版文件路径
   * @param outputFile 代码文件输出路径
   */
  @SuppressFBWarnings("TEMPLATE_INJECTION_FREEMARKER")
  private static void write(Map<String, Object> objectMap, String templatePath, String outputFile) {
    if (StringUtils.isAllBlank(templatePath, outputFile)) {
      logger.error("生成参数为空, 跳过");
      return;
    }
    File target = new File(outputFile);
    if (!target.exists()) {
      try (FileOutputStream fileOutputStream = new FileOutputStream(target)) {
        Template template = configuration.getTemplate(templatePath);
        template.process(objectMap, new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));
      }
      catch (Exception e) {
        logger.error("Failed to process template {}", templatePath, e);
        throw new IllegalStateException(e);
      }
    }
  }

  /**
   * 构造 FreeMarker 配置
   */
  private static Configuration buildFreemarkerConfiguration() {
    Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
    configuration.setTemplateLoader(new SpringTemplateLoader(new DefaultResourceLoader(), "classpath:code-templates/"));
    configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
    // 避免检查 locale 模板的开销
    configuration.setLocalizedLookup(false);
    // 避免格式化数字的显示
    configuration.setNumberFormat("#");
    return configuration;
  }
}
