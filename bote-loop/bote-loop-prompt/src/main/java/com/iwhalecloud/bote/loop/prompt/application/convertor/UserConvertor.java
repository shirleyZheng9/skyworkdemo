package com.iwhalecloud.bote.loop.prompt.application.convertor;


import com.iwhalecloud.bote.loop.client.prompt.domain.user.UserInfoDetailDTO;
import com.iwhalecloud.bote.loop.domain.component.rpc.dto.RpcUserInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户信息转换器
 * 迁移对应关系: Go语言modules/prompt/application/convertor.UserConvertor
 * - 功能: 将用户信息领域对象(DO)与数据传输对象(DTO)之间进行转换
 * - 主要方法:
 * * batchUserInfoDO2DTO - 批量UserInfo DO转DTO
 * * userInfoDO2DTO - UserInfo DO转DTO
 * <p>
 * Java实现说明:
 * - 对应Go的convertor包中的转换函数
 * - 使用Java静态方法实现转换逻辑
 * - 处理空值检查和类型转换
 * - 支持批量转换操作
 * <p>
 * 技术栈迁移:
 * - Go函数 -> Java静态方法
 * - Go指针类型 -> Java对象引用
 * - Go切片类型 -> Java List
 * - Go nil检查 -> Java null检查
 * - Go ptr.Of -> Java直接赋值
 */
public final class UserConvertor {

  private UserConvertor() {
    // 工具类，禁止实例化
  }

  /**
   * 批量UserInfo DO转DTO
   * 迁移对应关系: Go语言convertor.BatchUserInfoDO2DTO
   * - 功能: 批量将UserInfo领域对象转换为UserInfoDetail DTO
   * - 参数: UserInfo领域对象列表
   * - 返回: UserInfoDetail DTO列表
   * - 处理逻辑:
   * * 空值检查: 如果输入为空或空列表，返回null
   * * 批量转换: 遍历每个UserInfo对象，调用单个转换方法
   * * 过滤空值: 跳过转换结果为null的对象
   * * 结果检查: 如果转换后列表为空，返回null
   */
  public static List<UserInfoDetailDTO> batchUserInfoDO2DTO(List<RpcUserInfo> userInfos) {
    if (userInfos == null || userInfos.isEmpty()) {
      return null;
    }

    List<UserInfoDetailDTO> dtos = new ArrayList<>();
    for (RpcUserInfo userInfo : userInfos) {
      UserInfoDetailDTO dto = userInfoDO2DTO(userInfo);
      if (dto == null) {
        continue;
      }
      dtos.add(dto);
    }

    if (dtos.isEmpty()) {
      return null;
    }
    return dtos;
  }

  /**
   * UserInfo DO转DTO
   * 迁移对应关系: Go语言convertor.UserInfoDO2DTO
   * - 功能: 将UserInfo领域对象转换为UserInfoDetail DTO
   * - 参数: UserInfo领域对象
   * - 返回: UserInfoDetail DTO对象
   * - 处理逻辑:
   * * 空值检查: 如果输入为null，返回null
   * * 字段映射: 将DO的字段映射到DTO的对应字段
   * * 类型转换: 处理必要的类型转换
   * * 对象构建: 使用Builder模式构建DTO对象
   */
  public static UserInfoDetailDTO userInfoDO2DTO(RpcUserInfo userInfo) {
    if (userInfo == null) {
      return null;
    }

    return UserInfoDetailDTO.builder()
      .userId(userInfo.getUserId())
      .name(userInfo.getUserName())
      .nickName(userInfo.getNickName())
      .avatarUrl(userInfo.getAvatarUrl())
      .email(userInfo.getEmail())
      .mobile(userInfo.getMobile())
      .build();
  }
}
