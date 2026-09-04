<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${package.mapper}.${mapperName}">

  <sql id="${entityCode?uncap_first}Columns">
    <#list table.fields as field>
      <#if field_has_next>
    a.${field.columnName},
      <#else>
    a.${field.columnName}
      </#if>
    </#list>
  </sql>

  <sql id="batchQueryCondition">
    AND a.status_cd = '00A'
    <if test="query.searchContent != null and query.searchContent != ''">
      AND (a.code LIKE
      <include refid="com.iwhalecloud.bss.litchi.mybatis.mapper.SqlFragments.like-left"/>
        <#noparse>#</#noparse>{query.searchContent}
      <include refid="com.iwhalecloud.bss.litchi.mybatis.mapper.SqlFragments.like-right"/>
      OR a.name LIKE
      <include refid="com.iwhalecloud.bss.litchi.mybatis.mapper.SqlFragments.like-left"/>
        <#noparse>#</#noparse>{query.searchContent}
      <include refid="com.iwhalecloud.bss.litchi.mybatis.mapper.SqlFragments.like-right"/>
      )
    </if>
  </sql>

  <select id="exists${entityCode}Code" resultType="boolean">
    SELECT CASE WHEN count(*) &gt; 0 THEN 1 ELSE 0 END AS existence
    FROM ${table.name}
    WHERE status_cd = '00A'
    AND code = <#noparse>#</#noparse>{dto.code}
    <if test="dto.${table.primaryKey.paramName} != null">
      AND ${table.primaryKey.columnName} != <#noparse>#</#noparse>{dto.${table.primaryKey.paramName}}
    </if>
  </select>

  <select id="get${entityCode}" resultType="${package.dto}.${dtoName}">
    SELECT
    <include refid="${entityCode?uncap_first}Columns"/>
    FROM ${table.name} a
    WHERE ${table.primaryKey.columnName} = <#noparse>#{id}</#noparse>
    AND status_cd = '00A'
  </select>

  <insert id="insert${entityCode}">
    INSERT INTO ${table.name} (
    <#list table.fields as field>
      <#if field_has_next>
    ${field.columnName},
      <#else>
    ${field.columnName}
      </#if>
    </#list>
    ) VALUES (
    <#list table.fields as field>
      <#if field_has_next>
        <#if field.isCreatedTimeKey || field.isUpdatedTimeKey>
    <include refid="com.iwhalecloud.bss.litchi.mybatis.mapper.SqlFragments.func_now"/>,
        <#else>
    <#noparse>#{</#noparse>dto.${field.paramName}},
        </#if>
      <#else>
        <#if field.isCreatedTimeKey || field.isUpdatedTimeKey>
    <include refid="com.iwhalecloud.bss.litchi.mybatis.mapper.SqlFragments.func_now"/>
        <#else>
    <#noparse>#{</#noparse>dto.${field.paramName}}
        </#if>
      </#if>
    </#list>
    )
  </insert>

  <insert id="batchInsert${entityCode}">
    INSERT INTO ${table.name} (
    <#list table.fields as field>
      <#if field_has_next>
    ${field.columnName},
      <#else>
    ${field.columnName}
      </#if>
    </#list>
    ) VALUES
    <foreach collection="list" item="dto" index="index" separator=", ">
    (
    <#list table.fields as field>
      <#if field_has_next>
        <#if field.isCreatedTimeKey || field.isUpdatedTimeKey>
    <include refid="com.iwhalecloud.bss.litchi.mybatis.mapper.SqlFragments.func_now"/>,
        <#else>
    <#noparse>#{</#noparse>dto.${field.paramName}},
        </#if>
      <#else>
        <#if field.isCreatedTimeKey || field.isUpdatedTimeKey>
    <include refid="com.iwhalecloud.bss.litchi.mybatis.mapper.SqlFragments.func_now"/>
        <#else>
    <#noparse>#{</#noparse>dto.${field.paramName}}
        </#if>
      </#if>
    </#list>
    )
    </foreach>
  </insert>

  <insert id="batchInsert${entityCode}" databaseId="oracle">
    INSERT ALL
    <foreach collection="list" item="dto" index="index" separator=" ">
      INTO ${table.name} (
      <#list table.fields as field>
        <#if field_has_next>
      ${field.columnName},
        <#else>
      ${field.columnName}
        </#if>
      </#list>
      ) VALUES (
      <#list table.fields as field>
        <#if field_has_next>
          <#if field.isCreatedTimeKey || field.isUpdatedTimeKey>
      <include refid="com.iwhalecloud.bss.litchi.mybatis.mapper.SqlFragments.func_now"/>,
          <#else>
      <#noparse>#{</#noparse>dto.${field.paramName}},
          </#if>
        <#else>
          <#if field.isCreatedTimeKey || field.isUpdatedTimeKey>
      <include refid="com.iwhalecloud.bss.litchi.mybatis.mapper.SqlFragments.func_now"/>
          <#else>
      <#noparse>#{</#noparse>dto.${field.paramName}}
          </#if>
        </#if>
      </#list>
      )
    </foreach>
    SELECT 1 FROM dual
  </insert>

  <update id="update${entityCode}">
    UPDATE ${table.name}
    <set>
    <#list table.fields as field>
      <#if field.isCreatedTimeKey == false && field.isUpdatedTimeKey == false>
    <if test="dto.checkFieldUpdateFlag('${field.paramName}')">
      ${field.columnName} = <#noparse>#{</#noparse>dto.${field.paramName}},
    </if>
      </#if>
      <#if field.isUpdatedTimeKey>
    ${field.columnName} = <include refid="com.iwhalecloud.bss.litchi.mybatis.mapper.SqlFragments.func_now"/>,
      </#if>
    </#list>
    </set>
    WHERE ${table.primaryKey.columnName} = <#noparse>#{</#noparse>dto.${table.primaryKey.paramName}}
  </update>

  <update id="delete${entityCode}">
    UPDATE ${table.name}
    SET status_cd = '00X',
    updator_id = <#noparse>#</#noparse>{updatorId},
    updated_time = <include refid="com.iwhalecloud.bss.litchi.mybatis.mapper.SqlFragments.func_now"/>
    WHERE ${table.primaryKey.columnName} = <#noparse>#</#noparse>{${table.primaryKey.paramName}}
  </update>

  <select id="select${entityCode}List" resultType="${package.dto}.${dtoName}">
    SELECT
    <include refid="${entityCode?uncap_first}Columns"/>
    FROM ${table.name} a
    WHERE 1=1
    <include refid="batchQueryCondition"/>
  </select>

  <select id="select${entityCode}Page" resultType="${package.dto}.${dtoName}">
    SELECT
    <include refid="${entityCode?uncap_first}Columns"/>
    FROM ${table.name} a
    WHERE 1=1
    <include refid="batchQueryCondition"/>
  </select>

</mapper>
