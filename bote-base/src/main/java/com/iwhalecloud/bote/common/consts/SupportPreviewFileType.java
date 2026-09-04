package com.iwhalecloud.bote.common.consts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Content :文件类型，文本，office，压缩包等等
 */
public final class SupportPreviewFileType {

  private SupportPreviewFileType() {
  }


  private static final String[] OFFICE_TYPES = {"docx", "wps", "doc", "docm", "xls", "xlsx", "csv", "xlsm", "ppt", "pptx", "vsd", "rtf", "odt", "wmf", "emf", "dps", "et", "ods", "ots", "tsv", "odp", "otp", "sxi", "ott", "vsdx", "fodt", "fods", "xltx", "tga", "psd", "dotm", "ett", "xlt", "xltm", "wpt", "dot", "xlam", "dotx", "xla", "pages", "eps"};
  private static final String[] PICTURE_TYPES = {"jpg", "jpeg", "png", "gif", "bmp", "ico", "jfif", "webp"};
  private static final String[] ONLINE3D_TYPES = {"obj", "3ds", "stl", "ply", "off", "3dm", "fbx", "dae", "wrl", "3mf", "ifc", "glb", "o3dv", "gltf", "stp", "bim", "fcstd", "step", "iges", "brep"};
  private static final String[] EML_TYPES = {"eml"};
  private static final String[] XMIND_TYPES = {"xmind"};
  private static final String[] EPUB_TYPES = {"epub"};
  private static final String[] DCM_TYPES = {"dcm"};
  private static final String[] DRAWIO_TYPES = {"drawio"};
  private static final String[] XML_TYPES = {"xml", "xbrl"};
  private static final String[] TIFF_TYPES = {"tif", "tiff"};
  private static final String[] OFD_TYPES = {"ofd"};
  private static final String[] SVG_TYPES = {"svg"};
  private static final String[] CODES = {"java", "c", "php", "go", "python", "py", "js", "html", "ftl", "css", "lua", "sh", "rb", "yaml", "yml", "json", "h", "cpp", "cs", "aspx", "jsp", "sql"};
  private static final String[] PDF_TYPES = {"pdf"};
  private static final String[] TXT_TYPES = {"md", "mdx", "txt"};
  private static final String[] MEDIA_TYPES = {"mp3", "wav", "mp4", "flv"};


  private static final List<String> ALL_SUPPORT_TYPES = new ArrayList<>();

  static {
    ALL_SUPPORT_TYPES.addAll(Arrays.asList(OFFICE_TYPES));
    ALL_SUPPORT_TYPES.addAll(Arrays.asList(PICTURE_TYPES));
    ALL_SUPPORT_TYPES.addAll(Arrays.asList(ONLINE3D_TYPES));
    ALL_SUPPORT_TYPES.addAll(Arrays.asList(EML_TYPES));
    ALL_SUPPORT_TYPES.addAll(Arrays.asList(XMIND_TYPES));
    ALL_SUPPORT_TYPES.addAll(Arrays.asList(EPUB_TYPES));
    ALL_SUPPORT_TYPES.addAll(Arrays.asList(DCM_TYPES));
    ALL_SUPPORT_TYPES.addAll(Arrays.asList(DRAWIO_TYPES));
    ALL_SUPPORT_TYPES.addAll(Arrays.asList(XML_TYPES));
    ALL_SUPPORT_TYPES.addAll(Arrays.asList(TIFF_TYPES));
    ALL_SUPPORT_TYPES.addAll(Arrays.asList(OFD_TYPES));
    ALL_SUPPORT_TYPES.addAll(Arrays.asList(SVG_TYPES));
    ALL_SUPPORT_TYPES.addAll(Arrays.asList(CODES));
    ALL_SUPPORT_TYPES.addAll(Arrays.asList(PDF_TYPES));
    ALL_SUPPORT_TYPES.addAll(Arrays.asList(TXT_TYPES));
    ALL_SUPPORT_TYPES.addAll(Arrays.asList(MEDIA_TYPES));
  }

  /**
   * 文件类型是否支持预览
   *
   * @param fileType 文件类型
   * @return 是否
   */
  public static boolean supportPreview(String fileType) {
    if (StringUtils.isBlank(fileType)) {
      return false;
    }
    return ALL_SUPPORT_TYPES.contains(fileType.toLowerCase());
  }


}
